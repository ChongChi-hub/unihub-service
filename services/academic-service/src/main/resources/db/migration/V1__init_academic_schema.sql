CREATE TABLE IF NOT EXISTS faculties (
    id UUID PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS majors (
    id UUID PRIMARY KEY,
    faculty_id UUID NOT NULL REFERENCES faculties (id),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_majors_faculty_id ON majors (faculty_id);

CREATE TABLE IF NOT EXISTS admin_classes (
    id UUID PRIMARY KEY,
    major_id UUID NOT NULL REFERENCES majors (id),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    cohort_year INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_admin_classes_major_id ON admin_classes (major_id);

CREATE TABLE IF NOT EXISTS admin_class_students (
    admin_class_id UUID NOT NULL REFERENCES admin_classes (id) ON DELETE CASCADE,
    student_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (admin_class_id, student_id)
);

CREATE TABLE IF NOT EXISTS semesters (
    id UUID PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    registration_start TIMESTAMPTZ,
    registration_end TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS subjects (
    id UUID PRIMARY KEY,
    faculty_id UUID NOT NULL REFERENCES faculties (id),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    credits INT NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_subjects_faculty_id ON subjects (faculty_id);

CREATE TABLE IF NOT EXISTS subject_prerequisites (
    subject_id UUID NOT NULL REFERENCES subjects (id) ON DELETE CASCADE,
    prerequisite_subject_id UUID NOT NULL REFERENCES subjects (id) ON DELETE CASCADE,
    PRIMARY KEY (subject_id, prerequisite_subject_id)
);
