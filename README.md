# AK BANK — "Banking Made Simple."

> **DEMO DISCLAIMER**:
> **AK Bank is a fictional digital banking application created exclusively for demonstration and portfolio purposes.**
> No real banking, payment gateways, real financial accounts, or monetary services are provided. All balances, account numbers, IBANs, and transactions are strictly simulated demo data.

---

## 🏛️ Project Overview

**AK Bank** is a complete, production-grade digital banking application designed with modern aesthetics comparable to major Pakistani mobile banking applications (e.g. Meezan, HBL, Bank Alfalah), while featuring completely original branding, minimalist luxury design (`#0A2540` Navy, `#00D09C` Emerald Mint), and local database persistence.

### Key Capabilities
- **Role-Based Access Control (RBAC)**:
  - 👤 **Customer**: Accounts, transfers, bill payments, mobile top-up, virtual cards, AI financial assistant, statements.
  - 🛡️ **Administrator**: System liquidity overview, customer management, account suspension/activation, transaction reviews, immutable audit trail.
  - 🎧 **Support Desk Agent**: Customer inquiry lookup, support ticket thread messaging, resolution status.
- **Durable Local Persistence**:
  - Full relational database powered by **Room / SQLite** with rich pre-seeded mock transactions, accounts, cards, billers, and audit logs.
- **Interactive Financial Tools**:
  - Interbank transfers with live balance validation and digital PDF-style receipt generation.
  - Utility bill lookup and payment (K-Electric, LESCO, SSGC, PTCL).
  - Mobile airtime recharge (Jazz, Telenor, Zong, Ufone).
  - Virtual Debit Card with instant lock/freeze, spending limit sliders, and CVV reveal.
  - **AK Assistant**: Natural language financial insights engine analyzing your actual transaction history.

---

## 🔑 Demo Credentials

| Role | Email | Password | Transaction PIN |
|---|---|---|---|
| **Customer** | `demo@akbank.demo` | `Demo@12345` | `1234` |
| **Administrator** | `admin@akbank.demo` | `Admin@12345` | `9999` |
| **Support Agent** | `support@akbank.demo` | `Support@12345` | `5555` |

*(Quick 1-tap demo login buttons are provided on the login screen for instant evaluation).*

---

## 📱 Mobile Architecture & Tech Stack

- **Platform**: Android Native
- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Local Persistence**: Android Room Database (SQLite) + Kotlin Symbol Processing (KSP)
- **State Management**: Android Architecture Components (ViewModel, StateFlow, Coroutines)
- **Navigation**: Jetpack Navigation Compose
- **Design System**: AK Bank Custom Palette (Navy `#0A2540`, Mint `#00D09C`, Gold `#E5A93C`) with automatic Dark & Light mode.

---

## 🐳 Optional Full-Stack Docker Backend

A companion FastAPI + PostgreSQL + Redis backend is provided in the repository:

```bash
# Start backend and database services
docker compose up --build
```

- **Backend API**: `http://localhost:8000`
- **Interactive Swagger Docs**: `http://localhost:8000/docs`

---

## 📁 Project Structure

```
├── app/
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── BankDao.kt
│   │   │   │   └── DatabaseInitializer.kt
│   │   │   ├── model/
│   │   │   │   └── Entities.kt
│   │   │   └── repository/
│   │   │       └── BankRepository.kt
│   │   ├── ui/
│   │   │   ├── components/
│   │   │   │   └── CommonComponents.kt
│   │   │   ├── screens/
│   │   │   │   ├── admin/AdminScreen.kt
│   │   │   │   ├── analytics/AnalyticsScreen.kt
│   │   │   │   ├── assistant/AssistantScreen.kt
│   │   │   │   ├── auth/AuthScreens.kt
│   │   │   │   ├── cards/CardsScreen.kt
│   │   │   │   ├── home/HomeScreen.kt
│   │   │   │   ├── landing/LandingScreen.kt
│   │   │   │   ├── profile/ProfileScreen.kt
│   │   │   │   ├── search/SearchAndNotifications.kt
│   │   │   │   └── transfers/TransfersScreen.kt
│   │   │   └── theme/
│   │   │       ├── Color.kt
│   │   │       ├── Theme.kt
│   │   │       └── Type.kt
│   │   └── viewmodel/
│   │       └── BankViewModel.kt
│   └── build.gradle.kts
├── backend/
│   ├── Dockerfile
│   ├── main.py
│   └── requirements.txt
├── database/
│   └── init.sql
├── docker-compose.yml
├── metadata.json
└── README.md
```

---

## 🛡️ Play Policy & Privacy Compliance
- Zero dangerous runtime permissions required.
- No real banking or financial data collected.
- Fictional demo CNIC and phone formats strictly for demonstration.
