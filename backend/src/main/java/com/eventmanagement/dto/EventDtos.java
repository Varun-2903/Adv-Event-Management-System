package com.eventmanagement.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

public class EventDtos {
    public record CreateEventRequest(
            String title, String description, String category,
            LocalDate eventDate, LocalTime startTime, LocalTime endTime,
            String venue, int capacity, BigDecimal price) {}

    public record EventResponse(
            Long id, String title, String description, String category,
            LocalDate eventDate, LocalTime startTime, LocalTime endTime,
            String venue, int capacity, BigDecimal price, String organizer, String status,
            long registrations, long attendance) {}
}
