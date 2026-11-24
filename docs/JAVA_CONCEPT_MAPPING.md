# JAVA CONCEPT MAPPING & VIVA DEFENSE GUIDE
## Campus Lost & Found – IntelliRecover

This document maps every fundamental Java concept taught in university OOP courses to its exact implementation in the IntelliRecover codebase. Use this table and explanation guide directly during your viva examination.

---

## 1. Object-Oriented Programming (OOP) Concepts

### 1.1 Encapsulation
- **Exact Class & Method**:
  - `com.intellirecover.model.User` (private fields `id`, `username`, `passwordHash`, `salt`; public getters/setters).
  - `com.intellirecover.model.VerificationQuestion` (`getQuestion()`, `verifyAnswer()`, with private `secretAnswer`).
- **Why It Is Used**: To prevent direct modification of sensitive state from outside the class, and to ensure data integrity.
- **Viva Explanation**:
  > *"Encapsulation bundles data (fields) and the methods operating on that data into a single unit, hiding internal representation through private access modifiers. In `VerificationQuestion`, the `secretAnswer` is private and never exposed via `toString()`. Verification occurs internally via the `verifyAnswer(candidate)` method, ensuring confidential answers never leak to logs or terminal menus."*

### 1.2 Inheritance
- **Exact Class & Method**:
  - `User` $\to$ `Student` and `Administrator`.
  - `Item` $\to$ `LostItem` and `FoundItem`.
- **Why It Is Used**: To eliminate code duplication by sharing common attributes (e.g. `id`, `name`, `category`, `location`) in the superclass while allowing subclasses to define specialized properties.
- **Viva Explanation**:
  > *"Inheritance models an IS-A relationship using the `extends` keyword. A `Student` IS-A `User` with added attributes like `studentId` and `department`. Similarly, `LostItem` and `FoundItem` inherit identity, description, and status from the abstract `Item` class, promoting code reusability."*

### 1.3 Polymorphism (Method Overriding)
- **Exact Class & Method**:
  - `User.getRoleHeader()` overridden in `Student` and `Administrator`.
  - `Item.getItemType()` and `Item.getIncidentDate()` overridden in `LostItem` and `FoundItem`.
  - `MatchResult.compareTo(MatchResult other)` overriding `Comparable<MatchResult>`.
- **Why It Is Used**: Enables the program to invoke subclass-specific behavior through a superclass reference at runtime.
- **Viva Explanation**:
  > *"Polymorphism allows dynamic method dispatch at runtime. When calling `currentUser.getRoleHeader()`, the JVM checks the actual object type in memory. If it is a `Student`, it outputs student enrollment information; if it is an `Administrator`, it outputs staff control panel credentials without needing ugly `if-else` type-checking."*

### 1.4 Abstraction
- **Exact Class & Method**:
  - `public abstract class User` and `public abstract class Item`.
- **Why It Is Used**: You cannot have a generic 'User' or a generic 'Item' without knowing whether it is a lost or found report, or a student or administrator.
- **Viva Explanation**:
  > *"Abstraction hides implementation details and enforces a design contract. An abstract class cannot be instantiated directly with `new`. By declaring `User` and `Item` abstract, we force developers to instantiate meaningful subclasses (`Student`, `FoundItem`) and implement abstract contract methods."*

---

## 2. Java Collections Framework

### 2.1 HashMap
- **Exact Class & Method**:
  - `com.intellirecover.repository.UserRepository` (`usersById`, `usersByUsername`, `usersByEmail`).
  - `com.intellirecover.repository.ItemRepository` (`lostItems`, `foundItems`).
- **Why It Is Used**: To provide fast, constant-time $O(1)$ retrieval of records by primary key (ID) or unique index (username).
- **Viva Explanation**:
  > *"HashMap stores key-value pairs using hash tables. We use `usersById` and `usersByUsername` to look up users in $O(1)$ average time complexity instead of scanning an array or list with $O(N)$ linear time complexity."*

