package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UserTest {
    private User user1, user2;

    @BeforeEach
    public void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("Alice");
        user1.setEmail("alice@example.com");

        user2 = new User();
        user2.setName("Bob");
        user2.setEmail("bob@example.com");
    }

    @Test
    public void testGettersAndSetters() {
        assertEquals("Alice", user1.getName());
        assertEquals("alice@example.com", user1.getEmail());

        user2.setName("Charlie");
        user2.setEmail("charlie@example.com");

        assertEquals("Charlie", user2.getName());
        assertEquals("charlie@example.com", user2.getEmail());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertNotEquals(user1, user2);
        assertNotEquals(user1.hashCode(), user2.hashCode());

        User user3 = new User();
        user3.setId(1L);
        user3.setName("Alice");
        user3.setEmail("alice@example.com");

        assertEquals(user1, user3);
        assertEquals(user1.hashCode(), user3.hashCode());

        User user4 = new User();
        user4.setId(2L);
        user4.setName("Alice");
        user4.setEmail("alice@example.com");

        assertNotEquals(user1, user4);
        assertEquals(user1.hashCode(), user4.hashCode());
    }
}