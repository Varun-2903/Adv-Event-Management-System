package com.eventmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "participant_id"}))
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;

    @Column(nullable = false, unique = true)
    private String qrToken;

    @Column(nullable = false, unique = true, length = 32)
    private String bookingCode;

    private boolean checkedIn;
    private LocalDateTime registeredAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public User getParticipant() { return participant; }
    public String getQrToken() { return qrToken; }
    public String getBookingCode() { return bookingCode; }
    public boolean isCheckedIn() { return checkedIn; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }

    public void setId(Long id) { this.id = id; }
    public void setEvent(Event event) { this.event = event; }
    public void setParticipant(User participant) { this.participant = participant; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
