package com.harshitksinghai.CodeControl_Backend.AuthService.Enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    ADMIN_CREATE("admin:create"),
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),

    MANAGER_CREATE("manager:create"),
    MANAGER_READ("manager:read"),
    MANAGER_UPDATE("manager:update"),
    MANAGER_DELETE("manager:delete"),

    CREATOR_CREATE("creator:create"),
    CREATOR_READ("creator:read"),
    CREATOR_UPDATE("creator:update"),
    CREATOR_DELETE("creator:delete"),

    USER_CREATE("user:create"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),

    RESOURCE_CREATE("resource:create"),
    RESOURCE_READ("resource:read"),
    RESOURCE_UPDATE("resource:update"),
    RESOURCE_DELETE("resource:delete")

    ;

    @Getter
    private final String permission;
}
