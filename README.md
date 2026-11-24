# Campus Lost & Found – IntelliRecover
### A Smart Item Recovery and Ownership Verification System

> **Course**: Programming in Java (CS201 / Core OOP)  
> **Application Type**: Terminal-Based Java Application  
> **Execution Environment**: OpenJDK 17+ / Java 25 LTS (Pure Standard Java, No External Web/GUI Frameworks)

---

## 1. Project Title
**Campus Lost & Found – IntelliRecover: A Smart Item Recovery and Ownership Verification System**

## 2. Project Description
**IntelliRecover** is an enterprise-grade, terminal-driven console application engineered in pure Java to modernize university campus lost-and-found management. Unlike simplistic notice-board software or naive keyword-lookup catalogs, IntelliRecover integrates a **deterministic multi-attribute match engine**, **secret ownership verification questions**, cryptographic **Item DNA SHA-256 fingerprinting**, **duplicate report detection**, **Claim Protection controls**, **terminal-based campus incident heatmaps**, **smart expiry rescue prioritization**, and an **immutable chronological recovery audit timeline**.

## 3. Problem Statement
Every academic semester across university campuses, thousands of valuable personal belongings—such as water bottles, wallets, student ID cards, expensive scientific calculators, earphones, backpacks, and hostel keys—are misplaced. Traditional lost-and-found operations rely on disjointed physical notice boards, informal WhatsApp/Telegram groups, or fragmented security desks. This creates severe inefficiencies:
- **Search Fragmentation**: Students do not know which department or desk holds their belonging.
- **Inconsistent Descriptions**: Finders and losers describe identical objects differently (e.g., *"Black flask"* vs. *"Dark sports bottle"*).
- **Fraudulent Claims**: Malicious actors falsely claim attractive items when public listings reveal every distinctive attribute.
- **Duplicate Reports**: Repeated filings clutter logs and create administrative confusion.
- **Lack of Accountability**: No structured audit trail tracks who reported, claimed, reviewed, and physically received items.
- **Forgotten Unclaimed Items**: Misplaced items languish indefinitely in storage closets without an ethical disposal or rescue pipeline.

## 4. Objectives
1. Provide structured registration for both Lost and Found campus belongings.
2. Automate cross-referencing between active lost items and found items using a transparent 100-point deterministic algorithm.
3. Decouple **Match Confidence** from **True Ownership Verification** via private verification questions.
4. Detect duplicate reports before saving to prevent registry pollution.
5. Prevent simultaneous competing claims on the same physical item via **Claim Protection**.
6. Provide visual terminal-based text analytics and frequency distribution heatmaps.
7. Prioritize overdue unclaimed items for administrative handover or charity donation.
8. Maintain a persistent, verifiable recovery timeline for every item lifecycle.

## 5. Scope
- **Domain**: University campuses, collegiate academic blocks, laboratories, student hostels, libraries, dining halls, and sports complexes.
- **Form Factor**: Terminal-based interactive console interface (ANSI-compatible text tables and formatted menus).
- **Data Persistence**: Local RFC-4180 compliant CSV persistence with robust quote, comma, and newline handling.
- **Boundary**: Standalone desktop/workstation execution without third-party web frameworks, Spring Boot, or paid AI APIs.

## 6. Intended Users
- **University Students**: Misplaced an item or turned in a found belonging; challenge verification questions; track claims.
- **Campus Administrators / Security Officers**: Oversee item custody, evaluate claims, approve handovers, monitor hotspot analytics, and triage unclaimed items.
- **Guests**: Public catalog search and read-only lookup of found items (strictly redacting private verification data).

---

## PROJECT VISION AND FUTURE DEVELOPMENT

### Strategic Overview: Why IntelliRecover is Not an Ordinary Lost & Found
Traditional campus lost-and-found implementations function as passive digital bulletin boards:
- They store basic reports without intelligent cross-referencing.
- They rely entirely on students manually browsing and spotting matching items.
- They lack fraud-resistant ownership verification (publicly revealing all identifying traits).
- They have no audit trail for physical handovers and allow unclaimed goods to accumulate indefinitely.

