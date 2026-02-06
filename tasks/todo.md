# Findly - Location-Based Business Booking Platform

## Project Overview

**Problem:** Small businesses need visibility and consumers need to find nearby services easily.

**Solution:** A location-first platform where:
- Consumers share location → see businesses within 5km radius
- Businesses create profiles with exact coordinates
- Book appointments with nearby service providers
- SMS notifications keep everyone informed

**Scope:** POC (Proof of Concept) - extensible architecture for future features

---

## Platform Strategy

| Platform | Target User | Technology |
|----------|-------------|------------|
| **Web App** | Business Owners (admin dashboard) | React + Vite |
| **Web App** | Consumers (PWA - installable) | React + Vite |
| **Mobile App** | Consumers (iOS/Android) | React Native |

**POC Focus:** Web apps first, mobile app structure ready for Phase 2

---

## Decisions Made

| Question | Decision |
|----------|----------|
| Tech stack | **Java/Spring Boot** + React + React Native |
| SMS provider | **Twilio** (can switch later) |
| Authentication | **All:** Email/password + Phone OTP + Google OAuth |
| Services | **Multiple** per business |
| Pricing | **Show** prices to customers |
| Location | **PostGIS** for geo queries (5km radius) |
| Mobile | **React Native** (code sharing with web) |

---

## Final Tech Stack

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENTS                               │
├─────────────────┬─────────────────┬─────────────────────────┤
│   Web (Owner)   │  Web (Consumer) │   Mobile (Consumer)     │
│   React + Vite  │  React + Vite   │   React Native          │
│   Tailwind CSS  │  Tailwind + PWA │   Expo                  │
└────────┬────────┴────────┬────────┴────────────┬────────────┘
         │                 │                      │
         └─────────────────┼──────────────────────┘
                           │ REST API (JSON)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                     BACKEND (Spring Boot 3)                  │
├─────────────────────────────────────────────────────────────┤
│  Controllers → Services → Repositories → Database           │
│                                                              │
│  • Spring Security + JWT                                     │
│  • Spring Data JPA + PostGIS                                │
│  • Flyway Migrations                                         │
│  • Twilio SDK                                                │
│  • MapStruct (DTO mapping)                                   │
│  • OpenAPI/Swagger                                           │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                  PostgreSQL + PostGIS                        │
│  • Spatial indexes for geo queries                          │
│  • ST_DWithin for radius search                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Database Design (Updated with Geolocation)

### ERD (Entity Relationship Diagram)

