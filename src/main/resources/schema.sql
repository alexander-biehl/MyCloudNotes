
-- CREATE USER notes_user;

-- GRANT ALL PRIVILEGES ON DATABASE mycloudnotes TO notes_user;

CREATE TABLE IF NOT EXISTS notes (
    id uuid DEFAULT gen_random_uuid(),
    title varchar(32) NOT NULL,
    content varchar(255),
    PRIMARY KEY (id)
);