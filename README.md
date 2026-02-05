# Findly - Location-Based Business Booking Platform

A platform where consumers find nearby businesses within 5km and book appointments instantly.

## Features

- **Location-based search** - Find services within 5km radius
- **Multi-service booking** - Businesses offer multiple services with pricing
- **Guest booking** - Book without creating an account
- **SMS notifications** - Real-time alerts via Twilio
- **Business dashboard** - Manage bookings, services, and working hours

## Tech Stack

| Component | Technology |
|-----------|------------|
| Backend | Spring Boot 3, Spring Security, JWT |
| Database | PostgreSQL + PostGIS |
| Frontend | React, Vite, Tailwind CSS |
| Mobile | React Native (planned) |
| SMS | Twilio |

## Getting Started

### Prerequisites

- Java 17+
- Node.js 20+
- Docker & Docker Compose

### Local Development

1. **Start the database:**
   ```bash
   docker-compose up -d postgres
   ```

2. **Run the backend:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. **Run the frontend:**
   ```bash
   cd web
   npm install
   npm run dev
   ```

4. **Access the app:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

### Environment Variables

Create `.env` files based on the examples:

**Backend** (`backend/src/main/resources/application.yml`):
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_PHONE_NUMBER`
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`

**Frontend** (`web/.env`):
- `VITE_API_URL`

## Project Structure

```
findly/
├── backend/           # Spring Boot API
│   ├── src/main/java/com/findly/
│   │   ├── api/       # Controllers
│   │   ├── application/ # Services, DTOs
│   │   ├── domain/    # Entities, Repositories
│   │   ├── infrastructure/ # Security, SMS
│   │   └── config/    # Configuration
│   └── src/main/resources/
│       └── db/migration/ # Flyway migrations
│
├── web/               # React frontend
│   ├── src/
│   │   ├── pages/     # Consumer & Owner pages
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── services/  # API client
│   │   └── types/
│
├── mobile/            # React Native (planned)
│
├── docker-compose.yml
└── README.md
```

## API Documentation

Once running, visit http://localhost:8080/swagger-ui.html for interactive API docs.

### Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register user |
| POST | `/api/v1/auth/login` | Login |
| GET | `/api/v1/businesses?lat=X&lng=Y` | Search nearby |
| GET | `/api/v1/businesses/:id` | Business details |
| POST | `/api/v1/bookings` | Create booking |
| PUT | `/api/v1/owner/bookings/:id/approve` | Approve booking |

## Development

### Database Migrations

Migrations are managed by Flyway. Add new migrations to:
`backend/src/main/resources/db/migration/`

Naming: `V{number}__{description}.sql`

### Running Tests

```bash
# Backend
cd backend && ./mvnw test

# Frontend
cd web && npm run lint
```

## License

MIT
