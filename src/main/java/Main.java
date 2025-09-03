import javax.swing.*;

/**
 * Punto de entrada principal para la aplicación de Préstamos.
 * * @author tony
 */
public class Main {
    public static void main(String[] args) {
        // Configurar Look and Feel antes de crear componentes
        try {
            // Habilitar scaling automático para pantallas de alta resolución
            System.setProperty("sun.java2d.uiScale", "1");
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

            // Usar el Look and Feel del sistema
            // CORREGIDO: UIManager.getSystemLookAndFeelClassName() es el método correcto
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo configurar el Look and Feel: " + e.getMessage());
        }

        // Ejecutar en el Event Dispatch Thread para asegurar la seguridad de los hilos de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                // Inicializar la base de datos (simulada)
                ConexionDB.inicializarDB();

                // Crear y configurar la ventana principal
                JFrame frame = new JFrame("Sistema de Préstamos - Equipos Escolares - PREPA 36");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Configurar ventana responsive
                // 75% ancho, 85% alto de la pantalla, mínimo 60% ancho, 70% alto
                ScreenUtils.setupResponsiveWindow(frame, 0.75, 0.85, 0.60, 0.70);

                // Crear el formulario principal
                PrestamoForm prestamoForm = new PrestamoForm(frame);
                frame.add(prestamoForm);

                // Empaquetar el frame y hacerlo visible
                frame.pack();
                frame.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar la aplicación: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
