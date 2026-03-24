public final class SessionManager {

    private static Integer currentCustomerId;
    private static String currentEmail;
    private static String currentFullName;
    private static String currentRole;

    private SessionManager() {
    }

    public static void setUser(int customerId, String email, String fullName, String role) {
        currentCustomerId = customerId;
        currentEmail = email;
        currentFullName = fullName;
        currentRole = role;
    }

    public static Integer getCurrentCustomerId() {
        return currentCustomerId;
    }

    public static String getCurrentEmail() {
        return currentEmail;
    }

    public static String getCurrentFullName() {
        return currentFullName;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static void clear() {
        currentCustomerId = null;
        currentEmail = null;
        currentFullName = null;
        currentRole = null;
    }

    public static boolean isLoggedIn() {
        return currentCustomerId != null;
    }
}
