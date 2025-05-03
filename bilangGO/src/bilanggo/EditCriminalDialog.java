package bilanggo;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class EditCriminalDialog extends JDialog {
    private final int criminalId;
    private final JFrame parentFrame;
    
    // Form components
    private JTextField nameField;
    private JTextField aliasField;
    private JTextField dobField;
    private JComboBox<String> genderCombo;
    private JTextField nationalityField;
    private JComboBox<String> statusCombo;
    private JComboBox<String> dangerLevelCombo;
    private JTextField lastSeenField;
    private JButton saveButton;
    private JButton cancelButton;
    
    public EditCriminalDialog(JFrame parent, int id) {
        super(parent, "Edit Criminal Record", true);
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
        

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        //  form fields
        addFormField(formPanel, "Name:", nameField = new JTextField());
        addFormField(formPanel, "Alias:", aliasField = new JTextField());
        addFormField(formPanel, "Date of Birth (YYYY-MM-DD):", dobField = new JTextField());
        
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        addFormField(formPanel, "Gender:", genderCombo);
        
        addFormField(formPanel, "Nationality:", nationalityField = new JTextField());
        
        statusCombo = new JComboBox<>(new String[]{"Wanted", "Arrested", "At Large"});
        addFormField(formPanel, "Status:", statusCombo);
        
        dangerLevelCombo = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        addFormField(formPanel, "Danger Level:", dangerLevelCombo);
        
        addFormField(formPanel, "Last seen location:", lastSeenField = new JTextField());
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Button 
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveCriminal());
        saveButton.setBackground(new Color(100, 200, 100));
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void addFormField(JPanel panel, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl);
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
                
                Date dob = rs.getDate("date_of_birth");
                if (dob != null) {
                    dobField.setText(new SimpleDateFormat("yyyy-MM-dd").format(dob));
                }
                
                genderCombo.setSelectedItem(rs.getString("gender"));
                nationalityField.setText(rs.getString("nationality"));
                statusCombo.setSelectedItem(rs.getString("status"));
                dangerLevelCombo.setSelectedItem(rs.getString("danger_level"));
                lastSeenField.setText(rs.getString("last_seen_location"));
            }
        } catch (SQLException e) {
            showError("Error loading criminal data: " + e.getMessage());
        }
    }
    
    private void saveCriminal() {
        // Validator
        if (nameField.getText().trim().isEmpty()) {
            showError("Name is required");
            return;
        }
        
   
        java.sql.Date dob = null;
        if (!dobField.getText().trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                dob = new java.sql.Date(sdf.parse(dobField.getText()).getTime());
            } catch (ParseException e) {
                showError("Invalid date format. Please use YYYY-MM-DD");
                return;
            }
        }
        
        String url = "jdbc:mysql://localhost:3306/bilanggo";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String query = "UPDATE criminals SET " +
                "name = ?, alias = ?, date_of_birth = ?, gender = ?, " +
                "nationality = ?, status = ?, danger_level = ?, last_seen_location = ? " +
                "WHERE id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, nameField.getText());
            stmt.setString(2, aliasField.getText());
            stmt.setDate(3, dob);
            stmt.setString(4, (String) genderCombo.getSelectedItem());
            stmt.setString(5, nationalityField.getText());
            stmt.setString(6, (String) statusCombo.getSelectedItem());
            stmt.setString(7, (String) dangerLevelCombo.getSelectedItem());
            stmt.setString(8, lastSeenField.getText());
            stmt.setInt(9, criminalId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Criminal record updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh 
                if (parentFrame instanceof Records) {
                    ((Records) parentFrame).refreshTable();
                }
                
          
                
                // Close afterwards
                dispose();
            } else {
                showError("Failed to update criminal record");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}