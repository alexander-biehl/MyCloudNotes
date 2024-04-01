
-- CREATE USER notes_user;

-- GRANT ALL PRIVILEGES ON DATABASE mycloudnotes TO notes_user;

CREATE TABLE IF NOT EXISTS notes (
    id uuid DEFAULT gen_random_uuid(),
    title varchar(32) NOT NULL,
    content varchar(255),
    user_id uuid REFERENCES users (id),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users (
    id uuid DEFAULT gen_random_uuid(),
    username varchar(50) NOT NULL,
    password varchar(500) NOT NULL,
    active boolean DEFAULT 0 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS roles (
    id uuid DEFAULT gen_random_uuid(),
    name varchar(32) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id uuid REFERENCES users (id),
    role_id uuid REFERENCES roles (id),
    PRIMARY KEY (user_id, role_id)
);