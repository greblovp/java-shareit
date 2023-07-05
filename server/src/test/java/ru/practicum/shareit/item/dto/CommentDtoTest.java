package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentDtoTest {

//    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
//    private final Validator validator = validatorFactory.getValidator();

//    @Test
//    public void shouldFailValidationWhenTextIsNull() {
//        // Arrange
//        CommentDto commentDto = CommentDto.builder()
//                .id(1L)
//                .text(null)
//                .authorName("John Doe")
//                .created(LocalDateTime.now())
//                .build();
//
//        // Act
//        var violations = validator.validate(commentDto);
//
//        // Assert
//        assertEquals(2, validator.validate(commentDto).size());
//    }

//    @Test
//    public void shouldFailValidationWhenTextIsBlank() {
//        // Arrange
//        CommentDto commentDto = CommentDto.builder()
//                .id(1L)
//                .text("")
//                .authorName("John Doe")
//                .created(LocalDateTime.now())
//                .build();
//
//        // Act
//        var violations = validator.validate(commentDto);
//
//        // Assert
//        assertEquals(1, violations.size());
//        assertEquals("must not be blank", violations.iterator().next().getMessage());
//    }

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

//        assertEquals(0, validator.validate(comment).size());
    }




}