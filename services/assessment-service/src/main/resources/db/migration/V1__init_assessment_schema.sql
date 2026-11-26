CREATE TABLE IF NOT EXISTS questions (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL,
    created_by UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    options_json TEXT NOT NULL,
    correct_answers_json TEXT NOT NULL,
    difficulty VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
    topic VARCHAR(128),
    tags TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_questions_subject ON questions (subject_id);

CREATE TABLE IF NOT EXISTS quizzes (
    id UUID PRIMARY KEY,
    course_section_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 30,
    max_attempts INT NOT NULL DEFAULT 1,
    shuffle_questions BOOLEAN NOT NULL DEFAULT FALSE,
    show_correct_answers BOOLEAN NOT NULL DEFAULT TRUE,
    open_at TIMESTAMPTZ,
    close_at TIMESTAMPTZ,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quizzes_section ON quizzes (course_section_id);

CREATE TABLE IF NOT EXISTS quiz_questions (
    quiz_id UUID NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    sort_order INT NOT NULL DEFAULT 0,
    points NUMERIC(6, 2) NOT NULL DEFAULT 1.00,
    PRIMARY KEY (quiz_id, question_id)
);

CREATE TABLE IF NOT EXISTS quiz_attempts (
    id UUID PRIMARY KEY,
    quiz_id UUID NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    student_id UUID NOT NULL,
    attempt_number INT NOT NULL DEFAULT 1,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    submitted_at TIMESTAMPTZ,
    score NUMERIC(6, 2),
    status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS'
);

CREATE INDEX IF NOT EXISTS idx_quiz_attempts_quiz_student ON quiz_attempts (quiz_id, student_id);

CREATE TABLE IF NOT EXISTS quiz_answers (
    attempt_id UUID NOT NULL REFERENCES quiz_attempts (id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    selected_answers_json TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    earned_points NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (attempt_id, question_id)
);

CREATE TABLE IF NOT EXISTS grade_components (
    id UUID PRIMARY KEY,
    course_section_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    weight_percent NUMERIC(5, 2) NOT NULL,
    linked_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
    linked_id UUID,
    locked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_grade_components_section ON grade_components (course_section_id);

CREATE TABLE IF NOT EXISTS student_grades (
    id UUID PRIMARY KEY,
    course_section_id UUID NOT NULL,
    student_id UUID NOT NULL,
    component_id UUID NOT NULL REFERENCES grade_components (id) ON DELETE CASCADE,
    score NUMERIC(5, 2) NOT NULL,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    updated_by UUID NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, component_id)
);

CREATE INDEX IF NOT EXISTS idx_student_grades_section ON student_grades (course_section_id, student_id);

CREATE TABLE IF NOT EXISTS attendance_sessions (
    id UUID PRIMARY KEY,
    course_section_id UUID NOT NULL,
    session_date DATE NOT NULL,
    topic VARCHAR(255),
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_attendance_sessions_section ON attendance_sessions (course_section_id, session_date);

CREATE TABLE IF NOT EXISTS attendance_records (
    session_id UUID NOT NULL REFERENCES attendance_sessions (id) ON DELETE CASCADE,
    student_id UUID NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PRESENT',
    note VARCHAR(255),
    PRIMARY KEY (session_id, student_id)
);
