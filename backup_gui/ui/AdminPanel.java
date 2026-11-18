package com.maisonelle.ui;

import com.maisonelle.manager.OrderManager;
import com.maisonelle.manager.ProductManager;
import com.maisonelle.model.CartItem;
import com.maisonelle.model.Order;
import com.maisonelle.model.Product;
import com.maisonelle.util.ImageUtils;
import com.maisonelle.util.InvoiceGenerator;
import com.maisonelle.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/**
 * Modern Administrator Dashboard for Maisonelle:
 * KPI statistics, Product Catalogue CRUD, Order Fulfillment,
 * and Pure AWT Revenue Analytics.
 */
public class AdminPanel extends JPanel {

    private final ProductManager productManager;
    private final OrderManager orderManager;
    private final HomeFrame homeFrame;

    private JTable productTable;
    private DefaultTableModel productTableModel;
    private TableRowSorter<DefaultTableModel> productSorter;
    private JTextField productSearchField;
    private JComboBox<String> productCatFilterCombo;

    private JTable orderTable;
    private DefaultTableModel orderTableModel;
    private TableRowSorter<DefaultTableModel> orderSorter;
    private JComboBox<String> orderStatusFilterCombo;

    private JPanel kpiContainer;
    private JPanel chartPanel;
    private JTable bestSellersTable;
    private DefaultTableModel bestSellersModel;
    private JTable lowStockTable;
    private DefaultTableModel lowStockModel;

    public AdminPanel(ProductManager productManager, OrderManager orderManager, HomeFrame homeFrame) {
        this.productManager = productManager;
        this.orderManager = orderManager;
        this.homeFrame = homeFrame;

        setLayout(new BorderLayout());
        setBackground(Theme.MAIN_BACKGROUND);

        initUI();
    }

    private void initUI() {
        removeAll();

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(20, 28, 12, 28));

        JLabel title = new JLabel("Administrator Dashboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.DARK_TEXT);
        topBar.add(title, BorderLayout.WEST);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setOpaque(false);

        JButton viewStoreBtn = Theme.createPrimaryButton("🛍️ View Storefront");
        viewStoreBtn.addActionListener(e -> {
            if (homeFrame != null) homeFrame.showCatalogue();
        });
        topActions.add(viewStoreBtn);

        JButton refreshBtn = Theme.createSecondaryButton("🔄 Refresh Data");
        refreshBtn.addActionListener(e -> refreshDashboard());
        topActions.add(refreshBtn);

        topBar.add(topActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Container
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 28, 24, 28));

        // Top KPI Cards
        kpiContainer = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiContainer.setOpaque(false);
        center.add(kpiContainer, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_BODY_BOLD);
        tabs.addTab("🛍️ Product Catalogue Management", createProductManagementTab());
        tabs.addTab("📦 Customer Orders & Fulfillment", createOrderManagementTab());
        tabs.addTab("📊 Sales Analytics & Inventory Insights", createAnalyticsTab());

        center.add(tabs, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        refreshDashboard();
    }

    public void refreshDashboard() {
        refreshKPICards();
        refreshProductTable();
        refreshOrderTable();
        refreshInsightsTables();
        if (chartPanel != null) chartPanel.repaint();
    }

    private void refreshKPICards() {
        if (kpiContainer == null) return;
        kpiContainer.removeAll();

        BigDecimal totalSales = orderManager.getTotalSales();
        int totalOrders = orderManager.getTotalOrdersCount();
        int totalProducts = productManager.getTotalProductsCount();
        int lowStockCount = productManager.getLowStockProducts(10).size();

        kpiContainer.add(createKPICard("Total Sales Revenue", Theme.formatRupee(totalSales), "💰", Theme.MAIN_HEADINGS));
        kpiContainer.add(createKPICard("Total Orders Placed", String.valueOf(totalOrders), "📦", Theme.SUCCESS));
        kpiContainer.add(createKPICard("Active Catalogue Pieces", String.valueOf(totalProducts), "🏷️", Theme.PRIMARY_BUTTON));
        kpiContainer.add(createKPICard("Low Stock Items (<=10)", String.valueOf(lowStockCount), "⚠️",
                lowStockCount > 0 ? Theme.DANGER : Theme.MUTED_TEXT));

        kpiContainer.revalidate();
        kpiContainer.repaint();
    }

