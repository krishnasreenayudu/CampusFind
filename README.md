# CampusFind – Smart Campus Lost & Found Management System

## About the Project

CampusFind is a Java-based desktop application developed to make the lost and found process within a college campus easier and more organized.

Students can report lost or found items, search for items, view possible matches, and claim found belongings by providing confidential details to verify ownership. Administrators can review claims, manage users and items, approve or reject claims, and record item handovers.

The main objective of CampusFind is to provide a centralized platform for managing lost and found belongings while ensuring a reliable ownership verification process.

## Key Features

### Student Features
- **User Registration and Login:** Students can create accounts and securely log in.
- **Report Lost and Found Items:** Submit item details such as category, colour, location, date, and description.
- **Search Items:** Search and filter reported items using different criteria.
- **Find Matching Items:** View suggested found items that may match a reported lost item.
- **Ownership Verification:** Claim a found item by providing a confidential detail known to its actual owner.
- **Track Reports and Claims:** View personal reports and claim statuses.

### Administrator Features
- **Admin Dashboard:** View statistics related to users, items, claims, and suspicious activities.
- **Claim Management:** Review submitted claims and approve or reject them with remarks.
- **Item and User Management:** Manage registered users and reported items.
- **Handover Management:** Record the handover of approved items to their owners.
- **Suspicious Activity Detection:** Flag users who reach the configured threshold of rejected claims.
- **Claim History:** Maintain an audit trail of claim activities and recovery records.

## Project Workflow

1. Students register and log in to CampusFind.
2. Students report lost or found belongings by entering the required details.
3. Users search for reported items and view possible matches.
4. A student claiming a found item must provide its confidential identifying detail.
5. The system verifies the submitted detail and creates a claim if the verification is successful.
6. The administrator reviews the claim and approves or rejects it.
7. Once approved, the administrator records the item handover.
8. The item status is updated to recovered, and the activity is stored in the history.

## System Architecture

CampusFind follows a layered architecture that separates the user interface, business logic, database operations, and data models.

- **UI Layer:** Provides the Java Swing screens for students and administrators.
- **Service Layer:** Handles application logic, validations, claim processing, and transactions.
- **DAO Layer:** Performs database operations using SQL and JDBC.
- **Model Layer:** Contains the data classes used throughout the application.
- **Utility Layer:** Provides helper functions for database connections, password hashing, validation, and session management.

## Database

The application uses MySQL with six main tables:

| Table | Purpose |
|---|---|
| `users` | Stores student and administrator account details. |
| `items` | Stores lost and found item reports. |
| `claims` | Stores ownership claims and their statuses. |
| `verification_attempts` | Records confidential-detail verification attempts. |
| `claim_history` | Maintains the history of claim-related activities. |
| `recoveries` | Stores item handover records. |

## Security and Reliability

- Passwords and confidential identifying details are stored as BCrypt hashes.
- Students have a maximum of three failed verification attempts per item.
- Duplicate claims for the same item by the same student are prevented.
- Database transactions help maintain data consistency.
- Item records are locked during claim processing to prevent simultaneous conflicting claims.
- Suspicious users are flagged after reaching the configured rejection threshold.

## Technologies Used

- **Programming Language:** Java 17
- **User Interface:** Java Swing, FlatLaf
- **Database:** MySQL 8
- **Database Connectivity:** JDBC
- **Build Tool:** Maven
- **Security:** jBCrypt
