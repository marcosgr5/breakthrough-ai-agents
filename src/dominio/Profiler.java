package dominio;

import java.util.HashMap;
import java.util.Map;

/**
 * Profiler simple basado en System.nanoTime().
 * Uso:
 *   Profiler.start("miMetodo");
 *   miMetodo();
 *   Profiler.stop("miMetodo");
 *   Profiler.report();
 */
public class Profiler {

    private static final Map<String, long[]> datos = new HashMap<>();
    // datos[nombre] = { tiempoAcumulado(ns), numLlamadas, tiempoInicio }

    /** Inicia la medición de un método/bloque. */
    public static void start(String nombre) {
        datos.computeIfAbsent(nombre, k -> new long[]{0L, 0L, 0L});
        datos.get(nombre)[2] = System.nanoTime();
    }

    /** Para la medición y acumula el tiempo. */
    public static void stop(String nombre) {
        long fin = System.nanoTime();
        long[] entry = datos.get(nombre);
        if (entry == null) return;
        entry[0] += fin - entry[2]; // tiempo acumulado
        entry[1]++;                  // número de llamadas
    }

    /** Resetea todos los contadores. */
    public static void reset() {
        datos.clear();
    }

    /** Imprime el informe ordenado de mayor a menor tiempo total. */
    public static void report() {
        System.out.println("\n========== PROFILING REPORT ==========");
        System.out.printf("%-40s %10s %12s %15s%n",
                "Método", "Llamadas", "Total (ms)", "Promedio (µs)");
        System.out.println("-".repeat(80));

        datos.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0])) // mayor tiempo primero
            .forEach(e -> {
                String nombre   = e.getKey();
                long totalNs    = e.getValue()[0];
                long llamadas   = e.getValue()[1];
                double totalMs  = totalNs / 1_000_000.0;
                double promedioUs = llamadas > 0 ? (totalNs / 1_000.0) / llamadas : 0;
                System.out.printf("%-40s %10d %12.3f %15.2f%n",
                        nombre, llamadas, totalMs, promedioUs);
            });

        System.out.println("=".repeat(80));
    }
}