CREATE TABLE IF NOT EXISTS chapters (
    id UUID PRIMARY KEY,
    course_section_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chapters_section ON chapters (course_section_id, sort_order);

CREATE TABLE IF NOT EXISTS lessons (
    id UUID PRIMARY KEY,
    chapter_id UUID NOT NULL REFERENCES chapters (id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    video_url VARCHAR(512),
    video_file_id VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_lessons_chapter ON lessons (chapter_id, sort_order);

CREATE TABLE IF NOT EXISTS lesson_attachments (
    id UUID PRIMARY KEY,
    lesson_id UUID NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    file_id VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    file_size BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS lesson_progress (
    lesson_id UUID NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    student_id UUID NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (lesson_id, student_id)
);

CREATE TABLE IF NOT EXISTS assignments (
    id UUID PRIMARY KEY,
    course_section_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    attachment_file_id VARCHAR(255),
    due_at TIMESTAMPTZ NOT NULL,
    max_score NUMERIC(6, 2) NOT NULL DEFAULT 10.00,
    allow_late BOOLEAN NOT NULL DEFAULT FALSE,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_assignments_section ON assignments (course_section_id);

CREATE TABLE IF NOT EXISTS submissions (
    id UUID PRIMARY KEY,
    assignment_id UUID NOT NULL REFERENCES assignments (id) ON DELETE CASCADE,
    student_id UUID NOT NULL,
    file_id VARCHAR(255),
    text_content TEXT,
    version INT NOT NULL DEFAULT 1,
    is_late BOOLEAN NOT NULL DEFAULT FALSE,
    score NUMERIC(6, 2),
    feedback TEXT,
    graded_by UUID,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    graded_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_submissions_assignment_student ON submissions (assignment_id, student_id, version DESC);