```
┌─────────────────────┐
│       USERS         │
├─────────────────────┤
│ id (UUID, PK)       │
│ email (unique)      │
│ password_hash       │
│ phone (unique)      │
│ first_name          │
│ last_name           │
│ role (enum)         │
│ email_verified      │
│ phone_verified      │
│ avatar_url          │
│ created_at          │
│ updated_at          │
│ deleted_at (soft)   │
└─────────────────────┘
         │
         │ 1:N (owner)
         ▼
┌─────────────────────────────────────┐
│           BUSINESSES                 │
├─────────────────────────────────────┤
│ id (UUID, PK)                       │
│ owner_id (FK) ──────────────────────┼──→ users.id
│ name                                │
│ description                         │
│ category_id (FK) ───────────────────┼──→ categories.id
│ phone                               │
│ email                               │
│ website                             │
│ image_url                           │
│                                     │
│ ── LOCATION (PostGIS) ──            │
│ address_line1                       │
│ address_line2                       │
│ city                                │
│ state                               │
│ postal_code                         │
│ country                             │
│ location (GEOGRAPHY POINT) ◄────────┼── lat/lng stored here
│ location_updated_at                 │
│                                     │
│ ── STATUS ──                        │
│ status (ACTIVE/INACTIVE/PENDING)    │
│ is_verified                         │
│ rating_avg                          │
│ rating_count                        │
│                                     │
│ created_at                          │
│ updated_at                          │
│ deleted_at (soft)                   │
└─────────────────────────────────────┘
         │
         │ 1:N
         ▼
┌─────────────────────┐       ┌─────────────────────┐
│   WORKING_HOURS     │       │     SERVICES        │
├─────────────────────┤       ├─────────────────────┤
│ id (UUID, PK)       │       │ id (UUID, PK)       │
│ business_id (FK)    │       │ business_id (FK)    │
│ day_of_week (0-6)   │       │ name                │
│ start_time (TIME)   │       │ description         │
│ end_time (TIME)     │       │ duration_mins       │
│ is_closed           │       │ price               │
│ break_start         │       │ currency (default)  │
│ break_end           │       │ is_active           │
└─────────────────────┘       │ sort_order          │
                              │ created_at          │
                              │ updated_at          │
                              └─────────────────────┘
                                       │
                                       │ 1:N
                                       ▼
┌───────────────────────────────────────────────────┐
│                    BOOKINGS                        │
├───────────────────────────────────────────────────┤
│ id (UUID, PK)                                     │
│ business_id (FK) ─────────────────────────────────┼──→ businesses.id
│ service_id (FK) ──────────────────────────────────┼──→ services.id
│ customer_id (FK) ─────────────────────────────────┼──→ users.id (nullable for guests)
│                                                   │
│ ── GUEST INFO (if no account) ──                  │
│ guest_name                                        │
│ guest_phone                                       │
│ guest_email                                       │
│                                                   │
│ ── APPOINTMENT ──                                 │
│ date (DATE)                                       │
│ start_time (TIME)                                 │
│ end_time (TIME)                                   │
│ status (enum)                                     │
│ notes                                             │
│ rejection_reason                                  │
│                                                   │
│ ── TRACKING ──                                    │
│ booked_at                                         │
│ confirmed_at                                      │
│ cancelled_at                                      │
│ completed_at                                      │
│ created_at                                        │
│ updated_at                                        │
└───────────────────────────────────────────────────┘

┌─────────────────────┐       ┌─────────────────────┐
│    CATEGORIES       │       │   NOTIFICATIONS     │
├─────────────────────┤       ├─────────────────────┤
│ id (UUID, PK)       │       │ id (UUID, PK)       │
│ name                │       │ user_id (FK)        │
│ slug                │       │ booking_id (FK)     │
│ icon                │       │ type (enum)         │
│ parent_id (FK,self) │       │ channel (SMS/EMAIL) │
│ sort_order          │       │ recipient           │
│ is_active           │       │ message             │
└─────────────────────┘       │ status (enum)       │
                              │ sent_at             │
                              │ error_message       │
                              │ created_at          │
                              └─────────────────────┘

┌─────────────────────┐
│  REFRESH_TOKENS     │  (for JWT refresh)
├─────────────────────┤
│ id (UUID, PK)       │
│ user_id (FK)        │
│ token_hash          │
│ device_info         │
│ expires_at          │
│ created_at          │
└─────────────────────┘
```

### Enums

```java
// BookingStatus
PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED, NO_SHOW

// UserRole
CUSTOMER, BUSINESS_OWNER, ADMIN

// BusinessStatus
ACTIVE, INACTIVE, PENDING_APPROVAL, SUSPENDED

// NotificationType
BOOKING_CREATED, BOOKING_APPROVED, BOOKING_REJECTED,
BOOKING_CANCELLED, BOOKING_REMINDER

// NotificationStatus
PENDING, SENT, FAILED
```

### Key Indexes

```sql
-- Spatial index for location queries (CRITICAL for performance)
CREATE INDEX idx_businesses_location ON businesses USING GIST(location);

-- Common query indexes
CREATE INDEX idx_businesses_category ON businesses(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_businesses_status ON businesses(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_bookings_business_date ON bookings(business_id, date);
CREATE INDEX idx_bookings_customer ON bookings(customer_id);
CREATE INDEX idx_bookings_status ON bookings(status);
```

### Geo Query Example (5km radius)

```sql
-- Find businesses within 5km of user's location
SELECT b.*,
       ST_Distance(b.location, ST_MakePoint(:lng, :lat)::geography) as distance_meters
FROM businesses b
WHERE ST_DWithin(
    b.location,
    ST_MakePoint(:lng, :lat)::geography,
    5000  -- 5km in meters
)
AND b.status = 'ACTIVE'
AND b.deleted_at IS NULL
ORDER BY distance_meters;
```

---

## API Endpoints Design (v1)

### Base URL: `/api/v1`

### Auth
```
POST   /auth/register              # Email/password registration
POST   /auth/login                 # Email/password login
POST   /auth/logout                # Invalidate refresh token
POST   /auth/refresh               # Refresh access token
POST   /auth/forgot-password       # Send reset email
POST   /auth/reset-password        # Reset with token
POST   /auth/verify-email          # Verify email token
POST   /auth/send-otp              # Send phone OTP
POST   /auth/verify-otp            # Verify phone OTP
GET    /auth/google                # Google OAuth redirect
GET    /auth/google/callback       # Google OAuth callback
GET    /auth/me                    # Get current user
```

