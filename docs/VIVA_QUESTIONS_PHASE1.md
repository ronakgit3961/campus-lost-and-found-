# PHASE 1 VIVA VOCE QUESTIONS & DEFENSE GUIDE
## Campus Lost & Found – IntelliRecover
### Evaluation Window: November 25–28, 2026

This guide provides direct, crisp, and technically accurate answers to expected viva questions from your university evaluators.

---

## Part 1: Architecture & Design Decisions

### Q1: What is the high-level architecture of your Phase 1 application?
**Answer**:
IntelliRecover follows a modular, layered architecture:
1. **Presentation Layer (`com.intellirecover.ui`)**: Terminal console menus (`ConsoleUI`, `StudentMenu`, `AdminMenu`, `GuestMenu`) handling user input and table formatting.
2. **Business Service Layer (`com.intellirecover.service`)**: Handles business logic (`AuthenticationService`, `ItemService`, `MatchingService`).
3. **Repository Layer (`com.intellirecover.repository`)**: In-memory caching using `HashMap` for fast $O(1)$ lookups.
4. **Storage Persistence Layer (`com.intellirecover.storage`)**: Reads and writes physical CSV files (`FileStorageManager`, `CSVUtil`) under `data/`.
5. **Utility & Models (`com.intellirecover.util`, `model`)**: Shared entities, security hashing, validation, and ID generation.

### Q2: Why did you choose a terminal-based application instead of a web or GUI framework?
**Answer**:
This project was specifically designed to demonstrate core **Java programming fundamentals**—OOP design patterns, collections, multi-level inheritance, custom exception handling, NIO file I/O, and algorithmic text comparison. Building a web dashboard or GUI often distracts with HTML/CSS or UI boilerplate, whereas a clean terminal interface keeps the focus strictly on core Java engineering, algorithm explainability, and memory management.

### Q3: How do you prevent the application from crashing when a user enters invalid input?
**Answer**:
All console inputs are read as raw strings using `Scanner.nextLine()` and routed through static methods in `InputValidator`. Numeric menu inputs are parsed using `InputValidator.parsePositiveInt()`, which wraps `Integer.parseInt()` inside a `try-catch (NumberFormatException)` block and returns a safe fallback default value instead of throwing an unhandled exception to the main thread.

---

## Part 2: Object-Oriented Programming (OOP)

### Q4: Give a concrete example of Encapsulation in your Phase 1 project.
**Answer**:
In `com.intellirecover.model.User`, all fields (`id`, `username`, `passwordHash`, `salt`, `fullName`, `email`, `role`) are declared `private`. They can only be accessed through public getters and controlled setters. In `VerificationQuestion`, the `secretAnswer` is stored privately and is never exposed through `toString()`, ensuring that confidential verification data is protected from leaking into logs or terminal menus.

### Q5: How is Inheritance implemented and what problem does it solve?
**Answer**:
Inheritance models an **IS-A** relationship. We have an abstract base class `User` that contains common user properties, which is extended by `Student` (adding `studentId`, `department`, `phone`) and `Administrator` (adding `staffId`, `department`). Similarly, `Item` is extended by `LostItem` and `FoundItem`. This eliminates redundant code and enforces a single source of truth for identity and timestamps.

### Q6: Where is Polymorphism used, and how does method overriding work here?
**Answer**:
Polymorphism is demonstrated in `User.getRoleHeader()`. The abstract `User` class declares:
```java
public abstract String getRoleHeader();
```
`Student` overrides it to display:
```java
"STUDENT PORTAL | " + getFullName() + " (ID: " + studentId + ")"
```
`Administrator` overrides it to display:
```java
"ADMINISTRATOR CONTROL PANEL | " + getFullName() + " (Staff ID: " + staffId + ")"
```
At runtime, calling `currentUser.getRoleHeader()` dynamically invokes the appropriate subclass implementation via virtual method invocation, without requiring manual `instanceof` checks.

### Q7: Why did you make `User` and `Item` abstract classes instead of concrete classes or interfaces?
**Answer**:
A student or admin cannot lose a "generic item"; they lose a specific `LostItem` (with a loss date and reward) or find a specific `FoundItem` (with a storage custody desk). Making `Item` abstract prevents developers from instantiating incomplete, ambiguous objects while still allowing us to share common concrete state (such as `id`, `name`, `category`, and `location`) and concrete helper methods, which interfaces cannot hold as mutable instance state.

