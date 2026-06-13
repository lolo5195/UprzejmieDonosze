package com.projekt.uprzejmiedonosze.model;

public enum Role {
    ADMIN,
    USER,
    // GUEST reprezentuje konto demonstracyjne; goście anonimowi korzystają z permitAll w SecurityConfig.
    GUEST
}