**IntelliRecover** is conceived as a smart item recovery platform:
1. **Intelligent Discovery**: Automatically cross-references lost and found reports using deterministic multi-attribute scoring.
2. **Explainable Matching**: Shows why reports correlate through granular mathematical breakdowns.
3. **Item Identity (Item DNA)**: Uses cryptographic SHA-256 hashing to identify duplicate filings.
4. **Ownership Confidence**: Conceals private identifying traits; challenges claimants with secret questions.
5. **Controlled Custody Recovery**: Enforces single-active-claim protection and administrator-reviewed handovers.
6. **Campus Intelligence**: Generates text-based spatial heatmaps to pinpoint high-loss campus zones.

### Phase 1 vs. Phase 2 Capability Roadmap

| Capability | Phase 1 (Working Prototype) | Phase 2 (Advanced Enterprise Platform) |
| :--- | :--- | :--- |
| **Student Authentication** | **Working**: Salted SHA-256 password hashing, student/admin registration, login, RBAC, input validation. | **Improved Security**: Role session timeouts, audit security logging, department-level authorization tiers. |
| **Lost/Found Reporting** | **Working**: Structured forms, category enums, date validation, location tracking, persistent `LST-XXXX` & `FND-XXXX` IDs. | **Enhanced Report Management**: Multi-stage report management, reward escrow notes, photo attachment metadata. |
| **Search and Filtering** | **Working**: Multi-criteria search by keyword, category, location, date range; active catalog views. | **Advanced Filtering**: Compound boolean queries, fuzzy typo-tolerant search, cross-category filtering. |
| **Basic Automatic Matching** | **Working**: 100-point deterministic engine comparing Category, Keywords, Location, and Date. | **Weighted Jaccard Matching**: Full Jaccard token similarity with dynamic stop-word dictionaries and configurable thresholds. |
| **Match Ranking** | **Basic**: Descending sort order with transparent point breakdown and classification tiers. | **Advanced Explainable Ranking**: Max-heap PriorityQueue ranking with fine-grained multi-attribute correlation. |
| **Item DNA Fingerprint** | **Foundation Designed**: Normalized attribute payload specification. | **SHA-256 Implementation**: Automated duplicate detection alerting students to existing reports (>70%). |
| **Duplicate Detection** | **Basic / Planned**: Core similarity comparison logic designed. | **Advanced Fingerprint-Based Detection**: Interactive pre-persistence warning with View, Proceed, or Cancel options. |
| **Ownership Verification** | **Design Established**: Concept of private questions defined. | **Private Verification Questions**: Finder-configured questions, normalized scoring, and threshold pass/fail. |
| **Claim Management** | **Basic / Planned**: Claim models and states planned. | **Administrator-Reviewed Claims & Return**: Single-active-claim protection, admin approval workflow, and physical handover. |
| **Campus Analytics** | **Foundation Formulated**: Metric counters and location normalization. | **Text-Based Heatmap & Statistics**: Proportional ASCII horizontal bar charts for campus hotspots and recovery rates. |
| **Expiry and Rescue** | **Holding Window Defined**: Retention period heuristics established. | **Overdue Unclaimed Item Priority Queue**: Heap-based overdue item triage recommending charity donation or vault transfer. |
| **Recovery Timeline** | **Lifecycle Stages Defined**: Operational events classified. | **Comprehensive Chronological Audit Trail**: Step-by-step history log from initial report to verified owner receipt. |

---

---

