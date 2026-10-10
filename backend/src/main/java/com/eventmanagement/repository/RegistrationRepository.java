package com.eventmanagement.repository;

import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.Registration;
import com.eventmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByEventAndParticipant(Event event, User participant);
    Optional<Registration> findByQrToken(String qrToken);
    List<Registration> findByParticipant(User participant);
    List<Registration> findByEvent(Event event);
    long countByEvent(Event event);
    long countByEventAndCheckedInTrue(Event event);
}
