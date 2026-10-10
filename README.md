# Event Management System
### React + Java Spring Boot + MySQL

This project follows the workflow in the supplied project design:

1. Event Planning
2. Event Approval
3. Event Publishing
4. User Discovery & Registration
5. QR Check-in
6. Event Execution
7. Feedback & Certificate
8. Event Analytics

## Technology Stack

- Frontend: React + Vite
- Backend: Java 17 + Spring Boot + REST API
- Database: MySQL 8+
- Authentication: JWT + role-based authorization
- QR generation: ZXing
- Build tools: Maven and npm

## Project Structure

```text
EventManagementSystem/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/eventmanagement/
│       ├── EventManagementApplication.java
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── repository/
│       └── service/
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
├── database/
│   └── schema.sql
└── README.md
```

## 1. Create MySQL database

Open MySQL Workbench or MySQL command line and run:

```sql
CREATE DATABASE event_management;
```

Then check `database/schema.sql` if you want the starter database/data.

Default admin account is created automatically by the backend if it does not exist:

- Email: `admin@event.com`
- Password: `admin123`

Change this password before using the system seriously.

## 2. Configure backend

Open:

`backend/src/main/resources/application.properties`

Change the MySQL username/password if required.

Example:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

## 3. Start backend

From the `backend` folder:

```bash
mvn spring-boot:run
```

Backend runs at:

`http://localhost:8080`

## 4. Start frontend

From the `frontend` folder:

```bash
npm install
npm run dev
```

Frontend normally runs at:

`http://localhost:5173`

## Main workflow

### Organizer
Register as ORGANIZER -> Create event -> Wait for admin -> Admin approves -> Event becomes visible -> Manage attendance.

### Admin
Login -> Review pending events -> Approve/reject -> View analytics.

### Participant
Register/login -> Browse published events -> Register -> Receive QR -> Show QR at venue -> Organizer scans/checks in -> Submit feedback -> Certificate becomes available after attendance.

## API examples

### Register

`POST /api/auth/register`

```json
{
  "name": "John",
  "email": "john@example.com",
  "password": "password123",
  "role": "PARTICIPANT"
}
```

### Login

`POST /api/auth/login`

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

The response contains a JWT token. The React frontend stores it in localStorage and sends it as:

`Authorization: Bearer <token>`

## Important

This is a complete academic starter project designed to be easy to run and extend. For production deployment, add stronger password policies, email verification, refresh tokens, rate limiting, cloud storage for certificates, HTTPS and production secrets.


## Razorpay payment integration (Test Mode)

Paid events now have a ticket price. Organizers enter a price when creating an event; use `0` for a free event. Participants use **Pay & Register** for paid events. The backend creates the Razorpay order, verifies the payment signature, saves the payment status, and only then creates the registration/QR ticket. Each registration receives a unique human-readable booking code such as `EVT-A1B2C3D4`. The code is saved in MySQL, shown in **My Registrations**, and returned on successful payment. The QR code remains linked to the registration token for check-in.

1. Create/sign in to a Razorpay account at https://dashboard.razorpay.com/ and enable **Test Mode**.
2. Generate Test API keys. Do not share the Key Secret or put it in frontend code.
3. In PowerShell, set environment variables in the same terminal used to start Spring Boot:

   ```powershell
   $env:RAZORPAY_KEY_ID="rzp_test_your_key_id"
   $env:RAZORPAY_KEY_SECRET="your_test_key_secret"
   mvn spring-boot:run
   ```

   Or place values in `backend/src/main/resources/application.properties` for local testing only, replacing the blank values for `razorpay.key-id` and `razorpay.key-secret`. Never commit real secrets.
4. Restart the backend after adding keys. Start the frontend with `npm run dev` in the `frontend` folder.
5. Create a paid event as an Organizer, approve it as Admin, then sign in as a Participant and choose **Pay & Register**. Use Razorpay's Test Mode payment details shown in its official testing documentation; no real money should be charged in Test Mode.

**Important:** This is a student/demo integration. Test successful, failed, cancelled, and repeated payments before a demo. Before live payments, add webhook reconciliation and production-grade idempotency/monitoring, HTTPS, and proper secret management.


## Unique booking code after payment

After Razorpay payment signature verification succeeds, the backend creates the registration and generates a unique `EVT-XXXXXXXX` booking code. The database enforces uniqueness for both the booking code and QR token. The payment-success message displays the code, and the participant can find it later in **My Registrations** beside the QR ticket. Free event registrations also receive a booking code.
