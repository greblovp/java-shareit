package ru.practicum.shareit.item;

import lombok.Builder;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean isAvailable;
    private Long ownerId;
}
