package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @SneakyThrows
    @Test
    void testGetUsers() {
        UserDto userToCreate = UserDto.builder()
                .email("test@test.test")
                .name("name")
                .build();
        when(userService.getUsers()).thenReturn(List.of(userToCreate));

        String response = mockMvc.perform(get("/users")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).getUsers();
        assertEquals(objectMapper.writeValueAsString(List.of(userToCreate)), response);
    }

    @SneakyThrows
    @Test
    void testCreateValidUser() {
        UserDto userToCreate = UserDto.builder()
                .email("test@test.test")
                .name("name")
                .build();
        when(userService.createUser(userToCreate)).thenReturn(userToCreate);

        String response = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).createUser(userToCreate);
        assertEquals(objectMapper.writeValueAsString(userToCreate), response);
    }

    @SneakyThrows
    @Test
    void testCreateInValidUser() {
        UserDto userToCreate = UserDto.builder().email("badEmail").build();
        when(userService.createUser(userToCreate)).thenReturn(userToCreate);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @SneakyThrows
    @Test
    void testUpdateValidUser() {
        Long userId = 1L;

        UserDto userToUpdate = UserDto.builder()
                .email("test@test.test")
                .name("name")
                .build();
        when(userService.patchUser(userId, userToUpdate)).thenReturn(userToUpdate);

        String response = mockMvc.perform(patch("/users/" + userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).patchUser(userId, userToUpdate);
        assertEquals(objectMapper.writeValueAsString(userToUpdate), response);
    }

    @SneakyThrows
    @Test
    void testUpdateInValidUser() {
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
    @SneakyThrows
    public void testFindById() {
        Long userId = 1L;
        UserDto user = UserDto.builder().build();
        user.setId(userId);

        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    @SneakyThrows
    public void testFindByIdNotFound() {
        Long userId = 1L;

        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(String.format("Пользователь с ID = %d не найден.", userId)));

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void testRemoveUser() {
        Long userId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/" + userId))
                .andExpect(status().isOk());
        verify(userService, times(1)).removeUser(userId);
    }

}