package com.taskanalysis.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for strong passwords
 * Requirements:
 * - At least 8 characters
 * - Contains uppercase and lowercase letters
 * - Contains at least one digit
 * - Contains at least one special character
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    
    String message() default "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    int minLength() default 8;
    
    boolean requireUppercase() default true;
    
    boolean requireLowercase() default true;
    
    boolean requireDigit() default true;
    
    boolean requireSpecialChar() default true;
}