## 7. Major Features
| Feature | Technical Implementation | Purpose & Value |
| :--- | :--- | :--- |
| **1. Intelligent Match Engine** | Jaccard text similarity + multi-criteria weighted scoring (100 pts) | Automatically discovers lost/found pairs with full mathematical breakdown |
| **2. Secret Ownership Verification** | Case-insensitive normalized answer comparison | Prevents fraud by requiring claimants to prove intimate item knowledge |
| **3. Item DNA Fingerprint** | Cryptographic SHA-256 hashing via `MessageDigest` | Generates unique deterministic fingerprint from normalized attributes |
| **4. Duplicate Detection** | Pre-persistence similarity check (>70% threshold warning) | Warns students if an identical report already exists; offers review options |
| **5. Claim Protection** | Strict state machine: 1 active claim per found item | Eliminates race conditions and prevents simultaneous competing claims |
| **6. Campus Heatmap Analytics** | Text-based proportional ASCII horizontal bar charts | Visualizes campus incident hotspots and category recovery rates |
| **7. Smart Expiry & Rescue** | PriorityQueue ranking overdue found items | Flags long-unclaimed belongings for donation or storage vault transfer |
| **8. Recovery Timeline** | Immutable chronological audit trail (`RecoveryEvent`) | Complete lifecycle tracking from `REPORTED` to `RECOVERY_COMPLETED` |

---

## 8. Technology Stack
- **Language**: Java 17+ (Compiled and tested on OpenJDK 25 LTS).
- **Core Libraries**: Standard Java SE (`java.time`, `java.security`, `java.util`, `java.nio.file`, `java.io`).
- **Dependencies**: **Zero third-party runtime dependencies** (Pure standard Java).
- **Build & Test Automation**: Shell scripts (`build.sh`, `run.sh`, `test.sh`, `demo.sh`).
- **Version Control**: Git.

---

## 9. Java Concepts Used
1. **Object-Oriented Programming (OOP)**:
   - **Encapsulation**: Private fields, immutable models, defensive copying, and redacted accessors.
   - **Inheritance**: Abstract `User` extended by `Student` and `Administrator`; abstract `Item` extended by `LostItem` and `FoundItem`.
   - **Polymorphism**: Subclass overrides of `getRoleHeader()`, `getItemType()`, `getIncidentDate()`, and `compareTo()`.
   - **Abstraction**: Abstract classes enforcing service contracts and storage boundaries.
2. **Collections Framework**:
   - `HashMap`: Fast $O(1)$ in-memory lookups by ID, username, and email.
   - `HashSet`: Keyword tokenization, stop-word filtering, and Jaccard intersection sets.
   - `ArrayList`: Dynamic ordered storage of reports, audit events, and notifications.
   - `PriorityQueue`: Min/Max heap-based ranking of match results and unclaimed rescue priorities.
   - `TreeMap` / `LinkedHashMap`: Ordered frequency sorting in analytics generation.
3. **Exception Handling**:
   - Hierarchy of custom checked exceptions: `IntelliRecoverException`, `InvalidReportException`, `AuthenticationException`, `StorageException`, `ClaimAlreadyExistsException`, `ResourceNotFoundException`.
   - Fault-tolerant multi-catch blocks and atomic file rewrite fallbacks.
4. **File I/O & Streams**:
   - Java NIO (`Path`, `Files`, `BufferedReader`, `StandardCopyOption.ATOMIC_MOVE`).
   - Custom state-machine CSV parser (`CSVUtil`) supporting RFC-4180 quotes, commas, and embedded newlines.
   - Java Streams API (`filter`, `map`, `collect`, `count`, `sorted`).
5. **Security & Cryptography**:
   - SHA-256 password hashing with 16-byte cryptographically secure random salts (`SecureRandom` + `MessageDigest`).
   - SHA-256 Item DNA fingerprinting over normalized attribute strings.

---

## 10. System Architecture
IntelliRecover employs a layered architectural pattern with clean boundaries between UI, business services, repository caching, and physical file persistence:

