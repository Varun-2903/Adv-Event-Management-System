package com.eventmanagement.service;

import com.eventmanagement.dto.RegistrationDtos;
import com.eventmanagement.entity.*;
import com.eventmanagement.repository.RegistrationRepository;
import com.eventmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationService {
    private final RegistrationRepository registrations;
    private final UserRepository users;
    private final EventService eventService;

    public RegistrationService(RegistrationRepository registrations, UserRepository users,
                                EventService eventService) {
        this.registrations = registrations;
        this.users = users;
        this.eventService = eventService;
    }

    public RegistrationDtos.RegistrationResponse register(Long eventId, String email) {
        Event event = eventService.get(eventId);
        if (event.getPrice() != null && event.getPrice().signum() > 0)
            throw new IllegalArgumentException("This is a paid event. Use Pay & Register to complete payment first.");
        return registerInternal(event, email);
    }

    public RegistrationDtos.RegistrationResponse registerAfterPayment(Long eventId, String email) {
        Event event = eventService.get(eventId);
        return registerInternal(event, email);
    }

    private RegistrationDtos.RegistrationResponse registerInternal(Event event, String email) {
        User user = users.findByEmail(email).orElseThrow();

        if (registrations.findByEventAndParticipant(event, user).isPresent())
            throw new IllegalArgumentException("Already registered for this event");

        if (registrations.countByEvent(event) >= event.getCapacity())
            throw new IllegalArgumentException("Event capacity is full");

        Registration r = new Registration();
        r.setEvent(event);
        r.setParticipant(user);
        r.setQrToken(UUID.randomUUID().toString());
        r.setBookingCode(generateBookingCode());
        r.setCheckedIn(false);
        return dto(registrations.save(r));
    }

    public List<RegistrationDtos.RegistrationResponse> mine(String email) {
        User user = users.findByEmail(email).orElseThrow();
        return registrations.findByParticipant(user).stream().map(this::dto).toList();
    }

    public List<RegistrationDtos.RegistrationResponse> eventRegistrations(Long eventId) {
        Event event = eventService.get(eventId);
        return registrations.findByEvent(event).stream().map(this::dto).toList();
    }

    public RegistrationDtos.RegistrationResponse checkIn(String qrToken) {
        Registration r = registrations.findByQrToken(qrToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid QR code"));
        if (r.isCheckedIn()) throw new IllegalArgumentException("Already checked in");
        r.setCheckedIn(true);
        return dto(registrations.save(r));
    }

    private String generateBookingCode() {
        // Short, human-friendly code; the unique DB constraint prevents duplicates.
        return "EVT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(java.util.Locale.ROOT);
    }

    private RegistrationDtos.RegistrationResponse dto(Registration r) {
        return new RegistrationDtos.RegistrationResponse(
                r.getId(), r.getEvent().getId(), r.getEvent().getTitle(),
                r.getParticipant().getName(), r.getQrToken(), r.getBookingCode(), r.isCheckedIn());
    }
}
