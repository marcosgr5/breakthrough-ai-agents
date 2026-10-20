package dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Minimax {
    private final Random random = new Random();

    public Sucesor obtenerMejorMovimiento(MotorJuego juego, int profundidad) {
        Profiler.start("Minimax.obtenerMejorMovimiento");

        EstadoTablero estadoOriginal = juego.getEstadoActual();
        boolean somosblancas = estadoOriginal.getTurno();
        List<List<Sucesor>> sucesoresAgrupados = juego.getSucesores();
        List<Sucesor> sucesoresdesordenados = DesordenarSucesor(sucesoresAgrupados);
        Sucesor mejorSucesor = null;

        if (somosblancas) {
            int mejorpuntuacion = Integer.MIN_VALUE;
            int alfa = Integer.MIN_VALUE;
            for (Sucesor sucesor : sucesoresdesordenados) {
                EstadoTablero estadoFuturo = sucesor.getEstado();
                EstadoTablero copia = new EstadoTablero(estadoFuturo.getPiezasBlancas(), estadoFuturo.getPiezasNegras(), !estadoOriginal.getTurno());
                int puntuacion = calcularMinimax(juego, copia, profundidad - 1, alfa, Integer.MAX_VALUE, false);
                if (puntuacion > mejorpuntuacion) {
                    mejorpuntuacion = puntuacion;
                    mejorSucesor = sucesor;
                }
                alfa = Math.max(alfa, mejorpuntuacion);
            }
        } else {
            int mejorPuntuacion = Integer.MAX_VALUE;
            int beta = Integer.MAX_VALUE;
            for (Sucesor sucesor : sucesoresdesordenados) {
                EstadoTablero estadoFuturo = sucesor.getEstado();
                EstadoTablero copia = new EstadoTablero(estadoFuturo.getPiezasBlancas(), estadoFuturo.getPiezasNegras(), !estadoOriginal.getTurno());
                int puntuacion = calcularMinimax(juego, copia, profundidad - 1, Integer.MIN_VALUE, beta, true);
                if (puntuacion < mejorPuntuacion) {
                    mejorPuntuacion = puntuacion;
                    mejorSucesor = sucesor;
                }
                beta = Math.min(beta, mejorPuntuacion);
            }
        }
        juego.setEstadoTablero(estadoOriginal);

        Profiler.stop("Minimax.obtenerMejorMovimiento");
        return mejorSucesor;
    }

    private int calcularMinimax(MotorJuego juego, EstadoTablero estadoActual, int profundidad,
                                int alfa, int beta, boolean esTurnoMax) {
        Profiler.start("Minimax.calcularMinimax");

        if (profundidad == 0 || estadoActual.esVictoriaBlancas() || estadoActual.esVictoriaNegras()) {
            int val = evaluarTablero(estadoActual);
            Profiler.stop("Minimax.calcularMinimax");
            return val;
        }

        EstadoTablero estadoReal = juego.getEstadoActual();
        juego.setEstadoTablero(estadoActual);
        List<List<Sucesor>> sucesoresAgrupados = juego.getSucesores();
        juego.setEstadoTablero(estadoReal);

        List<Sucesor> sucesores = DesordenarSucesor(sucesoresAgrupados);

        int result;
        if (esTurnoMax) {
            int maxEval = Integer.MIN_VALUE;
            for (Sucesor sucesor : sucesores) {
                EstadoTablero estadoFuturo = sucesor.getEstado();
                estadoFuturo.setTurno(!estadoActual.getTurno());
                int eval = calcularMinimax(juego, estadoFuturo, profundidad - 1, alfa, beta, false);
                maxEval = Math.max(maxEval, eval);
                alfa = Math.max(alfa, eval);
                if (beta <= alfa) break;
            }
            result = maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Sucesor sucesor : sucesores) {
                EstadoTablero estadoFuturo = sucesor.getEstado();
                estadoFuturo.setTurno(!estadoActual.getTurno());
                int eval = calcularMinimax(juego, estadoFuturo, profundidad - 1, alfa, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alfa) break;
            }
            result = minEval;
        }

        Profiler.stop("Minimax.calcularMinimax");
        return result;
    }

    public List<Sucesor> DesordenarSucesor(List<List<Sucesor>> sucesoresAgrupadoList) {
        Profiler.start("Minimax.DesordenarSucesor");

        List<Sucesor> mezclados = new ArrayList<>();
        List<List<Sucesor>> copiaAgrupados = new ArrayList<>();
        for (List<Sucesor> sublista : sucesoresAgrupadoList) {
            copiaAgrupados.add(new ArrayList<>(sublista));
        }
        while (!copiaAgrupados.isEmpty()) {
            int indiceorigen = random.nextInt(copiaAgrupados.size());
            List<Sucesor> sucesoresDeOrigen = copiaAgrupados.get(indiceorigen);
            if (sucesoresDeOrigen.isEmpty()) {
                copiaAgrupados.remove(indiceorigen);
                continue;
            }
            int indiceDestino = random.nextInt(sucesoresDeOrigen.size());
            mezclados.add(sucesoresDeOrigen.remove(indiceDestino));
            if (sucesoresDeOrigen.isEmpty()) {
                copiaAgrupados.remove(indiceorigen);
            }
        }

        Profiler.stop("Minimax.DesordenarSucesor");
        return mezclados;
    }

    public int evaluarTablero(EstadoTablero estado) {
        Profiler.start("Minimax.evaluarTablero");

        if (estado.esVictoriaBlancas()) { Profiler.stop("Minimax.evaluarTablero"); return 100000; }
        if (estado.esVictoriaNegras())  { Profiler.stop("Minimax.evaluarTablero"); return -100000; }

        long blancas = estado.getPiezasBlancas();
        long negras  = estado.getPiezasNegras();
        int puntuacionBlancas = 0;
        int puntuacionNegras  = 0;
        int[] pesosBlancas = {0, 1, 2, 5, 15, 40, 100, 500, 0};
        int[] pesosNegras  = {0, 0, 500, 100, 40, 15, 5, 2, 1};

        for (int fila = 1; fila <= 8; fila++) {
            int numBlancas = Long.bitCount(blancas & 0xFFL);
            int numNegras  = Long.bitCount(negras & 0xFFL);
            puntuacionBlancas += numBlancas * pesosBlancas[fila];
            puntuacionNegras  += numNegras * pesosNegras[fila];
            blancas >>>= 8;
            negras  >>>= 8;
        }

        Profiler.stop("Minimax.evaluarTablero");
        return puntuacionBlancas - puntuacionNegras;
    }
}