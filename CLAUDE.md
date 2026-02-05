# Findly - Business Booking Platform

## Overview

A web platform connecting small businesses with consumers for appointment booking.

**Repository:** https://github.com/rawanManasra/findly

## Quick Start

```bash
# Push changes
git add . && git commit -m "message" && git push
```

## Documentation

- **Full Plan & DB Design:** `tasks/todo.md`
- **Current Phase:** Planning & Design

## Tech Stack (Pending Decision)

| Layer    | Option A (MVP)     | Option B (Production) |
|----------|--------------------|-----------------------|
| Frontend | React + Tailwind   | React + Tailwind      |
| Backend  | Node.js + Express  | Spring Boot           |
| Database | PostgreSQL         | PostgreSQL            |
| SMS      | Twilio             | Twilio                |

## Key Entities

- **Users** - Customers & Business Owners
- **Businesses** - Profiles with working hours
- **Services** - What businesses offer
- **Bookings** - Appointments with status workflow
- **Notifications** - SMS alerts

## Rules

1. Always update `tasks/todo.md` after changes
2. Keep commits small and focused
3. Test before pushing
