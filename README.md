# Consulting Platform — EECS 3311 Project

A backend REST API for a consulting booking platform built with Spring Boot and PostgreSQL. Clients can browse services, book sessions, and make payments. Consultants can accept, reject, or complete bookings. Admins manage consultant approvals and system policies.

**GitHub Repository:** https://github.com/bhanuRakshita/EECS-3311-Project

---

## Architecture Overview

The application follows a **layered, package-by-feature architecture**:

```
com.consultingplatform
├── admin/               # Consultant approval, system policy management
├── auth/                # Authentication endpoints and payload DTOs
├── booking/             # Booking lifecycle and state machine
├── config/              # Global configurations (Security, Exception Handling)
├── consultant/          # Consultant profiles and availability slots
├── consultingservice/   # Core consulting services catalog
├── notification/        # System and email notifications
├── payment/             # Payment processing, payment methods, payment history
├── security/            # JWT filters, UserDetailsService, security chains
└── user/                # User management — Client, Consultant, Admin (inheritance)
```

Each package is internally organized into:
- `domain/` — JPA entities and enums
- `repository/` — Spring Data JPA interfaces
- `service/` — Business logic (interface + implementation)
- `web/` — REST controllers and DTOs

**Project layout:**
- `backend/` — Spring Boot application (API, business logic, persistence)
- `frontend/` — Static demo UI (`index.html`) for exercising all API flows
- `diagrams/` — UML and design diagrams

**Tech Stack:** Java 17, Spring Boot 4.0.3, Spring Data JPA, PostgreSQL, Lombok, Docker

**High-level flow:**
1. Consultants create services and availability slots
2. Clients browse services and create bookings from available slots
3. Consultants accept or reject bookings
4. Confirmed bookings can be paid through the payment module
5. After payment, consultants mark sessions as complete

---

## Getting Started / How to Run

This project supports running via **Docker** (recommended) or running **locally directly on your machine**.

### 1. Prerequisites
- **Java 17** (if running via IDE/Maven directly)
- **Docker & Docker Compose** (if running containerized)
- Make sure you have a `.env` file in the root of the project. (You can copy `.env.example` to `.env` if one exists).

### 2. The Three Databases
This project uses three different databases depending on how you run it:
1. **Docker PostgreSQL (Local):** An isolated local DB created by Docker. Used when running `docker compose up`.
2. **Neon DB (Cloud PostgreSQL):** A live remote database. Uncomment the `# 3. REMOTE NEON DATABASE` section in `.env` to connect to this.
3. **H2 (In-Memory Database):** A fake, temporary DB used **only for running automated tests**. Keeps tests fast and safe.

### 3. Running with Docker (Recommended)
This method spins up the **PostgreSQL database**, the **Spring Boot backend**, and the **Vite React frontend** inside isolated containers. You do not need to install Java, Node, or Postgres on your Mac.

```bash
# Build and start the backend, frontend, and database
docker compose up --build

# Stop the containers
docker compose down

# Note: If you want to wipe the database clean, run:
docker compose down -v
```
*(The backend API will be available at `http://localhost:8080`)*
*(The frontend application will be available at `http://localhost:3000`)*

### 4. Running Locally (Without Docker)
If you prefer to run the backend directly from IntelliJ/Eclipse or Terminal, you must provide it with a database:
1. **Using Docker just for the DB:**
   Run `docker compose up db` to start the local DB. Then uncomment the `# 2. SPRING BOOT APP` section in `.env`.
2. **Using the Neon Cloud DB:**
   Uncomment the `# 3. REMOTE NEON DATABASE` section in `.env`.
3. **Start the app:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

