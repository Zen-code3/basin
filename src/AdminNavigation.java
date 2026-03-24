import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;

public final class AdminNavigation {

    private AdminNavigation() {
    }

    private static void makeClickable(JLabel label, Runnable action) {
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });
    }

    public static void attachAdminNav(JFrame frame, JLabel jLabel2, JLabel jLabel3, JLabel jLabel4,
            JLabel jLabel5, JLabel jLabel6, JLabel jLabel7) {

        makeClickable(jLabel2, () -> openDashboard(frame));
        makeClickable(jLabel3, () -> openProducts(frame));
        makeClickable(jLabel4, () -> openUsers(frame));
        makeClickable(jLabel5, () -> openOrders(frame));
        makeClickable(jLabel6, () -> openProfile(frame));
        makeClickable(jLabel7, () -> {
            SessionManager.clear();
            new Homepage().setVisible(true);
            frame.dispose();
        });
    }

    private static void openDashboard(JFrame from) {
        if (from instanceof AdminDashboard) {
            ((AdminDashboard) from).refreshStats();
            return;
        }
        new AdminDashboard().setVisible(true);
        from.dispose();
    }

    private static void openProducts(JFrame from) {
        if (from instanceof AdminProduct) {
            ((AdminProduct) from).reloadProducts();
            return;
        }
        new AdminProduct().setVisible(true);
        from.dispose();
    }

    private static void openUsers(JFrame from) {
        if (from instanceof AdminUser) {
            ((AdminUser) from).reloadUsers();
            return;
        }
        new AdminUser().setVisible(true);
        from.dispose();
    }

    private static void openOrders(JFrame from) {
        if (from instanceof AdminOrders) {
            ((AdminOrders) from).reloadOrders();
            return;
        }
        new AdminOrders().setVisible(true);
        from.dispose();
    }

    private static void openProfile(JFrame from) {
        if (from instanceof AdminProfile) {
            ((AdminProfile) from).reloadProfileFields();
            return;
        }
        new AdminProfile().setVisible(true);
        from.dispose();
    }
}
