package com.eventmanagement.service;

import com.eventmanagement.dto.RegistrationDtos;
import com.eventmanagement.entity.*;
import com.eventmanagement.repository.FeedbackRepository;
import com.eventmanagement.repository.RegistrationRepository;
import com.eventmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {
    private final FeedbackRepository feedback;
    private final UserRepository users;
    private final EventService events;
    private final RegistrationRepository registrations;

    public FeedbackService(FeedbackRepository feedback, UserRepository users,
                           EventService events, RegistrationRepository registrations) {
        this.feedback = feedback;
        this.users = users;
        this.events = events;
        this.registrations = registrations;
    }

    public Feedback submit(RegistrationDtos.FeedbackRequest req, String email) {
        User user = users.findByEmail(email).orElseThrow();
        Event event = events.get(req.eventId());

        Registration registration = registrations.findByEventAndParticipant(event, user)
                .orElseThrow(() -> new IllegalArgumentException("Register for the event first"));

        if (!registration.isCheckedIn())
            throw new IllegalArgumentException("Feedback is available after check-in");

        if (feedback.existsByEventAndParticipant(event, user))
            throw new IllegalArgumentException("Feedback already submitted");

        if (req.rating() < 1 || req.rating() > 5)
            throw new IllegalArgumentException("Rating must be 1 to 5");

        Feedback f = new Feedback();
        f.setEvent(event);
        f.setParticipant(user);
        f.setRating(req.rating());
        f.setComment(req.comment());
        return feedback.save(f);
    }
}
