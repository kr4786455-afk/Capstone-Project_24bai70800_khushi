# UniLoop — Smart Campus Sharing & Sustainability Platform

**Share More. Waste Less. Grow Together.**

Students list books, calculators, lab gear and other academic supplies they aren't using, and other students on the same campus borrow them. Every handover is verified with a QR code, every loan builds (or costs) a trust score, and a dashboard shows the CO₂ and money the community has saved.

| Layer | Tech |
|-------|------|
| Frontend | React 18 (Vite), React Router, Recharts, qrcode.react |
| Backend | Spring Boot 3.3 (Java 17+), Spring Security, Spring Data JPA |
| Database | PostgreSQL (or in-memory H2 for a zero-setup demo) |
| Auth | JWT (stateless, BCrypt password hashing) |

No Docker required.

## Features

- **JWT auth** – register / login, protected routes and API endpoints
- **Item listings** – product cards with category, condition, max borrow days, owner trust badge
- **Search & filters** – text search, category filter, "available only"
- **Borrow workflow with status tracking** – `REQUESTED → APPROVED → HANDED_OVER → RETURNED` (plus `REJECTED` / `CANCELLED`), shown as a progress tracker
- **QR-verified handovers** – on approval the borrower gets a pickup QR; the owner scans/pastes it to confirm. A second QR is issued for the return. Tokens are random UUIDs and are only ever sent to the borrower, so an owner can't "confirm" without actually meeting the borrower.
- **Borrowing history** – every transaction, filterable by borrowing / lending
- **Trust scores (0–100)** – start at 50; return on time **+5**, lend successfully **+2**, return late **−10**. Below 20 you can't borrow. Levels: Low / Neutral / Trusted / Campus Champion.
- **Sustainability analytics** – CO₂ avoided (estimated per category), trees-equivalent, money saved, shares per month, top categories, leaderboard, and a personal impact panel

## Prerequisites

- Java 17 or newer, and Maven 3.9+
- Node.js 18 or newer
- PostgreSQL 14+ *(optional – skip it by using the H2 profile below)*

## Run it

### 1. Backend

**Option A – no database install (quickest, data resets on restart):**

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

**Option B – PostgreSQL:**

```sql
-- in psql
CREATE DATABASE uniloop;
```

```bash
cd backend
# defaults: localhost:5432, user postgres, password postgres. Override if yours differ:
#   export DB_URL=jdbc:postgresql://localhost:5432/uniloop
#   export DB_USERNAME=postgres
#   export DB_PASSWORD=yourpassword
mvn spring-boot:run
```

Tables are created automatically (`ddl-auto: update`). On an empty database, demo users, items and finished loans are inserted so the dashboard isn't blank. The API runs on **http://localhost:8080**.

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. Vite proxies `/api` to the backend, so there is nothing to configure in development.

### Demo accounts

All use the password `password123`: `aarav@uniloop.dev`, `diya@uniloop.dev`, `kabir@uniloop.dev`.

**Try the full QR flow:** log in as Diya in one window and Aarav in a private window. Aarav requests Diya's calculator → Diya approves → Aarav clicks *Show pickup QR* → Diya pastes the code and clicks *Confirm handover* → later Aarav shows the *return QR* → Diya confirms the return → both trust scores go up and the dashboard updates.

## REST API

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/api/auth/register`, `/api/auth/login` | – | Get a JWT |
| GET | `/api/users/me` | ✔ | Current user + trust score |
| GET | `/api/items?q=&category=&onlyAvailable=` | – | Search items |
| GET | `/api/items/{id}` | – | One item |
| GET | `/api/items/mine` | ✔ | My items |
| POST / PUT / DELETE | `/api/items[/{id}]` | ✔ owner | Manage listings |
| PATCH | `/api/items/{id}/availability?value=` | ✔ owner | Pause / resume sharing |
| GET | `/api/transactions` | ✔ | My borrowing + lending history |
| POST | `/api/transactions` | ✔ | Request an item `{itemId, days, message}` |
| POST | `/api/transactions/{id}/approve` · `/reject` | ✔ owner | Answer a request |
| POST | `/api/transactions/{id}/cancel` | ✔ borrower | Cancel before pickup |
| POST | `/api/transactions/{id}/verify-handover` | ✔ owner | `{token}` from borrower's QR |
| POST | `/api/transactions/{id}/verify-return` | ✔ owner | `{token}` from borrower's return QR |
| GET | `/api/analytics/public` | – | Platform-wide sustainability stats |
| GET | `/api/analytics/me` | ✔ | My personal impact |

Send the token as `Authorization: Bearer <jwt>`.

## Project layout

```
uniloop/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/uniloop/
│       ├── config/       SecurityConfig, error handling, DataSeeder
│       ├── security/     JwtService, JwtAuthFilter, AuthUser
│       ├── model/        User, Item, BorrowTransaction + enums
│       ├── repository/   Spring Data JPA repositories
│       ├── service/      Auth, Item, Transaction (state machine), Analytics
│       ├── controller/   REST controllers
│       └── dto/          Request/response records
└── frontend/
    └── src/
        ├── pages/        Browse, Login, Register, MyItems, Transactions, Dashboard
        ├── components/   Navbar, ItemCard, Badges (status tracker), QR + borrow modals
        ├── api.js        fetch wrapper (adds JWT)
        └── auth.jsx      auth context
```

## Configuration

Set via environment variables (see `backend/src/main/resources/application.yml`): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (**change it** – at least 32 characters), `CORS_ORIGINS`. Set `uniloop.seed-demo-data: false` to disable demo data.

For a production frontend, run `npm run build` and serve `frontend/dist`, with `CORS_ORIGINS` set to that site's origin and the `/api` calls routed to the backend.

## Ideas to extend

- Camera-based QR scanning for the owner (e.g. `html5-qrcode`) instead of pasting the code
- Image upload for items, in-app chat, email/push reminders for due dates
- Admin role: moderate listings, adjust trust scores
- Flyway migrations instead of `ddl-auto: update`

## Notes on the sustainability numbers

CO₂ per share and "money saved" are **estimates**: CO₂ uses a fixed kg value per category (`Category.java`), and money saved is the item's "price if bought new" per completed share. Adjust them to match whatever source you cite in your report.
