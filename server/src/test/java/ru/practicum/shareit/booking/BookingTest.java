package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {
    @Test
    void testEqualsAndHashCode() {
        LocalDateTime endDate1 = LocalDateTime.of(2022, 1, 1, 10, 0);
        LocalDateTime endDate2 = LocalDateTime.of(2022, 1, 1, 12, 0);
        LocalDateTime startDate = LocalDateTime.of(2022, 1, 2, 12, 0);
        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(1L);
        item.setName("test");
        item.setOwnerId(1L);
        User booker = new User();
        booker.setId(1L);
        booker.setName("Alice");
        booker.setEmail("alice@example.com");

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setItem(item);
        booking1.setBooker(booker);
        booking1.setStartDate(startDate);
        booking1.setEndDate(endDate1);

        Booking booking2 = new Booking();
        booking2.setId(1L);  // Same ID as booking1
        booking2.setItem(item);
        booking2.setBooker(booker);
        booking2.setStartDate(startDate);
        booking2.setEndDate(endDate2);

        // Test equals()
        assertEquals(booking1, booking2);
        assertEquals(booking2, booking1);

        // Test hashCode()
        assertEquals(booking1.hashCode(), booking2.hashCode());
    }
}