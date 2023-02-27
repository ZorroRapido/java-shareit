package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryInMemoryImpl implements UserRepository {
    private final Map<Long, User> users;
    private static long counter = 0;

    public UserRepositoryInMemoryImpl() {
        this.users = new HashMap<>();
    }

    @Override
    public User save(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public User update(User user, Long userId) {
        User oldUser = users.get(userId);

        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }

        return oldUser;
    }

    @Override
    public User get(Long userId) {
        return users.get(userId);
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void delete(Long userId) {
        users.remove(userId);
    }

    @Override
    public List<String> getEmailsList() {
        return users.values().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getIdsList() {
        return new ArrayList<>(users.keySet());
    }

    private long getNextId() {
        return ++counter;
    }
}
