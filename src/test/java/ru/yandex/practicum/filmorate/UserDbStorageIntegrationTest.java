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

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageIntegrationTest {

    private final UserDbStorage userDbStorage;

    @Test
    void testCreateFindByIdAndFindAll() {
        // подготавливаем нового пользователя
        User userToCreate = User.builder()
                .email("a@b.c")
                .login("login")
                .name("Имя")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        // создаём
        User createdUser = userDbStorage.create(userToCreate);
        assertThat(createdUser.getId()).isPositive();

        // находим по id
        User fetchedUser = userDbStorage.findById(createdUser.getId());
        assertThat(fetchedUser).isNotNull()
                .extracting("id", "email", "login", "name")
                .containsExactly(
                        createdUser.getId(),
                        "a@b.c",
                        "login",
                        "Имя"
                );

        // проверяем findAll
        List<User> allUsers = userDbStorage.findAll();
        assertThat(allUsers).extracting("id")
                .contains(createdUser.getId());
    }

    @Test
    void testUpdateUser() {
        // создаём пользователя
        User originalUser = User.builder()
                .email("x@y.z")
                .login("userLogin")
                .name("ПервоеИмя")
                .birthday(LocalDate.now())
                .build();
        User createdUser = userDbStorage.create(originalUser);

        // обновляем
        createdUser.setName("ВтороеИмя");
        createdUser.setEmail("new@e.e");
        User updatedUser = userDbStorage.update(createdUser);

        // проверяем обновления
        User fetchedUser = userDbStorage.findById(updatedUser.getId());
        assertThat(fetchedUser.getName()).isEqualTo("ВтороеИмя");
        assertThat(fetchedUser.getEmail()).isEqualTo("new@e.e");
    }

    @Test
    void testFriendshipFlow() {
        // создаём двух пользователей
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

        // добавляем второго в друзья первого
        userDbStorage.addFriend(firstUser.getId(), secondUser.getId());

        // убеждаемся, что в списке друзей первого теперь только второй
        List<User> friendsOfFirst = userDbStorage.getFriends(firstUser.getId());
        assertThat(friendsOfFirst).extracting("id")
                .containsExactly(secondUser.getId());

        // проверяем, что у второго нет первого в друзьях
        List<User> friendsOfSecond = userDbStorage.getFriends(secondUser.getId());
        assertThat(friendsOfSecond).isEmpty();

        userDbStorage.removeFriend(firstUser.getId(), secondUser.getId());
        List<User> friendsAfterRemoval = userDbStorage.getFriends(firstUser.getId());
        assertThat(friendsAfterRemoval).isEmpty();
    }
}
