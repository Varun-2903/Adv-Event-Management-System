# REST API

## Public
- `GET /api/events/public`

## Authentication
- `POST /api/auth/register`
- `POST /api/auth/login`

## Organizer
- `POST /api/events`
- `GET /api/events/mine`
- `POST /api/registrations/checkin`

## Admin
- `GET /api/events/pending`
- `PUT /api/events/{id}/approve`
- `PUT /api/events/{id}/reject`

## Participant
- `POST /api/registrations/event/{eventId}`
- `GET /api/registrations/mine`
- `POST /api/feedback`

All protected endpoints require:

`Authorization: Bearer <JWT>`
