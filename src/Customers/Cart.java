package Customers;

/**
 * Simple cart view for customers.
 */
public class Cart extends javax.swing.JFrame {

    private javax.swing.JTable cartTable;
    private javax.swing.JLabel totalLabel;

    public Cart() {
        initComponents();
        initCustom();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cart");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initCustom() {
        WindowHelper.sizeAndCenter(this);

        cartTable = new javax.swing.JTable();
        cartTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"Name", "Price", "Qty", "Subtotal"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(cartTable);

        totalLabel = new javax.swing.JLabel("Total: ₱0.00");

        javax.swing.JButton printButton = new javax.swing.JButton("Print Receipt");
        javax.swing.JButton downloadButton = new javax.swing.JButton("Download Receipt");
        javax.swing.JButton clearButton = new javax.swing.JButton("Clear Cart");

        printButton.addActionListener(e -> onPrint());
        downloadButton.addActionListener(e -> onDownload());
        clearButton.addActionListener(e -> onClear());

        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        bottomPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        bottomPanel.add(totalLabel);
        bottomPanel.add(clearButton);
        bottomPanel.add(printButton);
        bottomPanel.add(downloadButton);

        getContentPane().removeAll();
        getContentPane().setLayout(new java.awt.BorderLayout(5, 5));
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        reloadTable();
        revalidate();
        repaint();
    }

    private void reloadTable() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) cartTable.getModel();
        model.setRowCount(0);
        for (CartStorage.Item it : CartStorage.getItems()) {
            model.addRow(new Object[]{
                it.name,
                String.format("₱%.2f", it.price),
                it.quantity,
                String.format("₱%.2f", it.getSubtotal())
            });
        }
        totalLabel.setText(String.format("Total: ₱%.2f", CartStorage.getTotal()));
    }

    private String buildReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Qualimed Pharmacy\n");
        sb.append("Customer Cart Receipt\n\n");
        for (CartStorage.Item it : CartStorage.getItems()) {
            sb.append(it.name)
                    .append("  x")
                    .append(it.quantity)
                    .append("  @ ₱")
                    .append(String.format("%.2f", it.price))
                    .append("  = ₱")
                    .append(String.format("%.2f", it.getSubtotal()))
                    .append("\n");
        }
        sb.append("\nTotal: ₱").append(String.format("%.2f", CartStorage.getTotal())).append("\n");
        sb.append("Thank you for your purchase!");
        return sb.toString();
    }

    private void onPrint() {
        String text = buildReceiptText();
        javax.swing.JTextArea area = new javax.swing.JTextArea(text);
        try {
            boolean done = area.print();
            if (!done) {
                javax.swing.JOptionPane.showMessageDialog(this, "Print was cancelled.",
                        "Print", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.awt.print.PrinterException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Unable to print: " + ex.getMessage(),
                    "Print error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDownload() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Save Receipt");
        chooser.setSelectedFile(new java.io.File("receipt.txt"));
        int result = chooser.showSaveDialog(this);
        if (result != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = chooser.getSelectedFile();
        try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
            fw.write(buildReceiptText());
            javax.swing.JOptionPane.showMessageDialog(this, "Receipt saved to:\n" + file.getAbsolutePath(),
                    "Saved", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Unable to save receipt: " + ex.getMessage(),
                    "Save error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onClear() {
        CartStorage.clear();
        reloadTable();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new Cart().setVisible(true));
    }
}

