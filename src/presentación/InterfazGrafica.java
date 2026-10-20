package presentación;

import dominio.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Interfaz gráfica Swing para el juego Breakthrough.
 * No modifica ninguna otra clase del proyecto.
 */
public class InterfazGrafica extends JFrame {

    // ── Constantes visuales ────────────────────────────────────────────────
    private static final int CELL   = 72;
    private static final int COLS   = 8;
    private static final int ROWS   = 8;

    private static final Color COLOR_CELDA_CLARA  = new Color(0xF0D9B5);
    private static final Color COLOR_CELDA_OSCURA  = new Color(0xB58863);
    private static final Color COLOR_SELECCION     = new Color(0x6AFF6A, true);  // verde translúcido
    private static final Color COLOR_DESTINO       = new Color(0x4488FF99, true); // azul translúcido
    private static final Color COLOR_FICHA_BLANCA  = new Color(0xFFFFFF);
    private static final Color COLOR_FICHA_NEGRA   = new Color(0x222222);
    private static final Color COLOR_FONDO_PANEL   = new Color(0x3A3A3A);
    private static final Color COLOR_TEXTO_PANEL   = new Color(0xEEEEEE);

    // ── Estado de juego ────────────────────────────────────────────────────
    private MotorJuego    juego;
    private Minimax       minimax;
    private Montecarlo    montecarlo;

    // ── Config IA ─────────────────────────────────────────────────────────
    private int  modoJuego   = 1;   // 1=PvP, 2=PvE (IA negras), 3=EvE
    private int  algBlancas  = 1;   // 1=Minimax, 2=Montecarlo
    private int  algNegras   = 1;
    private int  credBlancas = 500;
    private int  credNegras  = 500;
    private int  profMinimax = 5;
    private boolean modoInstantaneo = false;

    // ── Estado de la UI ────────────────────────────────────────────────────
    private long   origenSeleccionado = 0L;
    private List<String> destinosValidos = new ArrayList<>();
    private boolean iaOcupada = false;
    private int  nTurnos = 1;

    // ── Componentes Swing ─────────────────────────────────────────────────
    private TableroPanel tableroPanel;
    private JLabel       lblTurno;
    private JLabel       lblTurnos;
    private JTextArea    logArea;
    private JButton      btnGuardar;
    private JButton      btnCargar;
    private JButton      btnNueva;

    // ══════════════════════════════════════════════════════════════════════
    //  MAIN
    // ══════════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        Profiler.reset();
        SwingUtilities.invokeLater(() -> new InterfazGrafica().setVisible(true));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════════════

