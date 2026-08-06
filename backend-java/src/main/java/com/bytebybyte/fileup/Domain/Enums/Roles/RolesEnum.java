package com.bytebybyte.fileup.Domain.Enums.Roles;

public enum RolesEnum {
    Basic(1L, "ROLE_BASIC"),
    Admin(2L, "ROLE_ADMIN");

    private final Long roleId;
    private final String authority;

    RolesEnum(Long id, String authority) {
        this.roleId = id;
        this.authority = authority;
    }

    public Long getId() {
        return roleId;
    }

    public String getAuthority() {
        return authority;
    }

}
