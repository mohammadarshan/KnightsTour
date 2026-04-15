# Knight's Tour

A Java application that solves the Knight's Tour problem and visualizes the solution on an interactive chessboard GUI. The knight must visit every square on an m x n board exactly once. The solver uses Warnsdorff's heuristic as the primary algorithm and falls back to backtracking with move ordering when needed.

## Language and Tools

- **Language:** Java
- **GUI:** Java Swing
- **Version Control:** Git / GitHub

## Getting Started

### Clone the repository

```bash
git clone https://github.com/mohammadarshan/knights-tour.git
cd knights-tour/KnightsTour/src
```

### Compile

```bash
javac KnightsTour.java ChessboardGUI.java
```

### Run

**GUI mode (recommended):**

```bash
java ChessboardGUI
```

**Console mode:**

```bash
java KnightsTour
```

## Input Format

The program takes two integers for the board dimensions: rows and columns.

- **GUI mode:** Enter the values in the Rows and Cols fields at the top of the window, then click Start.
- **Console mode:** Type two space-separated integers when prompted (e.g. `8 8`).

Both values must be positive integers. The default board size in the GUI is 8x8.

## Expected Output

**GUI mode:** A dark-themed window with an interactive chessboard. The knight starts at the top-left corner. You can step through the tour using the Previous, Next, and Reset buttons, or use Auto-Play to animate the full tour. A speed slider controls the animation speed. Each visited square shows its move number after the knight leaves it.

**Console mode:** A numbered grid printed to the terminal showing the visit order for each square.

Example output for a 5x5 board:

```
 1 12 21  6 17
22  7 18 11 20
13  2  9 16  5
 8 23  4 19 10
 3 14 25 24 15
```

## Known Limitations

- No knight's tour exists on boards smaller than 5x5 (except the trivial 1x1).
- Boards where one dimension is 1 or 2 have no solution.
- Boards where both dimensions are odd and less than 5 have no solution.
- The backtracking fallback has an iteration cap of 4 million to avoid long wait times. In rare cases this may cause the solver to report no solution on a board that technically has one.

## Third-Party Libraries

None. The project uses only the Java standard library (Swing, AWT).
