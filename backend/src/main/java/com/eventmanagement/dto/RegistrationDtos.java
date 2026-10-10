package com.eventmanagement.dto;

public class RegistrationDtos {
    public record RegistrationResponse(
            Long id, Long eventId, String eventTitle, String participant,
            String qrToken, String bookingCode, boolean checkedIn) {}
    public record CheckInRequest(String qrToken) {}
    public record FeedbackRequest(Long eventId, int rating, String comment) {}
}
