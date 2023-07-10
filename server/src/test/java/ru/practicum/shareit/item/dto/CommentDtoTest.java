package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentDtoTest {

    @Test
    void builder_withValidValues_shouldCreateObject() {
        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("This is a comment.")
                .authorName("Alice")
                .created(LocalDateTime.now())
                .build();
        assertNotNull(comment.getId());
        assertNotNull(comment.getText());
        assertEquals("Alice", comment.getAuthorName());
        assertNotNull(comment.getCreated());
    }


}