### Businesses (Public - Location Based)
```
GET    /businesses                 # Search with filters
       ?lat=32.08&lng=34.78        # User location (required)
       &radius=5000                # Radius in meters (default 5000)
       &category=haircut           # Category filter
       &q=salon                    # Text search
       &sort=distance|rating       # Sort order
       &page=0&size=20             # Pagination

GET    /businesses/:id             # Business details
GET    /businesses/:id/services    # List services
GET    /businesses/:id/hours       # Working hours
GET    /businesses/:id/slots       # Available slots
       ?date=2026-02-10            # Specific date
       &service_id=xxx             # For specific service
```

### Businesses (Owner - Protected)
```
POST   /owner/businesses           # Create business
GET    /owner/businesses           # List my businesses
GET    /owner/businesses/:id       # Get my business
PUT    /owner/businesses/:id       # Update business
DELETE /owner/businesses/:id       # Soft delete
PUT    /owner/businesses/:id/location  # Update coordinates

POST   /owner/businesses/:id/services      # Add service
PUT    /owner/businesses/:id/services/:sid # Update service
DELETE /owner/businesses/:id/services/:sid # Delete service

PUT    /owner/businesses/:id/hours  # Update working hours (bulk)
```

### Bookings (Consumer)
```
POST   /bookings                   # Create booking (guest or logged in)
GET    /bookings                   # My bookings (requires auth)
GET    /bookings/:id               # Booking details
PUT    /bookings/:id/cancel        # Cancel booking
```

### Bookings (Owner)
```
GET    /owner/bookings             # All bookings for my businesses
       ?business_id=xxx            # Filter by business
       &status=PENDING             # Filter by status
       &date=2026-02-10            # Filter by date
PUT    /owner/bookings/:id/approve # Approve booking
PUT    /owner/bookings/:id/reject  # Reject booking (with reason)
PUT    /owner/bookings/:id/complete # Mark completed
PUT    /owner/bookings/:id/no-show  # Mark no-show
```

### Categories (Public)
```
GET    /categories                 # List all active categories
GET    /categories/:id/businesses  # Businesses in category (with location)
```

---

## Project Structure (Clean Architecture)

```
findly/
├── backend/                          # Spring Boot API
│   ├── src/main/java/com/findly/
│   │   ├── FindlyApplication.java
│   │   │
│   │   ├── config/                   # Configuration
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtConfig.java
│   │   │   ├── TwilioConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   └── OpenApiConfig.java
│   │   │
│   │   ├── domain/                   # Domain layer (entities)
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Business.java
│   │   │   │   ├── Service.java
│   │   │   │   ├── WorkingHours.java
│   │   │   │   ├── Booking.java
│   │   │   │   ├── Category.java
│   │   │   │   └── Notification.java
│   │   │   ├── enums/
│   │   │   │   ├── UserRole.java
│   │   │   │   ├── BookingStatus.java
│   │   │   │   └── BusinessStatus.java
│   │   │   └── repository/           # Repository interfaces
│   │   │       ├── UserRepository.java
│   │   │       ├── BusinessRepository.java
│   │   │       └── ...
│   │   │
│   │   ├── application/              # Application layer (use cases)
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── BusinessService.java
│   │   │   │   ├── BookingService.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   └── GeoLocationService.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   └── mapper/               # MapStruct mappers
│   │   │
│   │   ├── infrastructure/           # Infrastructure layer
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   ├── sms/
│   │   │   │   └── TwilioSmsService.java
│   │   │   └── persistence/
│   │   │       └── CustomBusinessRepositoryImpl.java
│   │   │
│   │   ├── api/                      # API layer (controllers)
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── BusinessController.java
│   │   │   │   ├── OwnerBusinessController.java
│   │   │   │   ├── BookingController.java
│   │   │   │   └── CategoryController.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ApiException.java
│   │   │   └── validation/
│   │   │
│   │   └── shared/                   # Shared utilities
│   │       ├── util/
│   │       └── constant/
│   │
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/             # Flyway migrations
│   │       ├── V1__create_users.sql
│   │       ├── V2__create_businesses.sql
│   │       └── ...
│   │
│   ├── src/test/                     # Tests
│   ├── pom.xml
│   └── Dockerfile
│
├── web/                              # React Web App (Consumer + Owner)
│   ├── src/
│   │   ├── components/
│   │   │   ├── common/               # Shared components
│   │   │   ├── business/             # Business-related
│   │   │   ├── booking/              # Booking-related
│   │   │   └── layout/               # Layout components
│   │   ├── pages/
│   │   │   ├── consumer/             # Consumer pages
│   │   │   │   ├── Home.tsx
│   │   │   │   ├── Search.tsx
│   │   │   │   ├── BusinessDetail.tsx
│   │   │   │   └── MyBookings.tsx
│   │   │   ├── owner/                # Business owner pages
│   │   │   │   ├── Dashboard.tsx
│   │   │   │   ├── BusinessSetup.tsx
│   │   │   │   ├── Bookings.tsx
│   │   │   │   └── Settings.tsx
│   │   │   └── auth/
│   │   ├── hooks/
│   │   │   ├── useAuth.ts
│   │   │   ├── useLocation.ts        # Geolocation hook
│   │   │   └── useApi.ts
│   │   ├── services/
│   │   │   └── api.ts                # API client (axios)
│   │   ├── store/                    # State management
│   │   ├── types/
│   │   └── utils/
│   ├── package.json
│   └── vite.config.ts
│
├── mobile/                           # React Native App (Consumer only)
│   ├── src/
│   │   ├── components/
│   │   ├── screens/
│   │   ├── navigation/
│   │   ├── hooks/
│   │   ├── services/
│   │   └── utils/
│   ├── app.json
│   └── package.json
│
├── docker-compose.yml                # Local dev environment
├── CLAUDE.md
├── README.md
└── tasks/
    └── todo.md
```

