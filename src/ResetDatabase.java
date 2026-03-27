import java.io.File;

/**
 * Utility to delete the local SQLite database file so a new one
 * will be recreated on the next app start.
 */
public class ResetDatabase {

    public static void main(String[] args) {
        File db = new File(System.getProperty("user.dir"), "qualimed_pharmacy.db");
        if (!db.exists()) {
            System.out.println("Database not found: " + db.getAbsolutePath());
            System.out.println("Nothing to delete.");
            return;
        }
        if (db.delete()) {
            System.out.println("Deleted database: " + db.getAbsolutePath());
            System.out.println("Run the app again to recreate tables.");
        } else {
            System.out.println("Failed to delete database: " + db.getAbsolutePath());
            System.out.println("Make sure the app is closed and try again.");
        }
    }
}

