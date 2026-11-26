CREATE TABLE IF NOT EXISTS lesson_comments (
    id UUID PRIMARY KEY,
    lesson_id UUID NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    parent_id UUID REFERENCES lesson_comments (id) ON DELETE CASCADE,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    is_best_answer BOOLEAN NOT NULL DEFAULT FALSE,
    hidden BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_lesson_comments_lesson ON lesson_comments (lesson_id, created_at ASC);
