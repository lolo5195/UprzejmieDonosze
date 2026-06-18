CREATE INDEX idx_reports_author
    ON reports(author_id);

CREATE INDEX idx_reports_accused_user
    ON reports(accused_user_id);

CREATE INDEX idx_reports_paragraph
    ON reports(paragraph_id);

CREATE INDEX idx_reports_status_event_date
    ON reports(status, event_date);