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
}

