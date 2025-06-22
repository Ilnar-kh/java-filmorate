package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    private Long id;

    @NotBlank(message = "Электронная почта не может быть пустой и должна содержать символ '@'")
    @Pattern(
            regexp = "^.+@.+$",
            message = "Электронная почта не может быть пустой и должна содержать символ '@'"
    )
    private String email;

    @NotBlank(message = "Логин не может быть пустым и содержать пробелы")
    @Pattern(
            regexp = "\\S+",
            message = "Логин не может быть пустым и содержать пробелы"
    )
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
