package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetUsers() throws Exception {
        UserDto userToCreate = UserDto.builder()
                .email("test@test.test")
                .name("name")
                .build();
        when(userService.getUsers()).thenReturn(List.of(userToCreate));

        mockMvc.perform(get("/users")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value("name"))
                .andExpect(jsonPath("$.[0].email").value("test@test.test"));
    }

    @Test
    void testCreateValidUser() throws Exception {
        UserDto userToCreate = UserDto.builder()
                .email("test@test.test")
                .name("name")
                .build();
        when(userService.createUser(userToCreate)).thenReturn(userToCreate);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.email").value("test@test.test"));
    }

    @Test
    void testCreateInValidUser() throws Exception {
        UserDto userToCreate = UserDto.builder().email("badEmail").build();
        when(userService.createUser(userToCreate)).thenReturn(userToCreate);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    public void testCreate_whenUserAlreadyExists() throws Exception {
        UserDto userToCreate = UserDto.builder()
                .email("test@test.test")
                .name("name")
                .build();
        when(userService.createUser(userToCreate)).thenThrow(new EmailAlreadyExistsException("error"));

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateValidUser() throws Exception {
        Long userId = 1L;

        UserDto userToUpdate = UserDto.builder()
                .email("test@test.test")
                .name("name")
                .build();
        when(userService.patchUser(userId, userToUpdate)).thenReturn(userToUpdate);

        mockMvc.perform(patch("/users/" + userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.email").value("test@test.test"));
    }

    @Test
    void testUpdateInValidUser() throws Exception {
        Long userId = 1L;
        UserDto userToUpdate = UserDto.builder().email("badEmail").build();
        when(userService.patchUser(userId, userToUpdate)).thenReturn(userToUpdate);

        mockMvc.perform(patch("/users/" + userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).patchUser(userId, userToUpdate);
    }

    @Test
    public void testFindById() throws Exception {
        Long userId = 1L;
        UserDto user = UserDto.builder().build();
        user.setId(userId);

        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    public void testFindByIdNotFound() throws Exception {
        Long userId = 1L;

        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(String.format("Пользователь с ID = %d не найден.", userId)));

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRemoveUser() throws Exception {
        Long userId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/" + userId))
                .andExpect(status().isOk());
        verify(userService, times(1)).removeUser(userId);
    }

}