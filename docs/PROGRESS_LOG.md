# PROJECT MILESTONE & PROGRESS LOG
## Campus Lost & Found – IntelliRecover
### Development Log & Staged Academic Evaluation Roadmap

---

## Evaluation Schedule
- **Phase 1 Evaluation Window**: November 25–28, 2026
  - **Objective**: Deliver a solid, working, fully explainable Phase 1 terminal prototype with core authentication, lost/found reporting, multi-criteria search, basic automatic matching, and true CSV file persistence.
- **Phase 2 Evaluation Window**: Final Project Evaluation
  - **Objective**: Deliver the complete advanced system with secret verification questions, Item DNA fingerprinting, duplicate detection warnings, claim protection, campus incident heatmaps, and smart expiry rescue prioritization.

---

## Phase 1 Milestones (Completed & Fully Verified)

### Milestone 1: Project Architecture & Foundation Setup
- **Status**: Completed & Verified.
- **Deliverables**:
  - Clean package hierarchy under `com.intellirecover.*`.
  - Directory structure (`src/main/java`, `data`, `tests`, `docs`, `bin`).
  - Shell automation scripts (`build.sh`, `run.sh`, `test.sh`, `demo.sh`).
  - `.gitignore` configuration.
- **Java Concepts Used**: Package organization, Java compilation, environment variable configuration (`JAVA_HOME`).

### Milestone 2: User Authentication & Role-Based Access Control (RBAC)
- **Status**: Completed & Verified.
- **Deliverables**:
  - Abstract `User` base class.
  - Subclasses `Student` and `Administrator`.
  - Cryptographically secure salted password hashing via `PasswordUtil` (SHA-256 with 16-byte random salt).
  - Robust `InputValidator` regex for emails, phone numbers, and usernames.
  - Session management in `AuthenticationService`.
- **Java Concepts Used**: Inheritance, Polymorphism, Encapsulation, `SecureRandom`, `MessageDigest`.

### Milestone 3: Lost & Found Item Reporting & Validation
- **Status**: Completed & Verified.
- **Deliverables**:
  - Abstract `Item` base class with `ItemCategory` and `ItemStatus` enums.
  - `LostItem` (with incident date, reward notes).
  - `FoundItem` (with incident date, custody storage location).
  - Controlled atomic sequence ID generation (`LST-XXXX`, `FND-XXXX`).
  - Input validation preventing future dates and empty strings (`InvalidReportException`).
- **Java Concepts Used**: Abstraction, Enums, Custom Exceptions, Java Date & Time API (`LocalDate`).

### Milestone 4: CSV File Persistence & Repository Layer
- **Status**: Completed & Verified.
- **Deliverables**:
  - Custom RFC-4180 state-machine parser and writer (`CSVUtil`) supporting commas, quotes, and multi-line descriptions.
  - `FileStorageManager` coordinating disk read/writes to `data/users.csv`, `data/lost_items.csv`, and `data/found_items.csv`.
  - Atomic file replacement (`StandardCopyOption.ATOMIC_MOVE`) preventing corruption on crash.
  - In-memory `UserRepository` and `ItemRepository` caching via `HashMap` for $O(1)$ lookups.
- **Java Concepts Used**: Java NIO (`Path`, `Files`, `BufferedReader`), Collections (`HashMap`, `ArrayList`), Try-with-resources.

### Milestone 5: Search, Filtering & Basic Automatic Matching Engine
- **Status**: Completed & Verified.
- **Deliverables**:
  - Multi-criteria search by name keyword, category, location, and date range in `ItemService`.
  - Active lost and active found catalog display.
  - Initial 100-point matching engine (`MatchingService`) comparing Category (20 pts), Keywords (35 pts), Location (20 pts), and Date proximity (15 pts).
  - Natural ranking in descending order via `PriorityQueue<MatchResult>`.
  - Transparent score breakdown detailing why points were awarded.
- **Java Concepts Used**: Java Streams API (`filter`, `collect`), Collections (`HashSet`, `PriorityQueue`), Comparable interface.

---

## Phase 2 Milestones (Advanced Features)

### Milestone 6: Item DNA Fingerprinting & Duplicate Detection
- **Status**: Architecture & implementation ready; scheduled for Phase 2 viva defense.
- **Features**: SHA-256 cryptographic item fingerprinting, pre-persistence duplicate warning with interactive options (View, Continue, Cancel).

### Milestone 7: Secret Ownership Verification Challenges
- **Status**: Architecture & implementation ready; scheduled for Phase 2 viva defense.
- **Features**: Finder-configured private verification questions (`VerificationQuestion`), case-insensitive normalized candidate answer evaluation, fraud prevention.

### Milestone 8: Claim Protection & Physical Custody Lifecycle
- **Status**: Architecture & implementation ready; scheduled for Phase 2 viva defense.
- **Features**: State-machine claim lifecycle (`PENDING` $\to$ `APPROVED` $\to$ `RETURNED`), prevention of simultaneous competing claims, administrative review.

### Milestone 9: Campus Incident Analytics Heatmap & Smart Expiry Rescue
- **Status**: Architecture & implementation ready; scheduled for Phase 2 viva defense.
- **Features**: Proportional text-based horizontal ASCII bar charts, location hotspots, category recovery rates, PriorityQueue-based unclaimed item rescue tracking.

### Milestone 10: Chronological Recovery Timeline & Final System Polish
- **Status**: Architecture & implementation ready; scheduled for Phase 2 viva defense.
- **Features**: Step-by-step audit trail (`RecoveryEvent`) persisting the complete history of an item from initial report to verified owner handover.
