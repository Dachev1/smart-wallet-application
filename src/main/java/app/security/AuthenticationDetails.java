package app.security;

import app.user.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthenticationDetails implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String password;
    private final UserRole role;
    private final boolean isActive;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {

        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {

        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return isActive;
    }

    @Override
    public boolean isEnabled() {

        return isActive;
    }
}