    public InterfazGrafica() {
        juego      = new MotorJuego();
        minimax    = new Minimax();
        montecarlo = new Montecarlo();

        construirVentana();
        mostrarDialogoInicio();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CONSTRUCCIÓN DE LA VENTANA
    // ══════════════════════════════════════════════════════════════════════

    private void construirVentana() {
        setTitle("Breakthrough");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(COLOR_FONDO_PANEL);

        // Panel central: tablero
        tableroPanel = new TableroPanel();
        add(tableroPanel, BorderLayout.CENTER);

        // Panel lateral derecho
        add(construirPanelLateral(), BorderLayout.EAST);

        // Barra de estado inferior
        add(construirBarraEstado(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel construirPanelLateral() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO_PANEL);
        panel.setBorder(new EmptyBorder(12, 8, 12, 12));
        panel.setPreferredSize(new Dimension(210, CELL * ROWS));

        // Turno
        lblTurno = new JLabel("Turno: Blancas", SwingConstants.CENTER);
        lblTurno.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTurno.setForeground(COLOR_TEXTO_PANEL);
        lblTurno.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTurno);
        panel.add(Box.createVerticalStrut(4));

        lblTurnos = new JLabel("Turno nº 1", SwingConstants.CENTER);
        lblTurnos.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblTurnos.setForeground(new Color(0xAAAAAA));
        lblTurnos.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTurnos);
        panel.add(Box.createVerticalStrut(14));

        // Leyenda
        panel.add(construirLeyenda());
        panel.add(Box.createVerticalStrut(14));

        // Botones de acción
        btnNueva   = crearBoton("♻  Nueva partida");
        btnGuardar = crearBoton("💾  Guardar");
        btnCargar  = crearBoton("📂  Cargar");
        JButton btnProfiling = crearBoton("📊  Profiling");

        btnNueva.addActionListener(e -> nuevaPartida());
        btnGuardar.addActionListener(e -> guardarPartida());
        btnCargar.addActionListener(e -> cargarPartida());
        btnProfiling.addActionListener(e -> mostrarProfiling());

        for (JButton b : new JButton[]{btnNueva, btnGuardar, btnCargar, btnProfiling}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(b);
            panel.add(Box.createVerticalStrut(6));
        }

        panel.add(Box.createVerticalStrut(10));

        // Log
        logArea = new JTextArea(6, 16);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(0x1E1E1E));
        logArea.setForeground(new Color(0xCCCCCC));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(190, 130));
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x555555)),
                "Log", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.PLAIN, 11), new Color(0x999999)));
        panel.add(scroll);

        return panel;
    }

    private JPanel construirLeyenda() {
        JPanel p = new JPanel(new GridLayout(3, 2, 4, 4));
        p.setBackground(COLOR_FONDO_PANEL);
        p.setMaximumSize(new Dimension(190, 72));
        p.add(fichaLabel(COLOR_FICHA_BLANCA, "●"));
        p.add(etiqLabel("Blancas (▲)"));
        p.add(fichaLabel(COLOR_FICHA_NEGRA, "●"));
        p.add(etiqLabel("Negras (▼)"));
        p.add(fichaLabel(COLOR_SELECCION, "■"));
        p.add(etiqLabel("Seleccionado / destino"));
        return p;
    }

    private JLabel fichaLabel(Color c, String txt) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setForeground(c);
        l.setFont(new Font("SansSerif", Font.BOLD, 20));
        return l;
    }

    private JLabel etiqLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(new Color(0xCCCCCC));
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }

    private JButton crearBoton(String texto) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        b.setBackground(new Color(0x4A4A4A));
        b.setForeground(COLOR_TEXTO_PANEL);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setMaximumSize(new Dimension(190, 32));
        b.setPreferredSize(new Dimension(190, 32));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x666666)),
                new EmptyBorder(4, 8, 4, 8)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(0x666666)); }
            public void mouseExited(MouseEvent e)  { b.setBackground(new Color(0x4A4A4A)); }
        });
        return b;
    }

    private JPanel construirBarraEstado() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        p.setBackground(new Color(0x2A2A2A));
        JLabel info = new JLabel("Breakthrough  ·  haz clic en una pieza para seleccionarla");
        info.setForeground(new Color(0x888888));
        info.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(info);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DIÁLOGOS DE INICIO
    // ══════════════════════════════════════════════════════════════════════

    private void mostrarDialogoInicio() {
        Object[] opciones = {"Nueva partida", "Cargar partida"};
        int r = JOptionPane.showOptionDialog(this,
                "Bienvenido a Breakthrough", "Inicio",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, opciones, opciones[0]);
        if (r == 1) {
            cargarPartida();
        } else {
            mostrarDialogoConfiguracion();
        }
    }

    private void mostrarDialogoConfiguracion() {
        JDialog dlg = new JDialog(this, "Configurar partida", true);
        dlg.setLayout(new BorderLayout(8, 8));
        dlg.getContentPane().setBackground(COLOR_FONDO_PANEL);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBackground(COLOR_FONDO_PANEL);
        form.setBorder(new EmptyBorder(16, 16, 8, 16));

        // Modo
        JLabel lModo = etiq("Modo de juego:");
        String[] modos = {"PvP (Humano vs Humano)", "PvE (Humano vs IA)", "EvE (IA vs IA)"};
        JComboBox<String> cbModo = new JComboBox<>(modos);
        cbModo.setSelectedIndex(modoJuego - 1);

        // Algoritmo negras
        JLabel lAlgN = etiq("IA Negras:");
        String[] algs = {"Minimax", "Montecarlo"};
        JComboBox<String> cbAlgN = new JComboBox<>(algs);
        cbAlgN.setSelectedIndex(algNegras - 1);

        // Algoritmo blancas (solo EvE)
        JLabel lAlgB = etiq("IA Blancas:");
        JComboBox<String> cbAlgB = new JComboBox<>(algs);
        cbAlgB.setSelectedIndex(algBlancas - 1);

        // Créditos
        JLabel lCredN = etiq("Créditos IA Negras:");
        JSpinner spCredN = new JSpinner(new SpinnerNumberModel(credNegras, 10, 100000, 100));

        JLabel lCredB = etiq("Créditos IA Blancas:");
        JSpinner spCredB = new JSpinner(new SpinnerNumberModel(credBlancas, 10, 100000, 100));

        // Profundidad Minimax
        JLabel lProf = etiq("Profundidad Minimax:");
        JSpinner spProf = new JSpinner(new SpinnerNumberModel(profMinimax, 1, 10, 1));

        // Instantáneo (EvE)
        JLabel lInst = etiq("EvE instantáneo:");
        JCheckBox chkInst = new JCheckBox();
        chkInst.setBackground(COLOR_FONDO_PANEL);
        chkInst.setSelected(modoInstantaneo);

        // Añadir componentes
        form.add(lModo);  form.add(cbModo);
        form.add(lAlgN);  form.add(cbAlgN);
        form.add(lCredN); form.add(spCredN);
        form.add(lAlgB);  form.add(cbAlgB);
        form.add(lCredB); form.add(spCredB);
        form.add(lProf);  form.add(spProf);
        form.add(lInst);  form.add(chkInst);

        // Visibilidad dependiente del modo y algoritmo
        Runnable actualizarVisible = () -> {
            int m = cbModo.getSelectedIndex() + 1;
            boolean pve = m >= 2;
            boolean eve = m == 3;

            boolean montecarlN = pve && cbAlgN.getSelectedIndex() == 1;
            boolean minimaxN   = pve && cbAlgN.getSelectedIndex() == 0;
            boolean montecarlB = eve && cbAlgB.getSelectedIndex() == 1;
            boolean minimaxB   = eve && cbAlgB.getSelectedIndex() == 0;

            lAlgN.setVisible(pve);   cbAlgN.setVisible(pve);
            lAlgB.setVisible(eve);   cbAlgB.setVisible(eve);
            lCredN.setVisible(montecarlN); spCredN.setVisible(montecarlN);
            lCredB.setVisible(montecarlB); spCredB.setVisible(montecarlB);
            // Profundidad: visible si alguno de los activos usa Minimax
            boolean mostrarProf = minimaxN || minimaxB || (m == 1);
            lProf.setVisible(mostrarProf); spProf.setVisible(mostrarProf);
            lInst.setVisible(eve);   chkInst.setVisible(eve);
            dlg.pack();
        };
        cbModo.addActionListener(e -> actualizarVisible.run());
        cbAlgN.addActionListener(e -> actualizarVisible.run());
        cbAlgB.addActionListener(e -> actualizarVisible.run());
        actualizarVisible.run();

        // Botón OK
        JButton btnOk = new JButton("Empezar");
        btnOk.setBackground(new Color(0x3A7A3A));
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false);
        btnOk.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnOk.addActionListener(e -> {
            modoJuego      = cbModo.getSelectedIndex() + 1;
            algNegras      = cbAlgN.getSelectedIndex() + 1;
            algBlancas     = cbAlgB.getSelectedIndex() + 1;
            credNegras     = (int) spCredN.getValue();
            credBlancas    = (int) spCredB.getValue();
            profMinimax    = (int) spProf.getValue();
            modoInstantaneo= chkInst.isSelected();
            dlg.dispose();
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(COLOR_FONDO_PANEL);
        south.add(btnOk);

        dlg.add(form,  BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        iniciarPartida();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LÓGICA DE PARTIDA
    // ══════════════════════════════════════════════════════════════════════

    private void iniciarPartida() {
        nTurnos = 1;
        origenSeleccionado = 0L;
        destinosValidos.clear();
        actualizarUI();
        log("▶ Partida iniciada. Modo " + nombreModo());
        planificarTurnoIA();
    }

    private String nombreModo() {
        return switch (modoJuego) { case 2 -> "PvE"; case 3 -> "EvE"; default -> "PvP"; };
    }

    /** Decide si el turno actual le corresponde a la IA y lanza el hilo. */
    private void planificarTurnoIA() {
        if (!juego.enProgreso()) return;

        boolean turnoBlancas = juego.getEstadoActual().getTurno();
        boolean esIA = (modoJuego == 3) ||
                       (modoJuego == 2 && !turnoBlancas);

        if (esIA && !iaOcupada) {
            iaOcupada = true;
            tableroPanel.setEnabled(false);
            actualizarRotulo("IA pensando…");

            int alg = turnoBlancas ? algBlancas : algNegras;
            int cred = turnoBlancas ? credBlancas : credNegras;

            SwingWorker<Sucesor, Void> worker = new SwingWorker<>() {
                protected Sucesor doInBackground() {
                    if (alg == 1) return minimax.obtenerMejorMovimiento(juego, profMinimax);
                    else          return montecarlo.calcularMontecarlo(juego, juego.getEstadoActual(), cred);
                }
                protected void done() {
                    try {
                        Sucesor jugada = get();
                        if (jugada == null) {
                            log("⚠ IA sin movimientos.");
                            return;
                        }
                        juego.setEstadoTablero(jugada.getEstado());
                        juego.cambiarTurno();
                        nTurnos++;
                        iaOcupada = false;
                        actualizarUI();
                        verificarFin();
                        if (!modoInstantaneo && juego.enProgreso()) {
                            javax.swing.Timer t = new javax.swing.Timer(600, ev -> planificarTurnoIA());
                            t.setRepeats(false);
                            t.start();
                        } else {
                            planificarTurnoIA();
                        }
                    } catch (Exception ex) {
                        iaOcupada = false;
                        tableroPanel.setEnabled(true);
                        log("Error IA: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        } else {
            tableroPanel.setEnabled(true);
            actualizarUI();
        }
    }

    /** Maneja el clic humano en una celda (col 0-7, fila 0-7 desde arriba) */
    private void manejarClicCelda(int col, int filaVis) {
        if (!juego.enProgreso() || iaOcupada) return;

        // Convertir coordenadas visuales → bit
        int filaLogica = ROWS - filaVis; // fila 1-8 (1=abajo)
        char colChar = (char)('A' + col);
        String clave = "" + colChar + filaLogica;

        if (!juego.idValido(clave)) return;

        long bit = juego.boardLocationToLong(clave);
        long piezasPropias = juego.getEstadoActual().getTurno()
                ? juego.getEstadoActual().getPiezasBlancas()
                : juego.getEstadoActual().getPiezasNegras();

        if (origenSeleccionado == 0L) {
            // Primera selección: escoger pieza propia
            if ((piezasPropias & bit) != 0) {
                origenSeleccionado = bit;
                destinosValidos = juego.calcularDestinosValidos(bit);
                if (destinosValidos.isEmpty()) {
                    origenSeleccionado = 0L;
                    log("Sin movimientos para " + clave);
                } else {
                    log("Seleccionada " + clave + " → " + destinosValidos);
                }
                tableroPanel.repaint();
            }
        } else {
            // Segunda selección: mover o cambiar pieza
            if (destinosValidos.contains(clave)) {
                juego.setEstadoTablero(juego.ejecutarMovimiento(
                        origenSeleccionado, bit, juego.getEstadoActual()));
                juego.cambiarTurno();
                nTurnos++;
                log("Movimiento → " + clave);
                origenSeleccionado = 0L;
                destinosValidos.clear();
                actualizarUI();
                verificarFin();
                if (juego.enProgreso()) planificarTurnoIA();
            } else if ((piezasPropias & bit) != 0) {
                // Cambiar selección
                origenSeleccionado = bit;
                destinosValidos = juego.calcularDestinosValidos(bit);
                tableroPanel.repaint();
            } else {
                origenSeleccionado = 0L;
                destinosValidos.clear();
                tableroPanel.repaint();
            }
        }
    }

    private void verificarFin() {
        if (!juego.enProgreso()) {
            boolean blancoGana = juego.getEstadoActual().esVictoriaBlancas();
            String msg = blancoGana ? "🏆 ¡Blancas ganan!" : "🏆 ¡Negras ganan!";
            log(msg);
            actualizarRotulo(msg);
            tableroPanel.setEnabled(false);
            JOptionPane.showMessageDialog(this, msg, "Fin de partida", JOptionPane.INFORMATION_MESSAGE);
            Profiler.report();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  GUARDADO / CARGA / NUEVA PARTIDA
    // ══════════════════════════════════════════════════════════════════════

    private void nuevaPartida() {
        juego = new MotorJuego();
        minimax = new Minimax();
        montecarlo = new Montecarlo();
        Profiler.reset();
        origenSeleccionado = 0L;
        destinosValidos.clear();
        iaOcupada = false;
        mostrarDialogoConfiguracion();
    }

    private void guardarPartida() {
        String slot = JOptionPane.showInputDialog(this, "Ranura (0-9):", "Guardar", JOptionPane.PLAIN_MESSAGE);
        if (slot == null) return;
        try {
            int idx = Integer.parseInt(slot.trim());
            if (idx < 0 || idx > 9) throw new NumberFormatException();
            boolean ok = juego.guardarEstado(idx);
            log(ok ? "💾 Guardado en ranura " + idx : "⚠ Error al guardar.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ranura inválida (0-9).");
        }
    }

    private void cargarPartida() {
        String slot = JOptionPane.showInputDialog(this, "Ranura (0-9):", "Cargar", JOptionPane.PLAIN_MESSAGE);
        if (slot == null) return;
        try {
            int idx = Integer.parseInt(slot.trim());
            if (idx < 0 || idx > 9) throw new NumberFormatException();
            juego.cargarEstado(idx);
            nTurnos = 1;
            origenSeleccionado = 0L;
            destinosValidos.clear();
            iaOcupada = false;
            log("📂 Cargada ranura " + idx);
            actualizarUI();
            planificarTurnoIA();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ranura inválida (0-9).");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la ranura.");
        }
    }

    private void mostrarProfiling() {
        // Captura el report en un String
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(baos);
        java.io.PrintStream old = System.out;
        System.setOut(ps);
        Profiler.report();
        System.setOut(old);
        String reporte = baos.toString();

        JTextArea ta = new JTextArea(reporte, 20, 60);
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Profiling", JOptionPane.PLAIN_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ACTUALIZACIONES DE UI
    // ══════════════════════════════════════════════════════════════════════

    private void actualizarUI() {
        tableroPanel.repaint();
        EstadoTablero est = juego.getEstadoActual();
        String turnoStr = est.getTurno() ? "Blancas" : "Negras";
        actualizarRotulo("Turno: " + turnoStr);
        lblTurnos.setText("Turno nº " + nTurnos);
    }

    private void actualizarRotulo(String texto) {
        lblTurno.setText(texto);
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private JLabel etiq(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(COLOR_TEXTO_PANEL);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    /** Convierte un bit en la clave de casilla (e.g. "A1"). */
    private String bitAClave(long bit) {
        // Decodifica: el Decoder usa de A-H y 1-8
        // bit = 1 → H1, bit = 2 → G1, …, bit = 0x8000… → A8
        int pos = Long.numberOfTrailingZeros(bit); // 0-63
        int fila = pos / 8 + 1;           // 1-8
        int colPos = pos % 8;             // 0=H, 7=A
        char col = (char)('H' - colPos);
        return "" + col + fila;
    }

    /** Comprueba si la clave de destino corresponde al bit. */
    private boolean esDestino(long bit) {
        if (destinosValidos.isEmpty()) return false;
        String clave = bitAClave(bit);
        return destinosValidos.contains(clave);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PANEL DEL TABLERO (renderizado custom)
    // ══════════════════════════════════════════════════════════════════════

    private class TableroPanel extends JPanel {

        TableroPanel() {
            setPreferredSize(new Dimension(CELL * COLS + 24, CELL * ROWS + 24));
            setBackground(COLOR_FONDO_PANEL);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    // Las coordenadas del tablero empiezan en (20, 20)
                    int bx = e.getX() - 20;
                    int by = e.getY() - 20;
                    if (bx < 0 || by < 0 || bx >= CELL * COLS || by >= CELL * ROWS) return;
                    int col = bx / CELL;
                    int fila = by / CELL;
                    manejarClicCelda(col, fila);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int ox = 20, oy = 20; // offset para letras/números

            EstadoTablero est = juego.getEstadoActual();
            long blancas = est.getPiezasBlancas();
            long negras  = est.getPiezasNegras();

            // ── Celdas ────────────────────────────────────────────────────
            for (int fv = 0; fv < ROWS; fv++) {        // fila visual 0=arriba
                int fl = ROWS - fv;                     // fila lógica 8-1
                for (int c = 0; c < COLS; c++) {
                    int x = ox + c * CELL;
                    int y = oy + fv * CELL;

                    // Color base
                    boolean clara = (fv + c) % 2 == 0;
                    g2.setColor(clara ? COLOR_CELDA_CLARA : COLOR_CELDA_OSCURA);
                    g2.fillRect(x, y, CELL, CELL);

                    // Calcular el bit de esta celda
                    // La columna visual 0 = 'A', visual 7 = 'H'
                    // En el Decoder: col 'H'=0, col 'A'=7 dentro de la fila
                    int colLogica = COLS - 1 - c; // 7=A(col0), 0=H(col7)
                    int bitPos = (fl - 1) * 8 + colLogica; // 0-63
                    long bit = 1L << bitPos;

                    // Selección
                    if (bit == origenSeleccionado) {
                        g2.setColor(COLOR_SELECCION);
                        g2.fillRect(x, y, CELL, CELL);
                    } else if (esDestino(bit)) {
                        g2.setColor(COLOR_DESTINO);
                        g2.fillRect(x, y, CELL, CELL);
                        // Punto de destino
                        g2.setColor(new Color(0x2255CC));
                        g2.fillOval(x + CELL/2 - 10, y + CELL/2 - 10, 20, 20);
                    }

                    // Borde de celda sutil
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.drawRect(x, y, CELL - 1, CELL - 1);

                    // ── Fichas ─────────────────────────────────────────────
                    if ((blancas & bit) != 0) dibujarFicha(g2, x, y, true);
                    else if ((negras & bit) != 0) dibujarFicha(g2, x, y, false);
                }
            }

            // ── Etiquetas de columna (A-H) ─────────────────────────────────
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(0xCCCCCC));
            for (int c = 0; c < COLS; c++) {
                char col = (char)('A' + c);
                g2.drawString(String.valueOf(col), ox + c * CELL + CELL / 2 - 4, oy - 4);
            }

            // ── Etiquetas de fila (8-1) ─────────────────────────────────────
            for (int fv = 0; fv < ROWS; fv++) {
                int fl = ROWS - fv;
                g2.drawString(String.valueOf(fl), ox - 14, oy + fv * CELL + CELL / 2 + 5);
            }
        }

        private void dibujarFicha(Graphics2D g2, int x, int y, boolean blanca) {
            int margin = 8;
            int diam   = CELL - margin * 2;
            int fx = x + margin, fy = y + margin;

            // Sombra
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(fx + 3, fy + 3, diam, diam);

            // Cuerpo
            if (blanca) {
                g2.setColor(new Color(0xF5F5F5));
                g2.fillOval(fx, fy, diam, diam);
                g2.setColor(new Color(0xDDDDDD));
                g2.drawOval(fx, fy, diam, diam);
                // Brillo
                g2.setColor(new Color(0xFFFFFF, true));
                g2.fillOval(fx + diam / 4, fy + diam / 6, diam / 3, diam / 4);
                // Símbolo dirección (blancas suben)
                g2.setColor(new Color(0x555555));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString("▲", fx + diam / 2 - 6, fy + diam / 2 + 6);
            } else {
                // Gradiente oscuro
                GradientPaint gp = new GradientPaint(fx, fy, new Color(0x555555),
                        fx + diam, fy + diam, new Color(0x111111));
                g2.setPaint(gp);
                g2.fillOval(fx, fy, diam, diam);
                g2.setPaint(null);
                g2.setColor(new Color(0x333333));
                g2.drawOval(fx, fy, diam, diam);
                // Brillo tenue
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(fx + diam / 4, fy + diam / 6, diam / 3, diam / 4);
                // Símbolo dirección (negras bajan)
                g2.setColor(new Color(0xCCCCCC));
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.drawString("▼", fx + diam / 2 - 6, fy + diam / 2 + 6);
            }
        }
    }
}