package ru.vsu.cs.sheina.online_gallery_backend.configuration;

public enum Role {
    USER("USER"),
    ADMIN("ADMIN");

    private final String name;

    Role(String name) {
        this.name = name;
    }
}
