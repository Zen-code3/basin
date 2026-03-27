package Customers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class OrdersStorage {

    static final class OrderItem {
        final int productId;
        final String name;
        final double price;
        final int quantity;

        OrderItem(int productId, String name, double price, int quantity) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        double getSubtotal() {
            return price * quantity;
        }
    }

    static final class Order {
        final int orderNo;
        final String createdAt;
        final List<OrderItem> items;
        final double total;

        Order(int orderNo, String createdAt, List<OrderItem> items, double total) {
            this.orderNo = orderNo;
            this.createdAt = createdAt;
            this.items = items;
            this.total = total;
        }
    }

    private static final AtomicInteger ORDER_SEQ = new AtomicInteger(1000);
    private static final List<Order> ORDERS = new ArrayList<>();

    private OrdersStorage() {
    }

    static synchronized void createOrderFromCart(List<CartStorage.Item> cartItems, double total) {
        List<OrderItem> items = new ArrayList<>();
        for (CartStorage.Item it : cartItems) {
            items.add(new OrderItem(it.productId, it.name, it.price, it.quantity));
        }
        String created = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        int no = ORDER_SEQ.incrementAndGet();
        ORDERS.add(0, new Order(no, created, Collections.unmodifiableList(items), total));
    }

    static synchronized List<Order> getOrders() {
        return Collections.unmodifiableList(new ArrayList<>(ORDERS));
    }

    static synchronized Order getOrderByNo(int orderNo) {
        for (Order o : ORDERS) {
            if (o.orderNo == orderNo) {
                return o;
            }
        }
        return null;
    }

    static String buildReceiptText(Order o) {
        StringBuilder sb = new StringBuilder();
        sb.append("Qualimed Pharmacy\n");
        sb.append("Receipt\n\n");
        sb.append("Order No: ").append(o.orderNo).append("\n");
        sb.append("Date: ").append(o.createdAt).append("\n\n");
        for (OrderItem it : o.items) {
            sb.append(it.name)
                    .append("  x")
                    .append(it.quantity)
                    .append("  @ ₱")
                    .append(String.format("%.2f", it.price))
                    .append("  = ₱")
                    .append(String.format("%.2f", it.getSubtotal()))
                    .append("\n");
        }
        sb.append("\nTotal: ₱").append(String.format("%.2f", o.total)).append("\n");
        sb.append("Thank you for your purchase!");
        return sb.toString();
    }
}

