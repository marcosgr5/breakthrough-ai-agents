# Breakthrough: agentes de IA (Java)

**ES** · [EN](#english)

Implementación en Java del juego de tablero **Breakthrough** con agentes que juegan solos. Proyecto académico en equipo (3 personas) de la Escuela Superior de Informática (UCLM).

## Qué incluye

- **Minimax con poda alfa-beta** (profundidad configurable) y **Monte Carlo Tree Search** (10.000 simulaciones en las pruebas).
- Interfaz de comandos y **interfaz gráfica** (Swing).
- Guardado y carga de partidas (`persistencia/`).
- Un **profiler** para medir el rendimiento de los agentes.
- Un banco de pruebas con partidas simuladas entre agentes (`estadisticas_breakthrough.csv`).

## Resultados (100 partidas por enfrentamiento)

| Blancas | Negras | Victorias blancas | Victorias negras |
|---|---|---|---|
| Aleatorio | Aleatorio | 50 | 50 |
| Aleatorio | Minimax P4 | 0 | 100 |
| Aleatorio | Montecarlo 10K | 0 | 100 |
| Minimax P4 | Minimax P4 | 55 | 45 |
| Minimax P4 | Montecarlo 10K | 100 | 0 |
| Montecarlo 10K | Montecarlo 10K | 54 | 46 |

`P4` es Minimax con profundidad 4 y `10K` es Monte Carlo con 10.000 simulaciones. Ambos agentes ganan el 100 % frente a un jugador aleatorio, y las partidas entre agentes iguales están equilibradas.

## Estructura

- `src/dominio/`: estado del tablero, movimientos y algoritmos de IA.
- `src/persistencia/`: guardado y carga.
- `src/presentación/`: interfaces de comandos y gráfica.

## Uso

Requiere Java 8 o superior.

```
find src -name "*.java" > fuentes.txt
javac -encoding UTF-8 -d bin @fuentes.txt
java -cp bin presentación.InterfazComandos
```

Para la interfaz gráfica, cambia la última clase por `presentación.InterfazGrafica`.

## Créditos

Equipo: **Marcos Galán Rodríguez**, **Álvaro Márquez Gavilán** y **Alba Romero de los Ríos**. La especificación del laboratorio pertenece a los profesores de la asignatura y no se incluye.

---

## English

**Breakthrough** board game in Java with self-playing AI agents. Academic team project (3 people) from the School of Computer Science (UCLM, Spain).

### Features

- **Minimax with alpha-beta pruning** (configurable depth) and **Monte Carlo Tree Search** (10,000 simulations in the benchmarks).
- Command-line interface and **graphical interface** (Swing).
- Game saving and loading (`persistencia/`).
- A **profiler** to measure agent performance.
- A benchmark of simulated games between agents (`estadisticas_breakthrough.csv`).

### Results (100 games per match-up)

| White | Black | White wins | Black wins |
|---|---|---|---|
| Random | Random | 50 | 50 |
| Random | Minimax P4 | 0 | 100 |
| Random | Montecarlo 10K | 0 | 100 |
| Minimax P4 | Minimax P4 | 55 | 45 |
| Minimax P4 | Montecarlo 10K | 100 | 0 |
| Montecarlo 10K | Montecarlo 10K | 54 | 46 |

`P4` is Minimax at depth 4 and `10K` is Monte Carlo with 10,000 simulations. Both agents win 100% against a random player, and games between identical agents are balanced.

### Usage

Requires Java 8 or later.

```
find src -name "*.java" > fuentes.txt
javac -encoding UTF-8 -d bin @fuentes.txt
java -cp bin presentación.InterfazComandos
```

Use `presentación.InterfazGrafica` for the graphical interface.

### Credits

Team: **Marcos Galán Rodríguez**, **Álvaro Márquez Gavilán** and **Alba Romero de los Ríos**. The lab specification belongs to the course instructors and is not included.
