package bilanggo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;

public class CriminalDetailsDialog extends JDialog {
    private final int criminalId;
    private final JFrame parentFrame;
    
    // Components
    private JLabel imageLabel;
    private JLabel nameField;
    private JLabel aliasField;
    private JLabel dobField;
    private JLabel genderField;
    private JLabel nationalityField;
    private JLabel statusField;
    private JLabel dangerLevelField;
    private JLabel lastSeenField;
    
    public CriminalDetailsDialog(JFrame parent, int id) {
        super(parent, "Criminal Details", true);
        this.parentFrame = parent;
        this.criminalId = id;
        setSize(800, 600);
        setLocationRelativeTo(parent);
        initComponents();
        loadCriminalData();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Image panel (left side)
        JPanel imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(300, 400));
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        imagePanel.add(imageLabel);
        mainPanel.add(imagePanel, BorderLayout.WEST);
        
        // Details panel (right side)
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        JPanel detailsGrid = new JPanel(new GridLayout(0, 2, 10, 10));
        detailsGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // fields with labels
        addDetailRow(detailsGrid, "Name:", nameField = new JLabel());
        addDetailRow(detailsGrid, "Alias:", aliasField = new JLabel());
        addDetailRow(detailsGrid, "Date of Birth:", dobField = new JLabel());
        addDetailRow(detailsGrid, "Gender:", genderField = new JLabel());
        addDetailRow(detailsGrid, "Nationality:", nationalityField = new JLabel());
        addDetailRow(detailsGrid, "Status:", statusField = new JLabel());
        addDetailRow(detailsGrid, "Danger Level:", dangerLevelField = new JLabel());
        addDetailRow(detailsGrid, "Last seen location:", lastSeenField = new JLabel());
        
        detailsPanel.add(detailsGrid);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this::deleteCriminal);
        deleteButton.setBackground(new Color(255, 100, 100));
        
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(this::editCriminal);
        editButton.setBackground(new Color(100, 100, 255));
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(closeButton);
        
        detailsPanel.add(Box.createVerticalStrut(20));
        detailsPanel.add(buttonPanel);
        
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void addDetailRow(JPanel panel, String label, JLabel field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl);
        field.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        panel.add(field);
    }
    
    private void loadCriminalData() {
        String url = "jdbc:mysql://localhost:3306/bilanggo";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT * FROM criminals WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, criminalId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                aliasField.setText(rs.getString("alias"));
                dobField.setText(formatDate(rs.getDate("date_of_birth")));
                genderField.setText(rs.getString("gender"));
                nationalityField.setText(rs.getString("nationality"));
                statusField.setText(rs.getString("status"));
                dangerLevelField.setText(rs.getString("danger_level"));
                lastSeenField.setText(rs.getString("last_seen_location"));
                
                Blob imageBlob = rs.getBlob("photo_path");
                if (imageBlob != null) {
                    byte[] imageData = imageBlob.getBytes(1, (int) imageBlob.length());
                    ImageIcon icon = new ImageIcon(imageData);
                    Image image = icon.getImage();
                    int width = 280;
                    int height = (int) (width * ((double) icon.getIconHeight() / icon.getIconWidth()));
                    imageLabel.setIcon(new ImageIcon(image.getScaledInstance(
                        width, height, Image.SCALE_SMOOTH)));
                } else {
                    imageLabel.setIcon(new ImageIcon(getClass().getResource("/images/no-photo.png")));
                }
            }
        } catch (SQLException e) {
            showError("Error loading criminal details: " + e.getMessage());
        }
    }
    
    private void editCriminal(ActionEvent e) {
        dispose();
        
        EditCriminalDialog editDialog = new EditCriminalDialog(parentFrame, criminalId);
        editDialog.setVisible(true);
        
        // close current and open new
    }
    
    private void deleteCriminal(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this criminal record?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String url = "jdbc:mysql://localhost:3306/bilanggo";
            String user = "root";
            String password = "";
            
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                String query = "DELETE FROM criminals WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, criminalId);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Criminal record deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                  
                    if (parentFrame instanceof Records) {
                        ((Records) parentFrame).refreshTable();
                    }
                    
                 
                    dispose();
                } else {
                    showError("Failed to delete criminal record");
                }
            } catch (SQLException ex) {
                showError("Database error: " + ex.getMessage());
            }
        }
    }
    
    private String formatDate(Date date) {
        if (date == null) return "N/A";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}