---

## Part 3: Java Collections Framework

### Q8: Why did you use `HashMap` in your repositories? What is its time complexity?
**Answer**:
In `UserRepository` and `ItemRepository`, we store records in `HashMap<String, User>` and `HashMap<String, Item>`. The average time complexity for retrieval (`get()`), insertion (`put()`), and deletion (`remove()`) is **$O(1)$** because it computes the hash code of the string key to access the bucket directly. If we had used an `ArrayList`, finding an item by ID would require scanning every element, resulting in **$O(N)$** linear time.

### Q9: Where did you use `HashSet` and why?
**Answer**:
In `MatchingService.extractKeywords()`, we extract unique description tokens into a `HashSet<String>`. A `HashSet` automatically prevents duplicate words (e.g., if "bottle" appears three times in a description, it is only stored once). Furthermore, calculating the Jaccard similarity requires computing the intersection of two keyword sets; `setA.retainAll(setB)` performs this operation efficiently.

### Q10: Where did you use `PriorityQueue`?
**Answer**:
In `MatchingService.findMatchesForLostItem()`, candidate matches are stored in a `PriorityQueue<MatchResult>`. `MatchResult` implements `Comparable<MatchResult>`, ordering instances by `totalScore` descending. Polling from the `PriorityQueue` automatically retrieves match recommendations ranked from highest confidence to lowest confidence in $O(N \log N)$ time.

### Q11: What is the difference between `ArrayList` and `LinkedList`, and why did you choose `ArrayList`?
**Answer**:
`ArrayList` is backed by a dynamically resizing contiguous array, offering fast $O(1)$ random access and cache-friendly memory locality. `LinkedList` is a doubly linked list with pointer overhead on every node and $O(N)$ random access. For our items catalog, reports list, and audit events, reading and iterating sequentially are the predominant operations, making `ArrayList` significantly faster and more memory-efficient.

---

## Part 4: File Persistence & CSV Handling

### Q12: Why did you build a custom CSV parser (`CSVUtil`) instead of just using `String.split(",")`?
**Answer**:
`String.split(",")` is naive and fundamentally breaks when user input contains commas, quotation marks, or newlines. For example, if a student enters the location `"Central Library, 2nd Floor"`, naive splitting splits the location across two separate array indices, misaligning all subsequent columns and corrupting the database. Our `CSVUtil` implements a character-by-character state machine compliant with the **RFC-4180 standard**, correctly preserving quoted commas and handling escaped quotes (`""`).

### Q13: How does your file persistence prevent accidental data loss or file corruption?
**Answer**:
In `FileStorageManager.atomicWrite()`, instead of overwriting the active CSV file directly, the system writes the new records to a temporary file (`.tmp`). Once the temporary file is completely and successfully written, it replaces the target file using `Files.move(tempFile, target, StandardCopyOption.ATOMIC_MOVE)`. If power is lost or the application crashes mid-write, the existing data file remains untouched and uncorrupted.

### Q14: How are new entity IDs synchronized after an application restart?
**Answer**:
In `IDGenerator.synchronizeSequence(String id)`, whenever an existing record is loaded from a CSV file during startup (e.g., `LST-1015`), the atomic sequence counter compares its current value against the parsed numeric suffix (`1015`) and updates its sequence to `Math.max(current, val)`. This guarantees that when the user reports a new item after a restart, the assigned ID will be `LST-1016`, eliminating primary key collisions.

---

## Part 5: Exception Handling

### Q15: What is the difference between Checked and Unchecked exceptions, and what did you use?
**Answer**:
- **Checked Exceptions**: Subclasses of `Exception` (excluding `RuntimeException`). The compiler forces the programmer to handle them using `try-catch` or declare them using `throws`.
- **Unchecked Exceptions**: Subclasses of `RuntimeException` (e.g., `NullPointerException`, `NumberFormatException`), representing programming bugs or unexpected runtime faults.
In IntelliRecover, all custom exceptions (`InvalidReportException`, `AuthenticationException`, `ClaimAlreadyExistsException`, `StorageException`) extend `IntelliRecoverException`, which extends `Exception` (Checked). This forces the UI layer to catch domain errors and inform the user cleanly instead of allowing the JVM to terminate.

