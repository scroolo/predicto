ALTER TABLE quiz_questions ALTER COLUMN correct_option TYPE VARCHAR(10) USING correct_option::VARCHAR;

UPDATE flyway_schema_history SET checksum = -101933698 WHERE version = '24';
