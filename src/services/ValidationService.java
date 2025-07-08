package services;

import java.util.regex.Pattern;

/**
 * ValidationService handles all input validation logic following Single Responsibility Principle.
 * Centralized validation for user input, business rules, and data integrity checks.
 */
public class ValidationService {
    
    private static ValidationService instance;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9]{10,15}$"
    );
    
    /**
     * Private constructor for singleton pattern.
     */
    private ValidationService() {}
    
    /**
     * Returns singleton instance of ValidationService.
     * @return ValidationService instance
     */
    public static synchronized ValidationService getInstance() {
        if (instance == null) {
            instance = new ValidationService();
        }
        return instance;
    }
    
    /**
     * Validates user registration data.
     * @param name User's full name
     * @param phone Phone number
     * @param email Email address
     * @param userName Username
     * @return true if all data is valid, false otherwise
     */
    public boolean validateUserRegistration(String name, String phone, String email, String userName) {
        return isValidName(name) && 
               isValidPhone(phone) && 
               isValidEmail(email) && 
               isValidUserName(userName);
    }
    
    /**
     * Validates contact information update.
     * @param phone Phone number
     * @param email Email address
     * @return true if valid, false otherwise
     */
    public boolean validateContactInfo(String phone, String email) {
        return isValidPhone(phone) && isValidEmail(email);
    }
    
    /**
     * Validates user name.
     * @param name Name to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }
    
    /**
     * Validates phone number.
     * @param phone Phone number to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.replaceAll("[\\s-()]", "")).matches();
    }
    
    /**
     * Validates email address.
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates username.
     * @param userName Username to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidUserName(String userName) {
        return userName != null && !userName.trim().isEmpty() && userName.trim().length() >= 3;
    }
    
    /**
     * Validates parking code.
     * @param codeStr Parking code as string
     * @return true if valid integer, false otherwise
     */
    public boolean isValidParkingCode(String codeStr) {
        if (codeStr == null || codeStr.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(codeStr.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validates car number format.
     * @param carNumber Car license number
     * @return true if valid, false otherwise
     */
    public boolean isValidCarNumber(String carNumber) {
        return carNumber != null && !carNumber.trim().isEmpty() && carNumber.trim().length() >= 6;
    }
    
    /**
     * Validates time duration in hours.
     * @param hours Hours to validate
     * @return true if within reasonable range, false otherwise
     */
    public boolean isValidDuration(int hours) {
        return hours > 0 && hours <= 12;
    }
    
    /**
     * Validates extension hours.
     * @param extensionHours Extension duration
     * @return true if valid extension range, false otherwise
     */
    public boolean isValidExtension(int extensionHours) {
        return extensionHours >= 1 && extensionHours <= 4;
    }
}