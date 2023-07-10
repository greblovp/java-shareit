package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtendedDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.exception.CommentNotAvailableException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongItemOwnerException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.*;

@Transactional
@TestPropertySource(properties = {"db.name=test"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DataJpaTest
class ItemServiceImplTest {

    private final TestEntityManager em;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    @MockBean
    private UserService userService;
    private final CommentRepository commentRepository;

    private ItemServiceImpl itemService;

    @BeforeEach
    public void setUp() {
        itemService = new ItemServiceImpl(itemRepository, bookingRepository, userService, commentRepository);
    }


    @Test
    void testCreateItem() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        User entity = UserMapper.toUser(sourceUserDto);
        em.persist(entity);
        em.flush();
        Long userId = entity.getId();
        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when
        ItemDto targetItemDto = itemService.createItem(userId, sourceItemDto);

        // then
        assertThat(targetItemDto.getId(), notNullValue());
        assertThat(targetItemDto.getName(), equalTo(sourceItemDto.getName()));
        assertThat(targetItemDto.getDescription(), equalTo(sourceItemDto.getDescription()));
        assertThat(targetItemDto.getAvailable(), equalTo(sourceItemDto.getAvailable()));

        Item persistedItem = em.find(Item.class, targetItemDto.getId());
        assertThat(persistedItem, notNullValue());
        assertThat(persistedItem.getName(), equalTo(sourceItemDto.getName()));
        assertThat(persistedItem.getDescription(), equalTo(sourceItemDto.getDescription()));
        assertThat(persistedItem.getIsAvailable(), equalTo(sourceItemDto.getAvailable()));

        verify(userService).getUserById(userId);
    }


    @Test
    public void testCreateItem_whenUserIdNotFound() {
        // given
        Long userId = 1L;
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("Пользователь с ID = " + userId + " не найден."));

        //when & then
        assertThatThrownBy(() -> itemService.createItem(userId, sourceItemDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Пользователь с ID = " + userId + " не найден.");

        verify(userService).getUserById(userId);
    }

    @Test
    void testPatchItem() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        ItemDto updatedItemDto = makeItemDto("item2", "description2", false);

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when
        ItemDto targetItemDto = itemService.patchItem(userId, itemId, updatedItemDto);

        // then
        assertThat(targetItemDto.getId(), notNullValue());
        assertThat(targetItemDto.getName(), equalTo(updatedItemDto.getName()));
        assertThat(targetItemDto.getDescription(), equalTo(updatedItemDto.getDescription()));
        assertThat(targetItemDto.getAvailable(), equalTo(updatedItemDto.getAvailable()));

        Item persistedItem = em.find(Item.class, targetItemDto.getId());
        assertThat(persistedItem, notNullValue());
        assertThat(persistedItem.getName(), equalTo(updatedItemDto.getName()));
        assertThat(persistedItem.getDescription(), equalTo(updatedItemDto.getDescription()));
        assertThat(persistedItem.getIsAvailable(), equalTo(updatedItemDto.getAvailable()));

        verify(userService).getUserById(userId);
    }


    @Test
    public void testPatchItem_whenUserIdNotFound() {
        // given
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto updatedItemDto = makeItemDto("item2", "description2", false);
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("Пользователь с ID = " + userId + " не найден."));

        // when & then
        assertThatThrownBy(() -> itemService.patchItem(userId, itemId, updatedItemDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Пользователь с ID = " + userId + " не найден.");

        verify(userService).getUserById(userId);
    }

    @Test
    public void testPatchItem_whenItemIdNotFound() {
        // given
        Long userId = 1L;
        Long itemId = 1L;
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        ItemDto updatedItemDto = makeItemDto("item2", "description2", false);
        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when & then
        assertThatThrownBy(() -> itemService.patchItem(userId, itemId, updatedItemDto))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessage("Вещь с ID = " + itemId + " не найдена.");

        verify(userService).getUserById(userId);
    }

    @Test
    public void testPatchItem_whenInvalidOwner() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        ItemDto updatedItemDto = makeItemDto("item2", "description2", false);

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        when(userService.getUserById(userId + 1L)).thenReturn(sourceUserDto);

        // when & then
        assertThatThrownBy(() -> itemService.patchItem(userId + 1L, itemId, updatedItemDto))
                .isInstanceOf(WrongItemOwnerException.class)
                .hasMessage("У вещи с ID = " + itemId + " другой владелец.");

        verify(userService).getUserById(userId + 1L);
    }


    @Test
    public void testGetItems() {
        // given
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        List<ItemDto> sourceItemDtos = List.of(
                makeItemDto("item1", "description", true),
                makeItemDto("item2", "description2", false)
        );


        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        for (ItemDto itemDto : sourceItemDtos) {
            Item entity = ItemMapper.toItem(itemDto);
            entity.setOwnerId(userId);
            em.persist(entity);
        }
        em.flush();

        // when
        Collection<ItemExtendedDto> targetItemDtos = itemService.getItems(userId, 0, 10);

        // then
        assertThat(targetItemDtos, hasSize(sourceItemDtos.size()));
        for (ItemDto sourceItemDto : sourceItemDtos) {
            assertThat(targetItemDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItemDto.getName())),
                    hasProperty("description", equalTo(sourceItemDto.getDescription())),
                    hasProperty("available", equalTo(sourceItemDto.getAvailable()))
            )));
        }
    }

    @Test
    public void testGetItemById() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        // when
        ItemExtendedDto targetItemDto = itemService.getItemById(userId, itemId);

        // then
        assertThat(targetItemDto.getId(), notNullValue());
        assertThat(targetItemDto.getName(), equalTo(sourceItemDto.getName()));
        assertThat(targetItemDto.getDescription(), equalTo(sourceItemDto.getDescription()));
        assertThat(targetItemDto.getAvailable(), equalTo(sourceItemDto.getAvailable()));
    }


    @Test
    public void testSearchItem() {
        // given
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        List<ItemDto> sourceItemDtos = List.of(
                makeItemDto("item1", "description", true),
                makeItemDto("item2", "description2", true)
        );


        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        for (ItemDto itemDto : sourceItemDtos) {
            Item entity = ItemMapper.toItem(itemDto);
            entity.setOwnerId(userId);
            em.persist(entity);
        }
        em.flush();

        // when
        Collection<ItemDto> targetItemDtos = itemService.searchItem("item", 0, 10);

        // then
        assertThat(targetItemDtos, hasSize(sourceItemDtos.size()));
        for (ItemDto sourceItemDto : sourceItemDtos) {
            assertThat(targetItemDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItemDto.getName())),
                    hasProperty("description", equalTo(sourceItemDto.getDescription())),
                    hasProperty("available", equalTo(sourceItemDto.getAvailable()))
            )));
        }
    }

    @Test
    public void testSearchItem_whenEmptyText() {
        // given
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        List<ItemDto> sourceItemDtos = List.of(
                makeItemDto("item1", "description", true),
                makeItemDto("item2", "description2", false)
        );


        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        for (ItemDto itemDto : sourceItemDtos) {
            Item entity = ItemMapper.toItem(itemDto);
            entity.setOwnerId(userId);
            em.persist(entity);
        }
        em.flush();

        // when
        Collection<ItemDto> result = itemService.searchItem("", 0, 10);

        // then
        assertThat(result, hasSize(0));
    }

    @Test
    void testAddComment() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        Booking bookingEntity = new Booking();
        bookingEntity.setItem(itemEntity);
        bookingEntity.setBooker(userEntity);
        bookingEntity.setStartDate(LocalDateTime.now().minusDays(5));
        bookingEntity.setEndDate(LocalDateTime.now().minusDays(1));
        bookingEntity.setStatus(BookingStatus.APPROVED);
        em.persist(bookingEntity);
        em.flush();

        CommentDto sourceCommentDto = CommentDto.builder().text("Great item!").build();

        sourceUserDto.setId(userId);
        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when
        CommentDto targetCommentDto = itemService.addComment(userId, itemId, sourceCommentDto);

        // then
        assertThat(targetCommentDto.getId(), notNullValue());
        assertThat(targetCommentDto.getText(), equalTo(sourceCommentDto.getText()));
        assertThat(targetCommentDto.getAuthorName(), equalTo(sourceUserDto.getName()));
        assertThat(targetCommentDto.getCreated(), notNullValue());

        Comment persistedComment = em.find(Comment.class, targetCommentDto.getId());
        assertThat(persistedComment, notNullValue());
        assertThat(persistedComment.getText(), equalTo(sourceCommentDto.getText()));

        verify(userService).getUserById(userId);
    }

    @Test
    void addComment_shouldThrowCommentNotAvailableException_whenNoBookingsExistForUserAndItem() {
        // given
        ItemDto sourceItemDto = makeItemDto("item1", "description", true);
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        Item itemEntity = ItemMapper.toItem(sourceItemDto);
        itemEntity.setOwnerId(userId);
        em.persist(itemEntity);
        em.flush();
        Long itemId = itemEntity.getId();

        CommentDto sourceCommentDto = CommentDto.builder().text("Great item!").build();

        sourceUserDto.setId(userId);
        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when & then
        assertThatThrownBy(() -> itemService.addComment(userId, itemId, sourceCommentDto))
                .isInstanceOf(CommentNotAvailableException.class)
                .hasMessage("У пользователя с ID = " + userId + " нет завершенных бронирований вещи с ID = " + itemId);

        verify(userService).getUserById(userId);
    }

    private ItemDto makeItemDto(String name, String description, boolean available) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(available)
                .build();
    }

    private UserDto makeUserDto(String email, String name) {
        return UserDto.builder()
                .email(email)
                .name(name)
                .build();
    }
}