package com.eventmanagement.controller;

import com.eventmanagement.dto.EventDtos;
import com.eventmanagement.entity.EventStatus;
import com.eventmanagement.entity.Event;
import com.eventmanagement.service.EventService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService service;

    public EventController(EventService service) { this.service = service; }

    @GetMapping("/public")
    public List<EventDtos.EventResponse> publicEvents() {
        return service.publicEvents();
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public Event create(@RequestBody EventDtos.CreateEventRequest req, Authentication auth) {
        return service.create(req, auth.getName());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('ORGANIZER')")
    public List<EventDtos.EventResponse> mine(Authentication auth) {
        return service.organizerEvents(auth.getName());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EventDtos.EventResponse> pending() {
        return service.pending();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public EventDtos.EventResponse approve(@PathVariable Long id) {
        return service.changeStatus(id, EventStatus.PUBLISHED);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public EventDtos.EventResponse reject(@PathVariable Long id) {
        return service.changeStatus(id, EventStatus.REJECTED);
    }
}
