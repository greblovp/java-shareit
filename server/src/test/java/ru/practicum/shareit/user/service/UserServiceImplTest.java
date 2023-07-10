package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@TestPropertySource(properties = {"db.name=test"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DataJpaTest
class UserServiceImplTest {

    private final TestEntityManager em;
    private final UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    public void testGetUsers() {
        // given
        List<UserDto> sourceUserDtos = List.of(
                makeUserDto("ivan@email", "Ivan"),
                makeUserDto("petr@email", "Petr"),
                makeUserDto("vasilii@email", "Vasilii")
        );

        for (UserDto userDto : sourceUserDtos) {
            User entity = UserMapper.toUser(userDto);
            em.persist(entity);
        }
        em.flush();

        // when
        Collection<UserDto> targetUserDtos = userService.getUsers();

        // then
        assertThat(targetUserDtos, hasSize(sourceUserDtos.size()));
        for (UserDto sourceUserDto : sourceUserDtos) {
            assertThat(targetUserDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUserDto.getName())),
                    hasProperty("email", equalTo(sourceUserDto.getEmail()))
            )));
        }
    }

    @Test
    public void testGetUsers_whenCollectionIsEmpty() {
        // given

        // when
        Collection<UserDto> targetUserDtos = userService.getUsers();

        // then
        assertThat(targetUserDtos, hasSize(0));
    }

    @Test
    public void testFindById() {
        // given
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        User entity = UserMapper.toUser(sourceUserDto);
        em.persist(entity);
        em.flush();

        // when
        UserDto targetUserDto = userService.getUserById(entity.getId());

        // then
        assertThat(targetUserDto, allOf(
                hasProperty("id", equalTo(entity.getId())),
                hasProperty("name", equalTo(sourceUserDto.getName())),
                hasProperty("email", equalTo(sourceUserDto.getEmail()))
        ));
    }

    @Test
    public void testFindBy_whenIdNotFound() {
        // given
        Long userId = 3L;

        // when & then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Пользователь с ID = " + userId + " не найден.");
    }


    @Test
    void testCreateUser() {
        // given
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");

        // when
        userService.createUser(sourceUserDto);

        // then
        TypedQuery<User> query = em.getEntityManager().createQuery("Select u from User u where u.email = :email",
                User.class);
        User targetUser = query.setParameter("email", sourceUserDto.getEmail())
                .getSingleResult();

        assertThat(targetUser.getId(), notNullValue());
        assertThat(targetUser.getName(), equalTo(sourceUserDto.getName()));
        assertThat(targetUser.getEmail(), equalTo(sourceUserDto.getEmail()));
    }

    @Test
    void testCreateUser_whenEmailAlreadyExists() {
        // given
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        User entity = UserMapper.toUser(sourceUserDto);
        em.persist(entity);
        em.flush();

        // when & then
        assertThatThrownBy(() -> userService.createUser(sourceUserDto))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    public void testUpdateUser() {
        // given
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        User entity = UserMapper.toUser(sourceUserDto);
        em.persist(entity);
        em.flush();
        Long userId = entity.getId();

        UserDto userDtoToUpdate = makeUserDto("petr@email", "Petr");

        // when
        UserDto updatedUserDto = userService.patchUser(userId, userDtoToUpdate);

        // then
        assertThat(updatedUserDto.getName(), equalTo(userDtoToUpdate.getName()));
        assertThat(updatedUserDto.getEmail(), equalTo(userDtoToUpdate.getEmail()));
        assertThat(updatedUserDto.getId(), equalTo(userId));
    }

    @Test
    public void testUpdateUser_whenEmailIsNull() {
        // given
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        User entity = UserMapper.toUser(sourceUserDto);
        em.persist(entity);
        em.flush();
        Long userId = entity.getId();

        UserDto userDtoToUpdate = makeUserDto(null, "Petr");

        // when
        UserDto updatedUserDto = userService.patchUser(userId, userDtoToUpdate);

        // then
        assertThat(updatedUserDto.getName(), equalTo(userDtoToUpdate.getName()));
        assertThat(updatedUserDto.getEmail(), equalTo(sourceUserDto.getEmail()));
        assertThat(updatedUserDto.getId(), equalTo(userId));
    }

    @Test
    public void testUpdateUser_whenNameIsNull() {
        // given
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        User entity = UserMapper.toUser(sourceUserDto);
        em.persist(entity);
        em.flush();
        Long userId = entity.getId();

        UserDto userDtoToUpdate = makeUserDto("petr@email", null);

        // when
        UserDto updatedUserDto = userService.patchUser(userId, userDtoToUpdate);

        // then
        assertThat(updatedUserDto.getName(), equalTo(sourceUserDto.getName()));
        assertThat(updatedUserDto.getEmail(), equalTo(userDtoToUpdate.getEmail()));
        assertThat(updatedUserDto.getId(), equalTo(userId));
    }

    @Test
    void testUpdateUser_whenUserNotFound() {
        // given
        Long userId = 3L;
        UserDto userDtoToUpdate = makeUserDto("petr@email", "Petr");

        // when & then
        assertThatThrownBy(() -> userService.patchUser(userId, userDtoToUpdate))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Пользователь с ID = " + userId + " не найден.");
    }


    @Test
    void removeUser_whenUserExists() {
        // given
        UserDto sourceUserDto = makeUserDto("ivan@email", "Ivan");
        User entity = UserMapper.toUser(sourceUserDto);
        em.persist(entity);
        em.flush();
        Long userId = entity.getId();

        // when
        userService.removeUser(userId);

        // then
        User targetUser = em.find(User.class, userId);
        assertThat(targetUser, nullValue());
    }

    @Test
    void removeUser_whenUserNotFound() {
        //given
        Long userId = 3L;

        // when & then
        assertThatThrownBy(() -> userService.removeUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Пользователь с ID = " + userId + " не найден.");
    }

    private UserDto makeUserDto(String email, String name) {
        return UserDto.builder()
                .email(email)
                .name(name)
                .build();
    }

}