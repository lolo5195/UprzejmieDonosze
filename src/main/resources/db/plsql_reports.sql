CREATE OR REPLACE TRIGGER trg_reports_share_token
BEFORE INSERT ON reports
FOR EACH ROW
WHEN (NEW.share_token IS NULL)
BEGIN
  :NEW.share_token := RAWTOHEX(SYS_GUID());
END;
/