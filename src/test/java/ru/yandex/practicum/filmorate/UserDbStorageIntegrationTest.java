package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageIntegrationTest {

    private final UserDbStorage userDbStorage;

    @Test
    void testCreateFindByIdAndFindAll() {
        User userToCreate = User.builder()
                .email("a@b.c")
                .login("login")
                .name("Имя")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userDbStorage.create(userToCreate);
        assertThat(createdUser.getId()).isPositive();

        Optional<User> fetchedOpt = userDbStorage.findById(createdUser.getId());
        assertThat(fetchedOpt).isPresent();
        User fetchedUser = fetchedOpt.get();

        assertThat(fetchedUser)
                .extracting(User::getId, User::getEmail, User::getLogin, User::getName)
                .containsExactly(createdUser.getId(), "a@b.c", "login", "Имя");

        List<User> allUsers = userDbStorage.findAll();
        assertThat(allUsers).extracting(User::getId)
                .contains(createdUser.getId());
    }

    @Test
    void testUpdateUser() {
        User originalUser = User.builder()
                .email("x@y.z")
                .login("userLogin")
                .name("ПервоеИмя")
                .birthday(LocalDate.now())
                .build();
        User createdUser = userDbStorage.create(originalUser);

        createdUser.setName("ВтороеИмя");
        createdUser.setEmail("new@e.e");
        User updatedUser = userDbStorage.update(createdUser);

        User fetchedUser = userDbStorage.findById(updatedUser.getId())
                .orElseThrow();

        assertThat(fetchedUser.getName()).isEqualTo("ВтороеИмя");
        assertThat(fetchedUser.getEmail()).isEqualTo("new@e.e");
    }

    @Test
    void testFriendshipFlow() {
        User firstUser = userDbStorage.create(User.builder()
                .email("first@user.com")
                .login("firstUser")
                .name("First")
                .birthday(LocalDate.now())
                .build());

        User secondUser = userDbStorage.create(User.builder()
                .email("second@user.com")
                .login("secondUser")
                .name("Second")
                .birthday(LocalDate.now())
                .build());

        userDbStorage.addFriend(firstUser.getId(), secondUser.getId());

        List<User> friendsOfFirst = userDbStorage.getFriends(firstUser.getId());
        assertThat(friendsOfFirst).extracting(User::getId)
                .containsExactly(secondUser.getId());

        List<User> friendsOfSecond = userDbStorage.getFriends(secondUser.getId());
        assertThat(friendsOfSecond).isEmpty();

        userDbStorage.removeFriend(firstUser.getId(), secondUser.getId());
        List<User> friendsAfterRemoval = userDbStorage.getFriends(firstUser.getId());
        assertThat(friendsAfterRemoval).isEmpty();
    }
}
