package com.eventmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "participant_id", nullable = false)
    private User participant;
    @Column(nullable = false, unique = true) private String razorpayOrderId;
    @Column(unique = true) private String razorpayPaymentId;
    @Column(nullable = false) private long amountPaise;
    @Column(nullable = false) private String status = "CREATED";
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId(){return id;}
    public Event getEvent(){return event;}
    public User getParticipant(){return participant;}
    public String getRazorpayOrderId(){return razorpayOrderId;}
    public String getRazorpayPaymentId(){return razorpayPaymentId;}
    public long getAmountPaise(){return amountPaise;}
    public String getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setEvent(Event v){event=v;}
    public void setParticipant(User v){participant=v;}
    public void setRazorpayOrderId(String v){razorpayOrderId=v;}
    public void setRazorpayPaymentId(String v){razorpayPaymentId=v;}
    public void setAmountPaise(long v){amountPaise=v;}
    public void setStatus(String v){status=v;}
}
