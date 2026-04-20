import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class TransportGUI {

    // Database configurations
    static final String DB_URL = "jdbc:mysql://localhost:3306/transport?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "dbms";

    JFrame frame;

    // Theme Colors & Fonts
    Color PRIMARY_COLOR = new Color(0, 102, 204); // Darker Blue
    Color DANGER_COLOR = new Color(204, 0, 0); // Darker Red for Delete
    Color DOWNLOAD_COLOR = new Color(0, 128, 43); // Darker Green
    Color HEADER_BG = new Color(245, 245, 245);
    Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public TransportGUI() {
        // Apply System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("OptionPane.messageFont", MAIN_FONT);
            UIManager.put("OptionPane.buttonFont", MAIN_FONT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Transport Management System");
        frame.setSize(1050, 650);
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setLayout(new BorderLayout());

        // Top Banner
        JPanel banner = new JPanel();
        banner.setBackground(PRIMARY_COLOR);
        banner.setPreferredSize(new Dimension(1000, 70));
        banner.setLayout(new GridBagLayout()); // For centering
        JLabel title = new JLabel("🚗 Transport Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        banner.add(title);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(Color.WHITE);
        tabs.addTab("  🚌 Vehicle Records  ", vehiclePanel());
        tabs.addTab("  👨‍✈️ Driver Records  ", driverPanel());
        tabs.addTab("  🛣️ Trip Records  ", tripPanel());

        frame.add(banner, BorderLayout.NORTH);
        frame.add(tabs, BorderLayout.CENTER);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // Utility Method to Style Buttons
    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        
        // Ensure background color is rendered accurately across LookAndFeels
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Utility Method to Style Tables
    private void styleTable(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(32);
        table.setSelectionBackground(new Color(187, 222, 251)); // Light Blue
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(224, 224, 224));

        JTableHeader header = table.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(HEADER_BG);
        header.setForeground(Color.DARK_GRAY);
        header.setPreferredSize(new Dimension(100, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // Utility Method to Download CSV
    private void downloadCSV(JTable table, String fileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as CSV");
        fileChooser.setSelectedFile(new File(fileName + "_Report.csv"));

        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // enforce csv extension
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }
            try (FileWriter fw = new FileWriter(fileToSave)) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                
                // Write Headers
                for (int i = 0; i < model.getColumnCount(); i++) {
                    fw.write(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");

                // Write Data rows
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object val = model.getValueAt(i, j);
                        String strVal = val != null ? val.toString().replace(",", " ") : ""; // Prevent CSV breaking
                        fw.write(strVal);
                        if (j < model.getColumnCount() - 1) fw.write(",");
                    }
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(frame, "Data successfully downloaded to:\n" + fileToSave.getAbsolutePath(), "Download Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ================= VEHICLE =================
    JPanel vehiclePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Vehicle ID", "Model", "Type", "Capacity"}, 0);
        JTable table = new JTable(model);
        styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton load = new JButton("🔄 Refresh Data");
        JButton add = new JButton("➕ Add Vehicle");
        JButton update = new JButton("✏️ Update Selected");
        JButton delete = new JButton("🗑️ Delete Selected");
        JButton download = new JButton("📥 Download CSV");

        styleButton(load, PRIMARY_COLOR);
        styleButton(add, PRIMARY_COLOR);
        styleButton(update, PRIMARY_COLOR);
        styleButton(delete, DANGER_COLOR);
        styleButton(download, DOWNLOAD_COLOR);

        btnPanel.add(load); btnPanel.add(add); btnPanel.add(update); btnPanel.add(delete); btnPanel.add(download);

        // Add Listeners
        download.addActionListener(e -> downloadCSV(table, "Vehicles"));

        load.addActionListener(e -> {
            model.setRowCount(0);
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS);
                 Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery("SELECT * FROM vehicle");
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("vehicle_id"), rs.getString("model"),
                        rs.getString("vehicle_type"), rs.getInt("capacity")
                    });
                }
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(null, "Error loading data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField modelField = new JTextField();
            JTextField typeField = new JTextField();
            JTextField capacityField = new JTextField();
            
            Object[] fields = { "Vehicle ID (e.g. V101):", idField, "Model:", modelField, "Type (e.g. Bus/Truck):", typeField, "Capacity:", capacityField };
            int option = JOptionPane.showConfirmDialog(null, fields, "➕ Add New Vehicle", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("INSERT INTO vehicle VALUES (?, ?, ?, ?)");
                    ps.setString(1, idField.getText());
                    ps.setString(2, modelField.getText());
                    ps.setString(3, typeField.getText());
                    ps.setInt(4, Integer.parseInt(capacityField.getText()));
                    ps.executeUpdate();
                    load.doClick(); 
                } catch (Exception ex) { 
                    JOptionPane.showMessageDialog(null, "Error adding: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        update.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(null, "Please select a row to update.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
            
            String oldId = model.getValueAt(r, 0).toString();
            JTextField idField = new JTextField(oldId);
            JTextField modelField = new JTextField(model.getValueAt(r, 1).toString());
            JTextField typeField = new JTextField(model.getValueAt(r, 2).toString());
            JTextField capacityField = new JTextField(model.getValueAt(r, 3).toString());

            Object[] fields = { "Vehicle ID:", idField, "Model:", modelField, "Type:", typeField, "Capacity:", capacityField };
            int option = JOptionPane.showConfirmDialog(null, fields, "✏️ Update Vehicle", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("UPDATE vehicle SET vehicle_id=?, model=?, vehicle_type=?, capacity=? WHERE vehicle_id=?");
                    ps.setString(1, idField.getText());
                    ps.setString(2, modelField.getText());
                    ps.setString(3, typeField.getText());
                    ps.setInt(4, Integer.parseInt(capacityField.getText()));
                    ps.setString(5, oldId);
                    ps.executeUpdate();
                    load.doClick(); 
                } catch (Exception ex) { 
                    JOptionPane.showMessageDialog(null, "Error updating: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        delete.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(null, "Please select a row to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
            String id = model.getValueAt(r, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Vehicle ID: " + id + "?", "🗑️ Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("DELETE FROM vehicle WHERE vehicle_id=?");
                    ps.setString(1, id);
                    ps.executeUpdate();
                    load.doClick(); 
                } catch (Exception ex) { 
                    JOptionPane.showMessageDialog(null, "Error deleting: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224)));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // Auto Load
        SwingUtilities.invokeLater(load::doClick);
        return panel;
    }

    // ================= DRIVER =================
    JPanel driverPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Driver ID", "Name", "Phone Number"}, 0);
        JTable table = new JTable(model);
        styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton load = new JButton("🔄 Refresh Data");
        JButton add = new JButton("➕ Add Driver");
        JButton update = new JButton("✏️ Update Selected");
        JButton delete = new JButton("🗑️ Delete Selected");
        JButton download = new JButton("📥 Download CSV");

        styleButton(load, PRIMARY_COLOR);
        styleButton(add, PRIMARY_COLOR);
        styleButton(update, PRIMARY_COLOR);
        styleButton(delete, DANGER_COLOR);
        styleButton(download, DOWNLOAD_COLOR);

        btnPanel.add(load); btnPanel.add(add); btnPanel.add(update); btnPanel.add(delete); btnPanel.add(download);

        // Actions
        download.addActionListener(e -> downloadCSV(table, "Drivers"));

        load.addActionListener(e -> {
            model.setRowCount(0);
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS);
                 Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery("SELECT * FROM driver");
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("driver_id"), rs.getString("driver_name"), rs.getString("phone")
                    });
                }
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(null, "Error loading data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField nameField = new JTextField();
            JTextField phoneField = new JTextField();
            Object[] fields = { "Driver ID (e.g. D201):", idField, "Name:", nameField, "Phone Number:", phoneField };
            int option = JOptionPane.showConfirmDialog(null, fields, "➕ Add New Driver", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("INSERT INTO driver VALUES (?, ?, ?)");
                    ps.setString(1, idField.getText());
                    ps.setString(2, nameField.getText());
                    ps.setString(3, phoneField.getText());
                    ps.executeUpdate();
                    load.doClick();
                } catch (Exception ex) { 
                    JOptionPane.showMessageDialog(null, "Error adding: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        update.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(null, "Please select a row to update.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
            
            String oldId = model.getValueAt(r, 0).toString();
            JTextField idField = new JTextField(oldId);
            JTextField nameField = new JTextField(model.getValueAt(r, 1).toString());
            JTextField phoneField = new JTextField(model.getValueAt(r, 2).toString());

            Object[] fields = { "Driver ID:", idField, "Name:", nameField, "Phone Number:", phoneField };
            int option = JOptionPane.showConfirmDialog(null, fields, "✏️ Update Driver", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("UPDATE driver SET driver_id=?, driver_name=?, phone=? WHERE driver_id=?");
                    ps.setString(1, idField.getText());
                    ps.setString(2, nameField.getText());
                    ps.setString(3, phoneField.getText());
                    ps.setString(4, oldId);
                    ps.executeUpdate();
                    load.doClick();
                } catch (Exception ex) { 
                    JOptionPane.showMessageDialog(null, "Error updating: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        delete.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(null, "Please select a row to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
            String id = model.getValueAt(r, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Driver ID: " + id + "?", "🗑️ Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("DELETE FROM driver WHERE driver_id=?");
                    ps.setString(1, id);
                    ps.executeUpdate();
                    load.doClick();
                } catch (Exception ex) { 
                    JOptionPane.showMessageDialog(null, "Error deleting: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224)));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // Auto Load
        SwingUtilities.invokeLater(load::doClick);
        return panel;
    }

    // ================= TRIP =================
    JPanel tripPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Trip ID", "Vehicle ID", "Distance (km)", "Date"}, 0);
        JTable table = new JTable(model);
        styleTable(table);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton load = new JButton("🔄 Refresh Data");
        JButton add = new JButton("➕ Add Trip");
        JButton update = new JButton("✏️ Update Selected");
        JButton delete = new JButton("🗑️ Delete Selected");
        JButton download = new JButton("📥 Download CSV");

        styleButton(load, PRIMARY_COLOR);
        styleButton(add, PRIMARY_COLOR);
        styleButton(update, PRIMARY_COLOR);
        styleButton(delete, DANGER_COLOR);
        styleButton(download, DOWNLOAD_COLOR);

        btnPanel.add(load); btnPanel.add(add); btnPanel.add(update); btnPanel.add(delete); btnPanel.add(download);

        // Actions
        download.addActionListener(e -> downloadCSV(table, "Trips"));

        load.addActionListener(e -> {
            model.setRowCount(0);
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS);
                 Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery("SELECT * FROM trip");
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("trip_id"), rs.getString("vehicle_id"),
                        rs.getInt("distance"), rs.getDate("trip_date")
                    });
                }
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(null, "Error loading data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField vehicleIdField = new JTextField();
            JTextField distanceField = new JTextField();
            JTextField dateField = new JTextField();
            Object[] fields = { "Trip ID (e.g. T301):", idField, "Vehicle ID:", vehicleIdField, "Distance in km:", distanceField, "Date (YYYY-MM-DD):", dateField };
            int option = JOptionPane.showConfirmDialog(null, fields, "➕ Add New Trip", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("INSERT INTO trip VALUES (?, ?, ?, ?)");
                    ps.setString(1, idField.getText());
                    ps.setString(2, vehicleIdField.getText());
                    ps.setInt(3, Integer.parseInt(distanceField.getText()));
                    ps.setString(4, dateField.getText());
                    ps.executeUpdate();
                    load.doClick();
                } catch (Exception ex) { 
                    JOptionPane.showMessageDialog(null, "Error adding: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        update.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(null, "Please select a row to update.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
            
            String oldId = model.getValueAt(r, 0).toString();
            JTextField idField = new JTextField(oldId);
            JTextField vehicleIdField = new JTextField(model.getValueAt(r, 1) != null ? model.getValueAt(r, 1).toString() : "");
            JTextField distanceField = new JTextField(model.getValueAt(r, 2).toString());
            JTextField dateField = new JTextField(model.getValueAt(r, 3).toString());

            Object[] fields = { "Trip ID:", idField, "Vehicle ID:", vehicleIdField, "Distance (km):", distanceField, "Date (YYYY-MM-DD):", dateField };
            int option = JOptionPane.showConfirmDialog(null, fields, "✏️ Update Trip", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("UPDATE trip SET trip_id=?, vehicle_id=?, distance=?, trip_date=? WHERE trip_id=?");
                    ps.setString(1, idField.getText());
                    ps.setString(2, vehicleIdField.getText());
                    ps.setInt(3, Integer.parseInt(distanceField.getText()));
                    ps.setString(4, dateField.getText());
                    ps.setString(5, oldId);
                    ps.executeUpdate();
                    load.doClick();
                } catch (Exception ex) { 
                    JOptionPane.showMessageDialog(null, "Error updating: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        delete.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(null, "Please select a row to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
            String id = model.getValueAt(r, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Trip ID: " + id + "?", "🗑️ Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    PreparedStatement ps = c.prepareStatement("DELETE FROM trip WHERE trip_id=?");
                    ps.setString(1, id);
                    ps.executeUpdate();
                    load.doClick();
                } catch (Exception ex) { 
                    JOptionPane.showMessageDialog(null, "Error deleting: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224)));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // Auto Load
        SwingUtilities.invokeLater(load::doClick);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TransportGUI::new);
    }
}
