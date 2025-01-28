package app.web.mapper;

import app.user.model.User;
import app.web.dto.UserEditRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static UserEditRequest mapToUserEditRequest(User user) {
        UserEditRequest request = new UserEditRequest();
        request.setFirstName(user.getFirstName());
        request.setLastName(user.getLastName());
        request.setEmail(user.getEmail());
        request.setProfilePicture(user.getProfilePicture());

        return request;
    }
}
