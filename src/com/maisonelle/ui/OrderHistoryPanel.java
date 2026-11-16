package com.maisonelle.ui;

import com.maisonelle.manager.OrderManager;
import com.maisonelle.manager.UserManager;
import com.maisonelle.model.CartItem;
import com.maisonelle.model.Customer;
import com.maisonelle.model.Order;
import com.maisonelle.util.InvoiceGenerator;
import com.maisonelle.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

/**
 * Customer Order History Panel displaying past purchases,
 * fulfillment status, and text invoice access.
 */
public class OrderHistoryPanel extends JPanel {

    private final OrderManager orderManager;
    private final UserManager userManager;
    private final HomeFrame homeFrame;

    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JLabel orderCountBadge;

    public OrderHistoryPanel(OrderManager orderManager, UserManager userManager, HomeFrame homeFrame) {
        this.orderManager = orderManager;
        this.userManager = userManager;
        this.homeFrame = homeFrame;

        setLayout(new BorderLayout());
        setBackground(Theme.MAIN_BACKGROUND);

        initUI();
    }

    private void initUI() {
        removeAll();

        // Top Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(20, 32, 14, 32));

        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleGroup.setOpaque(false);

        JLabel title = new JLabel("Your Order History");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.DARK_TEXT);
        titleGroup.add(title);

        orderCountBadge = Theme.createBadge("0 Orders", Theme.SECONDARY_BACKGROUND, Theme.DARK_TEXT);
        titleGroup.add(orderCountBadge);
        topBar.add(titleGroup, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton invoiceBtn = Theme.createPrimaryButton("📄 View Invoice (.txt)");
        invoiceBtn.addActionListener(e -> handleOpenSelectedInvoice());
        rightActions.add(invoiceBtn);

        JButton shopBtn = Theme.createSecondaryButton("← Back to Catalogue");
        shopBtn.addActionListener(e -> {
            if (homeFrame != null) homeFrame.showCatalogue();
        });
        rightActions.add(shopBtn);

        topBar.add(rightActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(6, 32, 28, 32));

        String[] cols = {"Order ID", "Date", "Items", "Payment", "Final Amount", "Fulfillment Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        ordersTable = new JTable(tableModel);
        ordersTable.setFont(Theme.FONT_BODY);
        ordersTable.setRowHeight(36);
        ordersTable.getTableHeader().setFont(Theme.FONT_HEADER);
        ordersTable.getTableHeader().setBackground(Theme.SECONDARY_BACKGROUND);
        ordersTable.getTableHeader().setForeground(Theme.DARK_TEXT);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        ordersTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        ordersTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        ordersTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        ordersTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        ordersTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleOpenSelectedInvoice();
                }
            }
        });

        JScrollPane sp = new JScrollPane(ordersTable);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDERS));
        tableWrapper.add(sp, BorderLayout.CENTER);

        add(tableWrapper, BorderLayout.CENTER);

        refreshOrders();
    }

    public void refreshOrders() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        Customer current = userManager.getCurrentUser();
        if (current == null) return;

        List<Order> list = current.isAdmin()
                ? orderManager.getAllOrders()
                : orderManager.getOrdersForCustomer(current.getId());

        if (orderCountBadge != null) {
            orderCountBadge.setText(list.size() + (list.size() == 1 ? " Order" : " Orders"));
        }

        for (Order o : list) {
            tableModel.addRow(new Object[]{
                    o.getOrderId(),
                    o.getFormattedDate(),
                    o.getTotalItemQuantity() + " pieces",
                    o.getPaymentMethod(),
                    Theme.formatRupee(o.getTotalAmount()),
                    o.getStatus()
            });
        }
    }

    private void handleOpenSelectedInvoice() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an order from the table to view its invoice.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(row, 0);
        Order order = orderManager.getOrderById(orderId);
        if (order == null) return;

        File f = null;
        if (order.getInvoiceFilePath() != null) {
            f = new File(order.getInvoiceFilePath());
        }
        if (f == null || !f.exists()) {
            try {
                InvoiceGenerator gen = new InvoiceGenerator(new File("invoices"));
                f = gen.generateInvoiceFile(order);
                order.setInvoiceFilePath(f.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not generate invoice file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        InvoiceGenerator.openInvoiceFile(f);
    }
}
