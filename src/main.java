import UI.Stadium;

import javax.swing.SwingUtilities;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Stadium view = new Stadium();
            view.setVisible(true);
        });
    }
}