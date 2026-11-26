CREATE TABLE IF NOT EXISTS course_sections (
    id UUID PRIMARY KEY,
    semester_id UUID NOT NULL REFERENCES semesters (id),
    subject_id UUID NOT NULL REFERENCES subjects (id),
    code VARCHAR(64) NOT NULL UNIQUE,
    max_capacity INT NOT NULL,
    enrolled_count INT NOT NULL DEFAULT 0,
    room VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_course_sections_semester ON course_sections (semester_id);
CREATE INDEX IF NOT EXISTS idx_course_sections_subject ON course_sections (subject_id);

CREATE TABLE IF NOT EXISTS course_section_schedules (
    id UUID PRIMARY KEY,
    course_section_id UUID NOT NULL REFERENCES course_sections (id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL,
    start_period SMALLINT NOT NULL,
    end_period SMALLINT NOT NULL,
    room VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_schedules_section ON course_section_schedules (course_section_id);

CREATE TABLE IF NOT EXISTS course_section_lecturers (
    course_section_id UUID NOT NULL REFERENCES course_sections (id) ON DELETE CASCADE,
    lecturer_id UUID NOT NULL,
    PRIMARY KEY (course_section_id, lecturer_id)
);

CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY,
    course_section_id UUID NOT NULL REFERENCES course_sections (id),
    student_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENROLLED',
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    dropped_at TIMESTAMPTZ,
    UNIQUE (course_section_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_student ON enrollments (student_id, status);
