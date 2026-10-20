package presentación;

import dominio.*;
import java.util.*;

public class InterfazComandos {
    static Scanner KB = new Scanner(System.in);

    static class ConfigIA {
        int algoritmo = 1; // 1: Minimax, 2: Montecarlo
        int creditos = 0;
    }

    public static void main(String[] args) {
        // Resetear el profiler al inicio
        Profiler.reset();

        MotorJuego juego = new MotorJuego();
        Minimax minimax = new Minimax();
        Montecarlo montecarlo = new Montecarlo();

        configurarInicio(juego);

        String modo = elegirModo();
        ConfigIA configB = new ConfigIA();
        ConfigIA configN = new ConfigIA();
        boolean instantaneo = false;

        if (modo.equals("3")) {
            System.out.println("¿Acciones instantáneas? (true/false)");
            instantaneo = KB.nextBoolean();
            KB.nextLine();
            configurarIA(configB, "Blancas");
            configurarIA(configN, "Negras");
        } else if (modo.equals("2")) {
            configurarIA(configN, "Negras");
        }

        int nTurnos = 1;
        while (juego.enProgreso()) {
            System.out.println("\nTurno: " + nTurnos++);
            pintarTablero(juego.getEstadoActual());

            boolean turnoBlancas = juego.getEstadoActual().getTurno();
            boolean esTurnoIA = modo.equals("3") || (modo.equals("2") && !turnoBlancas);

            if (esTurnoIA) {
                ConfigIA currentCfg = turnoBlancas ? configB : configN;
                ejecutarIA(juego, minimax, montecarlo, currentCfg, turnoBlancas, instantaneo);
            } else {
                ejecutarHumano(juego);
            }
        }

        pintarTablero(juego.getEstadoActual());
        System.out.println(juego.getEstadoActual().esVictoriaBlancas() ? "¡Blancas ganan!" : "¡Negras ganan!");

        // ───── Mostrar informe de profiling al finalizar la partida ─────
        Profiler.report();
    }

    // --- MÓDULOS DE CONFIGURACIÓN ---

    private static void configurarInicio(MotorJuego juego) {
        System.out.println("Bienvenido a Breakthrough! Nueva partida (-n) o cargar (-c)?");
        String opt = KB.nextLine();
        if (opt.equals("-c")) {
            System.out.println("Ranura a cargar (0-9):");
            int slot = leerEnteroValido(0, 9);
            juego.cargarEstado(slot);
            System.out.println("Ranura " + slot + " cargada!");
        }
    }

    private static String elegirModo() {
        System.out.println("Modo: 1-PvP, 2-PvE (IA Negras), 3-EvE (IA vs IA)");
        String modo = KB.nextLine();
        while (!modo.matches("[1-3]")) {
            System.out.print("Opción inválida. Elige 1, 2 o 3: ");
            modo = KB.nextLine();
        }
        return modo;
    }

    private static void configurarIA(ConfigIA cfg, String color) {
        System.out.println("Algoritmo IA " + color + ": 1-Minimax / 2-Montecarlo");
        cfg.algoritmo = leerEnteroValido(1, 2);
        if (cfg.algoritmo == 2) {
            System.out.println("Créditos para Montecarlo (" + color + ")?");
            cfg.creditos = leerEnteroPositivo();
        }
    }

    // --- MÓDULOS DE JUEGO ---

    private static void ejecutarIA(MotorJuego juego, Minimax mm, Montecarlo mc,
                                   ConfigIA cfg, boolean isWhite, boolean instant) {
        System.out.println("La IA (" + (isWhite ? "Blancas" : "Negras") + ") está pensando...");

        // Medimos el tiempo total del turno de la IA
        String labelTurno = isWhite ? "Turno.IA.Blancas" : "Turno.IA.Negras";
        Profiler.start(labelTurno);

        Sucesor jugada = (cfg.algoritmo == 1)
                ? mm.obtenerMejorMovimiento(juego, 5)
                : mc.calcularMontecarlo(juego, juego.getEstadoActual(), cfg.creditos);

        Profiler.stop(labelTurno);

        if (jugada == null) {
            System.out.println("IA no encontró movimientos.");
            System.exit(0);
        }

        juego.setEstadoTablero(jugada.getEstado());
        juego.cambiarTurno();
        if (!instant) {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
    }

    private static void ejecutarHumano(MotorJuego juego) {
        while (true) {
            System.out.println("Movimiento en formato ColumnaFila (ej. A2B3):");
            String[] casillas = parsearMovimiento(KB.nextLine());

            if (casillas == null || !juego.idValido(casillas[0]) || !juego.idValido(casillas[1])) {
                System.out.println("Formato o casillas inválidas.");
                continue;
            }

            long origen = juego.boardLocationToLong(casillas[0]);
            ArrayList<String> destinos = juego.calcularDestinosValidos(origen);

            if (destinos.isEmpty()) {
                System.out.println("Pieza sin movimientos válidos.");
                continue;
            }

            if (!destinos.contains(casillas[1])) {
                System.out.println("Destino inválido. Opciones: " + destinos);
                continue;
            }

            juego.setEstadoTablero(juego.ejecutarMovimiento(origen,
                    juego.boardLocationToLong(casillas[1]), juego.getEstadoActual()));
            juego.cambiarTurno();
            break;
        }
    }

    // --- AUXILIARES ---

    private static int leerEnteroValido(int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(KB.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.println("Rango permitido: " + min + "-" + max);
            } catch (Exception e) { System.out.println("Error: introduce un número."); }
        }
    }

    private static int leerEnteroPositivo() {
        while (true) {
            try {
                int v = Integer.parseInt(KB.nextLine().trim());
                if (v > 0) return v;
                System.out.println("Debe ser mayor a 0.");
            } catch (Exception e) { System.out.println("Error: introduce un número."); }
        }
    }

    public static void pintarTablero(EstadoTablero estado) {
        System.out.println("\n- - - - - - - - - \n/ A B C D E F G H ");
        int row = 8;
        long bit = 0x8000000000000000L;
        System.out.print(row-- + " ");
        for (int i = 1; i <= 64; i++) {
            if ((estado.getPiezasBlancas() & bit) != 0) System.out.print("B ");
            else if ((estado.getPiezasNegras() & bit) != 0) System.out.print("N ");
            else System.out.print("o ");
            bit >>>= 1;
            if (i % 8 == 0) {
                System.out.println();
                if (row != 0) System.out.print(row-- + " ");
            }
        }
        System.out.println("- - - - - - - - - \nTURNO: " + (estado.getTurno() ? "Blancas" : "Negras"));
    }

    public static String[] parsearMovimiento(String m) {
        if (m == null || m.length() != 4) return null;
        if (!Character.isLetter(m.charAt(0)) || !Character.isDigit(m.charAt(1)) ||
            !Character.isLetter(m.charAt(2)) || !Character.isDigit(m.charAt(3))) {
            return null;
        }
        return new String[]{
            String.valueOf(m.charAt(0)).toUpperCase() + m.charAt(1),
            String.valueOf(m.charAt(2)).toUpperCase() + m.charAt(3)
        };
    }
}