# Java Project for Library Management System
This project is a simple **console-based Library Management System** written in Java.
 The system manages books, members, issuing, returns, fines, and basic persistence.

---

## 🚀 Features

* Add & remove books
* Register members (student, faculty, other)
* Issue books with loan limits
* Return books with late fine calculation
* View all books or only available books
* Track member loans
* Save/load data using lightweight CSV files (`books.csv`, `members.csv`)

---

## 📁 File Structure

```
LibraryManagementSystem.java   # Main program + domain logic
books.csv                      # Auto-generated on save
members.csv                    # Auto-generated on save
```

---

## ▶️ How to Run

Make sure you have **Java 8+** installed.

### **1. Compile**

```
javac LibraryManagementSystem.java
```

### **2. Run**

```
java LibraryManagementSystem
```

On first run, if CSV files don’t exist, the program simply starts with an empty library.

---

## 💡 Persistence (How data is saved)

The system uses **plain CSV files**, not Java serialization.

### `books.csv`

Stores lines like:

```
ISBN,title,author,totalCopies,availableCopies
```

### `members.csv`

Stores:

```
memberId,name,email,type,loanData
```

Where `loanData` looks like:

```
isbn1:epochMillis;isbn2:epochMillis
```

This format is intentionally simple and not production-safe.

---

## 📜 Business Rules

* **Loan Period:** 14 days
* **Fine:** 1 unit/day after due date
* **Loan Limits:**

  * Students: max 3 books
  * Faculty: max 10 books
  * Others: default to 3

---

## 🛠️ Possible Improvements


* Proper CSV parser
* Switch persistence to JSON or a database (MySQL/SQLite)
* Add JavaFX UI
* Add authentication (admin/user roles)
* Add search by title/author
* Export reports

---

## 🧠 How It Works Internally

### 1. **Data Storage (CSV-Based Persistence)**

The system uses two CSV files:

* `books.csv` → stores book records
* `members.csv` → stores member data + issued book timestamps

Data is loaded at startup and written back when the user exits. CSV parsing is simple:

* Fields are split by commas
* Loans are stored as `isbn:epochMillis` pairs

### 2. **Core Classes**

#### **Book**

* Stores ISBN, title, author, total copies, and available copies
* Handles borrowing and returning operations

#### **Member**

* Stores basic user info and a map of issued books with timestamps
* Each loan record tracks when the book was issued

#### **Lib (Library Logic Layer)**

Responsible for all main logic:

* Adding/removing books
* Issuing/returning books
* Calculating fines
* Enforcing limits
* Saving/loading data

### 3. **Loan Process**

When a book is issued:

* Check if the member exists
* Check if the book exists
* Validate availability
* Validate loan limits
* Save loan timestamp

### 4. **Return Process**

* Fetch issue timestamp
* Compute elapsed days
* Charge fine if overdue
* Update availability
* Remove loan entry

### 5. **Menu + Input Layer**

The main class handles:

* User interaction via console
* Prompt methods for each operation
* Calling `Lib` methods for backend logic

---
## ✍️ About the Author

Hi! I'm **Jalaj Srivastava**, a developer who enjoys building practical, no-frills software projects.

I focus on writing code that is:

* understandable,
* functional,
* and easy to extend.

This project is part of my learning journey in Java and backend concepts.
Feel free to customize, improve, or fork it as you like!

**Contact:** [[jalaj.24bce10130@vitbhopal.ac.in](mailto:jalaj.24bce10130@vitbhopal.ac.in)]
