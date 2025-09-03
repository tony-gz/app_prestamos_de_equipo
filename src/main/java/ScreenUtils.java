import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class ScreenUtils {

    // Obtener información de la pantalla
    public static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    // Calcular tamaño de ventana como porcentaje de la pantalla
    public static Dimension calculateWindowSize(double widthPercent, double heightPercent) {
        Dimension screenSize = getScreenSize();
        int width = (int) (screenSize.width * widthPercent);
        int height = (int) (screenSize.height * heightPercent);
        return new Dimension(width, height);
    }

    // Calcular tamaño mínimo de ventana
    public static Dimension calculateMinimumSize(double widthPercent, double heightPercent) {
        Dimension screenSize = getScreenSize();
        int minWidth = (int) (screenSize.width * widthPercent);
        int minHeight = (int) (screenSize.height * heightPercent);
        return new Dimension(minWidth, minHeight);
    }

    // Obtener factor de escala basado en DPI
    public static double getScaleFactor() {
        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        AffineTransform transform = gc.getDefaultTransform();
        double scaleX = transform.getScaleX();
        double scaleY = transform.getScaleY();

        // Usar el mayor factor de escala
        return Math.max(scaleX, scaleY);
    }

    // Ajustar tamaño de fuente basado en la escala
    public static int getScaledFontSize(int baseFontSize) {
        double scaleFactor = getScaleFactor();
        // Aplicar un factor de ajuste más conservador
        return (int) Math.max(baseFontSize, baseFontSize * Math.sqrt(scaleFactor));
    }

    // Obtener información detallada de la pantalla
    public static void printScreenInfo() {
        Dimension screenSize = getScreenSize();
        double scaleFactor = getScaleFactor();

        System.out.println("=== INFORMACIÓN DE PANTALLA ===");
        System.out.println("Resolución: " + screenSize.width + "x" + screenSize.height);
        System.out.println("Factor de escala: " + scaleFactor);
        System.out.println("DPI aproximado: " + (int)(96 * scaleFactor));
        System.out.println("===============================");
    }

    // Configurar ventana con tamaños responsive
    public static void setupResponsiveWindow(JFrame frame,
                                             double widthPercent,
                                             double heightPercent,
                                             double minWidthPercent,
                                             double minHeightPercent) {

        // Tamaño preferido
        Dimension preferredSize = calculateWindowSize(widthPercent, heightPercent);
        frame.setSize(preferredSize);

        // Tamaño mínimo
        Dimension minimumSize = calculateMinimumSize(minWidthPercent, minHeightPercent);
        frame.setMinimumSize(minimumSize);

        // Centrar en pantalla
        frame.setLocationRelativeTo(null);

        // Permitir redimensionar
        frame.setResizable(true);

        // Imprimir info para debugging
        printScreenInfo();
        System.out.println("Ventana configurada: " + preferredSize.width + "x" + preferredSize.height);
        System.out.println("Tamaño mínimo: " + minimumSize.width + "x" + minimumSize.height);
    }
}