---

## Development Phases (POC)

### Phase 0: Project Setup ✅ COMPLETED
- [x] Initialize Spring Boot project with dependencies
- [x] Initialize React web project (Vite + TypeScript)
- [x] Set up PostgreSQL + PostGIS with Docker
- [x] Configure Flyway migrations
- [x] Set up basic CI (GitHub Actions)

### Phase 1: Core Backend ✅ COMPLETED
- [x] Database migrations (all tables)
- [x] User entity + repository
- [x] Business entity with PostGIS location
- [x] JWT authentication (register/login)
- [x] Basic error handling
- [x] All entities: Service, WorkingHours, Booking, Category, Notification, RefreshToken
- [x] All repositories with geo queries
- [x] Spring Security configuration
- [x] CORS and OpenAPI configuration

### Phase 2: Location & Search ✅ COMPLETED
- [x] Geo query for nearby businesses (PostGIS ST_DWithin)
- [x] Business CRUD (owner)
- [x] Services CRUD (owner)
- [x] Working hours management
- [x] Category listing

### Phase 3: Booking Flow ✅ COMPLETED
- [x] Slot availability calculation
- [x] Create booking (guest + registered)
- [x] Approve/Reject (owner)
- [x] Cancel booking
- [x] Booking status workflow

### Phase 4: Web Frontend ✅ COMPLETED
- [x] Consumer: Home with location permission
- [x] Consumer: Search results with filters
- [x] Consumer: Business detail + booking modal
- [x] useAuth hook with context provider
- [x] API service with all endpoints
- [x] Owner: Dashboard with real API data
- [x] Owner: Bookings page (approve/reject/complete/no-show)
- [x] Owner: Services page (add/edit/delete)
- [x] Owner: Working Hours page
- [x] Owner: Settings page (business setup/location)

### Phase 4.5: Unit Tests ✅ COMPLETED
- [x] BookingService unit tests (slot availability, create booking, owner operations)
- [x] BusinessService unit tests (search, CRUD, location)
- [x] AuthService unit tests (register, login, refresh token, logout)
- [x] ServiceService unit tests (CRUD operations)
- [x] BookingController tests
- [x] AuthController tests
- [x] Booking entity tests (status transitions)
- [x] JaCoCo coverage plugin configured (85% minimum)

### Phase 5: Notifications
- [ ] Twilio integration
- [ ] SMS on booking created (to owner)
- [ ] SMS on booking approved/rejected (to customer)

### Phase 6: Mobile Prep (Structure Only)
- [ ] Initialize React Native project
- [ ] Set up navigation structure
- [ ] Create shared API service
- [ ] Basic screens (placeholder)

---

## Future Features (Post-POC)

