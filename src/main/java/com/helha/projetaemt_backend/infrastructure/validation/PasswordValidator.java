package com.helha.projetaemt_backend.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // Nothing to initialize
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false;
        }

        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        if (password.length() < MIN_LENGTH) {
            errorMessage.append("8 caractères minimum. ");
            isValid = false;
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            errorMessage.append("Une lettre majuscule requise. ");
            isValid = false;
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            errorMessage.append("Une lettre minuscule requise. ");
            isValid = false;
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            errorMessage.append("Un chiffre requis. ");
            isValid = false;
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            errorMessage.append("Un caractère spécial requis (!@#$%...). ");
            isValid = false;
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessage.toString().trim())
                    .addConstraintViolation();
        }

        return isValid;
    }
}
