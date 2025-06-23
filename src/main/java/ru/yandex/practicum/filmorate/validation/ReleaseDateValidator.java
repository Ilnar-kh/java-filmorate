package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateValidator
        implements ConstraintValidator<ValidReleaseDate, LocalDate> {

    private static final LocalDate EARLIEST = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext ctx) {
        if (date == null) return true;  // @NotNull обрабатывает null
        return !date.isBefore(EARLIEST);
    }
}
