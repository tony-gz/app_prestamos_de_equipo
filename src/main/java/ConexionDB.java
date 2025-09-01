import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ConexionDB {
    private static final String DB_NAME = "prestamos.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_NAME;

    // Inicializar la base de datos y crear tablas si no existen
    public static void inicializarDB() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                crearTablas(conn);
                insertarEquiposIniciales(conn);
                System.out.println("Base de datos inicializada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Obtener conexión a la base de datos
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Crear las tablas necesarias
    private static void crearTablas(Connection conn) throws SQLException {
        String sqlPrestamos = "CREATE TABLE IF NOT EXISTS prestamos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "alumno_nombre TEXT NOT NULL, " +
                "matricula TEXT, " +
                "profesor_nombre TEXT NOT NULL, " +
                "equipos TEXT NOT NULL, " +  // Cambiado para almacenar múltiples equipos
                "hora_prestamo TEXT NOT NULL, " +
                "fecha DATE, " +
                "salon TEXT, " +
                "devuelto BOOLEAN DEFAULT 0, " +
                "hora_devolucion TEXT, " +
                "observaciones TEXT" +
                ")";

        String sqlEquipos = "CREATE TABLE IF NOT EXISTS equipos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tipo TEXT NOT NULL, " +  // 'laptop' o 'proyector'
                "numero INTEGER NOT NULL, " +  // 1, 2, 3, etc.
                "nombre TEXT NOT NULL UNIQUE, " +  // 'Laptop 1', 'Proyector 1', etc.
                "disponible BOOLEAN DEFAULT 1" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlPrestamos);
            stmt.execute(sqlEquipos);

            // Agregar columnas si no existen (para compatibilidad con DB existente)
            try {
                stmt.execute("ALTER TABLE equipos ADD COLUMN tipo TEXT DEFAULT 'laptop'");
                stmt.execute("ALTER TABLE equipos ADD COLUMN numero INTEGER DEFAULT 1");
            } catch (SQLException e) {
                // Las columnas ya existen, continuar
            }

            // Actualizar tabla existente si tiene la estructura anterior
            try {
                stmt.execute("ALTER TABLE prestamos RENAME COLUMN alumno_control TO matricula");
            } catch (SQLException e) {
                // La columna ya tiene el nombre correcto o no existe
            }
        }
    }

    // Insertar equipos iniciales si no existen
    private static void insertarEquiposIniciales(Connection conn) throws SQLException {
        String verificar = "SELECT COUNT(*) FROM equipos";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(verificar)) {

            if (rs.getInt(1) == 0) { // Si no hay equipos, insertar algunos por defecto
                String insertar = "INSERT INTO equipos (tipo, numero, nombre) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertar)) {
                    // Insertar 7 laptops
                    for (int i = 1; i <= 7; i++) {
                        pstmt.setString(1, "laptop");
                        pstmt.setInt(2, i);
                        pstmt.setString(3, "Laptop " + i);
                        pstmt.executeUpdate();
                    }

                    // Insertar 7 proyectores
                    for (int i = 1; i <= 7; i++) {
                        pstmt.setString(1, "proyector");
                        pstmt.setInt(2, i);
                        pstmt.setString(3, "Proyector " + i);
                        pstmt.executeUpdate();
                    }

                    // Insertar 3 bocinas
                    for (int i = 1; i <= 3; i++) {
                        pstmt.setString(1, "bocina");
                        pstmt.setInt(2, i);
                        pstmt.setString(3, "Bocina " + i);
                        pstmt.executeUpdate();
                    }
                }
                System.out.println("Equipos iniciales agregados: 7 laptops, 7 proyectores y 3 bocinas");
            }
        }
    }

    // Obtener equipos disponibles por tipo - MODIFICADO: Sin filtro de fecha
    public static List<String> getEquiposDisponiblesPorTipo(String tipo) {
        List<String> equipos = new ArrayList<>();

        // Primero obtener todos los equipos del tipo
        String sqlTodos = "SELECT nombre FROM equipos WHERE disponible = 1 AND tipo = ? ORDER BY numero";

        // Obtener equipos prestados actualmente - SIN FILTRO DE FECHA
        String sqlPrestados = "SELECT equipos FROM prestamos WHERE devuelto = 0";

        try (Connection conn = getConnection()) {
            // Obtener todos los equipos del tipo
            List<String> todosEquipos = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(sqlTodos)) {
                pstmt.setString(1, tipo);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        todosEquipos.add(rs.getString("nombre"));
                    }
                }
            }

            // Obtener equipos prestados (separados por coma) - SIN FILTRO DE FECHA
            List<String> equiposPrestados = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlPrestados)) {
                while (rs.next()) {
                    String equiposStr = rs.getString("equipos");
                    if (equiposStr != null && !equiposStr.trim().isEmpty()) {
                        // Separar por coma y limpiar espacios
                        String[] equiposArray = equiposStr.split(",\\s*");
                        for (String equipo : equiposArray) {
                            equiposPrestados.add(equipo.trim());
                        }
                    }
                }
            }

            // Filtrar equipos disponibles (los que no están prestados)
            for (String equipo : todosEquipos) {
                if (!equiposPrestados.contains(equipo)) {
                    equipos.add(equipo);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener equipos disponibles: " + e.getMessage());
        }

        return equipos;
    }

    // Obtener equipos disponibles
    public static List<String> getEquiposDisponibles() {
        List<String> laptops = getEquiposDisponiblesPorTipo("laptop");
        List<String> proyectores = getEquiposDisponiblesPorTipo("proyector");

        List<String> todosEquipos = new ArrayList<>();
        todosEquipos.addAll(laptops);
        todosEquipos.addAll(proyectores);

        return todosEquipos;
    }

    // Registrar nuevo préstamo con hora y fecha automáticas
    public static boolean registrarPrestamo(Prestamo prestamo) {
        String sql = "INSERT INTO prestamos (alumno_nombre, matricula, profesor_nombre, " +
                "equipos, hora_prestamo, fecha, " +
                "salon, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Generar hora y fecha actuales
            LocalTime horaActual = LocalTime.now();
            LocalDate fechaActual = LocalDate.now();
            DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            pstmt.setString(1, prestamo.getAlumnoNombre());
            pstmt.setString(2, prestamo.getMatricula());
            pstmt.setString(3, prestamo.getProfesorNombre());
            pstmt.setString(4, prestamo.getEquipo()); // Ahora puede contener múltiples equipos separados por coma
            pstmt.setString(5, horaActual.format(horaFormatter));
            pstmt.setString(6, fechaActual.format(fechaFormatter)); // Usar la fecha actual del sistema
            pstmt.setString(7, prestamo.getSalon());
            pstmt.setString(8, prestamo.getObservaciones());

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar préstamo: " + e.getMessage());
            return false;
        }
    }

    // Obtener todos los préstamos (para el panel de admin)
    public static List<Prestamo> getTodosPrestamos() {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = "SELECT * FROM prestamos ORDER BY fecha DESC, hora_prestamo DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Prestamo prestamo = new Prestamo();
                prestamo.setId(rs.getInt("id"));
                prestamo.setAlumnoNombre(rs.getString("alumno_nombre"));
                prestamo.setMatricula(rs.getString("matricula"));
                prestamo.setProfesorNombre(rs.getString("profesor_nombre"));

                // Manejar equipos (puede ser múltiples)
                String equipos = rs.getString("equipos");
                prestamo.setEquipo(equipos);

                prestamo.setHoraPrestamo(rs.getString("hora_prestamo"));
                prestamo.setFecha(rs.getString("fecha"));
                prestamo.setSalon(rs.getString("salon"));
                prestamo.setDevuelto(rs.getBoolean("devuelto"));
                prestamo.setHoraDevolucion(rs.getString("hora_devolucion"));
                prestamo.setObservaciones(rs.getString("observaciones"));

                prestamos.add(prestamo);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener préstamos: " + e.getMessage());
        }

        return prestamos;
    }

    // Marcar préstamo como devuelto con hora automática
    public static boolean marcarComoDevuelto(int prestamoId) {
        String sql = "UPDATE prestamos " +
                "SET devuelto = 1, hora_devolucion = ? " +
                "WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Generar hora actual de devolución
            LocalTime horaActual = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            pstmt.setString(1, horaActual.format(formatter));
            pstmt.setInt(2, prestamoId);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al marcar como devuelto: " + e.getMessage());
            return false;
        }
    }

    // Obtener estadísticas rápidas - Todos los préstamos
    public static String getEstadisticas() {
        String sql = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN devuelto = 0 THEN 1 ELSE 0 END) as activos, " +
                "SUM(CASE WHEN devuelto = 1 THEN 1 ELSE 0 END) as devueltos " +
                "FROM prestamos";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int total = rs.getInt("total");
                int activos = rs.getInt("activos");
                int devueltos = rs.getInt("devueltos");

                return String.format("Total: %d préstamos | %d activos | %d devueltos",
                        total, activos, devueltos);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
        }

        return "Estadísticas no disponibles";
    }

    // Verificar si un equipo específico está disponible - MODIFICADO: Sin filtro de fecha
    public static boolean equipoDisponible(String nombreEquipo) {
        String sql = "SELECT COUNT(*) FROM prestamos " +
                "WHERE devuelto = 0 " +
                "AND equipos LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nombreEquipo + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar disponibilidad: " + e.getMessage());
            return false;
        }
    }

    // Método específico para verificar disponibilidad de bocinas - MODIFICADO: Sin filtro de fecha
    public static boolean bocinasDisponibles() {
        String sql = "SELECT equipos FROM prestamos WHERE devuelto = 0";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Contar cuántas bocinas específicas están prestadas
            int bocinasOcupadas = 0;

            while (rs.next()) {
                String equipos = rs.getString("equipos");
                if (equipos != null) {
                    // Verificar si contiene bocinas específicas
                    if (equipos.contains("Bocina 1")) bocinasOcupadas++;
                    if (equipos.contains("Bocina 2")) bocinasOcupadas++;
                    if (equipos.contains("Bocina 3")) bocinasOcupadas++;
                    // También verificar si dice solo "Bocinas" (las 3 juntas)
                    if (equipos.contains("Bocinas") && !equipos.contains("Bocina 1") &&
                            !equipos.contains("Bocina 2") && !equipos.contains("Bocina 3")) {
                        bocinasOcupadas = 3; // Si dice "Bocinas", asumimos que son las 3
                    }
                }
            }

            // Disponibles si hay menos de 3 bocinas ocupadas
            return bocinasOcupadas < 3;

        } catch (SQLException e) {
            System.err.println("Error al verificar bocinas: " + e.getMessage());
            return false;
        }
    }
}