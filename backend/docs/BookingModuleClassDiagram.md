# Booking Module - Class Diagram

```mermaid
classDiagram
    %% ==================== WEB LAYER ====================
    class BookingController {
        -BookingService bookingService
        +requestBooking(BookingRequest) Booking
        +getBookingById(Long) Booking
        +cancelBooking(Long) Booking
        +acceptBooking(Long) Booking
        +rejectBooking(Long) Booking
        +completeBooking(Long) Booking
        +processPayment(Long) Booking
        +getClientBookings(Long) List~Booking~
    }

    class BookingRequest {
        +Long clientId
        +Long slotId
    }

    %% ==================== SERVICE LAYER ====================
    class BookingService {
        <<interface>>
        +requestBooking(BookingRequest) Booking
        +getBookingById(Long) Booking
        +cancelBooking(Long) Booking
        +getClientBookings(Long) List~Booking~
        +acceptBooking(Long) Booking
        +rejectBooking(Long) Booking
        +completeBooking(Long) Booking
        +processPayment(Long) Booking
    }

    class BookingServiceImpl {
        -BookingRepository bookingRepository
        -AvailabilitySlotRepository availabilitySlotRepository
        +requestBooking(BookingRequest) Booking
        +getBookingById(Long) Booking
        +cancelBooking(Long) Booking
        +getClientBookings(Long) List~Booking~
        +acceptBooking(Long) Booking
        +rejectBooking(Long) Booking
        +completeBooking(Long) Booking
        +processPayment(Long) Booking
    }

    %% ==================== REPOSITORY LAYER ====================
    class BookingRepository {
        <<interface>>
        +save(Booking) Booking
        +findById(Long) Optional~Booking~
        +findAll() List~Booking~
        +delete(Booking) void
        +findByClientId(Long) List~Booking~
        +findByConsultantId(Long) List~Booking~
        +findByConsultantIdAndStatus(Long, String) List~Booking~
    }

    %% ==================== DOMAIN LAYER ====================
    class Booking {
        -Long id
        -Long clientId
        -Long consultantId
        -Long serviceId
        -Long availabilitySlotId
        -OffsetDateTime requestedStartAt
        -OffsetDateTime requestedEndAt
        -String status
        -OffsetDateTime requestedAt
        -OffsetDateTime createdAt
        -OffsetDateTime updatedAt
        -OffsetDateTime cancelledAt
        -OffsetDateTime completedAt
        -OffsetDateTime consultantDecidedAt
        -String rejectionReason
        -BookingState state
        +initializeState() void
        +accept() void
        +reject() void
        +complete() void
        +cancel() void
        +processPayment() void
    }

    %% ==================== STATE PATTERN ====================
    class BookingState {
        <<interface>>
        +accept(Booking) void
        +reject(Booking) void
        +complete(Booking) void
        +cancel(Booking) void
        +processPayment(Booking) void
        +getStateName() String
    }

    class RequestedState {
        +accept(Booking) void
        +reject(Booking) void
        +complete(Booking) void
        +cancel(Booking) void
        +processPayment(Booking) void
        +getStateName() String
    }

    class ConfirmedState {
        +accept(Booking) void
        +reject(Booking) void
        +complete(Booking) void
        +cancel(Booking) void
        +processPayment(Booking) void
        +getStateName() String
    }

    class PaidState {
        +accept(Booking) void
        +reject(Booking) void
        +complete(Booking) void
        +cancel(Booking) void
        +processPayment(Booking) void
        +getStateName() String
    }

    class CompletedState {
        +accept(Booking) void
        +reject(Booking) void
        +complete(Booking) void
        +cancel(Booking) void
        +processPayment(Booking) void
        +getStateName() String
    }

    class CancelledState {
        +accept(Booking) void
        +reject(Booking) void
        +complete(Booking) void
        +cancel(Booking) void
        +processPayment(Booking) void
        +getStateName() String
    }

    class RejectedState {
        +accept(Booking) void
        +reject(Booking) void
        +complete(Booking) void
        +cancel(Booking) void
        +processPayment(Booking) void
        +getStateName() String
    }

    %% ==================== EXTERNAL DEPENDENCIES ====================
    class AvailabilitySlot {
        <<external>>
        -Long id
        -Long consultantId
        -Long serviceId
        -OffsetDateTime startAt
        -OffsetDateTime endAt
        -Boolean isAvailable
        -OffsetDateTime createdAt
    }

    class AvailabilitySlotRepository {
        <<interface>>
        <<external>>
        +findById(Long) Optional~AvailabilitySlot~
        +save(AvailabilitySlot) AvailabilitySlot
    }

    %% ==================== RELATIONSHIPS ====================
    
    %% Controller Layer
    BookingController --> BookingService : uses
    BookingController ..> BookingRequest : receives
    BookingController ..> Booking : returns

    %% Service Layer
    BookingService <|.. BookingServiceImpl : implements
    BookingServiceImpl --> BookingRepository : uses
    BookingServiceImpl --> AvailabilitySlotRepository : uses
    BookingServiceImpl ..> BookingRequest : receives
    BookingServiceImpl ..> Booking : manipulates
    BookingServiceImpl ..> AvailabilitySlot : reads/updates

    %% Repository Layer
    BookingRepository ..> Booking : manages

    %% Domain Layer - State Pattern
    Booking --> BookingState : has
    BookingState <|.. RequestedState : implements
    BookingState <|.. ConfirmedState : implements
    BookingState <|.. PaidState : implements
    BookingState <|.. CompletedState : implements
    BookingState <|.. CancelledState : implements
    BookingState <|.. RejectedState : implements

    %% State transitions
    RequestedState ..> ConfirmedState : transitions to
    RequestedState ..> RejectedState : transitions to
    RequestedState ..> CancelledState : transitions to
    ConfirmedState ..> PaidState : transitions to
    ConfirmedState ..> CompletedState : transitions to
    ConfirmedState ..> CancelledState : transitions to
    PaidState ..> CompletedState : transitions to
    PaidState ..> CancelledState : transitions to

    %% External Dependencies
    AvailabilitySlotRepository ..> AvailabilitySlot : manages
    Booking --> AvailabilitySlot : references via availabilitySlotId

    %% Notes
    note for Booking "Entity with State Pattern\n- @Entity JPA mapping\n- @Transient state field\n- @PostLoad initializes state\n- Delegates all transitions to state"
    note for BookingState "State Pattern Interface\nDefines all possible transitions\nEach implementation enforces\nvalid state transitions"
    note for BookingServiceImpl "Business Logic Layer\n- @Transactional operations\n- Slot availability management\n- State transition orchestration\n- Cross-module coordination"
    note for RequestedState "Initial State\nValid: accept, reject, cancel\nInvalid: complete, payment"
    note for ConfirmedState "Accepted State\nValid: payment, complete, cancel\nInvalid: accept, reject"
    note for CompletedState "Terminal State\nNo transitions allowed"
```

