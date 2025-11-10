package main.java.core.services;

import main.java.core.models.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsersService {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();
    private User currentUser;

    public Map<UUID, User> getUsers() {
        return users;
    }

    public User createUser() {
        User user;

        // Создаем пользователя с проверкой на коллизию
        do {
            user = new User();
        } while (users.containsKey(user.getUserUUID()));

        users.put(user.getUserUUID(), user);

        return user;
    }

    public User getUserByUUID(UUID uuid) {
        return users.get(uuid);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
