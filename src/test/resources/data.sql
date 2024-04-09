
INSERT INTO roles (id, name) SELECT 'b1f1ce4f-f294-4994-befc-0673b8771e77', 'ROLE_USER' WHERE NOT EXISTS (SELECT * FROM roles WHERE id='b1f1ce4f-f294-4994-befc-0673b8771e77');
INSERT INTO roles (id, name) SELECT '3dd44c43-ff43-4891-8a14-c36a8fe72347', 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT * FROM roles WHERE id='3dd44c43-ff43-4891-8a14-c36a8fe72347');

INSERT INTO users (id, username, password) SELECT '5c553179-71b1-4c85-842e-b6ff67dc8e61', 'test_user', 'password' WHERE NOT EXISTS (SELECT * FROM users WHERE id='5c553179-71b1-4c85-842e-b6ff67dc8e61');
INSERT INTO users (id, username, password) SELECT 'bfd6ada0-2b46-4971-a314-d5abd7b7ebb1', 'test_admin', 'password' WHERE NOT EXISTS (SELECT * FROM users WHERE id='bfd6ada0-2b46-4971-a314-d5abd7b7ebb1');
INSERT INTO users (id, username, password) SELECT 'fd210c53-8e93-46c6-81b4-4c3188568619', 'test_user2', 'password' WHERE NOT EXISTS (SELECT * FROM users WHERE id='fd210c53-8e93-46c6-81b4-4c3188568619');

-- give test_user the USER role
INSERT INTO user_roles (user_id, role_id) VALUES ('5c553179-71b1-4c85-842e-b6ff67dc8e61', 'b1f1ce4f-f294-4994-befc-0673b8771e77') ON CONFLICT (user_id, role_id) DO NOTHING;
-- give test_admin the USER and ADMIN role
INSERT INTO user_roles (user_id, role_id) VALUES ('bfd6ada0-2b46-4971-a314-d5abd7b7ebb1', 'b1f1ce4f-f294-4994-befc-0673b8771e77') ON CONFLICT (user_id, role_id) DO NOTHING;
INSERT INTO user_roles (user_id, role_id) VALUES ('bfd6ada0-2b46-4971-a314-d5abd7b7ebb1', '3dd44c43-ff43-4891-8a14-c36a8fe72347') ON CONFLICT (user_id, role_id) DO NOTHING;
-- give test_user2 the USER role
INSERT INTO user_roles (user_id, role_id) VALUES ('fd210c53-8e93-46c6-81b4-4c3188568619', 'b1f1ce4f-f294-4994-befc-0673b8771e77') ON CONFLICT (user_id, role_id) DO NOTHING;

-- create a note for the test user
INSERT INTO notes (id, user_id, title, content) SELECT '6608cd99-e2f7-4dca-b4dc-9a40a21a25b9', '5c553179-71b1-4c85-842e-b6ff67dc8e61', 'title', 'content' WHERE NOT EXISTS (SELECT * FROM notes WHERE id='6608cd99-e2f7-4dca-b4dc-9a40a21a25b9');
INSERT INTO notes (id, user_id, title, content) SELECT '6c8ea691-2573-4274-9ac8-f09f0dcaa13a', 'bfd6ada0-2b46-4971-a314-d5abd7b7ebb1', 'admin title', 'admin content' WHERE NOT EXISTS (SELECT * FROM notes WHERE id='6c8ea691-2573-4274-9ac8-f09f0dcaa13a');