    private JPanel createKPICard(String title, String value, String icon, Color accent) {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout());

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_SMALL_BOLD);
        t.setForeground(Theme.MUTED_TEXT);
        left.add(t);

        left.add(Box.createVerticalStrut(4));

        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 19));
        v.setForeground(Theme.DARK_TEXT);
        left.add(v);

        card.add(left, BorderLayout.CENTER);

        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("SansSerif", Font.PLAIN, 24));
        ic.setForeground(accent);
        card.add(ic, BorderLayout.EAST);

        return card;
    }

    private JPanel createProductManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 12, 12, 12));

        JPanel topControls = new JPanel();
        topControls.setLayout(new BoxLayout(topControls, BoxLayout.Y_AXIS));
        topControls.setOpaque(false);

        // Action Buttons Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton addBtn = Theme.createPrimaryButton("+ Add New Piece");
        addBtn.addActionListener(e -> showAddProductDialog());
        toolbar.add(addBtn);

        JButton editBtn = Theme.createSecondaryButton("✏️ Edit Selected");
        editBtn.addActionListener(e -> handleEditSelectedProduct());
        toolbar.add(editBtn);

        JButton stockBtn = Theme.createSecondaryButton("📦 Update Stock");
        stockBtn.addActionListener(e -> handleUpdateStock());
        toolbar.add(stockBtn);

        JButton priceBtn = Theme.createSecondaryButton("💲 Update Price");
        priceBtn.addActionListener(e -> handleUpdatePrice());
        toolbar.add(priceBtn);

        JButton delBtn = Theme.createDangerButton("🗑 Delete Piece");
        delBtn.addActionListener(e -> handleDeleteProduct());
        toolbar.add(delBtn);

        topControls.add(toolbar);
        topControls.add(Box.createVerticalStrut(10));

        // Search & Category Filter Row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        filterRow.add(searchIcon);

        productSearchField = Theme.createTextField(18);
        productSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyProductFilter();
            }
        });
        filterRow.add(productSearchField);

        JLabel catLbl = new JLabel("Category:");
        catLbl.setFont(Theme.FONT_SMALL_BOLD);
        catLbl.setForeground(Theme.MUTED_TEXT);
        filterRow.add(catLbl);

        productCatFilterCombo = new JComboBox<>(new String[]{
                "All Categories", "Electronics", "Fashion", "Home & Living", "Accessories", "Beauty & Lifestyle"
        });
        productCatFilterCombo.setFont(Theme.FONT_BODY);
        productCatFilterCombo.setBackground(Color.WHITE);
        productCatFilterCombo.addActionListener(e -> applyProductFilter());
        filterRow.add(productCatFilterCombo);

        JButton resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            productSearchField.setText("");
            productCatFilterCombo.setSelectedIndex(0);
            applyProductFilter();
        });
        filterRow.add(resetBtn);

        topControls.add(filterRow);
        panel.add(topControls, BorderLayout.NORTH);

        // Product Table
        String[] cols = {"ID", "Name", "Category", "Brand", "Price", "Original", "Discount", "Stock", "Rating", "Featured"};
        productTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        productTable = new JTable(productTableModel);
        productTable.setFont(Theme.FONT_BODY);
        productTable.setRowHeight(32);
        productTable.getTableHeader().setFont(Theme.FONT_HEADER);
        productTable.getTableHeader().setBackground(Theme.SECONDARY_BACKGROUND);
        productTable.getTableHeader().setForeground(Theme.DARK_TEXT);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        productSorter = new TableRowSorter<>(productTableModel);
        productTable.setRowSorter(productSorter);

        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleEditSelectedProduct();
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer);

        JScrollPane sp = new JScrollPane(productTable);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDERS));
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private void applyProductFilter() {
        if (productSorter == null) return;
        String query = productSearchField != null ? productSearchField.getText().trim() : "";
        String cat = productCatFilterCombo != null ? (String) productCatFilterCombo.getSelectedItem() : "All Categories";

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query), 1, 3));
        }
        if (cat != null && !cat.equalsIgnoreCase("All Categories")) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(cat) + "$", 2));
        }

        if (filters.isEmpty()) {
            productSorter.setRowFilter(null);
        } else {
            productSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void refreshProductTable() {
        if (productTableModel == null) return;
        productTableModel.setRowCount(0);
        List<Product> list = productManager.getAllProducts();
        for (Product p : list) {
            String discountStr = p.hasDiscount() ? p.getDiscountPercentage() + "% OFF" : "None";
            productTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getCategory(),
                    p.getBrand(),
                    Theme.formatRupee(p.getPrice()),
                    Theme.formatRupee(p.getOriginalPrice()),
                    discountStr,
                    p.getStock(),
                    "★ " + p.getRating(),
                    p.isFeatured() ? "Yes" : "No"
            });
        }
    }

    private Product getSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product from the table first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = productTable.convertRowIndexToModel(row);
        int id = (int) productTableModel.getValueAt(modelRow, 0);
        return productManager.getProductById(id);
    }

    private void showAddProductDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Boutique Piece", true);
        dialog.setSize(520, 640);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField nameF = Theme.createTextField(20);
        JComboBox<String> catCombo = new JComboBox<>(new String[]{"Electronics", "Fashion", "Home & Living", "Accessories", "Beauty & Lifestyle"});
        JTextField brandF = Theme.createTextField(20);
        JTextField priceF = Theme.createTextField(20);
        JTextField origPriceF = Theme.createTextField(20);
        JTextField stockF = Theme.createTextField(20);
        JTextArea descA = new JTextArea(3, 20);
        descA.setBorder(BorderFactory.createLineBorder(Theme.BORDERS));
        descA.setLineWrap(true);
        descA.setWrapStyleWord(true);
        JCheckBox featC = new JCheckBox("Mark as Featured Piece");

        // Image Selection Row with Live Preview Thumbnail
        JComboBox<String> imageCombo = new JComboBox<>(new String[]{
                "sony_headphones.jpg", "jbl_speaker.jpg", "apple_watch.jpg", "mechanical_keyboard.jpg",
                "logitech_mouse.jpg", "nike_sneakers.jpg", "casual_shoes.jpg", "champion_hoodie.jpg",
                "table_lamp.jpg", "coffee_mug.jpg", "bedsheet.jpg", "laptop_bag.jpg", "skincare_kit.jpg"
        });
        imageCombo.setBackground(Color.WHITE);

        JLabel imgPreview = new JLabel();
        imgPreview.setPreferredSize(new Dimension(80, 80));
        imgPreview.setBorder(BorderFactory.createLineBorder(Theme.BORDERS));
        imgPreview.setHorizontalAlignment(SwingConstants.CENTER);
        imgPreview.setBackground(Color.WHITE);
        imgPreview.setOpaque(true);
        imgPreview.setIcon(ImageUtils.loadProductImage("sony_headphones.jpg", 76, 76));

        imageCombo.addActionListener(e -> {
            String sel = (String) imageCombo.getSelectedItem();
            imgPreview.setIcon(ImageUtils.loadProductImage(sel, 76, 76));
        });

        JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        imgRow.setOpaque(false);
        imgRow.add(imgPreview);
        imgRow.add(imageCombo);

        form.add(new JLabel("Product Name:")); form.add(nameF); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Category:")); form.add(catCombo); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Brand:")); form.add(brandF); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Price (₹):")); form.add(priceF); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Original Price (₹, optional for discount):")); form.add(origPriceF); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Available Stock:")); form.add(stockF); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Product Image:")); form.add(imgRow); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Description:")); form.add(new JScrollPane(descA)); form.add(Box.createVerticalStrut(6));
        form.add(featC); form.add(Box.createVerticalStrut(14));

        JButton saveBtn = Theme.createPrimaryButton("Save Product to Catalogue");
        saveBtn.addActionListener(e -> {
            try {
                String name = nameF.getText().trim();
                String cat = (String) catCombo.getSelectedItem();
                String brand = brandF.getText().trim();
                BigDecimal price = new BigDecimal(priceF.getText().trim());
                String origStr = origPriceF.getText().trim();
                BigDecimal origPrice = origStr.isEmpty() ? price : new BigDecimal(origStr);
                int stock = Integer.parseInt(stockF.getText().trim());
                String desc = descA.getText().trim();
                boolean feat = featC.isSelected();
                String imgName = (String) imageCombo.getSelectedItem();

                if (name.isEmpty() || brand.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name and brand are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Product p = new Product(0, name, cat, desc, price, origPrice, stock, imgName, 4.8, brand, feat);
                productManager.addProduct(p);
                dialog.dispose();
                refreshDashboard();
                if (homeFrame != null) {
                    homeFrame.showToast("Product Added: " + p.getName());
                    homeFrame.refreshCatalogue();
                }
                JOptionPane.showMessageDialog(this, "Product added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid numeric input: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(saveBtn);
        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void handleEditSelectedProduct() {
        Product p = getSelectedProduct();
        if (p == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Product: " + p.getName(), true);
        dialog.setSize(520, 640);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField nameF = Theme.createTextField(20); nameF.setText(p.getName());
        JComboBox<String> catCombo = new JComboBox<>(new String[]{"Electronics", "Fashion", "Home & Living", "Accessories", "Beauty & Lifestyle"});
        catCombo.setSelectedItem(p.getCategory());
        JTextField brandF = Theme.createTextField(20); brandF.setText(p.getBrand());
        JTextField priceF = Theme.createTextField(20); priceF.setText(p.getPrice().toString());
        JTextField origPriceF = Theme.createTextField(20); origPriceF.setText(p.getOriginalPrice().toString());
        JTextField stockF = Theme.createTextField(20); stockF.setText(String.valueOf(p.getStock()));
        JTextArea descA = new JTextArea(3, 20); descA.setText(p.getDescription());
        descA.setBorder(BorderFactory.createLineBorder(Theme.BORDERS));
        descA.setLineWrap(true);
        descA.setWrapStyleWord(true);
        JCheckBox featC = new JCheckBox("Featured Product", p.isFeatured());

        JLabel imgPreview = new JLabel();
        imgPreview.setPreferredSize(new Dimension(80, 80));
        imgPreview.setBorder(BorderFactory.createLineBorder(Theme.BORDERS));
        imgPreview.setHorizontalAlignment(SwingConstants.CENTER);
        imgPreview.setBackground(Color.WHITE);
        imgPreview.setOpaque(true);
        imgPreview.setIcon(ImageUtils.loadProductImage(p.getImagePath(), 76, 76));

        JComboBox<String> imageCombo = new JComboBox<>(new String[]{
                p.getImagePath(), "sony_headphones.jpg", "jbl_speaker.jpg", "apple_watch.jpg",
                "mechanical_keyboard.jpg", "logitech_mouse.jpg", "nike_sneakers.jpg", "table_lamp.jpg"
        });
        imageCombo.setBackground(Color.WHITE);
        imageCombo.addActionListener(e -> {
            String sel = (String) imageCombo.getSelectedItem();
            imgPreview.setIcon(ImageUtils.loadProductImage(sel, 76, 76));
        });

        JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        imgRow.setOpaque(false);
        imgRow.add(imgPreview);
        imgRow.add(imageCombo);

        form.add(new JLabel("Product Name:")); form.add(nameF); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Category:")); form.add(catCombo); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Brand:")); form.add(brandF); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Price (₹):")); form.add(priceF); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Original Price (₹):")); form.add(origPriceF); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Available Stock:")); form.add(stockF); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Product Image:")); form.add(imgRow); form.add(Box.createVerticalStrut(6));
        form.add(new JLabel("Description:")); form.add(new JScrollPane(descA)); form.add(Box.createVerticalStrut(6));
        form.add(featC); form.add(Box.createVerticalStrut(14));

        JButton updateBtn = Theme.createPrimaryButton("Update Product Details");
        updateBtn.addActionListener(e -> {
            try {
                p.setName(nameF.getText().trim());
                p.setCategory((String) catCombo.getSelectedItem());
                p.setBrand(brandF.getText().trim());
                p.setPrice(new BigDecimal(priceF.getText().trim()));
                String origStr = origPriceF.getText().trim();
                if (!origStr.isEmpty()) p.setOriginalPrice(new BigDecimal(origStr));
                p.setStock(Integer.parseInt(stockF.getText().trim()));
                p.setImagePath((String) imageCombo.getSelectedItem());
                p.setDescription(descA.getText().trim());
                p.setFeatured(featC.isSelected());

                productManager.updateProduct(p);
                dialog.dispose();
                refreshDashboard();
                if (homeFrame != null) {
                    homeFrame.showToast("Product Updated: " + p.getName());
                    homeFrame.refreshCatalogue();
                }
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(updateBtn);
        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void handleUpdateStock() {
        Product p = getSelectedProduct();
        if (p == null) return;

        String input = JOptionPane.showInputDialog(this,
                "Enter new stock quantity for " + p.getName() + " (Current: " + p.getStock() + "):",
                p.getStock());
        if (input != null && !input.trim().isEmpty()) {
            try {
                int stock = Integer.parseInt(input.trim());
                productManager.updateStock(p.getId(), stock);
                refreshDashboard();
                if (homeFrame != null) {
                    homeFrame.showToast("Stock updated for: " + p.getName());
                    homeFrame.refreshCatalogue();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number entered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleUpdatePrice() {
        Product p = getSelectedProduct();
        if (p == null) return;

        String input = JOptionPane.showInputDialog(this,
                "Enter new price (₹) for " + p.getName() + " (Current: " + p.getPrice() + "):",
                p.getPrice());
        if (input != null && !input.trim().isEmpty()) {
            try {
                BigDecimal price = new BigDecimal(input.trim());
                productManager.updatePrice(p.getId(), price);
                refreshDashboard();
                if (homeFrame != null) {
                    homeFrame.showToast("Price updated for: " + p.getName());
                    homeFrame.refreshCatalogue();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid price entered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteProduct() {
        Product p = getSelectedProduct();
        if (p == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + p.getName() + "'?\nThis change cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            productManager.deleteProduct(p.getId());
            refreshDashboard();
            if (homeFrame != null) {
                homeFrame.showToast("Deleted " + p.getName());
                homeFrame.refreshCatalogue();
            }
            JOptionPane.showMessageDialog(this, "Product deleted.");
        }
    }

    private JPanel createOrderManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton statusBtn = Theme.createPrimaryButton("Update Fulfillment Status");
        statusBtn.addActionListener(e -> handleUpdateOrderStatus());
        toolbar.add(statusBtn);

        JButton invoiceBtn = Theme.createSecondaryButton("📄 View Invoice (.txt)");
        invoiceBtn.addActionListener(e -> handleOpenOrderInvoice());
        toolbar.add(invoiceBtn);

        toolbar.add(Box.createHorizontalStrut(14));

        JLabel filterLbl = new JLabel("Status Filter:");
        filterLbl.setFont(Theme.FONT_SMALL_BOLD);
        filterLbl.setForeground(Theme.MUTED_TEXT);
        toolbar.add(filterLbl);

        orderStatusFilterCombo = new JComboBox<>(new String[]{
                "All Orders", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
        });
        orderStatusFilterCombo.setFont(Theme.FONT_BODY);
        orderStatusFilterCombo.setBackground(Color.WHITE);
        orderStatusFilterCombo.addActionListener(e -> applyOrderFilter());
        toolbar.add(orderStatusFilterCombo);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Date", "Customer", "Items", "Subtotal", "Discount", "Grand Total", "Status"};
        orderTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        orderTable = new JTable(orderTableModel);
        orderTable.setFont(Theme.FONT_BODY);
        orderTable.setRowHeight(32);
        orderTable.getTableHeader().setFont(Theme.FONT_HEADER);
        orderTable.getTableHeader().setBackground(Theme.SECONDARY_BACKGROUND);
        orderTable.getTableHeader().setForeground(Theme.DARK_TEXT);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        orderSorter = new TableRowSorter<>(orderTableModel);
        orderTable.setRowSorter(orderSorter);

        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleUpdateOrderStatus();
            }
        });

        JScrollPane sp = new JScrollPane(orderTable);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDERS));
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private void applyOrderFilter() {
        if (orderSorter == null) return;
        String status = orderStatusFilterCombo != null ? (String) orderStatusFilterCombo.getSelectedItem() : "All Orders";
        if (status == null || status.equalsIgnoreCase("All Orders")) {
            orderSorter.setRowFilter(null);
        } else {
            orderSorter.setRowFilter(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(status) + "$", 7));
        }
    }

    private void refreshOrderTable() {
        if (orderTableModel == null) return;
        orderTableModel.setRowCount(0);
        List<Order> list = orderManager.getAllOrders();
        for (Order o : list) {
            orderTableModel.addRow(new Object[]{
                    o.getOrderId(),
                    o.getFormattedDate(),
                    o.getCustomerName(),
                    o.getTotalItemQuantity() + " pieces",
                    Theme.formatRupee(o.getSubtotal()),
                    "- " + Theme.formatRupee(o.getDiscountAmount()),
                    Theme.formatRupee(o.getTotalAmount()),
                    o.getStatus()
            });
        }
    }

    private Order getSelectedOrder() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an order from the table first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = orderTable.convertRowIndexToModel(row);
        String oid = (String) orderTableModel.getValueAt(modelRow, 0);
        return orderManager.getOrderById(oid);
    }

    private void handleUpdateOrderStatus() {
        Order o = getSelectedOrder();
        if (o == null) return;

        Order.Status[] statuses = Order.Status.values();
        Order.Status chosen = (Order.Status) JOptionPane.showInputDialog(this,
                "Select new fulfillment status for Order " + o.getOrderId() + ":",
                "Update Order Status", JOptionPane.PLAIN_MESSAGE, null, statuses, o.getStatus());

        if (chosen != null && chosen != o.getStatus()) {
            orderManager.updateOrderStatus(o.getOrderId(), chosen);
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "Order status updated to " + chosen);
        }
    }

    private void handleOpenOrderInvoice() {
        Order o = getSelectedOrder();
        if (o == null) return;

        File f = null;
        if (o.getInvoiceFilePath() != null) f = new File(o.getInvoiceFilePath());
        if (f == null || !f.exists()) {
            try {
                InvoiceGenerator gen = new InvoiceGenerator(new File("invoices"));
                f = gen.generateInvoiceFile(o);
                o.setInvoiceFilePath(f.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generating invoice: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        InvoiceGenerator.openInvoiceFile(f);
    }

    private JPanel createAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Custom Swing Sales Revenue Bar Chart
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(Theme.CARD_BACKGROUND);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
                g2.setColor(Theme.BORDERS);
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 14, 14));

                g2.setColor(Theme.DARK_TEXT);
                g2.setFont(Theme.FONT_SUBTITLE);
                g2.drawString("Sales Revenue by Category (₹)", 20, 28);

                Map<String, BigDecimal> catTotals = new LinkedHashMap<>();
                for (String c : productManager.getCategories()) {
                    if (!c.equalsIgnoreCase("All")) catTotals.put(c, BigDecimal.ZERO);
                }

                for (Order o : orderManager.getAllOrders()) {
                    for (CartItem item : o.getItems()) {
                        String cat = item.getProduct().getCategory();
                        BigDecimal cur = catTotals.getOrDefault(cat, BigDecimal.ZERO);
                        catTotals.put(cat, cur.add(item.getSubtotal()));
                    }
                }

                BigDecimal maxVal = catTotals.values().stream().max(BigDecimal::compareTo).orElse(new BigDecimal("10000"));
                if (maxVal.compareTo(BigDecimal.ZERO) == 0) maxVal = new BigDecimal("10000");

                int bottom = h - 45;
                int top = 55;
                int barAreaH = bottom - top;

                g2.setColor(Theme.BORDERS);
                g2.drawLine(30, bottom, w - 30, bottom);

                String[] cats = catTotals.keySet().toArray(new String[0]);
                int count = cats.length;
                if (count > 0) {
                    int slotW = (w - 60) / count;
                    int barW = Math.min(54, slotW - 24);

                    Color[] colors = {Theme.PRIMARY_BUTTON, Theme.MAIN_HEADINGS, Theme.SOFT_ACCENT, Theme.SUCCESS, Theme.SECONDARY_ACCENT};

                    for (int i = 0; i < count; i++) {
                        String cat = cats[i];
                        BigDecimal amt = catTotals.get(cat);
                        double ratio = amt.doubleValue() / maxVal.doubleValue();
                        int barH = (int) (ratio * (barAreaH - 20));
                        int x = 30 + (i * slotW) + (slotW - barW) / 2;
                        int y = bottom - barH;

                        g2.setColor(colors[i % colors.length]);
                        g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, 6, 6));

                        g2.setColor(Theme.DARK_TEXT);
                        g2.setFont(Theme.FONT_SMALL_BOLD);
                        String valStr = Theme.formatRupee(amt);
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(valStr, x + (barW - fm.stringWidth(valStr)) / 2, y - 5);

                        g2.setColor(Theme.MUTED_TEXT);
                        g2.setFont(Theme.FONT_SMALL);
                        FontMetrics fmLbl = g2.getFontMetrics();
                        g2.drawString(cat, x + (barW - fmLbl.stringWidth(cat)) / 2, bottom + 18);
                    }
                }
                g2.dispose();
            }
        };
        chartPanel.setPreferredSize(new Dimension(800, 220));
        panel.add(chartPanel, BorderLayout.NORTH);

        // Lower split tables: Top Bestsellers & Low Stock Alerts
        JPanel lower = new JPanel(new GridLayout(1, 2, 14, 0));
        lower.setOpaque(false);

        // Bestsellers Box
        JPanel bestBox = Theme.createCardPanel();
        bestBox.setLayout(new BorderLayout(0, 6));
        JLabel bestTitle = new JLabel("🔥 Top Rated Pieces");
        bestTitle.setFont(Theme.FONT_HEADER);
        bestTitle.setForeground(Theme.DARK_TEXT);
        bestBox.add(bestTitle, BorderLayout.NORTH);

        bestSellersModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Rating", "Stock", "Price"}, 0);
        bestSellersTable = new JTable(bestSellersModel);
        bestSellersTable.setFont(Theme.FONT_SMALL);
        bestSellersTable.setRowHeight(24);
        bestBox.add(new JScrollPane(bestSellersTable), BorderLayout.CENTER);
        lower.add(bestBox);

        // Low Stock Box
        JPanel lowBox = Theme.createCardPanel();
        lowBox.setLayout(new BorderLayout(0, 6));
        JLabel lowTitle = new JLabel("⚠️ Low-Stock Inventory Alerts");
        lowTitle.setFont(Theme.FONT_HEADER);
        lowTitle.setForeground(Theme.DANGER);
        lowBox.add(lowTitle, BorderLayout.NORTH);

        lowStockModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Current Stock", "Status"}, 0);
        lowStockTable = new JTable(lowStockModel);
        lowStockTable.setFont(Theme.FONT_SMALL);
        lowStockTable.setRowHeight(24);
        lowStockTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = lowStockTable.getSelectedRow();
                    if (r >= 0) {
                        int id = (int) lowStockModel.getValueAt(r, 0);
                        Product p = productManager.getProductById(id);
                        if (p != null) {
                            String in = JOptionPane.showInputDialog(AdminPanel.this,
                                    "Enter new stock quantity for " + p.getName() + " (Current: " + p.getStock() + "):",
                                    p.getStock());
                            if (in != null && !in.trim().isEmpty()) {
                                try {
                                    productManager.updateStock(id, Integer.parseInt(in.trim()));
                                    refreshDashboard();
                                    if (homeFrame != null) {
                                        homeFrame.showToast("Restocked " + p.getName());
                                        homeFrame.refreshCatalogue();
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
            }
        });
        lowBox.add(new JScrollPane(lowStockTable), BorderLayout.CENTER);
        lower.add(lowBox);

        panel.add(lower, BorderLayout.CENTER);
        return panel;
    }

    private void refreshInsightsTables() {
        if (bestSellersModel != null) {
            bestSellersModel.setRowCount(0);
            List<Product> top = productManager.getAllProducts().stream()
                    .sorted((p1, p2) -> Double.compare(p2.getRating(), p1.getRating()))
                    .limit(8)
                    .toList();
            for (Product p : top) {
                bestSellersModel.addRow(new Object[]{p.getId(), p.getName(), "★ " + p.getRating(), p.getStock(), Theme.formatRupee(p.getPrice())});
            }
        }

        if (lowStockModel != null) {
            lowStockModel.setRowCount(0);
            List<Product> low = productManager.getLowStockProducts(12);
            for (Product p : low) {
                String st = p.getStock() <= 0 ? "OUT OF STOCK" : "CRITICAL (" + p.getStock() + " left)";
                lowStockModel.addRow(new Object[]{p.getId(), p.getName(), p.getStock(), st});
            }
        }
    }
}
