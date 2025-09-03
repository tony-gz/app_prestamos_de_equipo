import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.Timer;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Random;


public class AdminPanel extends JFrame {
    // Colores del sistema
    private static class AppColors {
        public static final Color PRIMARY = new Color(37, 99, 235);        // Azul corporativo
        public static final Color SECONDARY = new Color(16, 185, 129);     // Verde éxito
        public static final Color WARNING = new Color(245, 158, 11);       // Amarillo advertencia
        public static final Color DANGER = new Color(239, 68, 68);         // Rojo peligro
        public static final Color BACKGROUND = new Color(249, 250, 251);   // Gris muy claro
        public static final Color SURFACE = Color.WHITE;                   // Blanco
        public static final Color TEXT_PRIMARY = new Color(17, 24, 39);    // Gris muy oscuro
        public static final Color TEXT_SECONDARY = new Color(107, 114, 128); // Gris medio
        public static final Color BORDER = new Color(209, 213, 219);       // Gris claro para bordes
        public static final Color SUCCESS_BG = new Color(240, 253, 244);   // Verde muy claro
        public static final Color ERROR_BG = new Color(254, 242, 242);     // Rojo muy claro
    }

    // Fuentes del sistema
    // También agregar la clase AppFonts responsive al AdminPanel:
    private static class AppFonts {
        // Tamaños base de fuente
        private static final int BASE_TITLE = 24;
        private static final int BASE_SUBTITLE = 16;
        private static final int BASE_BODY = 13;
        private static final int BASE_CAPTION = 11;
        private static final int BASE_BUTTON = 13;
        private static final int BASE_LABEL = 12;

        // Fuentes escaladas dinámicamente
        public static final Font TITLE = new Font("Segoe UI", Font.BOLD,
                ScreenUtils.getScaledFontSize(BASE_TITLE));
        public static final Font SUBTITLE = new Font("Segoe UI", Font.BOLD,
                ScreenUtils.getScaledFontSize(BASE_SUBTITLE));
        public static final Font BODY = new Font("Segoe UI", Font.PLAIN,
                ScreenUtils.getScaledFontSize(BASE_BODY));
        public static final Font CAPTION = new Font("Segoe UI", Font.PLAIN,
                ScreenUtils.getScaledFontSize(BASE_CAPTION));
        public static final Font BUTTON = new Font("Segoe UI", Font.BOLD,
                ScreenUtils.getScaledFontSize(BASE_BUTTON));
        public static final Font LABEL = new Font("Segoe UI", Font.BOLD,
                ScreenUtils.getScaledFontSize(BASE_LABEL));
    }

    private JTable tablaPrestamos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JCheckBox chkSoloActivos;
    private JLabel lblEstadisticas;
    private JButton btnRegresar;

    private Timer inactivityTimer;
    private final int INACTIVITY_TIMEOUT = 60000;

    // Lista de colores pastel para las filas
    private final Color[] pastelColors = {
            new Color(248, 250, 252), // Azul muy claro
            new Color(249, 250, 251), // Gris muy claro
            new Color(254, 249, 195), // Amarillo muy claro
            new Color(236, 253, 245), // Verde muy claro
            new Color(255, 247, 237), // Naranja muy claro
            new Color(245, 245, 255)  // Lavanda muy claro
    };

    // Método para obtener el color pastel de una fila
    private Color getPastelColor(int row) {
        return pastelColors[row % pastelColors.length];
    }

    // Método para oscurecer un color
    private Color darkenColor(Color color) {
        return new Color(
                (int) (color.getRed() * 0.85),
                (int) (color.getGreen() * 0.85),
                (int) (color.getBlue() * 0.85)
        );
    }

    public AdminPanel(JFrame parent) {
        super("Panel de Administración - Préstamos - PREPA 36");
        inicializarComponentes();
        configurarLayout();
        configurarEventos();
        cargarDatos();
        configurarTimeoutInactividad();

        // Configuración responsive de la ventana
        // 80% ancho, 75% alto de la pantalla, mínimo 70% ancho, 65% alto
        ScreenUtils.setupResponsiveWindow(this, 0.80, 0.75, 0.70, 0.65);

        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Fondo de la ventana
        getContentPane().setBackground(AppColors.BACKGROUND);
    }

