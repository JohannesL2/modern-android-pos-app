---
id: architecture
title: App Architecture
slug: /architecture
---

# Architecture & Design

This page outlines the technical foundation of the POS application. The project is built with a focus on speed, offline-first reliability, and a modern developer experience.

<style>{`
  .arch-grid {
    display: flex;
    flex-direction: row;
    gap: 1.5rem;
    align-items: center;
    justify-content: center;
    margin: 2rem 0;
  }
  .arch-img {
    width: 50%;
    height: auto;
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(124, 58, 237, 0.15);
    border: 1px solid rgba(124, 58, 237, 0.1);
  }
  @media (max-width: 768px) {
    .arch-grid {
      flex-direction: column;
    }
    .arch-img {
      width: 100%;
    }
  }
`}</style>

## Feature-Based MVVM
We utilize a **Feature-based MVVM** (Model-View-ViewModel) structure. By grouping code by functionality rather than technical layers, we ensure that the codebase remains scalable and easy to navigate.

### Core Pillars

1. **The Composable View**
   - Built entirely with **Jetpack Compose**.
   - Uses `State Hoisting` for modularity.
   - Observes `StateFlow` from the ViewModel for real-time UI updates.

2. **The Feature ViewModel**
   - Manages UI state for specific features (e.g., Payments, History).
   - Communicates directly with **Room DAOs** and **Ktor** to reduce boilerplate.
   - Handles asynchronous tasks via Kotlin Coroutines.

3. **Data Sources**
   - **Room Database:** Local persistence for transactions and settings.
   - **Ktor:** High-performance networking for terminal communication.

---

## Technical Stack

| Layer | Technology |
| :--- | :--- |
| **UI** | Jetpack Compose (Material 3) |
| **Logic** | Kotlin Coroutines & Flow |
| **DI** | Dagger 2 (via KSP) |
| **Database** | Room SQLite |
| **Networking** | Ktor Client |
| **Animations** | Lottie Compose |

---

## Project Structure

```text
com.example.pos_app
├── features               # Functional modules (UI + Logic)
│   ├── payment            # Checkout UI & Transaction ViewModels
│   └── history            # Transaction logs & History ViewModels
├── data                   # Global Data Engine
│   ├── dao                # Room Data Access Objects (SQL Queries)
│   ├── entity             # Database Tables / Data Models
│   ├── repository         # Data coordination (Local vs Remote)
│   └── utils              # Data-related helpers & formatters
├── di                     # Dependency Injection (Dagger/Hilt)
└── ui.theme               # Design System (Color, Type, Theme)