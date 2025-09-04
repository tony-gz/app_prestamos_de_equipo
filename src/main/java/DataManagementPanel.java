import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class DataManagementPanel extends JFrame {

    private static class AppColors {
        public static final Color PRIMARY = new Color(37, 99, 235);
        public static final Color SECONDARY = new Color(16, 185, 129);
        public static final Color DANGER = new Color(239, 68, 68);
        public static final Color BACKGROUND = new Color(249, 250, 251);
        public static final Color SURFACE = Color.WHITE;
        public static final Color TEXT_PRIMARY = new Color(17, 24, 39);
        public static final Color TEXT_SECONDARY = new Color(107, 114, 128);
        public static final Color BORDER = new Color(209, 213, 219);
    }

    private static class AppFonts {
        private static final int BASE_TITLE = 24;
        private static final int BASE_BODY = 16;
        private static final Font TITLE = new Font("Segoe UI", Font.BOLD,
                ScreenUtils.getScaledFontSize(BASE_TITLE));
        public static final Font BODY = new Font("Segoe UI", Font.PLAIN,
                ScreenUtils.getScaledFontSize(BASE_BODY));
    }

    private JLabel lblStatus;

    public DataManagementPanel(JFrame parent) {
        super("Administración de Datos");
        setSize(600, 300);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(AppColors.BACKGROUND);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppColors.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("Opciones de Administración");
        titleLabel.setFont(AppFonts.TITLE);
        titleLabel.setForeground(AppColors.PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel infoLabel = new JLabel("<html>Seleccione una opción para gestionar la base de datos de préstamos.</html>");
        infoLabel.setFont(AppFonts.BODY);
        infoLabel.setForeground(AppColors.TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnGuardarLimpiar = crearBoton("Guardar y Limpiar Préstamos", AppColors.PRIMARY);
        JButton btnDescargarCSV = crearBoton("Descargar CSV General", AppColors.SECONDARY);

        lblStatus = new JLabel("");
        lblStatus.setFont(AppFonts.BODY);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setForeground(AppColors.TEXT_PRIMARY);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(infoLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(btnGuardarLimpiar);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(btnDescargarCSV);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(lblStatus);

        add(mainPanel);

        // Lógica de los botones
        btnGuardarLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(
                        DataManagementPanel.this,
                        "Esta acción guardará los datos actuales y limpiará la base de datos de préstamos. ¿Desea continuar?",
                        "Confirmar Acción",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    new Thread(() -> {
                        try {
                            SwingUtilities.invokeLater(() -> setStatus("Guardando y limpiando..."));
                            ConexionDB.guardarYLimpiarPrestamos();
                            SwingUtilities.invokeLater(() -> {
                                setStatus("Datos guardados y base de datos limpiada exitosamente.");
                                JOptionPane.showMessageDialog(DataManagementPanel.this,
                                        "El proceso se completó correctamente.",
                                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            });
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> {
                                setStatus("Error al realizar la operación.");
                                JOptionPane.showMessageDialog(DataManagementPanel.this,
                                        "Ocurrió un error: " + ex.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    }).start();
                }
            }
        });

        btnDescargarCSV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Guardar archivo CSV");
                fileChooser.setSelectedFile(new File("prestamos_generales.csv"));
                int userSelection = fileChooser.showSaveDialog(DataManagementPanel.this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    new Thread(() -> {
                        try {
                            SwingUtilities.invokeLater(() -> setStatus("Generando archivo CSV..."));
                            ConexionDB.exportarGeneralToCSV(fileToSave.getAbsolutePath());
                            SwingUtilities.invokeLater(() -> {
                                setStatus("Archivo CSV generado exitosamente.");
                                JOptionPane.showMessageDialog(DataManagementPanel.this,
                                        "El archivo CSV se guardó en: " + fileToSave.getAbsolutePath(),
                                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            });
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> {
                                setStatus("Error al generar el archivo CSV.");
                                JOptionPane.showMessageDialog(DataManagementPanel.this,
                                        "Ocurrió un error: " + ex.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    }).start();
                }
            }
        });
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(AppFonts.BODY);
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setMaximumSize(new Dimension(300, 50));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private void setStatus(String message) {
        lblStatus.setText(message);
    }
}