```
┌─────────────────────────────────────────────────────────┐
│                    FUTURE ROADMAP                        │
├─────────────────────────────────────────────────────────┤
│ • Reviews & Ratings                                     │
│ • Favorites / Saved businesses                          │
│ • Push notifications (Firebase)                         │
│ • Payment integration (Stripe)                          │
│ • Business analytics dashboard                          │
│ • Multi-language support (i18n)                         │
│ • Business verification badge                           │
│ • Recurring appointments                                │
│ • Waitlist for fully booked slots                       │
│ • Staff management (multiple employees)                 │
│ • Calendar sync (Google Calendar)                       │
│ • Advanced search (filters, sorting)                    │
│ • Business promotions / discounts                       │
└─────────────────────────────────────────────────────────┘
```

---

## Coding Standards

### Backend (Java/Spring Boot)
- Use Lombok for boilerplate reduction
- DTOs for all API request/response (never expose entities)
- MapStruct for entity-DTO mapping
- Validation with Jakarta Bean Validation
- Consistent exception handling via @ControllerAdvice
- Unit tests for services, integration tests for controllers
- Follow REST naming conventions
- Use UUID for all primary keys
- Soft delete for important entities

### Frontend (React/TypeScript)
- Functional components with hooks
- TypeScript strict mode
- React Query for API state management
- Tailwind CSS for styling
- Component-driven development
- Lazy loading for routes
- Environment variables for API URLs

### Git
- Feature branches: `feature/xxx`
- Conventional commits: `feat:`, `fix:`, `refactor:`
- PR required for main branch
- Keep commits atomic and focused

---

## Session Log

### 2026-02-05
- Created initial project plan
- Designed database schema (ERD)
- Outlined API endpoints
- Defined development phases
- **Updated:** Added geolocation support (PostGIS)
- **Updated:** Multi-platform strategy (Web + Mobile)
- **Updated:** Clean architecture project structure
- **Updated:** Detailed coding plan with phases
- **COMPLETED Phase 0:**
  - Spring Boot 3 backend with all dependencies
  - PostgreSQL + PostGIS Docker Compose
  - Flyway migrations (V1: schema, V2: seed categories)
  - React + Vite + TypeScript + Tailwind frontend
  - Basic pages: Home, Search, BusinessDetail, Login, Register, Owner Dashboard
  - API service with axios interceptors
  - GitHub Actions CI pipeline
  - README.md with setup instructions
- **COMPLETED Phase 1:**
  - All domain entities with clean architecture
  - PostGIS location support on Business entity
  - JWT authentication with refresh tokens
  - Spring Security with role-based access
  - Global exception handling (production-ready)
  - MapStruct DTO mapping
  - Swagger/OpenAPI documentation
  - CORS configuration
- **COMPLETED Phase 2:**
  - BusinessService with geo queries (ST_DWithin for 5km radius)
  - Public BusinessController for nearby search
  - OwnerBusinessController for business CRUD
  - ServiceService and management endpoints
  - WorkingHoursService with bulk update
  - CategoryService with hierarchical categories
  - All DTOs with Jakarta validation
  - Complete MapStruct mappers
- **COMPLETED Phase 3:**
  - BookingService with slot availability calculation
  - Time slot generation considering working hours, breaks, existing bookings
  - Create booking for registered users and guest bookings
  - BookingController (consumer) with all booking endpoints
  - OwnerBookingController for approve/reject/complete/no-show
  - BookingMapper for entity-to-DTO conversion
  - Complete booking status workflow
- **COMPLETED Phase 4 (Web Frontend):**
  - Home page with geolocation and category browsing
  - Search page with real API integration and filters
  - Business detail page with booking modal
  - useAuth hook with React context
  - Complete API service layer (auth, business, booking, owner)
  - Guest booking support
  - Owner Dashboard with real API data (today's bookings, pending approvals)
  - Owner Bookings page (approve/reject/complete/no-show)
  - Owner Services page (add/edit/delete services)
  - Owner Working Hours page (set schedule for each day)
  - Owner Settings page (create/edit business, update location)
- **COMPLETED Phase 4.5 (Unit Tests):**
  - BookingService: 25+ tests covering slot availability, create booking, customer/owner operations
  - BusinessService: 15+ tests covering search, CRUD, location updates
  - AuthService: 15+ tests covering register, login, refresh token
  - ServiceService: 12+ tests covering CRUD operations
  - Controller tests: BookingController, AuthController
  - Domain entity tests: Booking status transitions
  - JaCoCo plugin for 85% minimum coverage requirement
