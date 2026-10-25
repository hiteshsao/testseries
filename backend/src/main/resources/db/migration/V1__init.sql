-- Crevolab Test Series Schema V1
-- Covers: tests, questions, attempts, attempt_answers, purchases, solution_access

CREATE TABLE IF NOT EXISTS tests (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    instructions TEXT,
    duration_minutes INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    access_type VARCHAR(20) NOT NULL DEFAULT 'FREE',
    price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    max_attempts INT NOT NULL DEFAULT 1,
    max_solution_views INT NOT NULL DEFAULT 3,
    max_pdf_downloads INT NOT NULL DEFAULT 1,
    source_pdf_path VARCHAR(500),
    solution_pdf_path VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS questions (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT NOT NULL,
    order_no INT NOT NULL,
    text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option VARCHAR(1), -- Nullable: 'A', 'B', 'C', 'D'
    marks NUMERIC(5, 2) NOT NULL DEFAULT 1.00,
    negative_marks NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_questions_test FOREIGN KEY (test_id) REFERENCES tests (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_questions_test_id ON questions (test_id);
CREATE INDEX IF NOT EXISTS idx_questions_test_order ON questions (test_id, order_no);

CREATE TABLE IF NOT EXISTS attempts (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT NOT NULL,
    student_id VARCHAR(100) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    total_marks NUMERIC(7, 2) NOT NULL DEFAULT 0.00,
    marks_obtained NUMERIC(7, 2) NOT NULL DEFAULT 0.00,
    correct INT NOT NULL DEFAULT 0,
    incorrect INT NOT NULL DEFAULT 0,
    unanswered INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_attempts_test FOREIGN KEY (test_id) REFERENCES tests (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_attempts_student_id ON attempts (student_id);
CREATE INDEX IF NOT EXISTS idx_attempts_test_student ON attempts (test_id, student_id);

CREATE TABLE IF NOT EXISTS attempt_answers (
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option VARCHAR(1), -- Nullable if marked for review without choosing
    marked_for_review BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_attempt_answers PRIMARY KEY (attempt_id, question_id),
    CONSTRAINT fk_attempt_answers_attempt FOREIGN KEY (attempt_id) REFERENCES attempts (id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_answers_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS purchases (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT NOT NULL,
    student_id VARCHAR(100) NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'SIMULATED', -- DEMO-ONLY: simulated payment provider
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchases_test FOREIGN KEY (test_id) REFERENCES tests (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_purchases_student ON purchases (student_id);
CREATE INDEX IF NOT EXISTS idx_purchases_test_student ON purchases (test_id, student_id);

CREATE TABLE IF NOT EXISTS solution_access (
    test_id BIGINT NOT NULL,
    student_id VARCHAR(100) NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    download_count INT NOT NULL DEFAULT 0,
    CONSTRAINT pk_solution_access PRIMARY KEY (test_id, student_id),
    CONSTRAINT fk_solution_access_test FOREIGN KEY (test_id) REFERENCES tests (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_solution_access_student ON solution_access (student_id);
