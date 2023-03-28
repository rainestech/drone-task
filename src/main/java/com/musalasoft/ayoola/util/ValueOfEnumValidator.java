package com.musalasoft.ayoola.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Stream;

public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {
    private List<String> subset;

    @Override
    public void initialize(ValueOfEnum constraintAnnotation) {
        this.subset = Stream.of(constraintAnnotation.enumClass().getEnumConstants())
                .map(Enum::name).toList();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && subset.contains(value.toString());
    }
}