### 2.2 HashSet
- **Exact Class & Method**:
  - `com.intellirecover.service.MatchingService.extractKeywords(String text)`.
  - `com.intellirecover.service.MatchingService.calculateJaccardSimilarity(Set<String> setA, Set<String> setB)`.
- **Why It Is Used**: To store unique descriptive tokens, eliminate repeated words, and perform rapid set intersection (`retainAll`) and union (`addAll`).
- **Viva Explanation**:
  > *"HashSet uses a hash table to guarantee uniqueness of elements. In our Jaccard similarity calculation, we tokenize descriptions into unique keywords. Using `HashSet` allows us to perform set intersection via `retainAll()` in linear time relative to token count."*

### 2.3 PriorityQueue
- **Exact Class & Method**:
  - `com.intellirecover.service.MatchingService.findMatchesForLostItem()`
  - `com.intellirecover.service.RescueService.getUnclaimedPriorityList()`
- **Why It Is Used**: Implements a min-heap or max-heap structure to rank elements by priority.
- **Viva Explanation**:
  > *"A PriorityQueue organizes elements based on their natural ordering or a custom `Comparator`. In `MatchingService`, candidate matches are inserted into a PriorityQueue ordered by `totalScore` descending. Polling from the queue ensures the highest-confidence matches are retrieved first in $O(N \log N)$ time."*

### 2.4 ArrayList
- **Exact Class & Method**:
  - `com.intellirecover.repository.HistoryRepository.history`.
  - `com.intellirecover.model.FoundItem.secretQuestions`.
- **Why It Is Used**: For ordered, indexed lists where elements must preserve chronological sequence.
- **Viva Explanation**:
  > *"ArrayList provides a resizable array implementation. We use it for audit event histories and questions because elements maintain their insertion order and can be iterated in sequence."*

---

## 3. Custom Exception Handling

### 3.1 Custom Exception Hierarchy
- **Exact Class & Method**:
  - `com.intellirecover.exception.IntelliRecoverException` (Base checked exception).
  - `com.intellirecover.exception.InvalidReportException`.
  - `com.intellirecover.exception.ClaimAlreadyExistsException`.
  - `com.intellirecover.exception.AuthenticationException`.
  - `com.intellirecover.exception.StorageException`.
- **Why It Is Used**: Checked exceptions enforce compile-time error handling, ensuring business failures (e.g., competing claims, invalid dates) are handled gracefully rather than terminating the application.
- **Viva Explanation**:
  > *"We created a domain-specific exception hierarchy extending `Exception`. By defining checked exceptions like `ClaimAlreadyExistsException`, the compiler obligates callers in the UI layer to catch and present friendly feedback to the student rather than crashing the JVM with an unhandled runtime error."*

### 3.2 Try-With-Resources & Multi-Catch
- **Exact Class & Method**:
  - `com.intellirecover.storage.FileStorageManager.loadUsers()`:
    ```java
    try (BufferedReader reader = Files.newBufferedReader(usersFile, StandardCharsets.UTF_8)) { ... }
    ```
- **Why It Is Used**: Automatically closes file streams at block termination, preventing operating system file handle leaks.
- **Viva Explanation**:
  > *"Try-with-resources implements the `AutoCloseable` interface. The `BufferedReader` is automatically closed when execution leaves the try block, even if an `IOException` or parsing exception occurs, eliminating the need for boilerplate `finally` blocks."*

---

## 4. File Persistence & Standard Java NIO

### 4.1 Java NIO Paths & Files
- **Exact Class & Method**:
  - `com.intellirecover.storage.FileStorageManager` (`java.nio.file.Path`, `Files.exists`, `Files.createDirectories`, `Files.move`).
