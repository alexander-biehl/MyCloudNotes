package com.alexbiehl.mycloudnotes.utils;

import java.util.UUID;

/**
 * Handy constants to refer to in tests when dealing with the database
 */
public class TestConstants {

    public static final String USER_ROLE_NAME = "ROLE_USER";
    public static final UUID USER_ROLE_ID = UUID.fromString("b1f1ce4f-f294-4994-befc-0673b8771e77");

    public static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";
    public static final UUID ADMIN_ROLE_ID = UUID.fromString("3dd44c43-ff43-4891-8a14-c36a8fe72347");

    public static final UUID TEST_USER_ID = UUID.fromString("5c553179-71b1-4c85-842e-b6ff67dc8e61");
    public static final UUID TEST_ADMIN_ID = UUID.fromString("bfd6ada0-2b46-4971-a314-d5abd7b7ebb1");
    public static final UUID TEST_USER2_ID = UUID.fromString("fd210c53-8e93-46c6-81b4-4c3188568619");

    public static final UUID TEST_NOTE_ID = UUID.fromString("6608cd99-e2f7-4dca-b4dc-9a40a21a25b9");

    public static final String PLAIN_TEXT_PASSWORD = "password";
    public static final String ENCRYPTED_PASSWORD = "$2y$11$t2/h9Y3rtugyl0tB56MaFOhCVxZnrY3sa.JRrDsmqAa8lZ2qCsl9i";

    public static final Long REFRESH_TOKEN_EXPIRY_MS = 60000L;
}