    private void inicializarComponentes() {
        // Campo de búsqueda estilizado
        txtBuscar = new JTextField(25);
        txtBuscar.setFont(AppFonts.BODY);
        txtBuscar.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        txtBuscar.setBackground(AppColors.SURFACE);

        // Checkbox estilizado
        chkSoloActivos = new JCheckBox("Solo préstamos activos", true);
        chkSoloActivos.setFont(AppFonts.BODY);
        chkSoloActivos.setBackground(AppColors.SURFACE);
        chkSoloActivos.setForeground(AppColors.TEXT_PRIMARY);

        // Estadísticas mejoradas
        lblEstadisticas = new JLabel();
        lblEstadisticas.setFont(AppFonts.SUBTITLE);
        lblEstadisticas.setForeground(AppColors.PRIMARY);

        // Botón regresar moderno con flecha
        btnRegresar = new JButton("←");
        btnRegresar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnRegresar.setBackground(AppColors.SURFACE);
        btnRegresar.setForeground(AppColors.TEXT_SECONDARY);
        btnRegresar.setPreferredSize(new Dimension(45, 45));
        btnRegresar.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER, 2, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        btnRegresar.setFocusPainted(false);
        btnRegresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegresar.setToolTipText("Regresar al formulario principal");

        btnRegresar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnRegresar.setBackground(AppColors.PRIMARY);
                btnRegresar.setForeground(Color.WHITE);
                btnRegresar.setBorder(new CompoundBorder(
                        new LineBorder(AppColors.PRIMARY, 2, true),
                        new EmptyBorder(5, 5, 5, 5)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnRegresar.setBackground(AppColors.SURFACE);
                btnRegresar.setForeground(AppColors.TEXT_SECONDARY);
                btnRegresar.setBorder(new CompoundBorder(
                        new LineBorder(AppColors.BORDER, 2, true),
                        new EmptyBorder(5, 5, 5, 5)
                ));
            }
        });

