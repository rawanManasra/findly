# Findly - Location-Based Business Booking Platform

## Overview

A location-first platform where consumers find nearby businesses (5km radius) and book appointments.

**Repository:** https://github.com/rawanManasra/findly
**Scope:** POC (Proof of Concept) - extensible architecture

## Quick Start

```bash
git add . && git commit -m "message" && git push
```

## Documentation

- **Full Plan, DB Design, API:** `tasks/todo.md`
- **Current Phase:** Phase 0 - Project Setup

## Platforms

| Platform | Target | Tech |
|----------|--------|------|
| Web (Owner) | Business dashboard | React + Vite |
| Web (Consumer) | PWA | React + Vite |
| Mobile | iOS/Android | React Native |

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 3 + Spring Security + JWT |
| Database | PostgreSQL + **PostGIS** (geo queries) |
| Frontend | React + Tailwind + Vite |
| Mobile | React Native + Expo |
| Auth | JWT + OAuth2 + Phone OTP |
| SMS | Twilio |

## Key Features

- **Location-based search** - Find businesses within 5km
- **Multi-service booking** - Businesses offer multiple services
- **Guest booking** - No account required
- **SMS notifications** - Real-time alerts

## Database Tables

`users` → `businesses` → `services` → `bookings`
                      → `working_hours`
                      → `categories`
`notifications`, `refresh_tokens`

## Rules

1. Always update `tasks/todo.md` after changes
2. Use DTOs - never expose entities
3. Soft delete for important data
4. UUID for all primary keys
5. **Every change must be tested and pushed** - Run tests before committing, push after every commit
