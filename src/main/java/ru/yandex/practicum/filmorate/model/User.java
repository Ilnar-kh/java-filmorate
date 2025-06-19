package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
public @Data class User {

    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
