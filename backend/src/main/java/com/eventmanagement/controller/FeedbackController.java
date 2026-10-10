package com.eventmanagement.controller;

import com.eventmanagement.dto.RegistrationDtos;
import com.eventmanagement.service.FeedbackService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService service;

    public FeedbackController(FeedbackService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasRole('PARTICIPANT')")
    public Object submit(@RequestBody RegistrationDtos.FeedbackRequest req, Authentication auth) {
        return service.submit(req, auth.getName());
    }
}
