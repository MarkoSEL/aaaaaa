package proyectofinal;

import javax.swing.SwingUtilities;
import proyectofinal.utp.legal.ui.LoginFrame;

public class MainLogin {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