```
+-------------------------------------------------------------------------+
|                         TERMINAL PRESENTATION LAYER                      |
|       ConsoleUI  |  StudentMenu  |  AdminMenu  |  GuestMenu             |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                            SERVICE BUSINESS LAYER                       |
|  AuthenticationService  |  ItemService          |  MatchingService      |
|  VerificationService    |  ClaimService         |  DuplicateDetection   |
|  AnalyticsService       |  RescueService        |  RecoveryService      |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                           IN-MEMORY REPOSITORY LAYER                     |
|  UserRepository  |  ItemRepository  |  ClaimRepository  |  HistoryRepo   |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                            STORAGE PERSISTENCE LAYER                    |
|          FileStorageManager   <--->   CSVUtil (RFC-4180 Engine)         |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                            DATA FILES DIRECTORY                         |
|   users.csv   |   lost_items.csv   |   found_items.csv                  |
|   claims.csv  |   recovery_history.csv  |  notifications.csv            |
+-------------------------------------------------------------------------+
```

---

## 11. Package Explanation
- `com.intellirecover.model`: Domain entities (`User`, `Student`, `Administrator`, `Item`, `LostItem`, `FoundItem`, `Claim`, `MatchResult`, `RecoveryEvent`, `Notification`, enums).
- `com.intellirecover.service`: Core business logic, scoring algorithms, verification challenges, and analytics engines.
- `com.intellirecover.repository`: In-memory caching and indexing with HashMap and Stream querying.
- `com.intellirecover.storage`: File persistence, atomic disk writes, and RFC-4180 CSV serialization.
- `com.intellirecover.ui`: ANSI-formatted menus, table formatters, and terminal input validators.
- `com.intellirecover.util`: Cryptographic utilities (`PasswordUtil`, `FingerprintUtil`), date tools (`DateUtil`), ID sequences (`IDGenerator`), and input regex (`InputValidator`).
- `com.intellirecover.exception`: Custom exception hierarchy representing domain errors.

---

## 12. Data Model
```
            +--------------------------+
            |          User            |
            +--------------------------+
            | - id: String             |
            | - username: String       |
            | - passwordHash: String   |
            | - salt: String           |
            | - role: UserRole         |
            +--------------------------+
                     ^        ^
                     |        |
         +-----------+        +-----------+
         |                                |
+-----------------+              +-----------------+
|     Student     |              |  Administrator  |
+-----------------+              +-----------------+
| - studentId     |              | - staffId       |
| - department    |              | - department    |
| - phone         |              +-----------------+
+-----------------+
```
```
            +--------------------------+
            |          Item            |
            +--------------------------+
            | - id: String             |
            | - name: String           |
            | - category: ItemCategory |
            | - location: String       |
            | - status: ItemStatus     |
            | - fingerprint: String    |
            | - brand: String          |
            +--------------------------+
                     ^        ^
                     |        |
         +-----------+        +-----------+
         |                                |
+-----------------+              +-----------------------------+
|    LostItem     |              |          FoundItem          |
+-----------------+              +-----------------------------+
| - dateLost      |              | - dateFound                 |
| - rewardNotes   |              | - storageLocation           |
+-----------------+              | - secretQuestions: List<VQ> |
                                 | - currentClaimId: String    |
                                 +-----------------------------+
```

---

## 13. Matching Algorithm
When comparing a candidate `LostItem` against a `FoundItem`, the algorithm executes the following deterministic pipeline:
1. **Normalization**: Strips non-alphanumeric punctuation and lowercases text.
2. **Stop-Word Removal**: Discards 24 common English prepositions/fillers ("the", "in", "at", "for", "with", etc.).
3. **Keyword Tokenization**: Extracts unique keyword sets $K_{lost}$ and $K_{found}$ from item names and descriptions.
4. **Jaccard Similarity Index**: Computes keyword overlap:
   $$J(K_{lost}, K_{found}) = \frac{|K_{lost} \cap K_{found}|}{|K_{lost} \cup K_{found}|}$$
5. **Location Proximity**: Evaluates exact location match or shared building keywords.
6. **Date Proximity**: Computes absolute day difference:
   $$\Delta_{days} = |\text{DateLost} - \text{DateFound}|$$
7. **Brand & Distinctive Attributes**: Rewards manufacturer identity and unique attribute matches.
8. **Ranking**: Places results into a `PriorityQueue` ordered by total score descending.

---

## 14. Matching Score Formula
The deterministic matching algorithm allocates up to **100 points**:

