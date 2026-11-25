CampusFind: Smart Campus Lost & Found Management System
A desktop application where students report lost and found items, search and match them, and claim found items by proving ownership. An administrator reviews claims, hands items over and monitors suspicious behaviour.

Course: CSE 309 Advanced Java Programming, SRM University-AP (Group A11) Stack: Java 17, Swing (FlatLaf look), JDBC, MySQL 8, Maven, jBCrypt

Member	Roll no.
Harshitha Bhavana	AP2411001383
Shaik Chandini	AP24110011351
Kallam Chinmaye Naga Purnima	AP24110011264
Nayudu Krishna Sree	AP24110010152
1. Problem and Aim
Lost items on campus are usually announced in chat groups. There is no record, no search, and no way to check that the person collecting an item is the real owner. CampusFind gives the campus one central system where:

anyone can report a lost or found item,
items can be searched and matched,
a found item can only be claimed by someone who knows a confidential detail about it,
an admin approves every claim and records the handover,
every step is stored as a history.
2. Features
Student

Register and log in (email or roll number, password stored as a BCrypt hash)
Report a LOST or FOUND item (category, colour, location, date, description)
When reporting a FOUND item, enter a confidential detail that only the owner would know; it is stored only as a hash
Search items with filters (type, category, colour, location, text)
"Find matches" on a lost report: suggested found items
Claim a found item by typing the confidential detail (3 failed attempts per item)
Track own reports and own claims
Administrator

Overview dashboard with totals
Review claims: approve or reject with remarks
Record the handover (creates a recovery record)
Manage items and users
Suspicious users list (users with 3 or more rejected claims are flagged)
Full claim history (audit trail)
3. Architecture
Layered design. Each layer only talks to the one below it.

ui  (Swing screens)
 └─ service  (business rules, transactions)
     └─ dao  (SQL with PreparedStatement)
         └─ MySQL via JDBC
model (plain data classes) and util (helpers) are used by all layers
Main design decisions:

Connection per operation. DBConnection opens a fresh connection for each operation. There is no shared global connection.
Transactions. Db.read() runs queries. Db.write() and Db.exec() run work in a transaction with commit and rollback.
BCrypt for passwords and for the confidential detail. Plain text is never stored.
Concurrency safety. A claim locks the item row (SELECT ... FOR UPDATE) so two people cannot claim the same item at once. UNIQUE(item_id, claimant_id) stops duplicate claims.
Auto setup. On start the app creates the database, runs schema.sql, creates the admin account and (optionally) demo data.
4. Project structure
CampusFind/
├─ pom.xml                       Maven build (shade plugin makes a runnable jar)
├─ README.md
├─ TEAM_SETUP.md                 Setup steps for teammates
├─ db.local.properties.example   Template for your own MySQL password
├─ .gitignore
├─ .vscode/settings.json
├─ sql/
│  ├─ schema.sql                 Table definitions
│  └─ useful_queries.sql         9 queries for the viva (JOIN, GROUP BY, subquery, HAVING)
└─ src/main/
   ├─ resources/
   │  ├─ db.properties           Shared default DB settings
   │  └─ schema.sql              Same schema, run automatically at start-up
   └─ java/campusfind/
      ├─ Main.java, ConnectionCheck.java
      ├─ model/    User, Role, Item, ItemType, ItemStatus, SearchCriteria,
      │            Claim, ClaimStatus, ClaimHistory, Recovery,
      │            VerificationAttempt, SuspiciousUser
      ├─ util/     DBConnection, PasswordHasher, SecretHasher, SessionManager,
      │            Validator, AppConfig
      ├─ service/  Db, ServiceException, AuthService, ItemService,
      │            ClaimService, ClaimResult, AdminService, FraudService
      ├─ dao/      UserDAO, ItemDAO, ClaimDAO, AttemptDAO, HistoryDAO,
      │            RecoveryDAO, DatabaseSetup
      └─ ui/
         ├─ auth/     LoginFrame, RegisterFrame
         ├─ common/   Theme, Ui, StatCard, StyledTable, PagePanel, BaseDashboard
         ├─ student/  StudentDashboard, StudentHomePanel, ReportItemPanel,
         │            SearchItemsPanel, MyReportsPanel, MatchesDialog,
         │            ClaimDialog, MyClaimsPanel
         └─ admin/    AdminDashboard, AdminOverviewPanel, ReviewClaimsPanel,
                      ManageItemsPanel, ManageUsersPanel,
                      SuspiciousUsersPanel, HistoryPanel
5. Database
Six tables (MySQL, InnoDB):

Table	Purpose
users	Accounts: name, roll_no, email, password_hash, role (STUDENT/ADMIN), is_flagged
items	Lost and found reports, including secret_hash for found items and status
claims	One claim per (item, claimant): status, notes, admin remarks
verification_attempts	Every secret-detail attempt (success or failure), used for the 3-attempt limit
claim_history	Audit trail: old status, new status, action, who, when
recoveries	Handover record created when the admin hands the item over
Item status: OPEN → CLAIM_PENDING → APPROVED → RECOVERED Claim status: PENDING / APPROVED / REJECTED

