package bilanggo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CaseDetailsDialog extends JDialog {
    private final int caseId;
    private final JFrame parentFrame;

    private JTextField titleField, notesField, txtCrimeType, txtLocation;
    private JFormattedTextField txtDateOfCrime;
    private JComboBox<String> cmbStatus;
    private JButton btnSave, btnDelete, btnCancel;
    private DefaultListModel<String> criminalListModel;
    private JList<String> criminalList;
    private java.util.List<Integer> criminalIds = new ArrayList<>();

    public CaseDetailsDialog(JFrame parent, int caseId) {
        super(parent, "Case Details", true);
        this.parentFrame = parent;
        this.caseId = caseId;
        initComponents();
        loadCaseData();
        loadLinkedCriminals();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        titleField = new JTextField(20);
        formPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Crime Type:"), gbc);
        gbc.gridx = 1;
        txtCrimeType = new JTextField(20);
        formPanel.add(txtCrimeType, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Date of Crime (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        txtDateOfCrime = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        txtDateOfCrime.setColumns(20);
        formPanel.add(txtDateOfCrime, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        txtLocation = new JTextField(20);
        formPanel.add(txtLocation, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"Open", "Under Investigation", "Closed"});
        formPanel.add(cmbStatus, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        notesField = new JTextField(20);
        formPanel.add(notesField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        formPanel.add(new JLabel("Linked Criminals:"), gbc);

        gbc.gridy++;
        criminalListModel = new DefaultListModel<>();
        criminalList = new JList<>(criminalListModel);
        JScrollPane scrollPane = new JScrollPane(criminalList);
        scrollPane.setPreferredSize(new Dimension(300, 100));
        formPanel.add(scrollPane, gbc);

        gbc.gridy++;
        JPanel criminalBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddCriminal = new JButton("Add Criminal");
        btnAddCriminal.addActionListener(e -> addCriminal());
        JButton btnRemoveCriminal = new JButton("Remove Selected");
        btnRemoveCriminal.addActionListener(e -> removeSelectedCriminal());

        criminalBtnPanel.add(btnAddCriminal);
        criminalBtnPanel.add(btnRemoveCriminal);
        formPanel.add(criminalBtnPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnSave = new JButton("Save");
        btnSave.addActionListener(e -> saveCase());
        buttonPanel.add(btnSave);

        btnDelete = new JButton("Delete");
        btnDelete.setBackground(new Color(255, 100, 100));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteCase());
        buttonPanel.add(btnDelete);

        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void loadCaseData() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bilanggo", "root", "")) {
            String query = "SELECT * FROM cases WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, caseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                titleField.setText(rs.getString("title"));
                txtCrimeType.setText(rs.getString("crime_type"));
                txtDateOfCrime.setValue(rs.getDate("date_of_crime"));
                txtLocation.setText(rs.getString("location"));
                cmbStatus.setSelectedItem(rs.getString("status"));
                notesField.setText(rs.getString("notes"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading case: " + e.getMessage());
        }
    }

    private void loadLinkedCriminals() {
        criminalListModel.clear();
        criminalIds.clear();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bilanggo", "root", "")) {
            String query = "SELECT c.id, c.name FROM criminals c " +
                    "JOIN case_criminals cc ON c.id = cc.criminal_id WHERE cc.case_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, caseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                criminalListModel.addElement(name);
                criminalIds.add(id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading criminals: " + e.getMessage());
        }
    }

   private void addCriminal() {
    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bilanggo", "root", "")) {
        String query = "SELECT id, name FROM criminals";
        PreparedStatement pstmt = conn.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        java.util.List<Integer> ids = new ArrayList<>();
        java.util.List<String> names = new ArrayList<>();

        while (rs.next()) {
            ids.add(rs.getInt("id"));
            names.add(rs.getString("name"));
        }

        if (names.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No criminals found in the database.");
            return;
        }

        String selectedName = (String) JOptionPane.showInputDialog(
                this,
                "Select a criminal to link to this case:",
                "Add Criminal",
                JOptionPane.PLAIN_MESSAGE,
                null,
                names.toArray(),
                names.get(0)
        );

        if (selectedName != null) {
            int selectedIndex = names.indexOf(selectedName);
            int selectedId = ids.get(selectedIndex);

        
            if (criminalIds.contains(selectedId)) {
                JOptionPane.showMessageDialog(this, "This criminal is already linked to the case.");
                return;
            }

            String insertLink = "INSERT INTO case_criminals (case_id, criminal_id) VALUES (?, ?)";
            PreparedStatement linkStmt = conn.prepareStatement(insertLink);
            linkStmt.setInt(1, caseId);
            linkStmt.setInt(2, selectedId);
            linkStmt.executeUpdate();

            loadLinkedCriminals();
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error adding existing criminal: " + e.getMessage());
    }
}


    private void removeSelectedCriminal() {
        int index = criminalList.getSelectedIndex();
        if (index != -1) {
            int criminalId = criminalIds.get(index);
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bilanggo", "root", "")) {
                String query = "DELETE FROM case_criminals WHERE case_id=? AND criminal_id=?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, caseId);
                pstmt.setInt(2, criminalId);
                pstmt.executeUpdate();

                loadLinkedCriminals();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error removing criminal: " + e.getMessage());
            }
        }
    }

    private void saveCase() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bilanggo", "root", "")) {
            String query = "UPDATE cases SET crime_type=?, date_of_crime=?, location=?, status=?, title=?, notes=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(query);

            pstmt.setString(1, txtCrimeType.getText());
            pstmt.setString(2, txtDateOfCrime.getText());
            pstmt.setString(3, txtLocation.getText());
            pstmt.setString(4, cmbStatus.getSelectedItem().toString());
            pstmt.setString(5, titleField.getText());
            pstmt.setString(6, notesField.getText());
            pstmt.setInt(7, caseId);

            int rowsAffected = pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, rowsAffected > 0 ? "Case updated successfully!" : "Failed to update case");
            if (rowsAffected > 0) dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving case: " + e.getMessage());
        }
    }

    private void deleteCase() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this case?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bilanggo", "root", "")) {
                PreparedStatement delLinks = conn.prepareStatement("DELETE FROM case_criminals WHERE case_id=?");
                delLinks.setInt(1, caseId);
                delLinks.executeUpdate();

                PreparedStatement delCase = conn.prepareStatement("DELETE FROM cases WHERE id=?");
                delCase.setInt(1, caseId);
                int rows = delCase.executeUpdate();
                JOptionPane.showMessageDialog(this, rows > 0 ? "Case deleted." : "Delete failed.");
                if (rows > 0) dispose();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting case: " + e.getMessage());
            }
        }
    }
}
