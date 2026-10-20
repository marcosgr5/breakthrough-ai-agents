package dominio;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Montecarlo {

    public HashMap<Integer, Nodo> nodos = new HashMap<>();
    public final int profundidadSimulacion = 400;
    private Random random = new Random();
    private MotorJuego juegoSimulacion = new MotorJuego();

    public Sucesor calcularMontecarlo(MotorJuego juego, EstadoTablero estado, int creditos) {
        Profiler.start("Montecarlo.calcularMontecarlo");

        int cont = 0;
        Sucesor sAux;
        Nodo nAct;

        EstadoTablero estadoOriginal = juego.getEstadoActual();
        juego.setEstadoTablero(estado);
        nodos.clear();

        Nodo raiz = new Nodo(cont++, estado, -1, 0, 0,juegoSimulacion);
        nodos.put(raiz.id, raiz);

        while (creditos > 0) {
            nAct = nodos.get(0);

            // 1. SELECCIÓN
            while (!nAct.sucesores.isEmpty() == false && !nAct.idHijos.isEmpty()) {
                int mejorId = getMejorUct(nAct);
                nAct = nodos.get(mejorId);
                if (nAct == null) break;
            }

            // 2. EXPANSIÓN
            if (!nAct.sucesores.isEmpty()) {
                sAux = nAct.sucesores.poll();

                // 3. SIMULACIÓN
                int valorSimulado = simuMonoloco(sAux, profundidadSimulacion);

                Nodo nHijo = new Nodo(cont++, sAux.estado, nAct.id, 0, 0, juegoSimulacion);
                nHijo.sucesorOrigen = sAux;
                nodos.put(nHijo.id, nHijo);
                nAct.idHijos.add(nHijo.id);

                // 4. BACKPROPAGATION
                backup(nHijo, valorSimulado);
            }
            creditos--;
        }

        juego.setEstadoTablero(estadoOriginal);

        Nodo nodoRaiz = nodos.get(0);
        if (nodoRaiz == null) { Profiler.stop("Montecarlo.calcularMontecarlo"); return null; }

        int mejorId = getMejorNodo(nodoRaiz);
        if (mejorId == -1) { Profiler.stop("Montecarlo.calcularMontecarlo"); return null; }

        Profiler.stop("Montecarlo.calcularMontecarlo");
        return nodos.get(mejorId).sucesorOrigen;
    }

    public int getMejorNodo(Nodo n) {
        Profiler.start("Montecarlo.getMejorNodo");
        int id = -1;
        boolean turnoBlancas = n.estado.getTurno();
        double mejorValor = turnoBlancas ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (int idAux : n.idHijos) {
            Nodo hijo = nodos.get(idAux);
            double vAux = (double) hijo.valor / hijo.nVisitado;
            if (turnoBlancas) {
                if (vAux > mejorValor) { mejorValor = vAux; id = idAux; }
            } else {
                if (vAux < mejorValor) { mejorValor = vAux; id = idAux; }
            }
        }
        Profiler.stop("Montecarlo.getMejorNodo");
        return id;
    }

    public int getMejorUct(Nodo n) {
        return getMejorUctFinal(n);
    }

    private int getMejorUctFinal(Nodo n) {
        Profiler.start("Montecarlo.getMejorUct");
        int id = -1;
        double maxUct = Double.NEGATIVE_INFINITY;
        boolean turnoBlancas = n.estado.getTurno();
        double logPadre = Math.log(n.nVisitado);
        for (int idAux : n.idHijos) {
            Nodo nAux = nodos.get(idAux);
            double v = (double) nAux.valor / nAux.nVisitado;
            double exploration = 2.0 * Math.sqrt(logPadre / (double) nAux.nVisitado);
            double uct = (turnoBlancas ? v : -v) + exploration;
            if (uct > maxUct) { maxUct = uct; id = idAux; }
        }
        Profiler.stop("Montecarlo.getMejorUct");
        return id;
    }

    public void backup(Nodo n, int valor) {
        Profiler.start("Montecarlo.backup");
        Nodo actual = n;
        while (actual != null) {
            actual.valor += valor;
            actual.nVisitado++;
            actual = (actual.idPadre != -1) ? nodos.get(actual.idPadre) : null;
        }
        Profiler.stop("Montecarlo.backup");
    }

    public int simuMonoloco(Sucesor suc, int profundidad) {
        Profiler.start("Montecarlo.simuMonoloco");

        EstadoTablero estadoSim = suc.getEstado();

        if (estadoSim.esVictoriaBlancas() || estadoSim.esVictoriaNegras() || profundidad == 0) {
            int val = evaluarNodo(estadoSim);
            Profiler.stop("Montecarlo.simuMonoloco");
            return val;
        }

        juegoSimulacion.setEstadoTablero(estadoSim);

        List<List<Sucesor>> sucesoresAgrupados = juegoSimulacion.getSucesores();
        if (sucesoresAgrupados.isEmpty()) {
            int val = evaluarNodo(estadoSim);
            Profiler.stop("Montecarlo.simuMonoloco");
            return val;
        }

        int indexAccion = random.nextInt(sucesoresAgrupados.size());
        List<Sucesor> opciones = sucesoresAgrupados.get(indexAccion);
        Sucesor randomSuc = opciones.get(random.nextInt(opciones.size()));

        Profiler.stop("Montecarlo.simuMonoloco");
        return simuMonoloco(randomSuc, profundidad - 1);
    }

    public int evaluarNodo(EstadoTablero estado) {
        Profiler.start("Montecarlo.evaluarNodo");
        if (estado.esVictoriaBlancas()) { 
            Profiler.stop("Montecarlo.evaluarNodo"); 
            return 1; 
        }
        if (estado.esVictoriaNegras()) { 
            Profiler.stop("Montecarlo.evaluarNodo"); 
            return -1; 
        }
        double numB = Long.bitCount(estado.getPiezasBlancas());
        double numN = Long.bitCount(estado.getPiezasNegras());
        double ventaja = (numB - numN) / (numB + numN);
        Profiler.stop("Montecarlo.evaluarNodo");
        return (int)(ventaja * 100);
    }
}