6. How a claim works
Student opens a found item and clicks Claim.
In one transaction the app locks the item row and checks the item is FOUND and OPEN, is not the student's own, and the student has not claimed it before.
It counts the student's failed attempts for this item. At 3 the student is blocked.
The typed detail is normalised (trimmed, lower-case, extra spaces collapsed) and compared with the stored BCrypt hash.
Wrong: the attempt is logged and attempts left are shown. Right: a PENDING claim is created, the item becomes CLAIM_PENDING and a history row is written.
Admin approves (claim APPROVED) or rejects (claim REJECTED, item reopened, and the user is flagged after 3 rejections).
Admin records the handover: a recoveries row is created and the item becomes RECOVERED.
Suggested matches
For a lost item, ItemDAO.findMatchesForLost returns FOUND items that are OPEN, have the same category, and have the same colour (case-insensitive) or a location containing the lost item's location, and were reported by a different user.

7. Setup
What you need
JDK 17 or newer
MySQL 8 running locally
Maven (or VS Code with the Java Extension Pack, which can run Maven)
Internet for the first build (to download dependencies)
Steps
Open the project folder in VS Code.
Copy db.local.properties.example to db.local.properties in the project root.
Put your own MySQL settings in it:
db.user=root
db.password=YOUR_MYSQL_PASSWORD
This file is git-ignored, so passwords are never shared.
Build and run:
mvn clean package
java -jar target/CampusFind-1.0.jar
(Use the actual jar name created in target/.) Or run campusfind.Main from VS Code.
The first run creates the campusfind database, tables, the admin account and demo data.
ConnectionCheck can be run on its own to test the database settings.

Settings
src/main/resources/db.properties holds defaults (host localhost, port 3306, database campusfind, user root). db.local.properties in the working directory overrides them. seed.demo=true adds demo data only while there are no students; set it to false for a clean start.

Reset the database
DROP DATABASE campusfind;
Start the app again and it rebuilds everything.

8. Demo accounts and data
Role	Login	Password
Administrator	admin@campus.edu (choose "Administrator")	admin123
Student	rahul@campus.edu	student123
Student	priya@campus.edu	student123
Student	arjun@campus.edu	student123
Seeded found items (reported by Priya) and the detail needed to claim each:

Item	Confidential detail
Black Leather Wallet	photo of a golden retriever inside
Wireless Earbuds	blue silicone case with a star sticker
Java Programming Textbook	name kiran written on page 10
Seeded lost items (reported by Rahul): Black Wallet, Bunch of Keys.

Capitalisation and extra spaces do not matter. Spelling and word order do.

9. How to test the whole flow
Login as Rahul → My Reports → select the lost Black Wallet → Find matches. Priya's found wallet is suggested.
Claim it as Rahul with the correct detail from section 8. A claim is created (CLAIM_PENDING). To test the lock instead, claim with wrong answers three times: the dialog shows attempts left, then blocks you.
Login as the admin → Review Claims → Approve.
Admin records the Handover. The item becomes RECOVERED.
To see fraud flagging, claim items with several accounts and reject three claims from one user. The user shows in red under Suspicious Users.
Open History to see every step.
Tip: run two copies of the app, one logged in as a student and one as admin, to see both sides together.

10. Testing done
During development the code was compiled with javac and checked with an automated flow test (50 checks covering registration, login, reporting, search, matching, claims, locking, approval, rejection, handover and flagging) and a UI smoke test (no errors). These ran against MariaDB. The Maven build and a real MySQL server were not available in that environment, so run mvn clean package on your machine for the first time.

11. Known limitations and future work
The confidential detail must match exactly (after trimming, lower-casing and collapsing spaces). A different word order or spelling fails.
After 3 wrong attempts a student is blocked from that item and there is no way to unlock it.
Future work: word-order tolerant matching, an admin "Reset attempts" button, email notifications, item photos.
12. Team contributions
Member	Main responsibility	Classes
Purnima	Start-up, database and login	Main, ConnectionCheck, DBConnection, PasswordHasher, SessionManager, Validator, Db, ServiceException, AuthService, DatabaseSetup, UserDAO, User, Role, LoginFrame, RegisterFrame, Theme, Ui; also pom.xml, db.properties, schema.sql, TEAM_SETUP.md
Chandini	Items, search and matching	ItemDAO, ItemService, Item, ItemType, ItemStatus, SearchCriteria, AppConfig, StudentDashboard, ReportItemPanel, SearchItemsPanel, MyReportsPanel, MatchesDialog
Harshitha	Claims and verification	ClaimDAO, AttemptDAO, RecoveryDAO, ClaimService, ClaimResult, SecretHasher, Claim, ClaimStatus, VerificationAttempt, Recovery, ClaimDialog, MyClaimsPanel, StudentHomePanel, ReviewClaimsPanel
Krishna Sree	Admin, history and fraud checks	HistoryDAO, AdminService, FraudService, ClaimHistory, SuspiciousUser, AdminDashboard, AdminOverviewPanel, ManageItemsPanel, ManageUsersPanel, SuspiciousUsersPanel, HistoryPanel, BaseDashboard, PagePanel, StatCard, StyledTable; also useful_queries.sql
13. Troubleshooting
Problem	What to check
"Cannot connect to database" on start	MySQL is running; db.password in db.local.properties is correct
"Invalid email or password"	Use the exact demo accounts; for the admin choose "Administrator" on the login screen. If the data is damaged, drop the database and restart
Nothing under Find matches	A match needs a FOUND item reported by a different user, with the same category and the same colour or a similar location
Cannot claim an item	You cannot claim your own item, an item already claimed by you, or one that is not OPEN
Maven cannot download dependencies	Check your internet connection and run mvn clean package again
14. Dependencies
Library	Version	Use
mysql-connector-j	8.4.0	JDBC driver
jbcrypt	0.4	BCrypt hashing
flatlaf	3.5.4	Modern Swing look and feel
