package com.eventmanagement.repository;

import com.eventmanagement.entity.Feedback;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByEvent(Event event);
    boolean existsByEventAndParticipant(Event event, User participant);
}
