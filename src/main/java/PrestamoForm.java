import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class PrestamoForm extends JPanel {

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

    // Clase interna para fuentes (version responsive)
    private static class AppFonts {
        // Tamaños base de fuente
        private static final int BASE_TITLE = 32;
        private static final int BASE_SUBTITLE = 20;
        private static final int BASE_BODY = 17;
        private static final int BASE_CAPTION = 17;
        private static final int BASE_BUTTON = 17;
        private static final int BASE_LABEL = 16;

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

    // Iconos Unicode
    private static final String ICON_USER = "👤";
    private static final String ICON_ID = "";
    private static final String ICON_TEACHER = "👨‍🏫";
    private static final String ICON_LAPTOP = "💻";
    private static final String ICON_PROJECTOR = "📽️";
    private static final String ICON_ROOM = "🏫";
    private static final String ICON_NOTES = "📝";
    private static final String ICON_SAVE = "💾";
    private static final String ICON_ADMIN = "⚙️";

    private JFrame parentFrame;
    private JTextField txtNombreAlumno;
    private JTextField txtProfesor;
    private JCheckBox[] chkLaptops;
    private JCheckBox[] chkProyectores;
    private JTextField txtSalon;
    private JTextArea txtObservaciones;
    private JButton btnPrestar;
    private JButton btnAdmin;
    private JLabel lblStatus;
    private JPanel statusPanel;
    private JPanel laptopsPanel;
    private JPanel proyectoresPanel;
    private JCheckBox[] chkBocinas;
    private JPanel bocinasPanel;

    public PrestamoForm(JFrame parent) {
        this.parentFrame = parent;
        inicializarComponentes();
        configurarPanelesResponsive();
        configurarLayout();
        configurarEventos();
        configurarTeclaEnter();
        configurarValidacion();
        actualizarEquiposDisponibles();
        mostrarMensajeBienvenida();
    }

    private void inicializarComponentes() {
        // Configurar el panel principal
        setBackground(AppColors.BACKGROUND);

        // Campos de texto mejorados
        txtNombreAlumno = crearCampoTextoEstilizado(20);
        txtProfesor = crearCampoTextoEstilizado(20);
        txtSalon = crearCampoTextoEstilizado(12);

        // Área de observaciones mejorada - SIN SCROLL INICIAL
        txtObservaciones = new JTextArea(3, 20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setFont(AppFonts.BODY);
        txtObservaciones.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        txtObservaciones.setBackground(AppColors.SURFACE);
// Establecer tamaño fijo para evitar scroll inicial
        txtObservaciones.setPreferredSize(new Dimension(280, 70));
        txtObservaciones.setMinimumSize(new Dimension(250, 70));

        // Inicializar checkboxes para equipos
        chkLaptops = new JCheckBox[7];
        chkProyectores = new JCheckBox[7];


        // Crear checkboxes para laptops (mÃ¡s compactos) - TAMAÑO FIJO
        laptopsPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        laptopsPanel.setBackground(AppColors.SURFACE);
        laptopsPanel.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
// Establecer tamaño fijo desde el inicio
        laptopsPanel.setPreferredSize(new Dimension(380, 110));
        laptopsPanel.setMinimumSize(new Dimension(400, 90));
        laptopsPanel.setMaximumSize(new Dimension(400, 90));

        for (int i = 0; i < 7; i++) {
            chkLaptops[i] = new JCheckBox("Laptop " + (i + 1));
            chkLaptops[i].setFont(new Font("Segoe UI", Font.PLAIN, 14)); // en lugar de AppFonts.CAPTION
            chkLaptops[i].setBackground(AppColors.SURFACE);
            // Establecer tamaño fijo para cada checkbox
            chkLaptops[i].setPreferredSize(new Dimension(120, 32));
            laptopsPanel.add(chkLaptops[i]);
        }

// Crear checkboxes para proyectores (mÃ¡s compactos) - TAMAÑO FIJO
        proyectoresPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        proyectoresPanel.setBackground(AppColors.SURFACE);
        proyectoresPanel.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
// Establecer tamaño fijo desde el inicio
        proyectoresPanel.setPreferredSize(new Dimension(380, 110));
        proyectoresPanel.setMinimumSize(new Dimension(400, 90));
        proyectoresPanel.setMaximumSize(new Dimension(400, 90));

        for (int i = 0; i < 7; i++) {
            chkProyectores[i] = new JCheckBox("Proyector " + (i + 1));
            chkProyectores[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            chkProyectores[i].setBackground(AppColors.SURFACE);
            // Establecer tamaño fijo para cada checkbox
            chkProyectores[i].setPreferredSize(new Dimension(120, 32));
            proyectoresPanel.add(chkProyectores[i]);
        }

// Crear checkbox para bocinas - TAMAÑO FIJO
        // Inicializar checkboxes para bocinas
        chkBocinas = new JCheckBox[5];

// Crear checkboxes para bocinas (más compactos) - TAMAÑO FIJO
        bocinasPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        bocinasPanel.setBackground(AppColors.SURFACE);
        bocinasPanel.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
// Establecer tamaño fijo desde el inicio
        bocinasPanel.setPreferredSize(new Dimension(380, 80));
        bocinasPanel.setMinimumSize(new Dimension(400, 70));
        bocinasPanel.setMaximumSize(new Dimension(400, 70));

        for (int i = 0; i < 5; i++) {
            chkBocinas[i] = new JCheckBox("Bocina " + (i + 1));
            chkBocinas[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            chkBocinas[i].setBackground(AppColors.SURFACE);
            // Establecer tamaño fijo para cada checkbox
            chkBocinas[i].setPreferredSize(new Dimension(120, 32));
            bocinasPanel.add(chkBocinas[i]);
        }

        // Botones mejorados
        btnPrestar = crearBotonPrincipal(ICON_SAVE + " REGISTRAR PRÉSTAMO", AppColors.SECONDARY);
        btnAdmin = crearBotonSecundario( " DEVOLUCIÓN", AppColors.SECONDARY);
        btnAdmin.setFont(AppFonts.BUTTON);
        btnAdmin.setPreferredSize(new Dimension(180, 35));


        // Panel de estado - ESPACIO FIJO DESDE EL INICIO
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(AppColors.BACKGROUND); // Cambiar a BACKGROUND para que se vea
        statusPanel.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
// ESTABLECER ALTURA FIJA - siempre visible
        statusPanel.setPreferredSize(new Dimension(0, 40));
        statusPanel.setMinimumSize(new Dimension(0, 40));
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        lblStatus = new JLabel(""); // Vacío al inicio
        lblStatus.setFont(AppFonts.CAPTION);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER); // Centrar texto
        statusPanel.add(lblStatus, BorderLayout.CENTER);

        statusPanel.setVisible(true);

        lblStatus = new JLabel();
        lblStatus.setFont(AppFonts.CAPTION);
        statusPanel.add(lblStatus, BorderLayout.CENTER);
    }

//    private JTextField crearCampoTextoEstilizado(int columnas) {
//        JTextField campo = new JTextField(columnas);
//        campo.setFont(AppFonts.BODY);
//        campo.setBorder(new CompoundBorder(
//                new LineBorder(AppColors.BORDER, 1, true),
//                new EmptyBorder(6, 10, 6, 10)
//        ));
//        campo.setBackground(AppColors.SURFACE);
//        campo.setPreferredSize(new Dimension(300, 42));
//        return campo;
//    }

//    private JButton crearBotonPrincipal(String texto, Color color) {
//        JButton boton = new JButton(texto);
//        boton.setFont(AppFonts.BUTTON);
//        boton.setBackground(color);
//        boton.setForeground(Color.WHITE);
//        boton.setPreferredSize(new Dimension(280, 52));
//        boton.setBorder(new EmptyBorder(10, 20, 15, 20));
//        boton.setFocusPainted(false);
//        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//        // Efecto hover
//        boton.addMouseListener(new java.awt.event.MouseAdapter() {
//            @Override
//            public void mouseEntered(java.awt.event.MouseEvent e) {
//                boton.setBackground(color.darker());
//            }
//
//            @Override
//            public void mouseExited(java.awt.event.MouseEvent e) {
//                boton.setBackground(color);
//            }
//        });
//
//        return boton;
//    }

    private JButton crearBotonSecundario(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        boton.setBackground(AppColors.SURFACE);
        boton.setForeground(color);
        boton.setPreferredSize(new Dimension(100, 36));
        boton.setBorder(new CompoundBorder(
                new LineBorder(color, 3, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                boton.setBackground(color);
                boton.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                boton.setBackground(AppColors.SURFACE);
                boton.setForeground(color);
            }
        });

        return boton;
    }

    private JLabel crearEtiqueta(String texto, String icono) {
        JLabel etiqueta = new JLabel(icono + " " + texto);
        etiqueta.setFont(AppFonts.LABEL);
        etiqueta.setForeground(AppColors.TEXT_PRIMARY);
        return etiqueta;
    }

    private void configurarLayout() {
        setLayout(new BorderLayout());

        // Header con título y botón admin
        JPanel headerPanel = crearHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Panel principal con formulario
        JPanel mainPanel = crearPanelPrincipal();
        add(mainPanel, BorderLayout.CENTER);

        // Panel de estado en la parte inferior - SIEMPRE PRESENTE
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppColors.SURFACE);
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER),
                new EmptyBorder(15, 25, 15, 25)
        ));

        // Título principal
        JLabel titulo = new JLabel("📚 Sistema de Préstamos");
        titulo.setFont(AppFonts.TITLE);
        titulo.setForeground(AppColors.PRIMARY);

        // Subtítulo
        JLabel subtitulo = new JLabel("Gestión de Material Audiovisual - Preparatoria No. 36");
        subtitulo.setFont(AppFonts.CAPTION);
        subtitulo.setForeground(AppColors.TEXT_SECONDARY);

        JPanel tituloPanel = new JPanel();
        tituloPanel.setLayout(new BoxLayout(tituloPanel, BoxLayout.Y_AXIS));
        tituloPanel.setBackground(AppColors.SURFACE);
        tituloPanel.add(titulo);
        tituloPanel.add(Box.createVerticalStrut(3));
        tituloPanel.add(subtitulo);

        header.add(tituloPanel, BorderLayout.WEST);
        header.add(btnAdmin, BorderLayout.EAST);

        return header;
    }

    private JPanel crearPanelPrincipal() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppColors.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(25, 35, 25, 35));

        // Card contenedor del formulario
        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(AppColors.SURFACE);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));
        // Panel principal dividido en dos columnas - CENTRADO
        JPanel contenidoPanel = new JPanel(new GridBagLayout());
        contenidoPanel.setBackground(AppColors.SURFACE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 25, 8, 25); // Aumentar espaciado horizontal
        gbc.anchor = GridBagConstraints.NORTH; // Centrar contenido

