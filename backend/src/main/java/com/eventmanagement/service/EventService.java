package com.eventmanagement.service;

import com.eventmanagement.dto.EventDtos;
import com.eventmanagement.entity.*;
import com.eventmanagement.repository.EventRepository;
import com.eventmanagement.repository.RegistrationRepository;
import com.eventmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EventService {
    private final EventRepository events;
    private final UserRepository users;
    private final RegistrationRepository registrations;

    public EventService(EventRepository events, UserRepository users,
                        RegistrationRepository registrations) {
        this.events = events;
        this.users = users;
        this.registrations = registrations;
    }

    public Event create(EventDtos.CreateEventRequest req, String email) {
        User organizer = users.findByEmail(email).orElseThrow();
        Event e = new Event();
        e.setTitle(req.title());
        e.setDescription(req.description());
        e.setCategory(req.category());
        e.setEventDate(req.eventDate());
        e.setStartTime(req.startTime());
        e.setEndTime(req.endTime());
        e.setVenue(req.venue());
        e.setCapacity(req.capacity());
        e.setPrice(req.price());
        e.setOrganizer(organizer);
        e.setStatus(EventStatus.PENDING);
        return events.save(e);
    }

    public List<EventDtos.EventResponse> publicEvents() {
        return events.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED)
                .stream().map(this::dto).toList();
    }

    public List<EventDtos.EventResponse> pending() {
        return events.findByStatusOrderByEventDateAsc(EventStatus.PENDING)
                .stream().map(this::dto).toList();
    }

    public List<EventDtos.EventResponse> organizerEvents(String email) {
        User user = users.findByEmail(email).orElseThrow();
        return events.findByOrganizerOrderByEventDateDesc(user)
                .stream().map(this::dto).toList();
    }

    public EventDtos.EventResponse changeStatus(Long id, EventStatus status) {
        Event e = events.findById(id).orElseThrow();
        e.setStatus(status);
        return dto(events.save(e));
    }

    public Event get(Long id) {
        return events.findById(id).orElseThrow();
    }

    public EventDtos.EventResponse dto(Event e) {
        return new EventDtos.EventResponse(
                e.getId(), e.getTitle(), e.getDescription(), e.getCategory(),
                e.getEventDate(), e.getStartTime(), e.getEndTime(), e.getVenue(),
                e.getCapacity(), e.getPrice(), e.getOrganizer() == null ? "" : e.getOrganizer().getName(),
                e.getStatus().name(),
                registrations.countByEvent(e),
                registrations.countByEventAndCheckedInTrue(e));
    }
}
