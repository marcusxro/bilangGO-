package bilanggo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Types;  
import java.text.SimpleDateFormat;

public class AddCriminalDialog extends JDialog {
    private final JFrame parentFrame;
    private JTextField txtName;
    private JTextField txtAlias;
    private JFormattedTextField txtDob;
    private JComboBox<String> cmbGender;
    private JTextField txtNationality;
    private JComboBox<String> cmbStatus;
    private JComboBox<String> cmbDangerLevel;
    private JTextField txtLastSeen;
    private JLabel lblPhotoPreview;
    private JButton btnUploadPhoto;

    private JButton btnSave;
    private JButton btnCancel;

    private File selectedImageFile = null;
    
    
    
    public AddCriminalDialog(JFrame parent, int id) {
        super(parent, "Edit Criminal Record", true);
        this.parentFrame = parent;
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }
    

    public AddCriminalDialog(JFrame parent, javax.swing.JFrame parentFrame) {
        super(parent, "Add Criminal", true);
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(9, 2, 10, 10));

        // Input fields
        txtName = new JTextField();
        txtAlias = new JTextField();

        txtDob = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        txtDob.setToolTipText("Format: yyyy-MM-dd");

        cmbGender = new JComboBox<>(new String[] { "Male", "Female", "Other" });
        txtNationality = new JTextField();

        cmbStatus = new JComboBox<>(new String[] { "Arrested", "Wanted", "Released" });
        cmbDangerLevel = new JComboBox<>(new String[] { "Low", "Medium", "High" });

        txtLastSeen = new JTextField();

        lblPhotoPreview = new JLabel("No Image", SwingConstants.CENTER);
        lblPhotoPreview.setPreferredSize(new Dimension(150, 150));
        lblPhotoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        btnUploadPhoto = new JButton("Upload Photo");
        btnUploadPhoto.addActionListener(e -> selectPhoto());

        // Form layout
        formPanel.add(new JLabel("Name:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Alias:"));
        formPanel.add(txtAlias);
        formPanel.add(new JLabel("Date of Birth (yyyy-MM-dd):"));
        formPanel.add(txtDob);
        formPanel.add(new JLabel("Gender:"));
        formPanel.add(cmbGender);
        formPanel.add(new JLabel("Nationality:"));
        formPanel.add(txtNationality);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(cmbStatus);
        formPanel.add(new JLabel("Danger Level:"));
        formPanel.add(cmbDangerLevel);
        formPanel.add(new JLabel("Last Seen Location:"));
        formPanel.add(txtLastSeen);
        formPanel.add(new JLabel("Photo:"));
        formPanel.add(btnUploadPhoto);

        JPanel imagePanel = new JPanel();
        imagePanel.add(lblPhotoPreview);

        // Buttons
        JPanel buttonPanel = new JPanel();
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> handleSave());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        add(formPanel, BorderLayout.CENTER);
        add(imagePanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(650, 400);
        setLocationRelativeTo(parent);
        this.parentFrame = parentFrame;
    }

    private void selectPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Image");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "jpeg", "png"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            ImageIcon imageIcon = new ImageIcon(new ImageIcon(selectedImageFile.getAbsolutePath()).getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH));
            lblPhotoPreview.setText("");
            lblPhotoPreview.setIcon(imageIcon);
        }
    }

    private void handleSave() {
            
        String SUrl, SUser, Spass, query;
        
        SUrl = "jdbc:mysql://localhost:3306/bilanggo";
        SUser = "root";
        Spass = "";
        

        String name = txtName.getText();
        String alias = txtAlias.getText();
        String dob = txtDob.getText();
        String gender = cmbGender.getSelectedItem().toString();
        String nationality = txtNationality.getText();
        String status = cmbStatus.getSelectedItem().toString();
        String dangerLevel = cmbDangerLevel.getSelectedItem().toString();
        String lastSeen = txtLastSeen.getText();
        String photoPath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : "";


        System.out.println("Saving criminal:");
        System.out.println("Name: " + name);
        System.out.println("Alias: " + alias);
        System.out.println("DOB: " + dob);
        System.out.println("Gender: " + gender);
        System.out.println("Nationality: " + nationality);
        System.out.println("Status: " + status);
        System.out.println("Danger Level: " + dangerLevel);
        System.out.println("Last Seen: " + lastSeen);
        System.out.println("Photo Path: " + photoPath);
        
    
            byte[] photoData = null;
            if (selectedImageFile != null) {
                try {
                    photoData = Files.readAllBytes(selectedImageFile.toPath());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error reading image file: " + e.getMessage());
                    return;
                }
            }
        
        
         try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(SUrl, SUser, Spass);
        
        
         query = "INSERT INTO criminals(alias, danger_level, date_of_birth, gender, " +
                      "last_seen_location, name, nationality, photo_path, status) " +
                      "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, alias);
        pstmt.setString(2, dangerLevel);
        pstmt.setString(3, dob);
        pstmt.setString(4, gender);
        pstmt.setString(5, lastSeen);
        pstmt.setString(6, name);
        pstmt.setString(7, nationality);
        
        if (photoData != null) {
            pstmt.setBytes(8, photoData);
        } else {
            pstmt.setNull(8, Types.BLOB); 
        }
        
        pstmt.setString(9, status);
        pstmt.executeUpdate();

        Records recorder = new Records();
        
        
        recorder.refreshTable();

        JOptionPane.showMessageDialog(this, "Criminal Saved Successfully!");
        dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving criminal: " + e.getMessage());
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(this, "Criminal Saved!");
        dispose();
    }
}