### 5. Running Tests
Tests are automatically configured to use the **H2 In-Memory Database** (so they don't break your real data).
```bash
cd backend
./mvnw test
```

### 6. AI Customer Assistant & Configuration
The platform integrates an **AI Customer Assistant chatbot** (powered by Gemini) for helping clients navigate the consulting offerings.

**Configuration Required (API Key):**
For the chatbot (and AI-integrated features) to function in the backend, you must provide your **Gemini API Key**.
1. Open or create a `.env` file at the root of your project directory (meaning at the same level as `docker-compose.yml`).
2. Add your key like so:
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
3. When running via `docker compose`, this key is automatically injected into the backend container. 

**Accessing the Chatbot:**
Once the full application (frontend + backend) is running:
1. Open your browser and go to `http://localhost:3000` (the frontend application).
2. The AI Customer Assistant is accessible via a Chat Widget / **Chatbot** page linked in the application navigation. Guests can use it to find answers and recommendations.

---

## Design Patterns

### 1. State Pattern — Booking Lifecycle

**Location:** `backend/src/main/java/com/consultingplatform/booking/state/`

The `BookingState` interface defines all valid booking transitions: `accept`, `reject`, `cancel`, `processPayment`, and `complete`. Each concrete state class enforces which transitions are legal, throwing an exception for invalid ones.

```
BookingState (interface)
├── RequestedState   — allows: accept, reject, cancel
├── ConfirmedState   — allows: processPayment, cancel
├── PaidState        — allows: complete
├── RejectedState    — terminal state
├── CancelledState   — terminal state
└── CompletedState   — terminal state
```

**Normal flow:** `REQUESTED → CONFIRMED → PAID → COMPLETED`
**Alternate paths:** `REQUESTED → REJECTED`, any non-terminal state → `CANCELLED`

This eliminates conditional transition logic from the service layer. Each state is responsible for its own allowed actions.

---

### 2. Strategy Pattern — Payment Processing

**Location:** `backend/src/main/java/com/consultingplatform/payment/domain/`

The `PaymentStrategy` interface defines: `validatePaymentDetails()`, `processPayment(amount)`, and `getPaymentType()`. Each payment method is a concrete strategy with its own validation and processing logic.

```
PaymentStrategy (interface)
├── CreditCardPayment
├── DebitCardPayment
├── PayPalPayment
└── BankTransferPayment
```

`PaymentService` selects the correct strategy at runtime based on the `paymentType` field in the incoming request. Adding a new payment method only requires implementing the interface — no changes to the service.

---

### 3. Factory Pattern — Booking State Creation

**Location:** `backend/src/main/java/com/consultingplatform/booking/state/BookingStateFactory.java`

`BookingStateFactory` provides a static `createState(String status)` method that maps a booking status string to the correct concrete `BookingState` object. This centralizes object creation and decouples the service layer from concrete state classes.

```
"REQUESTED" → new RequestedState()
"CONFIRMED" → new ConfirmedState()
"PAID"      → new PaidState()
"COMPLETED" → new CompletedState()
"CANCELLED" → new CancelledState()
"REJECTED"  → new RejectedState()
```

When a booking is loaded from the database, its status string is passed to the factory to reconstruct the correct state object, without any `if/else` chains in the service layer.

---

## Key API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Authenticate an existing user |
| GET | `/api/services` | List all consulting services |
| GET | `/api/services/{serviceId}/availability` | Get available slots for a specific service |
| POST | `/bookings` | Create a booking `{ clientId, slotId }` |
| GET | `/bookings/{id}` | Get booking by ID |
| PUT | `/bookings/{id}/cancel` | Client cancels a booking |
| PUT | `/api/consultant/{cid}/bookings/{bid}/accept` | Consultant accepts |
| PUT | `/api/consultant/{cid}/bookings/{bid}/reject` | Consultant rejects |
| PUT | `/api/consultant/{cid}/bookings/{bid}/complete` | Consultant completes |
| POST | `/api/payments/process` | Process a payment |
| GET | `/api/payments/history/{clientId}` | Client payment history |
| GET | `/api/users` | List all users |
| POST | `/api/users` | Create a user `{ role, firstName, lastName, email, ... }` |
| GET | `/api/admin/stats` | Get admin dashboard statistics |
| GET | `/api/admin/system/status` | Get system status |
| GET | `/api/admin/consultants/pending` | Get pending consultant registrations |
| POST | `/api/admin/consultants/{id}/approval` | Approve or reject a consultant |
| GET/PUT | `/api/admin/policies/{key}` | Get or update a system policy |
| GET/POST/PUT/DELETE | `/api/admin/services` | Admin management for consulting services |

---

