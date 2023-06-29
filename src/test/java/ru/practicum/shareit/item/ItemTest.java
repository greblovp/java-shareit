package ru.practicum.shareit.item;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ItemTest {

    @Test
    public void testEquals() {
        // create two items with the same id
        Item item1 = new Item();
        item1.setId(1L);
        Item item2 = new Item();
        item2.setId(1L);

        // asserting that they are equal
        assertEquals(item1, item2);
    }

    @Test
    public void testHashCode() {
        // create an item and calculate its hashcode
        Item item = new Item();
        item.setName("test");
        item.setOwnerId(1L);
        int expectedHashCode = 31 * "test".hashCode() + Long.valueOf(1L).hashCode();

        // asserting that the calculated hash code is equal to the expected one
        assertEquals(expectedHashCode, item.hashCode());
    }
}