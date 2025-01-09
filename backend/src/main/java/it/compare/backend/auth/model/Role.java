package it.compare.backend.auth.model;

@SuppressWarnings("java:S6548")
public enum Role {
    ADMIN;

    /**
     * Returns the role name with the 'ROLE_' prefix.
     */
    public String getPrefixedRole() {
        return "ROLE_" + name();
    }
}
