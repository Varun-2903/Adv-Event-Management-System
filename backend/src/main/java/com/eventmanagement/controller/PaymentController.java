package com.eventmanagement.controller;

import com.eventmanagement.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service){this.service=service;}

    @PostMapping("/create-order/{eventId}")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public Map<String,Object> createOrder(@PathVariable Long eventId, Authentication auth){
        return service.createOrder(eventId,auth.getName());
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public Map<String,Object> verify(@RequestBody VerifyRequest req, Authentication auth){
        return service.verify(auth.getName(),req.razorpay_order_id(),req.razorpay_payment_id(),req.razorpay_signature());
    }
    public record VerifyRequest(String razorpay_order_id,String razorpay_payment_id,String razorpay_signature){}
}
