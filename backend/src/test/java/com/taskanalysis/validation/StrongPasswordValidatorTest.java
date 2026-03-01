package com.taskanalysis.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StrongPasswordValidator
 */
class StrongPasswordValidatorTest {

    private StrongPasswordValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new StrongPasswordValidator();
        
        // Initialize with default settings
        StrongPassword annotation = createAnnotation(8, true, true, true, true);
        validator.initialize(annotation);
    }

    @Test
    @DisplayName("Valid password should pass all checks")
    void testValidPassword() {
        assertTrue(validator.isValid("Password123!", context));
        assertTrue(validator.isValid("MyP@ssw0rd", context));
        assertTrue(validator.isValid("Secure#Pass1", context));
        assertTrue(validator.isValid("Test@1234", context));
    }

    @Test
    @DisplayName("Null or empty password should fail")
    void testNullOrEmptyPassword() {
        assertFalse(validator.isValid(null, context));
        assertFalse(validator.isValid("", context));
    }

    @Test
    @DisplayName("Password shorter than minimum length should fail")
    void testPasswordTooShort() {
        assertFalse(validator.isValid("Pass1!", context)); // Only 6 chars
        assertFalse(validator.isValid("Abc@12", context)); // Only 6 chars
    }

    @Test
    @DisplayName("Password without uppercase should fail")
    void testPasswordWithoutUppercase() {
        assertFalse(validator.isValid("password123!", context));
        assertFalse(validator.isValid("myp@ssw0rd", context));
    }

    @Test
    @DisplayName("Password without lowercase should fail")
    void testPasswordWithoutLowercase() {
        assertFalse(validator.isValid("PASSWORD123!", context));
        assertFalse(validator.isValid("MYP@SSW0RD", context));
    }

    @Test
    @DisplayName("Password without digit should fail")
    void testPasswordWithoutDigit() {
        assertFalse(validator.isValid("Password!", context));
        assertFalse(validator.isValid("MyP@ssword", context));
    }

    @Test
    @DisplayName("Password without special character should fail")
    void testPasswordWithoutSpecialChar() {
        assertFalse(validator.isValid("Password123", context));
        assertFalse(validator.isValid("MyPassw0rd", context));
    }

    @Test
    @DisplayName("Minimum length validation works")
    void testMinimumLength() {
        // Exactly 8 characters
        assertTrue(validator.isValid("Pass@123", context));
        
        // More than 8
        assertTrue(validator.isValid("Password@123", context));
        
        // Less than 8
        assertFalse(validator.isValid("Pass@12", context));
    }

    @Test
    @DisplayName("All special characters are accepted")
    void testSpecialCharacters() {
        String[] specialChars = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "-", "=", "[", "]", "{", "}", "|", ";", ":", ",", ".", "<", ">", "?"};
        
        for (String special : specialChars) {
            String password = "Password1" + special;
            assertTrue(validator.isValid(password, context), 
                "Password with '" + special + "' should be valid");
        }
    }

    @Test
    @DisplayName("Real-world password examples")
    void testRealWorldPasswords() {
        // Valid
        assertTrue(validator.isValid("Welcome@2026", context));
        assertTrue(validator.isValid("TaskAnalysis#123", context));
        assertTrue(validator.isValid("Secure$Pass2026", context));
        
        // Invalid
        assertFalse(validator.isValid("welcome2026", context)); // No uppercase, no special
        assertFalse(validator.isValid("WELCOME2026!", context)); // No lowercase
        assertFalse(validator.isValid("Welcome!", context)); // No digit
        assertFalse(validator.isValid("Welcome2026", context)); // No special char
    }

    /**
     * Helper method to create annotation instance
     */
    private StrongPassword createAnnotation(int minLength, boolean requireUppercase, 
                                           boolean requireLowercase, boolean requireDigit, 
                                           boolean requireSpecialChar) {
        return new StrongPassword() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return StrongPassword.class;
            }

            @Override
            public String message() {
                return "Invalid password";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public int minLength() {
                return minLength;
            }

            @Override
            public boolean requireUppercase() {
                return requireUppercase;
            }

            @Override
            public boolean requireLowercase() {
                return requireLowercase;
            }

            @Override
            public boolean requireDigit() {
                return requireDigit;
            }

            @Override
            public boolean requireSpecialChar() {
                return requireSpecialChar;
            }
        };
    }
}
