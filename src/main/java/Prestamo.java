public class Prestamo {
    private int id;
    private String alumnoNombre;
    private String matricula;
    private String profesorNombre;
    private String equipo; // Ahora puede contener múltiples equipos separados por coma
    private String horaPrestamo;
    private String fecha;
    private String salon;
    private boolean devuelto;
    private String horaDevolucion;
    private String observaciones;

    // Constructor vacío
    public Prestamo() {}

    // Constructor con parámetros principales
    public Prestamo(String alumnoNombre, String matricula, String profesorNombre,
                    String equipo, String horaPrestamo,
                    String salon, String observaciones) {
        this.alumnoNombre = alumnoNombre;
        this.matricula = matricula;
        this.profesorNombre = profesorNombre;
        this.equipo = equipo; // Puede ser múltiples equipos separados por coma
        this.horaPrestamo = horaPrestamo;
        this.salon = salon;
        this.observaciones = observaciones;
        this.devuelto = false;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAlumnoNombre() { return alumnoNombre; }
    public void setAlumnoNombre(String alumnoNombre) { this.alumnoNombre = alumnoNombre; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getProfesorNombre() { return profesorNombre; }
    public void setProfesorNombre(String profesorNombre) { this.profesorNombre = profesorNombre; }

    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }

    public String getHoraPrestamo() { return horaPrestamo; }
    public void setHoraPrestamo(String horaPrestamo) { this.horaPrestamo = horaPrestamo; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getSalon() { return salon; }
    public void setSalon(String salon) { this.salon = salon; }

    public boolean isDevuelto() { return devuelto; }
    public void setDevuelto(boolean devuelto) { this.devuelto = devuelto; }

    public String getHoraDevolucion() { return horaDevolucion; }
    public void setHoraDevolucion(String horaDevolucion) {
        this.horaDevolucion = horaDevolucion;
    }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // Método para obtener estado legible
    public String getEstadoTexto() {
        return devuelto ? "Devuelto" : "Activo";
    }

    // Método para obtener equipos como lista (para múltiples equipos)
    public String[] getEquiposArray() {
        if (equipo == null || equipo.trim().isEmpty()) {
            return new String[0];
        }
        return equipo.split(",\\s*"); // Separar por coma y eliminar espacios
    }

    // Método para contar equipos
    public int getCantidadEquipos() {
        return getEquiposArray().length;
    }

    // Método toString para debugging
    @Override
    public String toString() {
        return String.format("Prestamo{id=%d, alumno='%s', equipos='%s', cantidad=%d, estado='%s'}",
                id, alumnoNombre, equipo, getCantidadEquipos(), getEstadoTexto());
    }
}