package Customers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

final class OrdersRepository {

    static final class OrderRow {
        final int orderId;
        final String orderDate;
        final double totalAmount;
        final String status;

        OrderRow(int orderId, String orderDate, double totalAmount, String status) {
            this.orderId = orderId;
            this.orderDate = orderDate;
            this.totalAmount = totalAmount;
            this.status = status;
        }
    }

    static final class OrderItemRow {
        final int productId;
        final String productName;
        final int quantity;
        final double subtotal;

        OrderItemRow(int productId, String productName, int quantity, double subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.subtotal = subtotal;
        }

        double getUnitPrice() {
            if (quantity <= 0) {
                return 0;
            }
            return subtotal / quantity;
        }
    }

    private OrdersRepository() {
    }

    static void addItemToPendingOrder(int customerId, int productId, int quantity, double unitPrice) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                int orderId = findPendingOrderId(c, customerId);
                if (orderId <= 0) {
                    orderId = createPendingOrder(c, customerId);
                }
                upsertOrderItem(c, orderId, productId, quantity, unitPrice);
                recomputeOrderTotal(c, orderId);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(oldAuto);
            }
        }
    }

    static int createOrderFromCart(int customerId, List<CartStorage.Item> items, double total) throws SQLException {
        String insertOrder = "INSERT INTO \"order\" (customer_id, total_amount, status) VALUES (?, ?, 'pending')";
        String insertItem = "INSERT INTO order_item (order_id, product_id, quantity, subtotal) VALUES (?, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                int orderId;
                try (PreparedStatement ps = c.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    ps.setDouble(2, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Could not get created order id.");
                        }
                        orderId = keys.getInt(1);
                    }
                }

                try (PreparedStatement psItem = c.prepareStatement(insertItem)) {
                    for (CartStorage.Item it : items) {
                        psItem.setInt(1, orderId);
                        psItem.setInt(2, it.productId);
                        psItem.setInt(3, it.quantity);
                        psItem.setDouble(4, it.getSubtotal());
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }

                c.commit();
                return orderId;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(oldAuto);
            }
        }
    }

    static List<OrderRow> listOrdersForCustomer(int customerId) throws SQLException {
        String sql = "SELECT order_id, order_date, total_amount, status FROM \"order\" WHERE customer_id = ? ORDER BY order_date DESC";
        List<OrderRow> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new OrderRow(
                            rs.getInt("order_id"),
                            rs.getString("order_date"),
                            rs.getDouble("total_amount"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return out;
    }

    static boolean hasPendingOrder(int customerId) throws SQLException {
        String sql = "SELECT 1 FROM \"order\" WHERE customer_id = ? AND lower(status) = 'pending' LIMIT 1";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static OrderRow getOrder(int orderId) throws SQLException {
        String sql = "SELECT order_id, order_date, total_amount, status FROM \"order\" WHERE order_id = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new OrderRow(
                        rs.getInt("order_id"),
                        rs.getString("order_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                );
            }
        }
    }

    static List<OrderItemRow> getOrderItems(int orderId) throws SQLException {
        String sql = "SELECT oi.product_id, p.product_name, oi.quantity, oi.subtotal "
                + "FROM order_item oi "
                + "LEFT JOIN product p ON p.product_id = oi.product_id "
                + "WHERE oi.order_id = ? "
                + "ORDER BY oi.order_item_id";
        List<OrderItemRow> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new OrderItemRow(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("subtotal")
                    ));
                }
            }
        }
        return out;
    }

    static String buildReceiptText(int orderId) throws SQLException {
        OrderRow o = getOrder(orderId);
        if (o == null) {
            return "Order not found.";
        }
        List<OrderItemRow> items = getOrderItems(orderId);

        StringBuilder sb = new StringBuilder();
        sb.append("Qualimed Pharmacy\n");
        sb.append("Receipt\n\n");
        sb.append("Order No: ").append(o.orderId).append("\n");
        sb.append("Date: ").append(o.orderDate != null ? o.orderDate : "").append("\n");
        sb.append("Status: ").append(o.status != null ? o.status : "").append("\n\n");

        for (OrderItemRow it : items) {
            String name = it.productName != null ? it.productName : ("Product #" + it.productId);
            sb.append(name)
                    .append("  x")
                    .append(it.quantity)
                    .append("  @ ₱")
                    .append(String.format("%.2f", it.getUnitPrice()))
                    .append("  = ₱")
                    .append(String.format("%.2f", it.subtotal))
                    .append("\n");
        }
        sb.append("\nTotal: ₱").append(String.format("%.2f", o.totalAmount)).append("\n");
        sb.append("Thank you for your purchase!");
        return sb.toString();
    }

    private static int findPendingOrderId(Connection c, int customerId) throws SQLException {
        String sql = "SELECT order_id FROM \"order\" WHERE customer_id = ? AND lower(status) = 'pending' ORDER BY order_id DESC LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("order_id");
                }
                return -1;
            }
        }
    }

    private static int createPendingOrder(Connection c, int customerId) throws SQLException {
        String sql = "INSERT INTO \"order\" (customer_id, total_amount, status) VALUES (?, 0, 'pending')";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not create pending order.");
    }

    private static void upsertOrderItem(Connection c, int orderId, int productId, int quantity, double unitPrice) throws SQLException {
        String selectSql = "SELECT order_item_id, quantity, subtotal FROM order_item WHERE order_id = ? AND product_id = ?";
        try (PreparedStatement ps = c.prepareStatement(selectSql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int orderItemId = rs.getInt("order_item_id");
                    int oldQty = rs.getInt("quantity");
                    double oldSubtotal = rs.getDouble("subtotal");
                    int newQty = oldQty + quantity;
                    double newSubtotal = oldSubtotal + (unitPrice * quantity);

                    String updateSql = "UPDATE order_item SET quantity = ?, subtotal = ? WHERE order_item_id = ?";
                    try (PreparedStatement ups = c.prepareStatement(updateSql)) {
                        ups.setInt(1, newQty);
                        ups.setDouble(2, newSubtotal);
                        ups.setInt(3, orderItemId);
                        ups.executeUpdate();
                    }
                    return;
                }
            }
        }

        String insertSql = "INSERT INTO order_item (order_id, product_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(insertSql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setDouble(4, unitPrice * quantity);
            ps.executeUpdate();
        }
    }

    private static void recomputeOrderTotal(Connection c, int orderId) throws SQLException {
        double total = 0;
        String sumSql = "SELECT IFNULL(SUM(subtotal), 0) AS total FROM order_item WHERE order_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sumSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
            }
        }
        String updateSql = "UPDATE \"order\" SET total_amount = ? WHERE order_id = ?";
        try (PreparedStatement ps = c.prepareStatement(updateSql)) {
            ps.setDouble(1, total);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }
}

