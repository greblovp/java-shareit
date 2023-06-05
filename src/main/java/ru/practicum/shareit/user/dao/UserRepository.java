package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    Collection<User> getUsers();

    Optional<User> getUserById(long id);

    User createUser(User user);

    Optional<User> patchUser(long userId, User user);

    void removeUser(long userId);
}
