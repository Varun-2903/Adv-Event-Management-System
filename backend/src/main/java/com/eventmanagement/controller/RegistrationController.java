package com.eventmanagement.controller;

import com.eventmanagement.dto.RegistrationDtos;
import com.eventmanagement.service.RegistrationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    private final RegistrationService service;

    public RegistrationController(RegistrationService service) { this.service = service; }

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public RegistrationDtos.RegistrationResponse register(@PathVariable Long eventId,
                                                            Authentication auth) {
        return service.register(eventId, auth.getName());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('PARTICIPANT')")
    public List<RegistrationDtos.RegistrationResponse> mine(Authentication auth) {
        return service.mine(auth.getName());
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public List<RegistrationDtos.RegistrationResponse> eventRegistrations(@PathVariable Long eventId) {
        return service.eventRegistrations(eventId);
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public RegistrationDtos.RegistrationResponse checkIn(@RequestBody RegistrationDtos.CheckInRequest req) {
        return service.checkIn(req.qrToken());
    }
}
