---
sidebar_label: 'Overview'
sidebar_position: 1
---

# Database Overview

The application utilizes an **Offline-first architecture**. This means the local database is the "Source of Truth" for the UI, ensuring the app remains fully functional without an active internet connection.

---

### Core Technology: Room Persistence
We use **Room** as our primary persistence layer. It provides an abstraction over SQLite, offering:
* **Compile-time verification** of SQL queries.
* **Reactive streams** using Kotlin Coroutines and Flow.
* **Type Safety** through strongly typed Entities and DAOs.

---

### The Data Flow
How the data moves through the app is important for consistency.

1.  **UI Layer:** Observes data via ViewModels.
2.  **Repository:** Orchestrates data movement and handles business logic (like barcode normalization).
3.  **DAO:** Executes the actual SQL commands.
4.  **Room Database:** Manages the SQLite file and migrations.

---

### Key Patterns
* **Single Source of Truth:** The UI never talks to the API directly; it observes the database, which is updated by the repository.
* **Immutability:** Data entities are immutable to prevent accidental side effects during state changes.

### Current Database Stats
| Feature | Status |
| :--- | :--- |
| **Engine** | SQLite / Room |
| **Version** | 12 |
| **Tables** | 5 (Product, Order, OrderRow, Payment, PaymentDetails) |
| **Migrations** | Destructive (Development mode) |