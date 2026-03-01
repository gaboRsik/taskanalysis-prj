package com.taskanalysis.exception;

/**
 * Exception thrown when account is locked due to too many failed login attempts
 */
public class AccountLockedException extends RuntimeException {
    
    private final long lockoutDurationMinutes;
    
    public AccountLockedException(String message, long lockoutDurationMinutes) {
        super(message);
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }
    
    public long getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
}
