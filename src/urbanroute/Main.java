package urbanroute;

import javax.swing.SwingUtilities;
import urbanroute.vista.InterfazUsuarioUI;

/**
 * Clase principal.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfazUsuarioUI().setVisible(true));
    }
}
