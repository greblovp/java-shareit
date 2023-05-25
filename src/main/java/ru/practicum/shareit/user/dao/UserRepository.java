package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    public Collection<User> getUsers();

    public Optional<User> getUserById(long id);

    public User createUser(User user);

    public Optional<User> patchUser(long userId, User user);

    public void removeUser(long userId);
}
