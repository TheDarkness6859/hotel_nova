# 🏨 HotelNova - Property Management System

HotelNova is a centralized management application designed to automate and streamline hotel operations, including room management, guest registration, and reservation tracking[cite: 6, 12]. [cite_start]It replaces manual spreadsheet processes with a robust Java-based solution, ensuring data integrity, security, and operational efficiency.

---

## 👤 Author Information
* **Name:** Emmanuel Suarez Garcia
* **ID (TI):** 1033186879
* **Contact:** suarezgarcia939@gmail.com

---

## 🚀 Technologies Used
* **Java 17+**: Core programming language using modern features like Records and Pattern Matching.
* **JavaFX**: Graphical user interface for desktop management.
* **Maven**: Dependency management and build automation.
* **PostgreSQL / MySQL**: Relational database for persistent storage.
* **JDBC**: Database connectivity and ACID transaction management.
* **JUnit 5**: Unit testing for business logic validation.
* **BCrypt**: Secure password hashing for user authentication.

---

## ⚙️ Configuration & Execution

### 1. Prerequisites
* **JDK 17** or higher.
* **Apache Maven** installed.
* **Linux Environment** (Optimized for Ubuntu/Mint).

### 2. Database Setup
The system reads environmental variables and business rules from a `config.properties` file located in the project root. Configure your credentials as follows:

```properties
db.url=jdbc:postgresql://localhost:5432/hotelnova
db.user=your_username
db.password=your_password
# Business Rules
horaCheckin=15
horaCheckOut=12
iva=0.19

---

## Clean and install dependencies
mvn clean install.

---

## Launch the JavaFX application
mvn javafx:run.

## Launch tests
mvn test
