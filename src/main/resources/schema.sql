-- CampusFind database schema (MySQL 8 / MariaDB).
-- The app runs this automatically on start-up (every statement is IF NOT EXISTS),
-- and you can also run it by hand in MySQL Workbench after
--   CREATE DATABASE campusfind; USE campusfind;

CREATE TABLE IF NOT EXISTS users (
  user_id       INT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  roll_no       VARCHAR(30)  NOT NULL UNIQUE,
  email         VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role          ENUM('STUDENT','ADMIN') NOT NULL DEFAULT 'STUDENT',
  is_flagged    BOOLEAN NOT NULL DEFAULT FALSE,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS items (
  item_id     INT AUTO_INCREMENT PRIMARY KEY,
  reporter_id INT NOT NULL,
  type        ENUM('LOST','FOUND') NOT NULL,
  title       VARCHAR(120) NOT NULL,
  category    VARCHAR(30)  NOT NULL,
  color       VARCHAR(40)  NOT NULL,
  location    VARCHAR(120) NOT NULL,
  event_date  DATE NOT NULL,
  description TEXT,
  secret_hash VARCHAR(100) NULL,
  status      ENUM('OPEN','CLAIM_PENDING','APPROVED','RECOVERED') NOT NULL DEFAULT 'OPEN',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_items_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id) ON DELETE CASCADE,
  INDEX idx_items_category (category),
  INDEX idx_items_type_status (type, status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS claims (
  claim_id     INT AUTO_INCREMENT PRIMARY KEY,
  item_id      INT NOT NULL,
  claimant_id  INT NOT NULL,
  status       ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  claim_notes  TEXT,
  admin_remarks VARCHAR(255),
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  decided_at   DATETIME NULL,
  CONSTRAINT fk_claims_item     FOREIGN KEY (item_id)     REFERENCES items(item_id) ON DELETE CASCADE,
  CONSTRAINT fk_claims_claimant FOREIGN KEY (claimant_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT uq_claim_once UNIQUE (item_id, claimant_id),
  INDEX idx_claims_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS verification_attempts (
  attempt_id   INT AUTO_INCREMENT PRIMARY KEY,
  item_id      INT NOT NULL,
  user_id      INT NOT NULL,
  is_success   BOOLEAN NOT NULL,
  attempted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_att_item FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE,
  CONSTRAINT fk_att_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  INDEX idx_att_user_item (user_id, item_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS claim_history (
  history_id  INT AUTO_INCREMENT PRIMARY KEY,
  claim_id    INT NOT NULL,
  old_status  VARCHAR(20) NULL,
  new_status  VARCHAR(20) NOT NULL,
  action_type VARCHAR(30) NOT NULL,
  changed_by  INT NULL,
  remarks     VARCHAR(255),
  changed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_hist_claim FOREIGN KEY (claim_id)   REFERENCES claims(claim_id) ON DELETE CASCADE,
  CONSTRAINT fk_hist_user  FOREIGN KEY (changed_by) REFERENCES users(user_id)  ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS recoveries (
  recovery_id    INT AUTO_INCREMENT PRIMARY KEY,
  claim_id       INT NOT NULL UNIQUE,
  item_id        INT NOT NULL,
  handed_over_by INT NULL,
  remarks        VARCHAR(255),
  handed_over_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rec_claim FOREIGN KEY (claim_id)       REFERENCES claims(claim_id) ON DELETE CASCADE,
  CONSTRAINT fk_rec_item  FOREIGN KEY (item_id)        REFERENCES items(item_id)   ON DELETE CASCADE,
  CONSTRAINT fk_rec_admin FOREIGN KEY (handed_over_by) REFERENCES users(user_id)   ON DELETE SET NULL
) ENGINE=InnoDB;