// Columna izquierda - Información personal
        JPanel columnaIzquierda = crearColumnaIzquierda();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Cambiar de BOTH a NONE
        gbc.anchor = GridBagConstraints.NORTH; // Centrar
        contenidoPanel.add(columnaIzquierda, gbc);

// Columna derecha - Equipos y datos adicionales
        JPanel columnaDerecha = crearColumnaDerecha();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.NORTH; // Agregar esta línea
        contenidoPanel.add(columnaDerecha, gbc);

        formCard.add(contenidoPanel);

        // Botón centrado
        formCard.add(Box.createVerticalStrut(20));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(AppColors.SURFACE);
        buttonPanel.add(btnPrestar);
        formCard.add(buttonPanel);

        mainPanel.add(formCard);
        return mainPanel;
    }

    private JPanel crearColumnaIzquierda() {
        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setBackground(AppColors.SURFACE);
        columna.setBorder(new EmptyBorder(0, 0, 0, 15));

        // Sección: Información del Estudiante
        columna.add(crearTituloSeccion("👨‍🎓 INFORMACIÓN DEL ESTUDIANTE"));
        columna.add(Box.createVerticalStrut(10));

        columna.add(crearCampoFormulario("Nombre Completo", ICON_USER, txtNombreAlumno));
        columna.add(crearCampoFormulario("Profesor", ICON_TEACHER, txtProfesor));
        columna.add(crearCampoFormulario("Grupo", ICON_ROOM, txtSalon));

        columna.add(Box.createVerticalStrut(15));

        // Observaciones - SIN SCROLL VISIBLE
        columna.add(crearEtiqueta("Observaciones (Opcional)", ICON_NOTES));
        columna.add(Box.createVerticalStrut(5));

        txtObservaciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(txtObservaciones);

        return columna;
    }

    private JPanel crearColumnaDerecha() {
        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setBackground(AppColors.SURFACE);
        columna.setBorder(new EmptyBorder(0, 15, 0, 0));

        // Sección: Selección de Equipos
        columna.add(crearTituloSeccion("🔧 SELECCIÓN DE EQUIPOS"));
        columna.add(Box.createVerticalStrut(10));




        // Laptops - TAMAÑOS YA ESTABLECIDOS
        columna.add(crearEtiqueta("Laptops", ICON_LAPTOP));
        columna.add(Box.createVerticalStrut(5));
// No necesitamos setMaximumSize aquí ya que está fijo desde inicializarComponentes
        laptopsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(laptopsPanel);

        columna.add(Box.createVerticalStrut(10));

// Proyectores - TAMAÑOS YA ESTABLECIDOS
        columna.add(crearEtiqueta("Proyectores", ICON_PROJECTOR));
        columna.add(Box.createVerticalStrut(5));
// No necesitamos setMaximumSize aquí ya que está fijo desde inicializarComponentes
        proyectoresPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(proyectoresPanel);

        columna.add(Box.createVerticalStrut(10));

// Bocinas - TAMAÑOS YA ESTABLECIDOS
        columna.add(crearEtiqueta("Bocinas", "🔊"));
        columna.add(Box.createVerticalStrut(5));
// No necesitamos setMaximumSize aquí ya que está fijo desde inicializarComponentes
        bocinasPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(bocinasPanel);

        return columna;
    }

    private JLabel crearTituloSeccion(String titulo) {
        JLabel seccionLabel = new JLabel(titulo);
        seccionLabel.setFont(AppFonts.SUBTITLE);
        seccionLabel.setForeground(AppColors.PRIMARY);
        seccionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return seccionLabel;
    }

    private JPanel crearCampoFormulario(String etiqueta, String icono, JComponent campo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppColors.SURFACE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = crearEtiqueta(etiqueta, icono);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(3));

        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(campo);
        panel.add(Box.createVerticalStrut(8));

        return panel;
    }

    private void configurarEventos() {
        btnPrestar.addActionListener(e -> procesarPrestamo());
        btnAdmin.addActionListener(e -> abrirPanelAdmin());

        // Agregar validación de checkboxes
        configurarValidacionCheckboxes();
    }
    private void configurarTeclaEnter() {
        // Configurar Enter para activar el botón de préstamo
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

        // Crear la acción
        Action enterAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnPrestar.isEnabled()) {
                    procesarPrestamo();
                }
            }
        };

        // Mapear la tecla Enter a la acción
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterKeyStroke, "enterPressed");
        this.getActionMap().put("enterPressed", enterAction);
    }


    private void configurarValidacionCheckboxes() {
        // Validación para laptops - solo una seleccionada
        for (int i = 0; i < chkLaptops.length; i++) {
            final int index = i;
            chkLaptops[i].addActionListener(e -> {
                if (chkLaptops[index].isSelected()) {
                    // Deseleccionar todas las otras laptops
                    for (int j = 0; j < chkLaptops.length; j++) {
                        if (j != index) {
                            chkLaptops[j].setSelected(false);
                        }
                    }
                }
            });
        }

        // Validación para proyectores - solo uno seleccionado
        for (int i = 0; i < chkProyectores.length; i++) {
            final int index = i;
            chkProyectores[i].addActionListener(e -> {
                if (chkProyectores[index].isSelected()) {
                    // Deseleccionar todos los otros proyectores
                    for (int j = 0; j < chkProyectores.length; j++) {
                        if (j != index) {
                            chkProyectores[j].setSelected(false);
                        }
                    }
                }
            });
        }
        // Validación para bocinas - solo una seleccionada
        for (int i = 0; i < chkBocinas.length; i++) {
            final int index = i;
            chkBocinas[i].addActionListener(e -> {
                if (chkBocinas[index].isSelected()) {
                    // Deseleccionar todas las otras bocinas
                    for (int j = 0; j < chkBocinas.length; j++) {
                        if (j != index) {
                            chkBocinas[j].setSelected(false);
                        }
                    }
                }
            });
        }
    }

    private void configurarValidacion() {
        // CAMBIAR estas líneas para usar validación de texto:
        configurarValidacionTexto(txtNombreAlumno, "Nombre requerido");
        configurarValidacionTexto(txtProfesor, "Profesor requerido");


        // Salón puede tener números y letras, solo convertir a mayúsculas:
        configurarValidacionSalon(txtSalon, "Salón requerido");
    }

    private void configurarValidacionCampo(JTextField campo, String mensajeError) {
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validarCampoVisual(campo);
            }
        });
    }

    private void validarCampoVisual(JTextField campo) {
        String texto = campo.getText().trim();
        boolean esValido = !texto.isEmpty();

        if (esValido) {
            campo.setBorder(new CompoundBorder(
                    new LineBorder(AppColors.SECONDARY, 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        } else {
            campo.setBorder(new CompoundBorder(
                    new LineBorder(AppColors.DANGER, 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        }
    }

    private void configurarValidacionTexto(JTextField campo, String mensajeError) {
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Convertir a mayúsculas al perder el foco
                String texto = campo.getText().trim().toUpperCase();
                campo.setText(texto);
                validarCampoVisual(campo);
            }
        });

        campo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();

                // Permitir solo letras, espacios y teclas de control
                if (!Character.isLetter(c) &&
                        c != ' ' &&
                        !Character.isISOControl(c)) {
                    e.consume();
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                // Convertir a mayúsculas en tiempo real
                String texto = campo.getText().toUpperCase();
                int posicion = campo.getCaretPosition();
                campo.setText(texto);
                // Mantener la posición del cursor
                try {
                    campo.setCaretPosition(Math.min(posicion, texto.length()));
                } catch (IllegalArgumentException ex) {
                    // Ignorar si la posición no es válida
                }
            }
        });
    }

    private void configurarValidacionSalon(JTextField campo, String mensajeError) {
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Convertir a mayúsculas al perder el foco
                String texto = campo.getText().trim().toUpperCase();
                campo.setText(texto);
                validarCampoVisual(campo);
            }
        });

        campo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                // Convertir a mayúsculas en tiempo real
                String texto = campo.getText().toUpperCase();
                int posicion = campo.getCaretPosition();
                campo.setText(texto);
                try {
                    campo.setCaretPosition(Math.min(posicion, texto.length()));
                } catch (IllegalArgumentException ex) {
                    // Ignorar si la posición no es válida
                }
            }
        });
    }

    private void procesarPrestamo() {
        if (!validarCampos()) {
            return;
        }

        // Mostrar indicador de carga
        mostrarEstado("Procesando prestamo...", AppColors.WARNING, false);
        btnPrestar.setEnabled(false);

        // Simular proceso asincrono
        SwingUtilities.invokeLater(() -> {
            try {
                // Obtener equipos seleccionados
                String equiposSeleccionados = obtenerEquiposSeleccionados();

                // Crear objeto Prestamo
                Prestamo prestamo = new Prestamo(
                        txtNombreAlumno.getText().trim(),
                        txtProfesor.getText().trim(),
                        equiposSeleccionados, // Equipos múltiples separados por coma
                        "", // Hora se asigna automáticamente
                        txtSalon.getText().trim(),
                        txtObservaciones.getText().trim()
                );

                // Registrar en la base de datos
                if (ConexionDB.registrarPrestamo(prestamo)) {
                    mostrarEstado("✅ Préstamo registrado exitosamente", AppColors.SECONDARY, true);
                    actualizarEquiposDisponibles(); // Actualizar ANTES de mostrar notificación
                    mostrarNotificacionExito(equiposSeleccionados);
                    limpiarFormulario();
                } else {
                    mostrarEstado("❌ Error al registrar el préstamo", AppColors.DANGER, true);
                }

            } catch (Exception ex) {
                mostrarEstado("❌ Error inesperado: " + ex.getMessage(), AppColors.DANGER, true);
                ex.printStackTrace();
            } finally {
                btnPrestar.setEnabled(true);
                // Ocultar estado después de 5 segundos
                Timer timer = new Timer(5000, e -> ocultarEstado());
                timer.setRepeats(false);
                timer.start();
            }
        });
    }

    private String obtenerEquiposSeleccionados() {
        List<String> equipos = new ArrayList<>();

        // Agregar laptops seleccionadas
        for (int i = 0; i < chkLaptops.length; i++) {
            if (chkLaptops[i].isSelected()) {
                equipos.add("Laptop " + (i + 1));
            }
        }

        // Agregar proyectores seleccionados
        for (int i = 0; i < chkProyectores.length; i++) {
            if (chkProyectores[i].isSelected()) {
                equipos.add("Proyector " + (i + 1));
            }
        }

        // Agregar bocinas seleccionadas
        for (int i = 0; i < chkBocinas.length; i++) {
            if (chkBocinas[i].isSelected()) {
                equipos.add("Bocina " + (i + 1));
            }
        }

        return String.join(", ", equipos);
    }

    private void mostrarNotificacionExito(String equipos) {
        LocalTime horaActual = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        String mensaje = String.format(
                "Préstamo Registrado Exitosamente\n\n" +
                        "📚 Equipos: %s\n" +
                        "👤 Estudiante: %s\n" +
                        "🕐 Hora préstamo: %s\n\n" +
                        "¡Recuerda devolver los equipos cuando termines!",
                equipos,
                txtNombreAlumno.getText(),
                horaActual.format(formatter)
        );

        JOptionPane.showMessageDialog(this, mensaje, "✅ Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarEstado(String mensaje, Color color, boolean autohide) {
        lblStatus.setText(mensaje);
        lblStatus.setForeground(color);
        statusPanel.setBackground(AppColors.BACKGROUND); // Asegurar fondo visible
        statusPanel.revalidate(); // Forzar actualización
        statusPanel.repaint();
    }

    private void ocultarEstado() {
        lblStatus.setText(""); // Vacío en lugar de espacio
        statusPanel.revalidate();
        statusPanel.repaint();
    }

    private void mostrarMensajeBienvenida() {
        SwingUtilities.invokeLater(() -> {
            mostrarEstado("Bienvenido al sistema de prestamos. Los horarios se registran automaticamente.",
                    AppColors.PRIMARY, false); // Cambiar a PRIMARY para mejor contraste
            Timer timer = new Timer(10000, e -> { // Aumentar a 5 segundos para ver mejor
                ocultarEstado();
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    private boolean validarCampos() {
        if (txtNombreAlumno.getText().trim().isEmpty()) {
            mostrarError("El nombre del estudiante es obligatorio");
            txtNombreAlumno.requestFocus();
            return false;
        }
        if (!txtNombreAlumno.getText().trim().matches("[A-ZÁÉÍÓÚÑa-záéíóúñ ]+")) {
            mostrarError("El nombre del estudiante debe contener solo letras");
            txtNombreAlumno.requestFocus();
            return false;
        }

        if (txtProfesor.getText().trim().isEmpty()) {
            mostrarError("El nombre del profesor es obligatorio");
            txtProfesor.requestFocus();
            return false;
        }
        if (!txtProfesor.getText().trim().matches("[A-ZÁÉÍÓÚÑa-záéíóúñ ]+")) {
            mostrarError("El nombre del profesor debe contener solo letras");
            txtProfesor.requestFocus();
            return false;
        }

        if (txtSalon.getText().trim().isEmpty()) {
            mostrarError("El salon/ubicacion es obligatorio");
            txtSalon.requestFocus();
            return false;
        }

        // Validar que al menos un equipo esté seleccionado
        boolean tieneSeleccion = false;
        for (JCheckBox chk : chkLaptops) {
            if (chk.isSelected()) {
                tieneSeleccion = true;
                break;
            }
        }
        if (!tieneSeleccion) {
            for (JCheckBox chk : chkProyectores) {
                if (chk.isSelected()) {
                    tieneSeleccion = true;
                    break;
                }
            }
        }

        if (!tieneSeleccion) {
            for (JCheckBox chk : chkBocinas) {
                if (chk.isSelected()) {
                    tieneSeleccion = true;
                    break;
                }
            }
        }

        if (!tieneSeleccion) {
            mostrarError("Debe seleccionar al menos un equipo (laptop, proyector o bocinas)");
            return false;
        }

        return true;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "⚠️ Error de Validación", JOptionPane.WARNING_MESSAGE);
    }

    private void limpiarFormulario() {
        txtNombreAlumno.setText("");
        txtProfesor.setText("");
        txtSalon.setText("");
        txtObservaciones.setText("");

        // Deseleccionar todos los checkboxes
        for (JCheckBox chk : chkLaptops) {
            chk.setSelected(false);
        }
        for (JCheckBox chk : chkProyectores) {
            chk.setSelected(false);
        }

        // Limpiar bocinas
        for (JCheckBox chk : chkBocinas) {
            chk.setSelected(false);
        }

        // Resetear bordes
        JTextField[] campos = {txtNombreAlumno, txtProfesor, txtSalon};
        for (JTextField campo : campos) {
            campo.setBorder(new CompoundBorder(
                    new LineBorder(AppColors.BORDER, 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        }

        txtNombreAlumno.requestFocus();
    }

    private void actualizarEquiposDisponibles() {
        List<String> equiposDisponibles = ConexionDB.getEquiposDisponibles();

        // Actualizar laptops - MANTENER TAMAÑO FIJO
        for (int i = 0; i < chkLaptops.length; i++) {
            String nombreLaptop = "Laptop " + (i + 1);
            boolean disponible = equiposDisponibles.contains(nombreLaptop);
            chkLaptops[i].setEnabled(disponible);
            if (!disponible) {
                chkLaptops[i].setSelected(false);
                chkLaptops[i].setText("L" + (i + 1) + " (No disp.)"); // Texto más corto
                chkLaptops[i].setForeground(AppColors.TEXT_SECONDARY);
            } else {
                chkLaptops[i].setText("Laptop " + (i + 1));
                chkLaptops[i].setForeground(AppColors.TEXT_PRIMARY);
            }
        }

        // Actualizar proyectores - MANTENER TAMAÑO FIJO
        for (int i = 0; i < chkProyectores.length; i++) {
            String nombreProyector = "Proyector " + (i + 1);
            boolean disponible = equiposDisponibles.contains(nombreProyector);
            chkProyectores[i].setEnabled(disponible);
            if (!disponible) {
                chkProyectores[i].setSelected(false);
                chkProyectores[i].setText("P" + (i + 1) + " (No disp.)"); // Texto más corto
                chkProyectores[i].setForeground(AppColors.TEXT_SECONDARY);
            } else {
                chkProyectores[i].setText("Proyector " + (i + 1));
                chkProyectores[i].setForeground(AppColors.TEXT_PRIMARY);
            }
        }

        // Actualizar bocinas - MANTENER TAMAÑO FIJO
        for (int i = 0; i < chkBocinas.length; i++) {
            String nombreBocina = "Bocina " + (i + 1);
            boolean disponible = equiposDisponibles.contains(nombreBocina);
            chkBocinas[i].setEnabled(disponible);
            if (!disponible) {
                chkBocinas[i].setSelected(false);
                chkBocinas[i].setText("B" + (i + 1) + " (No disp.)"); // Texto más corto
                chkBocinas[i].setForeground(AppColors.TEXT_SECONDARY);
            } else {
                chkBocinas[i].setText("Bocina " + (i + 1));
                chkBocinas[i].setForeground(AppColors.TEXT_PRIMARY);
            }
        }

// Verificar si hay equipos disponibles (incluyendo bocinas)
        boolean hayBocinasDisponibles = false;
        for (JCheckBox chk : chkBocinas) {
            if (chk.isEnabled()) {
                hayBocinasDisponibles = true;
                break;
            }
        }
        boolean hayDisponibles = !equiposDisponibles.isEmpty() || hayBocinasDisponibles;
        btnPrestar.setEnabled(hayDisponibles);

        if (!hayDisponibles) {
            mostrarEstado("⚠️ No hay equipos disponibles en este momento", AppColors.WARNING, true);
        }

        // Forzar repaint sin cambiar layout
        repaint();
    }

    private void abrirPanelAdmin() {
        // GUARDAR el tamaño y posición actuales
        Dimension originalSize = parentFrame.getSize();
        Point originalLocation = parentFrame.getLocation();

        // Ocultar la ventana principal
        parentFrame.setVisible(false);

        AdminPanel adminPanel = new AdminPanel(parentFrame);
        adminPanel.setVisible(true);

        // Actualizar equipos cuando regrese del panel admin
        adminPanel.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                // RESTAURAR el tamaño y posición originales
                parentFrame.setSize(originalSize);
                parentFrame.setLocation(originalLocation);

                // Mostrar nuevamente la ventana principal
                parentFrame.setVisible(true);
                actualizarEquiposDisponibles();
            }
        });
    }



    // Agregar estos métodos a PrestamoForm.java para hacer componentes responsive:

    private Dimension getResponsiveSize(int baseWidth, int baseHeight) {
        double scaleFactor = ScreenUtils.getScaleFactor();
        Dimension screenSize = ScreenUtils.getScreenSize();

        // Calcular tamaño basado en porcentaje de pantalla y factor de escala
        int width = Math.max(baseWidth, (int)(baseWidth * Math.sqrt(scaleFactor)));
        int height = Math.max(baseHeight, (int)(baseHeight * Math.sqrt(scaleFactor)));

        // Limitar al tamaño de pantalla disponible
        width = Math.min(width, screenSize.width / 4);
        height = Math.min(height, screenSize.height / 15);

        return new Dimension(width, height);
    }

    private JTextField crearCampoTextoEstilizado(int columnas) {
        JTextField campo = new JTextField(columnas);
        campo.setFont(AppFonts.BODY);
        campo.setBorder(new CompoundBorder(
                new LineBorder(AppColors.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        campo.setBackground(AppColors.SURFACE);

        // Tamaño responsive
        Dimension size = getResponsiveSize(300, 42);
        campo.setPreferredSize(size);
        campo.setMinimumSize(new Dimension(250, 35));

        return campo;
    }

    private JButton crearBotonPrincipal(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(AppFonts.BUTTON);
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);

        // Tamaño responsive
        Dimension size = getResponsiveSize(280, 52);
        boton.setPreferredSize(size);
        boton.setMinimumSize(new Dimension(200, 40));

        boton.setBorder(new EmptyBorder(10, 20, 15, 20));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                boton.setBackground(color.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                boton.setBackground(color);
            }
        });

        return boton;
    }

    private void configurarPanelesResponsive() {
        Dimension screenSize = ScreenUtils.getScreenSize();
        double scaleFactor = ScreenUtils.getScaleFactor();

        // Ajustar tamaños de paneles basado en el tamaño de pantalla
        int panelWidth = Math.max(380, (int)(screenSize.width * 0.3));
        int laptopPanelHeight = Math.max(90, (int)(110 * Math.sqrt(scaleFactor)));
        int projectorPanelHeight = Math.max(90, (int)(110 * Math.sqrt(scaleFactor)));
        int speakerPanelHeight = Math.max(45, (int)(55 * Math.sqrt(scaleFactor)));

        // Configurar panel de laptops
        laptopsPanel.setPreferredSize(new Dimension(panelWidth, laptopPanelHeight));
        laptopsPanel.setMinimumSize(new Dimension(300, 80));

        // Configurar panel de proyectores
        proyectoresPanel.setPreferredSize(new Dimension(panelWidth, projectorPanelHeight));
        proyectoresPanel.setMinimumSize(new Dimension(300, 80));

        // Configurar panel de bocinas
        // Configurar panel de bocinas

        bocinasPanel.setPreferredSize(new Dimension(panelWidth, speakerPanelHeight));
        bocinasPanel.setMinimumSize(new Dimension(300, 60));

        // Configurar área de observaciones
        int textAreaHeight = Math.max(70, (int)(70 * Math.sqrt(scaleFactor)));
        txtObservaciones.setPreferredSize(new Dimension(280, textAreaHeight));
        txtObservaciones.setMinimumSize(new Dimension(200, 60));
    }
}