## Relationships Explained

### 1. **Composition** (Solid line with filled diamond)
- `BookingServiceImpl` ◆→ `BookingRepository`: Service owns/manages repository
- `BookingServiceImpl` ◆→ `AvailabilitySlotRepository`: Service owns/manages repository

### 2. **Association** (Solid line with arrow)
- `BookingController` → `BookingService`: Controller uses service
- `Booking` → `BookingState`: Booking has a state

### 3. **Dependency** (Dashed line with arrow)
- `BookingController` ⋯→ `BookingRequest`: Controller depends on DTO
- `BookingServiceImpl` ⋯→ `Booking`: Service manipulates entity
- State classes ⋯→ Other States: State transitions

### 4. **Implementation** (Dashed line with hollow triangle)
- `BookingServiceImpl` ⊳⋯ `BookingService`: Implements interface
- All state classes ⊳⋯ `BookingState`: Implement state interface

### 5. **Reference** (Association)
- `Booking` → `AvailabilitySlot`: Foreign key reference via `availabilitySlotId`

## State Transition Flow

```mermaid
stateDiagram-v2
    [*] --> REQUESTED: Client creates booking
    
    REQUESTED --> CONFIRMED: Consultant accepts
    REQUESTED --> REJECTED: Consultant rejects
    REQUESTED --> CANCELLED: Client/Consultant cancels
    
    CONFIRMED --> PAID: Payment processed
    CONFIRMED --> COMPLETED: Service delivered (skip payment)
    CONFIRMED --> CANCELLED: Cancelled before service
    
    PAID --> COMPLETED: Service delivered
    PAID --> CANCELLED: Cancelled (refund issued)
    
    COMPLETED --> [*]
    CANCELLED --> [*]
    REJECTED --> [*]
```

## Package Structure

```
com.consultingplatform.booking/
│
├── domain/
│   └── Booking.java (Entity)
│
├── repository/
│   └── BookingRepository.java (Data Access)
│
├── service/
│   ├── BookingService.java (Interface)
│   └── BookingServiceImpl.java (Implementation)
│
├── web/
│   ├── BookingController.java (REST API)
│   └── BookingRequest.java (DTO)
│
└── state/ (State Pattern)
    ├── BookingState.java (Interface)
    ├── RequestedState.java
    ├── ConfirmedState.java
    ├── PaidState.java
    ├── CompletedState.java
    ├── CancelledState.java
    └── RejectedState.java
```

## Key Design Patterns

1. **State Pattern** - Booking state management
2. **Repository Pattern** - Data access abstraction
3. **Service Layer Pattern** - Business logic separation
4. **DTO Pattern** - BookingRequest for API input
5. **Dependency Injection** - Constructor injection throughout

## Cross-Module Dependencies

- **Consultant Module**: `AvailabilitySlot` and `AvailabilitySlotRepository`
  - Used for slot validation and availability management
  - Ensures no double-booking
  - Auto-populates booking details from slot