$$\text{Total Score} = S_{\text{category}} + S_{\text{keywords}} + S_{\text{location}} + S_{\text{date}} + S_{\text{attributes}}$$

| Component | Maximum Points | Scoring Rules |
| :--- | :---: | :--- |
| **Category Similarity** | **20 pts** | Exactly matching category = 20 pts; different category = 0 pts. |
| **Keyword Similarity** | **35 pts** | $\text{Round}(Jaccard \times 35.0)$ |
| **Location Similarity** | **20 pts** | Exact location string match = 20 pts; shared campus building zone = 14 pts; different zone = 0 pts. |
| **Date Proximity** | **15 pts** | $\Delta_{days} = 0 \to 15\text{ pts}$; $\Delta_{days} \le 1 \to 13\text{ pts}$; $\Delta_{days} \le 3 \to 10\text{ pts}$; $\Delta_{days} \le 7 \to 6\text{ pts}$; $\Delta_{days} \le 14 \to 3\text{ pts}$; $> 14 \to 0\text{ pts}$. |
| **Brand & Attributes** | **10 pts** | Matching brand = 6 pts; distinctive attribute keyword overlap = 4 pts. |

### Classification Tiers
- **80 – 100**: `STRONG MATCH`
- **60 – 79**: `POSSIBLE MATCH`
- **40 – 59**: `WEAK MATCH`
- **Below 40**: `NO MATCH` (Filtered out from standard display)

---

## 15. Item DNA Fingerprint Explanation
Every item registered generates a 64-character SHA-256 digital fingerprint using Java's `java.security.MessageDigest`.
- **Composition**:
  $$\text{Payload} = \text{Category} \parallel \text{Name}_{\text{normalized}} \parallel \text{Brand}_{\text{normalized}} \parallel \text{SortedKeywords} \parallel \text{Attributes}$$
- **Role in Duplicate Detection**: If two reports produce an identical fingerprint, they represent the exact same entity. The system warns the student with a 95%+ similarity warning.
- **Short Terminal Display**: Displayed as a 12-character hex hash (e.g. `076EE9780A89`).
- **Important**: The fingerprint is used for duplicate detection and record cross-referencing, **never as legal proof of ownership**.

---

## 16. Ownership Verification Explanation
IntelliRecover solves the fatal flaw of conventional lost-and-found systems: **false claims based on public details**.
- **Finder Question Configuration**: When reporting a found item, the finder can enter private identifying questions (e.g., *"What sticker is on the bottle?"*, *"What initials are on the calculator sticker?"*).
- **Concealment**: Secret questions and answers are **never displayed** in search listings or public guest directories.
- **Verification Challenge**: Claimants must answer these questions. The system performs case-insensitive normalization (stripping spaces and punctuation).
- **Verification Score**:
  $$\text{Score} = \left(\frac{\text{Correct Answers}}{\text{Total Questions}}\right) \times 100$$
- **Administrative Review**: Even passing claims (Score $\ge 75\%$) are submitted to the campus administrator for physical ID verification before the item is handed over.

---

## 17. File Storage Explanation
All records persist under the `data/` directory:
- `users.csv`: User profiles, salt, password hashes, roles, and department IDs.
- `lost_items.csv`: Lost items registry, dates, categories, locations, and fingerprints.
- `found_items.csv`: Found items, storage lockers, and secret verification questions.
- `claims.csv`: Ownership claims, candidate answers, verification percentages, and admin review notes.
- `recovery_history.csv`: Immutable chronological audit logs (`RecoveryEvent`).
- `notifications.csv`: User status updates and match notifications.

**RFC-4180 Engine (`CSVUtil`)**: Handles fields with internal commas (e.g., `"Central Library, 2nd Floor"`), embedded quotes (escaped as `""`), and multi-line descriptions without naive split errors. Writes use temporary files and atomic file replacement (`StandardCopyOption.ATOMIC_MOVE`).

---

