package bilanggo;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AddCriminalCase extends JDialog {
    private JTextField title;
    private JFormattedTextField txtDateOfCrime;
    private JTextField txtLocation;
    private JComboBox<String> cmbStatus;
    private JTextField notes;
    private JButton btnSave;
    private JButton btnCancel;
    private JList<String> lstCriminals;
    private JList<String> lstCrimeTypes;
    
    
    private ArrayList<Integer> criminalIds = new ArrayList<>();

    private Set<String> selectedCrimes = new HashSet<>();
    private Set<Integer> selectedCriminalIndices = new HashSet<>();

    public AddCriminalCase(JFrame parent) {
        super(parent, "Add New Case", true);
        initComponents();
        loadCriminals();
        setSize(600, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        title = new JTextField(20);
        formPanel.add(title, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Crime Type:"), gbc);
        gbc.gridx = 1;
        String[] crimeTypes = {
            "Theft", "Robbery", "Assault", "Murder", "Kidnapping", "Fraud",
            "Arson", "Drug Trafficking", "Cybercrime", "Extortion", "Bribery",
            "Human Trafficking", "Money Laundering", "Vandalism", "Homicide",
            "Burglary", "Domestic Violence", "Illegal Possession of Firearms"
        };
        lstCrimeTypes = new JList<>(crimeTypes);
        lstCrimeTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Single selection for toggling
        lstCrimeTypes.setCellRenderer(new ToggleListCellRenderer());
        lstCrimeTypes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = lstCrimeTypes.getSelectedValue();
                if (selected != null) {
                    if (selectedCrimes.contains(selected)) {
                        selectedCrimes.remove(selected);
                    } else {
                        selectedCrimes.add(selected);
                    }
                    lstCrimeTypes.clearSelection();
                    lstCrimeTypes.repaint();
                }
            }
        });
        JScrollPane crimeScroll = new JScrollPane(lstCrimeTypes);
        crimeScroll.setPreferredSize(new Dimension(250, 80));
        formPanel.add(crimeScroll, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Date of Crime:"), gbc);
        gbc.gridx = 1;
        txtDateOfCrime = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        txtDateOfCrime.setValue(new java.util.Date());
        formPanel.add(txtDateOfCrime, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        txtLocation = new JTextField(20);
        formPanel.add(txtLocation, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"Open", "Under Investigation", "Closed"});
        formPanel.add(cmbStatus, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        notes = new JTextField(20);
        formPanel.add(notes, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Link Criminals:"), gbc);
        gbc.gridx = 1;
        lstCriminals = new JList<>();
        lstCriminals.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
        lstCriminals.setCellRenderer(new ToggleListCellRenderer());
        lstCriminals.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = lstCriminals.getSelectedIndex();
                if (selectedIndex != -1) {
                    if (selectedCriminalIndices.contains(selectedIndex)) {
                        selectedCriminalIndices.remove(selectedIndex);
                    } else {
                        selectedCriminalIndices.add(selectedIndex);
                    }
                    lstCriminals.clearSelection();
                    lstCriminals.repaint();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(lstCriminals);
        scrollPane.setPreferredSize(new Dimension(250, 80));
        formPanel.add(scrollPane, gbc);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Save");
        btnSave.addActionListener(e -> saveCase());
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCriminals() {
        String url = "jdbc:mysql://localhost:3306/bilanggo";
        String user = "root";
        String password = "";

        DefaultListModel<String> model = new DefaultListModel<>();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT id, name FROM criminals";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                criminalIds.add(id);
                model.addElement(name);
            }

            lstCriminals.setModel(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading criminals: " + e.getMessage());
        }
    }

    private void saveCase() {
        String url = "jdbc:mysql://localhost:3306/bilanggo";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false); 

            String caseQuery = "INSERT INTO cases (crime_type, date_of_crime, location, status, title, notes) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(caseQuery, Statement.RETURN_GENERATED_KEYS);
            
            String joinedCrimes = String.join(", ", selectedCrimes);
            pstmt.setString(1, joinedCrimes);
            pstmt.setString(2, txtDateOfCrime.getText());
            pstmt.setString(3, txtLocation.getText());
            pstmt.setString(4, cmbStatus.getSelectedItem().toString());
            pstmt.setString(5, title.getText());
            pstmt.setString(6, notes.getText());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            int caseId = -1;
            if (keys.next()) {
                caseId = keys.getInt(1);
            }

            if (caseId != -1) {
          
                String linkQuery = "INSERT INTO case_criminals (case_id, criminal_id) VALUES (?, ?)";
                PreparedStatement linkStmt = conn.prepareStatement(linkQuery);
                for (int index : selectedCriminalIndices) {
                    int criminalId = criminalIds.get(index);
                    linkStmt.setInt(1, caseId);
                    linkStmt.setInt(2, criminalId);
                    linkStmt.addBatch();
                }
                linkStmt.executeBatch();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Case added successfully!");
            dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving case: " + e.getMessage());
        }
    }

 
    private class ToggleListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (list == lstCrimeTypes && selectedCrimes.contains(value)) {
                c.setBackground(new Color(200, 220, 255));
            } else if (list == lstCriminals && selectedCriminalIndices.contains(index)) {
                c.setBackground(new Color(200, 220, 255));
            } else {
                c.setBackground(list.getBackground());
            }
            
            return c;
        }
    }
}