/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ilove
 */
public class AdminOrders extends javax.swing.JFrame {

    private javax.swing.JTable orderTable;
    private javax.swing.table.DefaultTableModel orderTableModel;
    private javax.swing.JTextField orderSearchField;

    /**
     * Creates new form AdminOrders
     */
    public AdminOrders() {
        initComponents();
        initOrderTableUi();
        AdminNavigation.attachAdminNav(this, jLabel2, jLabel3, jLabel4, jLabel5, jLabel6, jLabel7);
        reloadOrders();
        WindowHelper.sizeAndCenter(this);
    }

    public void reloadOrders() {
        if (orderTableModel == null) {
            return;
        }
        orderTableModel.setRowCount(0);
        String term = "%";
        if (orderSearchField != null && orderSearchField.getText() != null && !orderSearchField.getText().trim().isEmpty()) {
            term = "%" + orderSearchField.getText().trim() + "%";
        }
        String sql = "SELECT o.order_id, c.full_name, o.order_date, o.total_amount, o.status "
                + "FROM \"order\" o JOIN customer c ON o.customer_id = c.customer_id "
                + "WHERE CAST(o.order_id AS CHAR) LIKE ? OR c.full_name LIKE ? OR LOWER(o.status) LIKE LOWER(?) "
                + "ORDER BY o.order_date DESC";
        try (java.sql.Connection c = DBConnection.getConnection();
                java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setString(3, term);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double total = rs.getDouble("total_amount");
                    String totalStr = "₱" + String.format(java.util.Locale.US, "%.2f", total);
                    java.sql.Timestamp ts = rs.getTimestamp("order_date");
                    String dateStr = ts != null ? ts.toString() : "";
                    String st = rs.getString("status");
                    if (st != null) {
                        st = st.trim().toLowerCase();
                    }
                    orderTableModel.addRow(new Object[]{
                        rs.getInt("order_id"),
                        rs.getString("full_name"),
                        dateStr,
                        totalStr,
                        st,
                        "Actions"
                    });
                }
            }
        } catch (java.sql.SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, DBConnection.userMessage(e), "Database error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initOrderTableUi() {
        orderSearchField = new javax.swing.JTextField();
        jPanel9.add(orderSearchField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 200, 26));
        javax.swing.JLabel lbl = new javax.swing.JLabel("Search");
        jPanel9.add(lbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 43, 60, 20));

        orderTableModel = new javax.swing.table.DefaultTableModel(
                new Object[]{"Order ID", "Customer", "Date", "Total", "Status", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new javax.swing.JTable(orderTableModel);
        orderTable.setRowHeight(24);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(orderTable);
        jPanel9.add(scroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 72, 535, 220));

        orderSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                reloadOrders();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                reloadOrders();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                reloadOrders();
            }
        });

        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int r = orderTable.rowAtPoint(e.getPoint());
                int c = orderTable.columnAtPoint(e.getPoint());
                if (r < 0 || c < 0) {
                    return;
                }
                int modelCol = orderTable.convertColumnIndexToModel(c);
                String colName = orderTableModel.getColumnName(modelCol);
                if (!"Actions".equals(colName)) {
                    return;
                }
                int orderId = (Integer) orderTableModel.getValueAt(r, 0);
                javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
                javax.swing.JMenuItem save = new javax.swing.JMenuItem("Save receipt (.txt for Notepad)");
                save.addActionListener(ae -> saveReceiptToFile(orderId));
                menu.add(save);
                javax.swing.JMenuItem print = new javax.swing.JMenuItem("Print receipt");
                print.addActionListener(ae -> printReceipt(orderId));
                menu.add(print);
                menu.addSeparator();
                String[] statuses = new String[]{"completed", "pending", "processing", "cancelled"};
                for (String st : statuses) {
                    javax.swing.JMenuItem mi = new javax.swing.JMenuItem("Set status: " + st);
                    final String s = st;
                    mi.addActionListener(ae -> updateOrderStatus(orderId, s));
                    menu.add(mi);
                }
                menu.show(orderTable, e.getX(), e.getY());
            }
        });
    }

    private void updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE \"order\" SET status = ? WHERE order_id = ?";
        try (java.sql.Connection c = DBConnection.getConnection();
                java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.trim().toLowerCase() : "pending");
            ps.setInt(2, orderId);
            ps.executeUpdate();
            reloadOrders();
        } catch (java.sql.SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, DBConnection.userMessage(e), "Database error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveReceiptToFile(int orderId) {
        try {
            String text = buildReceiptText(orderId);
            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
            chooser.setSelectedFile(new java.io.File("order-" + orderId + "-receipt.txt"));
            if (chooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File f = chooser.getSelectedFile();
                try (java.io.OutputStreamWriter fw = new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(f), java.nio.charset.StandardCharsets.UTF_8)) {
                    fw.write(text);
                }
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop dk = java.awt.Desktop.getDesktop();
                        if (dk.isSupported(java.awt.Desktop.Action.OPEN)) {
                            dk.open(f);
                        }
                    }
                } catch (Exception ignored) {
                }
                javax.swing.JOptionPane.showMessageDialog(this, "Receipt saved.", "Saved",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReceipt(int orderId) {
        try {
            String text = buildReceiptText(orderId);
            javax.swing.JTextArea ta = new javax.swing.JTextArea(text);
            ta.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            ta.print();
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Print error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildReceiptText(int orderId) throws java.sql.SQLException {
        String headerSql = "SELECT o.order_id, c.full_name, o.order_date, o.total_amount, o.status "
                + "FROM \"order\" o JOIN customer c ON o.customer_id = c.customer_id WHERE o.order_id = ?";
        StringBuilder sb = new StringBuilder();
        double orderTotal = 0;
        try (java.sql.Connection c = DBConnection.getConnection();
                java.sql.PreparedStatement ps = c.prepareStatement(headerSql)) {
            ps.setInt(1, orderId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "Order not found.";
                }
                orderTotal = rs.getDouble("total_amount");
                sb.append("Qualimed Pharmacy - Order Receipt\n");
                sb.append("Order ID: ").append(rs.getInt("order_id")).append("\n");
                sb.append("Customer: ").append(rs.getString("full_name")).append("\n");
                sb.append("Date: ").append(rs.getTimestamp("order_date")).append("\n");
                sb.append("Status: ").append(rs.getString("status")).append("\n");
                sb.append("-----------------------------\n");
            }
        }
        String itemsSql = "SELECT p.product_name, oi.quantity, oi.subtotal FROM order_item oi "
                + "JOIN product p ON oi.product_id = p.product_id WHERE oi.order_id = ?";
        try (java.sql.Connection c = DBConnection.getConnection();
                java.sql.PreparedStatement ps = c.prepareStatement(itemsSql)) {
            ps.setInt(1, orderId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    double sub = rs.getDouble("subtotal");
                    sb.append(rs.getString("product_name")).append(" x ").append(rs.getInt("quantity"))
                            .append("  |  ₱").append(String.format(java.util.Locale.US, "%.2f", sub)).append("\n");
                }
                if (!any) {
                    sb.append("(No line items recorded)\n");
                }
                sb.append("-----------------------------\n");
                sb.append("Total: ₱").append(String.format(java.util.Locale.US, "%.2f", orderTotal)).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(43, 182, 115));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Qualimed Pharmacy - Admin Panel");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, -1, 20));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 590, 20));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204), 2));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Dashboard");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, -1, -1));

        jPanel4.setBackground(new java.awt.Color(43, 182, 115));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        jPanel3.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 0, 10, 40));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("Products");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 10, -1, -1));

        jPanel5.setBackground(new java.awt.Color(43, 182, 115));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        jPanel3.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 0, 10, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Users");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 10, -1, -1));

        jPanel6.setBackground(new java.awt.Color(43, 182, 115));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        jPanel3.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 0, -1, -1));

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Orders");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 10, -1, -1));

        jPanel7.setBackground(new java.awt.Color(43, 182, 115));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        jPanel3.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 0, -1, -1));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setText("Profile");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 10, -1, -1));

        jPanel8.setBackground(new java.awt.Color(43, 182, 115));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        jPanel3.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 0, -1, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(153, 153, 153));
        jLabel7.setText("Sign Out");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, -1, -1));

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/13x13.png"))); // NOI18N
        jPanel3.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 10, -1, 20));

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/product.png"))); // NOI18N
        jPanel3.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 10, 30, 20));

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dashboard.png"))); // NOI18N
        jPanel3.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 30, 20));

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/people13x13.png"))); // NOI18N
        jPanel3.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 10, 30, 20));

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/user 13x13.png"))); // NOI18N
        jPanel3.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 10, 30, 20));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 590, 40));

        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(43, 182, 115)));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel9.setText("Orders");
        jPanel9.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jPanel1.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 560, 310));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdminOrders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminOrders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminOrders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminOrders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminOrders().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    // End of variables declaration//GEN-END:variables
}
