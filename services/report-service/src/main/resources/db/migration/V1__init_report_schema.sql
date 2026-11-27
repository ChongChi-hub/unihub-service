CREATE TABLE IF NOT EXISTS student_academic_summaries (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    semester_id UUID NOT NULL,
    course_section_id UUID NOT NULL,
    attendance_rate NUMERIC(5, 2) NOT NULL DEFAULT 100.00,
    current_score NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    progress_percent NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, course_section_id)
);

CREATE INDEX IF NOT EXISTS idx_summaries_section ON student_academic_summaries (course_section_id);
CREATE INDEX IF NOT EXISTS idx_summaries_student ON student_academic_summaries (student_id, semester_id);

CREATE TABLE IF NOT EXISTS alert_rules (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    max_absences INT NOT NULL DEFAULT 3,
    min_score NUMERIC(5, 2) NOT NULL DEFAULT 4.00,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS academic_alerts (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL,
    course_section_id UUID NOT NULL,
    rule_id UUID NOT NULL REFERENCES alert_rules (id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_alerts_student ON academic_alerts (student_id, resolved);
