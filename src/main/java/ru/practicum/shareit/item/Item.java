package ru.practicum.shareit.item;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "items", schema = "public")
@Getter @Setter @ToString
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
    @Column(name = "user_id")
    private Long ownerId;
}
