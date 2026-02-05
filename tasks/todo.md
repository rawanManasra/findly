# Findly - Business Booking Platform

## Project Overview

**Problem:** Small businesses need visibility and an easy way to manage appointments with customers.

**Solution:** A web platform where:
- Businesses create profiles and set availability
- Consumers discover businesses and book appointments
- Business owners approve/reject bookings
- SMS notifications keep everyone informed

---

## Phase 1: Planning & Design

### Requirements Analysis

#### Business Owner Features
- [ ] Register/Login
- [ ] Create business profile (name, description, category, location)
- [ ] Set working hours (per day of week)
- [ ] Define appointment slots (duration, services offered)
- [ ] View all appointments (pending/approved/rejected/cancelled)
- [ ] Approve or reject booking requests
- [ ] Receive SMS notifications for new bookings

#### Consumer Features
- [ ] Register/Login (or guest booking with phone)
- [ ] Browse/search businesses (by category, location, name)
- [ ] View business profile and available slots
- [ ] Book an available slot
- [ ] Cancel a booking
- [ ] Receive SMS confirmation

#### System Features
- [ ] SMS notification service (Twilio)
- [ ] Real-time availability updates
- [ ] Calendar view for appointments

---

## Tech Stack Recommendation

### Option A: Full JavaScript Stack (Recommended for MVP)
```
Frontend: React + Tailwind CSS
Backend:  Node.js + Express
Database: PostgreSQL
SMS:      Twilio
Hosting:  Vercel (frontend) + Railway (backend + DB)
```

### Option B: Java Stack (Your Expertise)
```
Frontend: React + Tailwind CSS
Backend:  Spring Boot + Spring Security
Database: PostgreSQL
SMS:      Twilio
Hosting:  AWS (EC2/RDS)
```

---

## Database Design

### ERD (Entity Relationship Diagram)

```
┌─────────────────┐       ┌─────────────────┐
│     USERS       │       │   BUSINESSES    │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)         │
│ email           │       │ owner_id (FK)───┼──→ users.id
│ password_hash   │       │ name            │
│ phone           │       │ description     │
│ name            │       │ category        │
│ role            │       │ address         │
│ created_at      │       │ phone           │
│ updated_at      │       │ image_url       │
└─────────────────┘       │ created_at      │
                          └─────────────────┘
                                   │
                                   │ 1:N
                                   ▼
┌─────────────────┐       ┌─────────────────┐
│   BOOKINGS      │       │ WORKING_HOURS   │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)         │
│ business_id(FK)─┼──→    │ business_id(FK)─┼──→ businesses.id
│ customer_id(FK)─┼──→    │ day_of_week     │ (0=Sun, 6=Sat)
│ service_id (FK) │       │ start_time      │
│ date            │       │ end_time        │
│ start_time      │       │ is_closed       │
│ end_time        │       └─────────────────┘
│ status          │
│ notes           │       ┌─────────────────┐
│ created_at      │       │    SERVICES     │
│ updated_at      │       ├─────────────────┤
└─────────────────┘       │ id (PK)         │
                          │ business_id(FK)─┼──→ businesses.id
                          │ name            │
                          │ description     │
                          │ duration_mins   │
                          │ price           │
                          │ is_active       │
                          └─────────────────┘

┌─────────────────┐
│  NOTIFICATIONS  │
├─────────────────┤
│ id (PK)         │
│ user_id (FK)    │
│ booking_id (FK) │
│ type            │ (SMS, EMAIL, PUSH)
│ message         │
│ status          │ (PENDING, SENT, FAILED)
│ sent_at         │
│ created_at      │
└─────────────────┘
```

### Status Enums

**Booking Status:**
- `PENDING` - Waiting for business approval
- `APPROVED` - Business approved
- `REJECTED` - Business rejected
- `CANCELLED` - Customer cancelled
- `COMPLETED` - Appointment done
- `NO_SHOW` - Customer didn't show up

**User Role:**
- `CUSTOMER`
- `BUSINESS_OWNER`
- `ADMIN`

---

## API Endpoints Design

### Auth
```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
POST /api/auth/forgot-password
```

### Businesses
```
GET    /api/businesses              # List/search businesses
GET    /api/businesses/:id          # Get business details
POST   /api/businesses              # Create business (owner)
PUT    /api/businesses/:id          # Update business
GET    /api/businesses/:id/slots    # Get available slots
```

### Bookings
```
GET    /api/bookings                # List my bookings
POST   /api/bookings                # Create booking
PUT    /api/bookings/:id/approve    # Approve (owner)
PUT    /api/bookings/:id/reject     # Reject (owner)
PUT    /api/bookings/:id/cancel     # Cancel (customer)
```

### Working Hours
```
GET    /api/businesses/:id/hours    # Get working hours
PUT    /api/businesses/:id/hours    # Update working hours
```

---

## Project Structure

```
findly/
├── frontend/                 # React app
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── hooks/
│   │   ├── services/
│   │   └── utils/
│   └── package.json
│
├── backend/                  # Node.js/Express API
│   ├── src/
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── routes/
│   │   ├── services/
│   │   ├── middleware/
│   │   └── utils/
│   ├── prisma/              # Database schema
│   └── package.json
│
├── CLAUDE.md
├── README.md
└── tasks/
    └── todo.md
```

---

## Development Phases

### Phase 1: Foundation (MVP)
- [ ] Set up project structure
- [ ] Database schema + migrations
- [ ] User authentication (register/login)
- [ ] Business CRUD
- [ ] Basic frontend with routing

### Phase 2: Core Booking
- [ ] Working hours management
- [ ] Services management
- [ ] Slot availability calculation
- [ ] Booking creation
- [ ] Approve/Reject flow

### Phase 3: Notifications
- [ ] Twilio SMS integration
- [ ] Notification on new booking
- [ ] Notification on status change

### Phase 4: Polish
- [ ] Search and filters
- [ ] Business categories
- [ ] Calendar view
- [ ] Mobile responsive design

---

## Decisions Made

| Question | Decision |
|----------|----------|
| Tech stack | **Java/Spring Boot** + React |
| SMS provider | **Twilio** (can switch later) |
| Authentication | **All:** Email/password + Phone OTP + Social (Google) |
| Services | **Multiple** per business |
| Pricing | **Show** prices to customers |

## Final Tech Stack

```
Frontend:  React + Tailwind CSS + Vite
Backend:   Spring Boot 3 + Spring Security + JWT
Database:  PostgreSQL + Flyway migrations
SMS:       Twilio
Auth:      JWT + OAuth2 (Google)
Build:     Maven
Deploy:    Docker → AWS/Railway
```

---

## Session Log

### 2026-02-05
- Created initial project plan
- Designed database schema (ERD)
- Outlined API endpoints
- Defined development phases
