import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class ImageRenderer implements TableCellRenderer {
    private final JLabel label = new JLabel();
    
    public ImageRenderer() {
        label.setOpaque(true);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        //  background colors for selection
        if (isSelected) {
            label.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
        } else {
            label.setBackground(table.getBackground());
            label.setForeground(table.getForeground());
        }
        
        //  image display
        if (value instanceof ImageIcon) {
            label.setIcon((ImageIcon) value);
            label.setText("");
        } else {
            label.setIcon(null);
            label.setText("No Image");
        }
        
 
        label.setPreferredSize(new Dimension(80, 80));
        
        return label;
    }
}