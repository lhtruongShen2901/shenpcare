package g6shenpcare.model;

public enum UserRole {
    DOCTOR,
    ADMIN,
    CUSTOMER;

    public static String toRoleString(UserRole role) {
        return role.name();
    }
}
