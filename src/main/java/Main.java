import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        // Ejecutar en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Inicializar la base de datos
                ConexionDB.inicializarDB();

                // Crear y mostrar la ventana principal
                JFrame frame = new JFrame("Sistema de Préstamos - Equipos Escolares - PREPA 36");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1200, 700); // Mismo ancho que AdminPanel, altura ajustada
                frame.setLocationRelativeTo(null); // Centrar ventana
                frame.setResizable(false); // No permitir redimensionar

                // Crear el formulario principal
                PrestamoForm prestamoForm = new PrestamoForm(frame);
                frame.add(prestamoForm);

                // Mostrar la ventana
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