- **Why It Is Used**: Modern Java File I/O (NIO.2) provides better cross-platform file manipulation, atomic moves, and charset support compared to legacy `java.io.File`.
- **Viva Explanation**:
  > *"Java NIO uses `Path` and `Files` utility methods. We use `Files.move(tempFile, target, StandardCopyOption.ATOMIC_MOVE)` to achieve atomic file replacement. If the system crashes during write, the existing data file remains uncorrupted."*

### 4.2 RFC-4180 State Machine CSV Parsing
- **Exact Class & Method**:
  - `com.intellirecover.storage.CSVUtil.parseLine(String line)`.
  - `com.intellirecover.storage.CSVUtil.readRecord(BufferedReader reader)`.
- **Why It Is Used**: Standard `String.split(",")` crashes or corrupts data when user descriptions contain commas, double quotes, or line breaks.
- **Viva Explanation**:
  > *"A naive `split(\",\")` fails when a user enters 'Central Library, 2nd Floor'. Our `CSVUtil` implements a character-by-character state machine tracking whether the parser is currently inside a quoted block (`inQuotes`). It handles escaped double quotes (`\"\"`) and multi-line descriptions according to the official RFC-4180 CSV standard."*

---

## 5. Security & Cryptography

### 5.1 Salted SHA-256 Password Hashing
- **Exact Class & Method**:
  - `com.intellirecover.util.PasswordUtil.hashPassword(String password, String salt)`.
  - `com.intellirecover.util.PasswordUtil.generateSalt()`.
- **Why It Is Used**: Storing plaintext passwords is a severe security vulnerability. Salting prevents rainbow table attacks.
- **Viva Explanation**:
  > *"We use `java.security.MessageDigest` to compute SHA-256 hashes. A 16-byte cryptographically secure random salt generated by `SecureRandom` is concatenated with the password before hashing. Even if two students choose the identical password, their stored hashes in `users.csv` are completely different."*

### 5.2 Item DNA Cryptographic Fingerprinting
- **Exact Class & Method**:
  - `com.intellirecover.util.FingerprintUtil.generateFingerprint(...)`.
- **Why It Is Used**: Creates a deterministic unique digital signature for an item based on its normalized attributes to detect duplicates.
- **Viva Explanation**:
  > *"Item DNA concatenates the normalized category, lowercase title, brand, sorted keywords, and distinctive attributes, passing the string through SHA-256. If a student attempts to file an identical item twice, the fingerprint matches and immediately flags a duplicate report."*

---

## 6. Java Date & Time API (JSR-310)

### 6.1 LocalDate & LocalDateTime
- **Exact Class & Method**:
  - `com.intellirecover.util.DateUtil`.
  - `ChronoUnit.DAYS.between(d1, d2)` in `DateUtil.daysBetween()`.
- **Why It Is Used**: Replaces the deprecated, mutable, and thread-unsafe `java.util.Date` and `Calendar` with immutable, thread-safe classes.
- **Viva Explanation**:
  > *"The modern `java.time` API (introduced in Java 8) provides immutable and thread-safe date classes. We use `LocalDate` for incident dates and `LocalDateTime` for audit timestamps. `ChronoUnit.DAYS.between()` cleanly computes day differences without manual millisecond arithmetic."*

---

## 7. Java Streams & Functional Programming

### 7.1 Filter, Map, and Collectors
- **Exact Class & Method**:
  - `com.intellirecover.service.ItemService.searchPublicFoundItems()`:
    ```java
    itemRepository.getAllFoundItems().stream()
        .filter(i -> i.getStatus() != ItemStatus.RETURNED)
        .filter(i -> filterItem(i, query, category, location, null, null))
        .collect(Collectors.toList());
    ```
- **Why It Is Used**: Enables declarative, readable data filtering and transformations without nested `for` loops.
- **Viva Explanation**:
  > *"Java Streams allow functional-style operations over collections. By chaining `.filter()` predicates and collecting with `.collect(Collectors.toList())`, multi-criteria search queries are expressed cleanly, concisely, and maintainably."*
