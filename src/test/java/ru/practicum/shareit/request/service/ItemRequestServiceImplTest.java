package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
@TestPropertySource(properties = {"db.name=test"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DataJpaTest
class ItemRequestServiceImplTest {
    private final TestEntityManager em;
    @MockBean
    private UserService userService;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private ItemRequestServiceImpl itemRequestService;

    @BeforeEach
    public void setUp() {
        itemRequestService = new ItemRequestServiceImpl(userService, itemRequestRepository, itemRepository);
    }


    @Test
    public void testCreateItemRequest() {
        // given
        ItemRequestDto sourceItemRequestDto = ItemRequestDto.builder().description("description").build();
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        sourceUserDto.setId(userId);
        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when
        ItemRequestDto targetItemRequestDto = itemRequestService.createItemRequest(userId, sourceItemRequestDto);

        // then
        assertThat(targetItemRequestDto.getId(), notNullValue());
        assertThat(targetItemRequestDto.getDescription(), equalTo(sourceItemRequestDto.getDescription()));

        ItemRequest persistedItemRequest = em.find(ItemRequest.class, targetItemRequestDto.getId());
        assertThat(persistedItemRequest, notNullValue());
        assertThat(persistedItemRequest.getDescription(), equalTo(sourceItemRequestDto.getDescription()));
        assertThat(persistedItemRequest.getRequestor(), equalTo(userEntity));

        verify(userService).getUserById(userId);
    }

    @Test
    public void testGetItemRequestById() {
        // given
        ItemRequestDto sourceItemRequestDto = ItemRequestDto.builder().description("description").build();
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        ItemRequest itemRequestEntity = ItemRequestMapper.toItemRequest(sourceItemRequestDto,  userEntity);
        em.persist(itemRequestEntity);
        em.flush();
        Long itemRequestId = itemRequestEntity.getId();

        sourceUserDto.setId(userId);
        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when
        ItemRequestDto targetItemRequestDto = itemRequestService.getItemRequestById(userId, itemRequestId);

        // then
        assertThat(targetItemRequestDto.getId(), notNullValue());
        assertThat(targetItemRequestDto.getDescription(), equalTo(sourceItemRequestDto.getDescription()));
        verify(userService).getUserById(userId);
    }


    @Test
    public void testGetItemRequestById_whenItemRequestNotFound() {
        // given
        Long itemRequestId = 2L;

        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        sourceUserDto.setId(userId);
        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when & then
        assertThatThrownBy(() -> itemRequestService.getItemRequestById(userId, itemRequestId))
                .isInstanceOf(ItemRequestNotFoundException.class)
                .hasMessage("Запрос на  вещь с ID = " + itemRequestId + " не найден.");
        verify(userService).getUserById(userId);
    }

    @Test
    public void testGetItemRequests() {
        // given
        List<ItemRequestDto> sourceItemRequestDtos = List.of(
                ItemRequestDto.builder().description("description1").build(),
                ItemRequestDto.builder().description("description2").build()
        );
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        for (ItemRequestDto sourceItemRequestDto : sourceItemRequestDtos) {
            ItemRequest entity = ItemRequestMapper.toItemRequest(sourceItemRequestDto, userEntity);
            em.persist(entity);
        }
        em.flush();

        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when
        Collection<ItemRequestDto> targetItemRequestDtos = itemRequestService.getItemRequests(userId);

        // then
        assertThat(targetItemRequestDtos, hasSize(sourceItemRequestDtos.size()));
        for (ItemRequestDto sourceItemRequestDto : sourceItemRequestDtos) {
            assertThat(targetItemRequestDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceItemRequestDto.getDescription()))
            )));
        }
        verify(userService).getUserById(userId);
    }

    @Test
    public void testGetItemRequests_WhenUserExistsButHasNoItemRequests() {
        // given
        Long userId = 1L;
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");

        when(userService.getUserById(userId)).thenReturn(sourceUserDto);

        // when
        Collection<ItemRequestDto> itemRequests = itemRequestService.getItemRequests(1L);

        // then
        assertThat(itemRequests.size(), equalTo(0));
        verify(userService).getUserById(userId);
    }

    @Test
    public void testGetAllItemRequests() {
        // given
        List<ItemRequestDto> sourceItemRequestDtos = List.of(
                ItemRequestDto.builder().description("description1").build(),
                ItemRequestDto.builder().description("description2").build()
        );
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        Integer from = 0;
        Integer size = 10;

        User userEntity = UserMapper.toUser(sourceUserDto);
        em.persist(userEntity);
        em.flush();
        Long userId = userEntity.getId();

        for (ItemRequestDto sourceItemRequestDto : sourceItemRequestDtos) {
            ItemRequest entity = ItemRequestMapper.toItemRequest(sourceItemRequestDto, userEntity);
            em.persist(entity);
        }
        em.flush();

        when(userService.getUserById(userId + 1L)).thenReturn(sourceUserDto);

        // when
        Collection<ItemRequestDto> targetItemRequestDtos = itemRequestService.getAllItemRequests(userId + 1L, from, size);

        // then
        assertThat(targetItemRequestDtos, hasSize(sourceItemRequestDtos.size()));
        for (ItemRequestDto sourceItemRequestDto : sourceItemRequestDtos) {
            assertThat(targetItemRequestDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceItemRequestDto.getDescription()))
            )));
        }

        verify(userService).getUserById(userId + 1L);
    }

    private UserDto makeUserDto(String email, String name) {
        return UserDto.builder()
                .email(email)
                .name(name)
                .build();
    }
}