package com.bytebybyte.fileup.Domain.Enums.Roles;

public enum RolesEnum {
    BASIC("ROLE_BASIC"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    RolesEnum(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

}
