CREATE OR REPLACE VIEW v_user_stats AS
SELECT
  u.id AS user_id,
  u.username AS username,
  (SELECT COUNT(*)
     FROM reports r
    WHERE r.author_id = u.id) AS submitted_count,
  (SELECT COUNT(*)
     FROM reports r
    WHERE r.accused_user_id = u.id) AS received_count
FROM app_users u;
/