### Q16: Show how you use Try-With-Resources.
**Answer**:
In `FileStorageManager.loadUsers()`:
```java
try (BufferedReader reader = Files.newBufferedReader(usersFile, StandardCharsets.UTF_8)) {
    String header = reader.readLine();
    List<String> record;
    while ((record = CSVUtil.readRecord(reader)) != null) {
        // process record
    }
} catch (IOException e) {
    throw new StorageException("Failed to read users", e);
}
```
Because `BufferedReader` implements `AutoCloseable`, the JVM guarantees that the file handle is closed as soon as the try block finishes, even if an exception occurs inside the loop.

---

## Part 6: Algorithm & Logic

### Q17: Explain the formula used in your Basic Automatic Matching Engine.
**Answer**:
The matching algorithm computes a weighted score up to **100 points**:
1. **Category Match (20 pts)**: Exactly matching category awards 20 points; different categories award 0.
2. **Keyword Similarity (35 pts)**: Computes Jaccard Similarity index over extracted description and title keywords ($J = \frac{|A \cap B|}{|A \cup B|}$) and multiplies by 35.
3. **Location Proximity (20 pts)**: Exact campus location match awards 20 points; sharing a campus building zone (e.g., both contain "Library") awards 14 points.
4. **Date Proximity (15 pts)**: Same-day loss and found awards 15 points; $\le 1$ day awards 13 points; $\le 3$ days awards 10 points; $\le 7$ days awards 6 points; $\le 14$ days awards 3 points.
5. **Brand / Distinctive Attributes (10 pts)**: Matching brand name awards 6 points; overlapping distinctive attributes award 4 points.
Total = $20 + 35 + 20 + 15 + 10 = 100$ points.

### Q18: What is Jaccard Similarity and why is it used?
**Answer**:
Jaccard Similarity measures the similarity between two finite sample sets by dividing the size of the intersection by the size of the union:
$$J(A, B) = \frac{|A \cap B|}{|A \cup B|}$$
In text matching, it calculates the ratio of common descriptive words to the total unique words across both reports. It produces a normalized decimal between 0.0 (no shared words) and 1.0 (identical keywords), scaling smoothly to prevent bias toward verbose descriptions.

---

## Part 7: Security & Cryptography

### Q19: How do you store user passwords securely in Phase 1?
**Answer**:
We use salted SHA-256 password hashing. In `PasswordUtil`:
1. When a student registers, `SecureRandom` generates a cryptographically random 16-byte salt (Base64 encoded).
2. The plaintext password is concatenated with the salt and hashed via `MessageDigest.getInstance("SHA-256")`.
3. The resulting hash and the salt are stored in `users.csv`.
4. Plaintext passwords are never stored on disk or held in memory. During login, the candidate password is hashed with the user's stored salt and compared against the stored hash.

---

## Part 8: Phase 1 Scope vs. Phase 2 Roadmap

### Q20: What is complete in Phase 1, and what is planned for Phase 2?
**Answer**:
- **Phase 1 Deliverables (Completed & Verified)**:
  1. Student & Administrator Authentication with salted SHA-256 and RBAC.
  2. Lost Item Reporting with input validation, date checks, and persistent `LST-XXXX` IDs.
  3. Found Item Reporting with custody tracking and persistent `FND-XXXX` IDs.
  4. Multi-criteria Search & Filtering (Name, Category, Location, Date, Active catalog views).
  5. Basic Automatic Matching Engine with Jaccard keywords, location proximity, PriorityQueue descending ranking, and transparent score explanations.
  6. Robust RFC-4180 CSV persistence across application restarts.
- **Phase 2 Extensions (Advanced Features)**:
  1. Secret Ownership Verification Challenges (finder-configured private questions to prevent false claims).
  2. Item DNA Fingerprinting (SHA-256 hash across normalized item properties).
  3. Pre-persistence Duplicate Report Warnings (preventing clutter).
  4. Claim Protection & Formal Handover Lifecycle (`CLAIM_PENDING` $\to$ `VERIFIED` $\to$ `RETURNED`).
  5. Terminal Campus Incident Heatmap Analytics (ASCII horizontal bar charts).
  6. Smart Expiry & Unclaimed Rescue Priority Queue.
  7. Chronological Audit Recovery Timeline (`RecoveryEvent`).
