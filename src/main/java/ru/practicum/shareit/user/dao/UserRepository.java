package ru.practicum.shareit.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
}
