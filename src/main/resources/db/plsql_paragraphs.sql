CREATE OR REPLACE TRIGGER trg_paragraph_delete_guard
BEFORE DELETE ON paragraphs
FOR EACH ROW
DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM reports WHERE paragraph_id = :OLD.id;

  IF v_count > 0 THEN
    RAISE_APPLICATION_ERROR(-20001, 'Nie mozna usunac paragrafu powiazanego z donosami.');
  END IF;
END;
/
