-- Queries you can run in MySQL Workbench (after USE campusfind;) and show in the viva.

-- 1. JOIN: pending claims with item and claimant details
SELECT c.claim_id, i.title AS item, u.name AS claimant, u.roll_no, c.status, c.created_at
FROM claims c
JOIN items i ON i.item_id = c.item_id
JOIN users u ON u.user_id = c.claimant_id
WHERE c.status = 'PENDING';

-- 2. Aggregate + GROUP BY: reports per category
SELECT category, COUNT(*) AS reports FROM items GROUP BY category ORDER BY reports DESC;

-- 3. Aggregate: items per type and status
SELECT type, status, COUNT(*) AS n FROM items GROUP BY type, status;

-- 4. Subquery: users with 3 or more rejected claims (the suspicious-user rule)
SELECT u.user_id, u.name, u.roll_no
FROM users u
WHERE (SELECT COUNT(*) FROM claims c WHERE c.claimant_id = u.user_id AND c.status = 'REJECTED') >= 3;

-- 5. GROUP BY + HAVING: users who failed verification at least twice on the same item
SELECT user_id, item_id, COUNT(*) AS failed
FROM verification_attempts
WHERE is_success = FALSE
GROUP BY user_id, item_id
HAVING COUNT(*) >= 2;

-- 6. Subquery: found items nobody has claimed yet
SELECT item_id, title, location FROM items
WHERE type = 'FOUND' AND item_id NOT IN (SELECT item_id FROM claims);

-- 7. Full audit trail of one claim (change the number)
SELECT h.changed_at, h.action_type, h.old_status, h.new_status, u.name AS changed_by, h.remarks
FROM claim_history h LEFT JOIN users u ON u.user_id = h.changed_by
WHERE h.claim_id = 1 ORDER BY h.changed_at;

-- 8. Handovers (recovery records)
SELECT r.recovery_id, i.title, cu.name AS received_by, au.name AS handed_over_by, r.handed_over_at
FROM recoveries r
JOIN items i ON i.item_id = r.item_id
JOIN claims c ON c.claim_id = r.claim_id
JOIN users cu ON cu.user_id = c.claimant_id
LEFT JOIN users au ON au.user_id = r.handed_over_by;

-- 9. The secret is only a hash - this is all anyone can see
SELECT item_id, title, secret_hash FROM items WHERE type = 'FOUND';
