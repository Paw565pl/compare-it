package it.compare.backend.auth.model;

public enum Role {
    ADMIN;

    /**
     * Returns the role name with the 'ROLE_' prefix.
     */
    public String getPrefixedRole() {
        return "ROLE_" + name();
    }
}
