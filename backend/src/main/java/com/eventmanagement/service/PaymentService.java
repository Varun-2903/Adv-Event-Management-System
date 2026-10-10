package com.eventmanagement.service;

import com.eventmanagement.entity.*;
import com.eventmanagement.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class PaymentService {
    private final EventRepository events; private final UserRepository users;
    private final PaymentRepository payments; private final RegistrationService registrations; private final RegistrationRepository registrationRepository;
    @Value("${razorpay.key-id:}") private String keyId;
    @Value("${razorpay.key-secret:}") private String keySecret;

    public PaymentService(EventRepository events, UserRepository users, PaymentRepository payments, RegistrationService registrations, RegistrationRepository registrationRepository) {
        this.events=events; this.users=users; this.payments=payments; this.registrations=registrations; this.registrationRepository=registrationRepository;
    }

    public Map<String,Object> createOrder(Long eventId, String email) {
        ensureConfigured();
        Event event=events.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (event.getStatus()!=EventStatus.PUBLISHED) throw new IllegalArgumentException("This event is not published");
        if (event.getPrice()==null || event.getPrice().compareTo(BigDecimal.ZERO)<=0) throw new IllegalArgumentException("This is a free event. Use Register instead.");
        User user=users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(registrationRepository.findByEventAndParticipant(event,user).isPresent()) throw new IllegalArgumentException("You are already registered for this event");
        if(registrationRepository.countByEvent(event) >= event.getCapacity()) throw new IllegalArgumentException("Event capacity is full");
        long amountPaise=event.getPrice().multiply(new BigDecimal("100")).longValueExact();
        Map<String,Object> request=Map.of("amount",amountPaise,"currency","INR","receipt","event_"+eventId+"_user_"+user.getId());
        Map response;
        try {
            response=RestClient.builder().defaultHeaders(h -> h.setBasicAuth(keyId,keySecret)).build()
                .post().uri("https://api.razorpay.com/v1/orders").body(request).retrieve().body(Map.class);
        } catch (Exception ex) { throw new IllegalStateException("Could not create Razorpay order. Check Test Mode API keys and internet connection."); }
        if(response==null || response.get("id")==null) throw new IllegalStateException("Razorpay did not return an order ID");
        Payment p=new Payment(); p.setEvent(event); p.setParticipant(user); p.setRazorpayOrderId(response.get("id").toString()); p.setAmountPaise(amountPaise); p.setStatus("CREATED"); payments.save(p);
        Map<String,Object> out=new HashMap<>(); out.put("keyId",keyId); out.put("orderId",p.getRazorpayOrderId()); out.put("amount",amountPaise); out.put("currency","INR"); out.put("eventTitle",event.getTitle()); out.put("participantName",user.getName()); out.put("participantEmail",user.getEmail()); return out;
    }

    @Transactional
    public Map<String,Object> verify(String email, String orderId, String paymentId, String signature) {
        ensureConfigured();
        Payment p=payments.findByRazorpayOrderId(orderId).orElseThrow(() -> new IllegalArgumentException("Payment order not found"));
        User user=users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(!p.getParticipant().getId().equals(user.getId())) throw new IllegalArgumentException("This payment does not belong to your account");
        if("PAID".equals(p.getStatus())) return Map.of("status","PAID","message","Payment already verified");
        String expected=hmacSha256(orderId+"|"+paymentId,keySecret);
        if(!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            p.setStatus("FAILED"); payments.save(p); throw new IllegalArgumentException("Payment signature verification failed");
        }
        p.setRazorpayPaymentId(paymentId); p.setStatus("PAID"); payments.save(p);
        try { registrations.registerAfterPayment(p.getEvent().getId(),email); }
        catch (IllegalArgumentException ex) {
            // If the participant was already registered, keep the successful payment recorded.
            if(!ex.getMessage().toLowerCase(Locale.ROOT).contains("already registered")) throw ex;
        }
        Map<String,Object> result = new HashMap<>();
        result.put("status", "PAID");
        result.put("message", "Payment verified and event registration confirmed");
        result.put("bookingCode", registrations.mine(email).stream()
                .filter(r -> r.eventId().equals(p.getEvent().getId()))
                .map(com.eventmanagement.dto.RegistrationDtos.RegistrationResponse::bookingCode)
                .findFirst().orElse(null));
        return result;
    }

    private String hmacSha256(String data, String secret) {
        try { Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch(Exception e){ throw new IllegalStateException("Could not verify payment signature"); }
    }
    private void ensureConfigured(){ if(keyId.isBlank() || keySecret.isBlank()) throw new IllegalStateException("Add your Razorpay Test Mode key ID and key secret to backend application.properties first"); }
}
