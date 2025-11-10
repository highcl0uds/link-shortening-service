package main.java.core.models;

import java.util.UUID;

public class User {
    private final UUID userUUID;
    private final String userUUIDStr;

    public User() {
        this.userUUID = UUID.randomUUID();
        this.userUUIDStr = this.userUUID.toString();
    }

    public UUID getUserUUID() {
        return userUUID;
    }

    public String getUserUUIDStr() {
        return userUUIDStr;
    }
}