## 18. Installation Instructions
### Prerequisites
- macOS, Linux, or Windows with Bash/WSL.
- Java Development Kit (JDK 17 or higher; OpenJDK 25 LTS verified).
- Git.

### Clone and Setup
```bash
# Clone the repository
git clone <repository-url>
cd "campus_lost _found"

# Make helper scripts executable
chmod +x build.sh run.sh test.sh demo.sh
```

---

## 19. Execution Instructions
### 1. Compile the Project
```bash
./build.sh
```

### 2. Run the Interactive Terminal Application
```bash
./run.sh
```

### 3. Run the Automated 20-Point Test Suite
```bash
./test.sh
```

### 4. Run the Official Scenario Demonstration
```bash
./demo.sh
```

---

## 20. Sample Demonstration Credentials
| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `alice` | `password123` | Student | Computer Science student; lost black water bottle |
| `bob` | `password123` | Student | Electrical Engineering student; found sports bottle |
| `charlie` | `password123` | Student | Mechanical Engineering student; claimed leather wallet |
| `diana` | `password123` | Student | Business student; lost AirPods Pro |
| `eva` | `password123` | Student | Biotechnology student; lost biochemistry textbook |
| `admin` | `adminpass` | Administrator | Dr. Arthur Vance, Head of Campus Security |

---

## 21. Sample Terminal Output

### Match Engine Transparent Explanation
```
LOST ITEM: Black Water Bottle (ID: LST-1001)
POSSIBLE FOUND ITEM: Black Sports Bottle (ID: FND-1001)
Match Score: 86/100

Reasons:
 - Same category (Water Bottles & Flasks): +20 pts
 - Description keyword similarity (Jaccard: 0.25, shared: [sports, black, bottle]): +8.8 pts
 - Exact campus location match ("Central Library 2nd Floor"): +20 pts
 - Exact same date of occurrence: +15 pts
 - Matching brand ("Milton"): +6 pts
 - Similar identifying attributes ([crest, university]): +4 pts

Classification: STRONG MATCH
```

### Campus Incident Heatmap
```
======================================================================
            CAMPUS INTELLIRECOVER - RECOVERY ANALYTICS & HEATMAP       
======================================================================

KEY METRICS SUMMARY:
 - Total Lost Items Reported  : 10
 - Total Found Items Handed In: 10
 - Successfully Recovered     : 2 (20.0% overall recovery rate)
 - Pending Ownership Claims   : 1
 - Active Unclaimed Items     : 7

----------------------------------------------------------------------
CAMPUS INCIDENT HOTSPOTS (INCIDENTS BY LOCATION):
----------------------------------------------------------------------
CENTRAL LIBRARY        | #########################  6
CAMPUS CAFETERIA       | #################          4
SCIENCE & ENG LABS     | #############              3
SPORTS COMPLEX         | #########                  2
PARKING LOTS           | #########                  2
MAIN AUDITORIUM        | #########                  2
```

---

## 22. Limitations
1. **Terminal Single-Process Prototype**: Designed for standard terminal interaction on a single machine; does not provide concurrent multi-user network sockets.
2. **Local Cryptographic Storage**: Passwords use salted SHA-256. In enterprise distributed production, PBKDF2, BCrypt, or Argon2 would be preferred.
3. **Keyword-Based Text Semantics**: Uses deterministic Jaccard token matching. While transparent and viva-defensible, it does not perform deep natural language semantic synonym expansion.

---

## 23. Future Enhancements
1. **Relational Database Migration**: Replace CSV persistence with JDBC + SQLite / PostgreSQL.
2. **REST API & Web Dashboard**: Expose services via Spring Boot REST controllers for a React/Mobile frontend.
3. **Automated Barcode / QR Labeling**: Generate custody tag QR codes for physical lockers.
4. **Campus Email Dispatcher**: Integrate JavaMail API to send SMTP email notices directly to student inboxes.

---

## 24. Team Contribution
- **Lead Software Architect & Developer**: System design, OOP architecture, matching algorithm, verification engine, CSV storage manager, terminal UI menus, automated test suite, and technical documentation.
