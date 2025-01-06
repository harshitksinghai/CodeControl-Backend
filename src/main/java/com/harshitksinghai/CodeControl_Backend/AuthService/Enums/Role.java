package com.harshitksinghai.CodeControl_Backend.AuthService.Enums;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.harshitksinghai.CodeControl_Backend.AuthService.Enums.Permission.*;

@Getter
@RequiredArgsConstructor
public enum Role {
    HEAD(
            Set.of(
                    ADMIN_CREATE,
                    ADMIN_READ,
                    ADMIN_UPDATE,
                    ADMIN_DELETE,

                    MANAGER_CREATE,
                    MANAGER_READ,
                    MANAGER_UPDATE,
                    MANAGER_DELETE,

                    CREATOR_CREATE,
                    CREATOR_READ,
                    CREATOR_UPDATE,
                    CREATOR_DELETE,

                    USER_CREATE,
                    USER_READ,
                    USER_UPDATE,
                    USER_DELETE,

                    RESOURCE_CREATE,
                    RESOURCE_READ,
                    RESOURCE_UPDATE,
                    RESOURCE_DELETE
            )
    ),
    ADMIN(
            Set.of(
                    ADMIN_READ,

                    MANAGER_CREATE,
                    MANAGER_READ,
                    MANAGER_UPDATE,
                    MANAGER_DELETE,

                    CREATOR_CREATE,
                    CREATOR_READ,
                    CREATOR_UPDATE,
                    CREATOR_DELETE,

                    USER_CREATE,
                    USER_READ,
                    USER_UPDATE,
                    USER_DELETE,

                    RESOURCE_CREATE,
                    RESOURCE_READ,
                    RESOURCE_UPDATE,
                    RESOURCE_DELETE
            )
    ),
    MANAGER(
            Set.of(
                    MANAGER_READ,

                    CREATOR_CREATE,
                    CREATOR_READ,
                    CREATOR_UPDATE,
                    CREATOR_DELETE,

                    USER_CREATE,
                    USER_READ,
                    USER_UPDATE,
                    USER_DELETE,

                    RESOURCE_CREATE,
                    RESOURCE_READ,
                    RESOURCE_UPDATE,
                    RESOURCE_DELETE
            )
    ),
    CREATOR(
            Set.of(
                    CREATOR_READ,

                    RESOURCE_CREATE,
                    RESOURCE_READ,
                    RESOURCE_UPDATE,
                    RESOURCE_DELETE
            )
    ),
    USER(
            Set.of(
                    RESOURCE_READ
            )
    )

    ;

    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities(){
        // Create a mutable list
        List<SimpleGrantedAuthority> authorities = new ArrayList<>(
                getPermissions()
                        .stream()
                        .map(permission -> new SimpleGrantedAuthority(permission.name()))
                        .toList()
        );

        // Add the role to the authorities list
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }
}
