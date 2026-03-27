/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ilove
 */
public class AdminEditproducts extends javax.swing.JFrame {

    private final int productId;
    private final AdminProduct adminProductParent;

    /**
     * Creates new form AdminEditproducts
     */
    public AdminEditproducts() {
        this(0, null);
    }

    public AdminEditproducts(int productId, AdminProduct parent) {
        this.productId = productId;
        this.adminProductParent = parent;
        initComponents();
        if (productId > 0) {
            loadProduct();
        }
        jButton1.addActionListener(evt -> onUpdate());
        WindowHelper.sizeAndCenter(this);
    }

    private void loadProduct() {
        String sql = "SELECT product_name, description, category, price, stock_quantity, expirydate FROM product WHERE product_id = ?";
        try (java.sql.Connection c = DBConnection.getConnection();
                java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Product not found.", "Missing",
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }
                jTextField1.setText(rs.getString("product_name"));
                jTextField3.setText(rs.getString("description"));
                jTextField5.setText(rs.getString("category"));
                jTextField6.setText(String.valueOf(rs.getDouble("price")));
                jTextField4.setText(String.valueOf(rs.getInt("stock_quantity")));
                String expRaw = rs.getString("expirydate");
                jTextField2.setText(expRaw != null ? expRaw.trim() : "");
            }
        } catch (java.sql.SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, DBConnection.userMessage(e), "Database error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdate() {
        if (productId <= 0) {
            return;
        }
        String name = jTextField1.getText() != null ? jTextField1.getText().trim() : "";
        String desc = jTextField3.getText() != null ? jTextField3.getText().trim() : "";
        String category = jTextField5.getText() != null ? jTextField5.getText().trim() : "";
        String priceRaw = jTextField6.getText() != null ? jTextField6.getText().trim() : "";
        String stockRaw = jTextField4.getText() != null ? jTextField4.getText().trim() : "";
        String expiryRaw = jTextField2.getText() != null ? jTextField2.getText().trim() : "";

        if (name.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Product name is required.", "Validation",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceRaw.replace(",", "").replace("₱", "").trim());
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Enter a valid price.", "Validation",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            stock = Integer.parseInt(stockRaw);
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Enter a valid stock quantity.", "Validation",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.sql.Date expirySql = null;
        if (expiryRaw != null && !expiryRaw.isEmpty()) {
            try {
                expirySql = java.sql.Date.valueOf(expiryRaw);
            } catch (IllegalArgumentException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Expiry must be YYYY-MM-DD.", "Validation",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String sql = "UPDATE product SET product_name=?, description=?, category=?, price=?, stock_quantity=?, expirydate=? WHERE product_id=?";
        try (java.sql.Connection c = DBConnection.getConnection();
                java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, desc);
            ps.setString(3, category);
            ps.setDouble(4, price);
            ps.setInt(5, stock);
            if (expirySql != null) {
                ps.setDate(6, expirySql);
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            ps.setInt(7, productId);
            ps.executeUpdate();
            javax.swing.JOptionPane.showMessageDialog(this, "Product updated successfully.", "Success",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            if (adminProductParent != null) {
                adminProductParent.reloadProducts();
            }
            dispose();
        } catch (java.sql.SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, DBConnection.userMessage(e), "Database error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Edit Product");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText("Product Name");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jTextField1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.lightGray, java.awt.Color.green, null, null));
        jPanel1.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 450, 40));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel3.setText("Description");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, -1));

        jTextField2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.lightGray, java.awt.Color.green, null, null));
        jPanel1.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 330, 160, 40));

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel5.setText("Category");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, -1));

        jTextField3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.lightGray, java.awt.Color.green, null, null));
        jPanel1.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, 450, 40));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel6.setText("Price");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 210, -1, -1));

        jTextField4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.lightGray, java.awt.Color.green, null, null));
        jPanel1.add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, 160, 40));

        jTextField5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.lightGray, java.awt.Color.green, null, null));
        jPanel1.add(jTextField5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, 160, 40));

        jTextField6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.lightGray, java.awt.Color.green, null, null));
        jPanel1.add(jTextField6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 240, 160, 40));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel7.setText("Stock Quantity");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 310, -1, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel4.setText("Expiry Date");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 310, -1, -1));

        jButton1.setText("Update Product");
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, 460, 40));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            java.util.logging.Logger.getLogger(AdminEditproducts.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminEditproducts.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminEditproducts.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminEditproducts.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminEditproducts().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    // End of variables declaration//GEN-END:variables
}
