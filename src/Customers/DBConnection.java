package Customers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class DBConnection {

    private static final String DEFAULT_URL = "jdbc:sqlite:qualimed_pharmacy.db";

    private static final String URL;
    private static volatile boolean schemaEnsured;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }

        Properties p = new Properties();
        p.setProperty("db.url", DEFAULT_URL);

        try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (Exception ignored) {
        }

        File local = new File(System.getProperty("user.dir"), "db.properties");
        if (local.isFile()) {
            try (FileInputStream fin = new FileInputStream(local)) {
                p.load(fin);
            } catch (Exception ignored) {
            }
        }

        URL = p.getProperty("db.url", DEFAULT_URL).trim();
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA journal_mode = WAL");
        }
        ensureSchemaOnce(c);
        return c;
    }

    private static void ensureSchemaOnce(Connection c) throws SQLException {
        if (schemaEnsured) {
            return;
        }
        synchronized (DBConnection.class) {
            if (schemaEnsured) {
                return;
            }
            runSqliteMigrations(c);
            schemaEnsured = true;
        }
    }

    private static void runSqliteMigrations(Connection c) throws SQLException {
        String[] ddl = new String[]{
            "CREATE TABLE IF NOT EXISTS customer ("
                    + "customer_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "full_name TEXT NOT NULL,"
                    + "email TEXT NOT NULL UNIQUE,"
                    + "password_hash TEXT NOT NULL,"
                    + "contact_number TEXT,"
                    + "address TEXT,"
                    + "role TEXT NOT NULL DEFAULT 'customer',"
                    + "account_status TEXT NOT NULL DEFAULT 'active'"
                    + ")",
            "CREATE TABLE IF NOT EXISTS product ("
                    + "product_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "product_name TEXT NOT NULL,"
                    + "description TEXT,"
                    + "category TEXT,"
                    + "price REAL NOT NULL DEFAULT 0,"
                    + "stock_quantity INTEGER NOT NULL DEFAULT 0,"
                    + "expirydate TEXT,"
                    + "image_path TEXT"
                    + ")",
            "CREATE TABLE IF NOT EXISTS \"order\" ("
                    + "order_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "customer_id INTEGER NOT NULL,"
                    + "order_date TEXT NOT NULL DEFAULT (datetime('now')),"
                    + "total_amount REAL NOT NULL DEFAULT 0,"
                    + "status TEXT NOT NULL DEFAULT 'pending',"
                    + "FOREIGN KEY (customer_id) REFERENCES customer(customer_id)"
                    + ")",
            "CREATE TABLE IF NOT EXISTS order_item ("
                    + "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "order_id INTEGER NOT NULL,"
                    + "product_id INTEGER NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "subtotal REAL NOT NULL,"
                    + "FOREIGN KEY (order_id) REFERENCES \"order\"(order_id) ON DELETE CASCADE,"
                    + "FOREIGN KEY (product_id) REFERENCES product(product_id)"
                    + ")",
            "CREATE TABLE IF NOT EXISTS payment ("
                    + "payment_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "order_id INTEGER NOT NULL,"
                    + "payment_method TEXT,"
                    + "payment_status TEXT,"
                    + "payment_date TEXT,"
                    + "FOREIGN KEY (order_id) REFERENCES \"order\"(order_id) ON DELETE CASCADE"
                    + ")",
            "INSERT INTO customer (full_name, email, password_hash, contact_number, address, role, account_status) "
                    + "VALUES ('System Admin','admin@qualimed.ph',"
                    + "'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','','','admin','active') "
                    + "ON CONFLICT(email) DO UPDATE SET role = excluded.role, account_status = excluded.account_status"
        };
        try (Statement st = c.createStatement()) {
            for (String sql : ddl) {
                st.execute(sql);
            }
        }
    }

    public static String userMessage(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) {
            msg = e.toString();
        }
        return msg;
    }
}

