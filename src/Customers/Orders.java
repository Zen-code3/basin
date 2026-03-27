package Customers;

public class Orders extends javax.swing.JFrame {

    private javax.swing.JTable ordersTable;

    public Orders() {
        initComponents();
        initCustom();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("My Orders");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 650, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 450, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initCustom() {
        WindowHelper.sizeAndCenter(this);

        ordersTable = new javax.swing.JTable();
        ordersTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"Order No", "Date", "Total", "Status"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(ordersTable);

        javax.swing.JButton backButton = new javax.swing.JButton("Back");
        javax.swing.JButton printButton = new javax.swing.JButton("Print Receipt");
        javax.swing.JButton downloadButton = new javax.swing.JButton("Download Receipt");
        javax.swing.JButton refreshButton = new javax.swing.JButton("Refresh");

        backButton.addActionListener(e -> dispose());
        refreshButton.addActionListener(e -> reloadOrders());
        printButton.addActionListener(e -> onPrint());
        downloadButton.addActionListener(e -> onDownload());

        javax.swing.JPanel bottom = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        bottom.add(backButton);
        bottom.add(refreshButton);
        bottom.add(printButton);
        bottom.add(downloadButton);

        getContentPane().removeAll();
        getContentPane().setLayout(new java.awt.BorderLayout(5, 5));
        getContentPane().add(new javax.swing.JLabel("My Orders"), java.awt.BorderLayout.NORTH);
        getContentPane().add(scroll, java.awt.BorderLayout.CENTER);
        getContentPane().add(bottom, java.awt.BorderLayout.SOUTH);

        reloadOrders();
        revalidate();
        repaint();
    }

    private void reloadOrders() {
        Integer customerId = CustomerSession.getCurrentCustomerId();
        if (customerId == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please login again.", "Orders",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) ordersTable.getModel();
        model.setRowCount(0);
        try {
            for (OrdersRepository.OrderRow o : OrdersRepository.listOrdersForCustomer(customerId)) {
                model.addRow(new Object[]{
                    o.orderId,
                    o.orderDate != null ? o.orderDate : "",
                    String.format("₱%.2f", o.totalAmount),
                    o.status != null ? o.status : ""
                });
            }
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, DBConnection.userMessage(ex), "Database error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer getSelectedOrderId() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select an order first.", "Orders",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (int) ordersTable.getModel().getValueAt(row, 0);
    }

    private void onPrint() {
        Integer orderId = getSelectedOrderId();
        if (orderId == null) {
            return;
        }
        try {
            String text = OrdersRepository.buildReceiptText(orderId);
            javax.swing.JTextArea area = new javax.swing.JTextArea(text);
            boolean done = area.print();
            if (!done) {
                javax.swing.JOptionPane.showMessageDialog(this, "Print was cancelled.",
                        "Print", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.awt.print.PrinterException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Unable to print: " + ex.getMessage(),
                    "Print error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, DBConnection.userMessage(ex), "Database error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDownload() {
        Integer orderId = getSelectedOrderId();
        if (orderId == null) {
            return;
        }
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Save Receipt");
        chooser.setSelectedFile(new java.io.File("order-" + orderId + "-receipt.txt"));
        int result = chooser.showSaveDialog(this);
        if (result != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = chooser.getSelectedFile();
        try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
            fw.write(OrdersRepository.buildReceiptText(orderId));
            javax.swing.JOptionPane.showMessageDialog(this, "Receipt saved to:\n" + file.getAbsolutePath(),
                    "Saved", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Unable to save receipt: " + ex.getMessage(),
                    "Save error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, DBConnection.userMessage(ex), "Database error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new Orders().setVisible(true));
    }
}

