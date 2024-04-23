package com.alexbiehl.mycloudnotes.api;

public class API {

    /** Entity Paths */
    public static final String NOTES = "/notes";
    public static final String ADMIN = "/admin";
    public static final String USERS = "/users";

    /** Sub-Entity Paths */
    public static final String BY_ID = "/{id}";

    /** User Paths */
    public static final String REGISTER_USER = "/register";
    public static final String LOGIN_USER = "/login";
    public static final String LOGOUT_USER = "/logout";

    /** Utility Paths */
    public static final String HEALTH_CHECK = "/health-check";
}
