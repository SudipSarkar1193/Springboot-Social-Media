package com.SSarkar.Xplore.dto.auth;

//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class UserRegistrationRequestDTO {

    @NotBlank(message = "firstName cannot be blank")
    @Size(min = 3, max = 20, message = "firstName must be between 3 and 20 characters")
    private String firstName;

    @NotBlank(message = "lastName cannot be blank")
    @Size(min = 3, max = 20, message = "lastName must be between 3 and 20 characters")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private String profilePictureUrl; // Optional field for profile picture URL

    public boolean isProfilePictureUrlValid() {
        // Check if the profile picture URL is valid (not blank or null)
        return profilePictureUrl != null && !profilePictureUrl.trim().isEmpty();
    }
}