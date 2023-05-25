package ru.practicum.shareit.user.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import java.util.*;

@Repository("inMemoryUserRepository")
@Slf4j
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private long idCounter;

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public Optional<User> getUserById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User createUser(User user) throws DataIntegrityViolationException {
        checkDuplicateUserByEmail(user);

        user.setId(++idCounter);
        emails.add(user.getEmail());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);

        return user;
    }

    @Override
    public Optional<User> patchUser(long userId, User user) {
        if (!users.containsKey(userId)) {
            return Optional.empty();
        }

        User userToUpdate = users.get(userId);
        String newName = user.getName();
        String newEmail = user.getEmail();

        if (newEmail != null && !newEmail.equals(userToUpdate.getEmail())) {
            checkDuplicateUserByEmail(user);
            emails.remove(userToUpdate.getEmail());
            userToUpdate.setEmail(newEmail);
            emails.add(newEmail);
        }


        if (newName != null) {
            userToUpdate.setName(newName);
        }


        log.info("Обновлен пользователь: {}", userToUpdate);

        return Optional.of(userToUpdate);
    }

    @Override
    public void removeUser(long userId) {
        emails.remove(users.get(userId).getEmail());
        users.remove(userId);
        log.info("Удален пользователь с Id: {}", userId);
    }

    private void checkDuplicateUserByEmail(User user) {
        if (emails.contains(user.getEmail())) {
            throw new DataIntegrityViolationException("Пользователь с email " +  user.getEmail() + " уже существует");
        }
    }
}
