package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;


@Data
public class UserEditRequest {

    @Size(max = 20, message = "First name must not exceed 20 characters")
    private String firstName;

    @Size(max = 20, message = "Last name must not exceed 20 characters")
    private String lastName;

    @Email(message = "Email should be in valid format")
    private String email;

    @URL(message = "Image URL must be a valid URL")
    private String profilePicture;
}
