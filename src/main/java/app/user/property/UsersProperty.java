package app.user.property;

import app.user.model.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "users")
public class UsersProperty {

    private UserRole defaultRole;
    private boolean defaultAccountState;

    public boolean getDefaultAccountState() {
        return defaultAccountState;
    }
}