        String[] columnas = {
                "ID", "Alumno", "Matrícula", "Profesor", "Equipos",
                "Fecha", "H. Préstamo", "Salón", "Estado", "H. Devolución", "Acción"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10;
            }
        };

        tablaPrestamos = new JTable(modeloTabla);
        tablaPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPrestamos.setRowHeight(40);
        tablaPrestamos.getTableHeader().setReorderingAllowed(false);
        tablaPrestamos.setFont(AppFonts.BODY);
        tablaPrestamos.setGridColor(AppColors.BORDER);
        tablaPrestamos.setSelectionBackground(AppColors.PRIMARY.brighter());
        tablaPrestamos.setSelectionForeground(Color.WHITE);

        // Header de tabla estilizado
        tablaPrestamos.getTableHeader().setFont(AppFonts.LABEL);
        tablaPrestamos.getTableHeader().setBackground(AppColors.PRIMARY);
        tablaPrestamos.getTableHeader().setForeground(Color.WHITE);
        tablaPrestamos.getTableHeader().setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Ajustes de ancho de columnas
        tablaPrestamos.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaPrestamos.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaPrestamos.getColumnModel().getColumn(2).setPreferredWidth(90);
        tablaPrestamos.getColumnModel().getColumn(3).setPreferredWidth(120);
        tablaPrestamos.getColumnModel().getColumn(4).setPreferredWidth(200);
        tablaPrestamos.getColumnModel().getColumn(5).setPreferredWidth(80);
        tablaPrestamos.getColumnModel().getColumn(6).setPreferredWidth(80);
        tablaPrestamos.getColumnModel().getColumn(7).setPreferredWidth(80);
        tablaPrestamos.getColumnModel().getColumn(8).setPreferredWidth(70);
        tablaPrestamos.getColumnModel().getColumn(9).setPreferredWidth(90);
        tablaPrestamos.getColumnModel().getColumn(10).setPreferredWidth(100);

        // Aplica el renderizador para el color de estado y el botón de acción
        tablaPrestamos.getColumnModel().getColumn(8).setCellRenderer(new EstadoCellRenderer());
        tablaPrestamos.getColumnModel().getColumn(10).setCellRenderer(new ButtonRenderer(this));
        tablaPrestamos.getColumnModel().getColumn(10).setCellEditor(new ButtonEditor(tablaPrestamos, this));
        tablaPrestamos.setRowSelectionAllowed(false);
        tablaPrestamos.getColumnModel().getColumn(10).setResizable(false);
        tablaPrestamos.setCellSelectionEnabled(true);
    }

    // Clase interna para el renderizado de celdas
    private class EstadoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Por defecto, establecer el color de texto a negro para todas las celdas
            if (!isSelected) {
                cellComponent.setForeground(AppColors.TEXT_PRIMARY); // Color negro
            }

            // Si la columna es la de estado, aplicar la lógica de color
            int estadoColumnIndex = 8; // La columna de estado es la 8
            if (column == estadoColumnIndex) {
                Object estadoValue = table.getValueAt(row, estadoColumnIndex);
                if (estadoValue != null && "Activo".equals(estadoValue.toString())) {
                    cellComponent.setForeground(AppColors.SECONDARY); // Verde para "Activo"
                } else {
                    cellComponent.setForeground(AppColors.TEXT_PRIMARY); // Negro para los demás estados
                }
            }
            return cellComponent;
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        private AdminPanel adminPanel;

        public ButtonRenderer(AdminPanel adminPanel) {
            this.adminPanel = adminPanel;
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            String estado = (String) table.getValueAt(row, 8);
            if ("Activo".equals(estado)) {
                setText("DEVOLVER");
                setBackground(AppColors.SECONDARY);
                setForeground(Color.WHITE);
                setEnabled(true);
                setBorder(new EmptyBorder(5, 10, 5, 10));
            } else {
                setText("DEVUELTO");
                setBackground(AppColors.TEXT_SECONDARY);
                setForeground(Color.WHITE);
                setEnabled(false);
                setBorder(new EmptyBorder(5, 10, 5, 10));
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private JTable table;
        private AdminPanel adminPanel;

        public ButtonEditor(JTable table, AdminPanel adminPanel) {
            super(new JCheckBox());
            this.table = table;
            this.adminPanel = adminPanel;
            this.button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));

            button.addActionListener(e -> {
                int selectedRow = table.getEditingRow();
                if (selectedRow != -1) {
                    adminPanel.procesarDevolucionFila(selectedRow);
                    fireEditingStopped();
                }
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            String estado = (String) table.getValueAt(row, 8);
            if ("Activo".equals(estado)) {
                button.setText("DEVOLVER");
                button.setBackground(AppColors.SECONDARY);
                button.setForeground(Color.WHITE);
                button.setEnabled(true);
                button.setBorder(new EmptyBorder(5, 10, 5, 10));
            } else {
                button.setText("DEVUELTO");
                button.setBackground(AppColors.TEXT_SECONDARY);
                button.setForeground(Color.WHITE);
                button.setEnabled(false);
                button.setBorder(new EmptyBorder(5, 10, 5, 10));
            }
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Devolver";
        }
    }

    private void procesarDevolucionFila(int fila) {
        if (fila < 0) return;
        int filaReal = tablaPrestamos.convertRowIndexToModel(fila);
        int prestamoId = (Integer) modeloTabla.getValueAt(filaReal, 0);
        String alumno = (String) modeloTabla.getValueAt(filaReal, 1);
        String equipos = (String) modeloTabla.getValueAt(filaReal, 4);
        String estado = (String) modeloTabla.getValueAt(filaReal, 8);
        if (!"Activo".equals(estado)) {
            return;
        }
        LocalTime horaActual = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String horaDevolucionTexto = horaActual.format(formatter);
        int respuesta = JOptionPane.showConfirmDialog(this,
                "<html><div style='font-family: Segoe UI; font-size: 13px;'>" +
                        "¿Confirmar devolución de equipos?<br><br>" +
                        "<b>Alumno:</b> " + alumno + "<br>" +
                        "<b>Equipos:</b> " + equipos + "<br>" +
                        "<b>Hora de devolución:</b> " + horaDevolucionTexto +
                        "</div></html>",
                "Confirmar Devolución",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                if (ConexionDB.marcarComoDevuelto(prestamoId)) {
                    JOptionPane.showMessageDialog(this,
                            "<html><div style='font-family: Segoe UI; font-size: 13px;'>" +
                                    "¡Equipos devueltos exitosamente!<br><br>" +
                                    "<b>Equipos:</b> " + equipos + "<br>" +
                                    "<b>Alumno:</b> " + alumno + "<br>" +
                                    "<b>Hora de devolución:</b> " + horaDevolucionTexto +
                                    "</div></html>",
                            "Devolución Exitosa",
                            JOptionPane.INFORMATION_MESSAGE);
                    cargarDatos();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "<html><div style='font-family: Segoe UI; font-size: 13px;'>" +
                                    "Error al procesar la devolución.<br>Intente nuevamente." +
                                    "</div></html>",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "<html><div style='font-family: Segoe UI; font-size: 13px;'>" +
                                "Error inesperado: " + e.getMessage() +
                                "</div></html>",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void configurarLayout() {
        setLayout(new BorderLayout());

        // Header principal
        JPanel headerPanel = crearHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Panel central con tabla
        JPanel centerPanel = crearPanelCentral();
        add(centerPanel, BorderLayout.CENTER);

        // Panel inferior
        JPanel footerPanel = crearFooter();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.SURFACE);
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, AppColors.BORDER),
                new EmptyBorder(20, 25, 20, 25)
        ));

        // Panel izquierdo con botón regresar
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(AppColors.SURFACE);
        leftPanel.add(btnRegresar);

        // Panel central con título
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(AppColors.SURFACE);

        JLabel titulo = new JLabel("Panel de Administración");
        titulo.setFont(AppFonts.TITLE);
        titulo.setForeground(AppColors.PRIMARY);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Gestión de Devoluciones y Préstamos");
        subtitulo.setFont(AppFonts.CAPTION);
        subtitulo.setForeground(AppColors.TEXT_SECONDARY);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(titulo);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitulo);

        // Panel derecho con estadísticas
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(AppColors.SURFACE);
        rightPanel.add(lblEstadisticas);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel crearPanelCentral() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(AppColors.BACKGROUND);
        centerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(AppColors.SURFACE);
        searchPanel.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel searchLabel = new JLabel("Buscar:");
        searchLabel.setFont(AppFonts.LABEL);
        searchLabel.setForeground(AppColors.TEXT_PRIMARY);

        searchPanel.add(searchLabel);
        searchPanel.add(Box.createHorizontalStrut(8));
        searchPanel.add(txtBuscar);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(chkSoloActivos);

        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Tabla con scroll
        JScrollPane scrollPane = new JScrollPane(tablaPrestamos);
        scrollPane.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(AppColors.BORDER, 1, true),
                        "Préstamos Registrados",
                        0, 0, AppFonts.LABEL, AppColors.TEXT_PRIMARY
                ),
                new EmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.setBackground(AppColors.SURFACE);
        scrollPane.getViewport().setBackground(AppColors.SURFACE);

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel crearFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(AppColors.SURFACE);
        footer.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, AppColors.BORDER),
                new EmptyBorder(12, 10, 12, 10)
        ));

        JLabel lblInfo = new JLabel("Selecciona el equipo a devolver usando el botón de la tabla");
        lblInfo.setForeground(AppColors.TEXT_SECONDARY);
        lblInfo.setFont(AppFonts.CAPTION);
        footer.add(lblInfo);

        return footer;
    }

    private void configurarEventos() {
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filtrarDatos();
            }
        });
        chkSoloActivos.addActionListener(e -> filtrarDatos());
        btnRegresar.addActionListener(e -> {
            if (inactivityTimer != null) {
                inactivityTimer.stop();
            }
            this.dispose();
        });
    }

    private void cargarDatos() {
        try {
            modeloTabla.setRowCount(0);
            List<Prestamo> prestamos = ConexionDB.getTodosPrestamos();
            for (Prestamo prestamo : prestamos) {
                Object[] fila = {
                        prestamo.getId(),
                        prestamo.getAlumnoNombre(),
                        prestamo.getMatricula() != null ? prestamo.getMatricula() : "",
                        prestamo.getProfesorNombre(),
                        prestamo.getEquipo(),
                        prestamo.getFecha(),
                        prestamo.getHoraPrestamo(),
                        prestamo.getSalon() != null ? prestamo.getSalon() : "",
                        prestamo.getEstadoTexto(),
                        prestamo.getHoraDevolucion() != null ? prestamo.getHoraDevolucion() : "",
                        "Acción"
                };
                modeloTabla.addRow(fila);
            }
            actualizarEstadisticas();
            filtrarDatos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "<html><div style='font-family: Segoe UI; font-size: 13px;'>" +
                            "Error al cargar los datos: " + e.getMessage() +
                            "</div></html>",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void filtrarDatos() {
        String busqueda = txtBuscar.getText().toLowerCase().trim();
        boolean soloActivos = chkSoloActivos.isSelected();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tablaPrestamos.setRowSorter(sorter);
        RowFilter<DefaultTableModel, Object> filtro = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                if (soloActivos) {
                    String estado = entry.getStringValue(8);
                    if (!"Activo".equals(estado)) {
                        return false;
                    }
                }
                if (!busqueda.isEmpty()) {
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        String valor = entry.getStringValue(i).toLowerCase();
                        if (valor.contains(busqueda)) {
                            return true;
                        }
                    }
                    return false;
                }
                return true;
            }
        };
        sorter.setRowFilter(filtro);
    }
    private void actualizarEstadisticas() {
        try {
            String estadisticas = ConexionDB.getEstadisticas();
            lblEstadisticas.setText(estadisticas);
        } catch (Exception e) {
            lblEstadisticas.setText("Estadísticas no disponibles");
        }
    }
    private void configurarTimeoutInactividad() {
        inactivityTimer = new Timer(INACTIVITY_TIMEOUT, e -> {
            dispose();
        });
        inactivityTimer.setRepeats(false);
        inactivityTimer.start();
        agregarListenersActividad(this);
    }
    private void agregarListenersActividad(Container container) {
        MouseMotionListener mouseMotionListener = new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                reiniciarTimer();
            }
        };
        MouseListener mouseListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                reiniciarTimer();
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                reiniciarTimer();
            }
        };
        KeyListener keyListener = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                reiniciarTimer();
            }
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                reiniciarTimer();
            }
        };
        aplicarListenersRecursivo(container, mouseMotionListener, mouseListener, keyListener);
    }
    private void aplicarListenersRecursivo(Container container,
                                           MouseMotionListener mouseMotionListener,
                                           MouseListener mouseListener,
                                           KeyListener keyListener) {
        container.addMouseMotionListener(mouseMotionListener);
        container.addMouseListener(mouseListener);
        container.addKeyListener(keyListener);
        for (Component component : container.getComponents()) {
            component.addMouseMotionListener(mouseMotionListener);
            component.addMouseListener(mouseListener);
            component.addKeyListener(keyListener);
            if (component instanceof Container) {
                aplicarListenersRecursivo((Container) component,
                        mouseMotionListener, mouseListener, keyListener);
            }
        }
    }
    private void reiniciarTimer() {
        if (inactivityTimer != null && inactivityTimer.isRunning()) {
            inactivityTimer.restart();
        }
    }
    @Override
    public void dispose() {
        if (inactivityTimer != null) {
            inactivityTimer.stop();
        }
        super.dispose();
    }
}