import javax.swing.*;
import java.awt.*;

/**
 * Swing GUI that draws an m x n chessboard and lets you step
 * through a completed Knight's Tour one move at a time using
 * Next, Previous, and Reset buttons.
 */
public class ChessboardGUI extends JFrame {

    private static final Color LIGHT = new Color(240, 217, 181);
    private static final Color DARK  = new Color(181, 136, 99);
    private static final int CELL_SIZE = 60;

    private final int rows;
    private final int cols;
    /** Full tour from the solver. tour[i] is the board index visited at step i. */
    private final int[] tour;
    /** Move number for each cell. moveAt[r][c] gives the 1-based visit order. */
    private final int[][] moveAt;
    /** How far we have stepped through the tour (0-based index into tour). */
    private int currentStep;

    private final BoardPanel boardPanel;

    public ChessboardGUI(int rows, int cols, int[] tour) {
        this.rows = rows;
        this.cols = cols;
        this.tour = tour;
        this.currentStep = 0;

        this.moveAt = new int[rows][cols];
        for (int i = 0; i < tour.length; i++)
            moveAt[tour[i] / cols][tour[i] % cols] = i + 1;

        setTitle("Knight's Tour - " + rows + "x" + cols);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createControlPanel() {
        JButton prevBtn  = new JButton("\u25C0 Previous");
        JButton nextBtn  = new JButton("Next \u25B6");
        JButton resetBtn = new JButton("Reset");

        prevBtn.addActionListener(e -> {
            if (currentStep > 0) {
                currentStep--;
                boardPanel.repaint();
            }
        });

        nextBtn.addActionListener(e -> {
            if (currentStep < tour.length - 1) {
                currentStep++;
                boardPanel.repaint();
            }
        });

        resetBtn.addActionListener(e -> {
            currentStep = 0;
            boardPanel.repaint();
        });

        JPanel panel = new JPanel();
        panel.add(prevBtn);
        panel.add(resetBtn);
        panel.add(nextBtn);
        return panel;
    }

    private class BoardPanel extends JPanel {

        BoardPanel() {
            setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Font numFont = new Font("SansSerif", Font.BOLD, CELL_SIZE / 3);
            g2.setFont(numFont);
            FontMetrics fm = g2.getFontMetrics();

            // Current knight position from the tour
            int knightRow = tour[currentStep] / cols;
            int knightCol = tour[currentStep] % cols;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    boolean light = (r + c) % 2 == 0;
                    g2.setColor(light ? LIGHT : DARK);
                    g2.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                    // Only show the number on squares the knight has already left
                    int move = moveAt[r][c];
                    if (move > 0 && move <= currentStep) {
                        String num = String.valueOf(move);
                        int tx = c * CELL_SIZE + (CELL_SIZE - fm.stringWidth(num)) / 2;
                        int ty = r * CELL_SIZE + (CELL_SIZE - fm.getHeight()) / 2
                                + fm.getAscent();
                        g2.setColor(light ? DARK : LIGHT);
                        g2.drawString(num, tx, ty);
                    }
                }
            }

            // Draw the knight symbol on its current square
            Font knightFont = new Font("Serif", Font.PLAIN, (int) (CELL_SIZE * 0.7));
            g2.setFont(knightFont);
            FontMetrics kfm = g2.getFontMetrics();
            String knight = "\u265E";
            int kx = knightCol * CELL_SIZE + (CELL_SIZE - kfm.stringWidth(knight)) / 2;
            int ky = knightRow * CELL_SIZE + (CELL_SIZE - kfm.getHeight()) / 2
                    + kfm.getAscent();
            boolean knightOnLight = (knightRow + knightCol) % 2 == 0;
            g2.setColor(knightOnLight ? new Color(40, 40, 40) : new Color(255, 255, 255));
            g2.drawString(knight, kx, ky);
        }
    }

    /**
     * Runs the solver and returns a tour. Returns null if no tour exists.
     */
    private static int[] solve(int rows, int cols) {
        KnightsTour.ROWS = rows;
        KnightsTour.COLS = cols;

        boolean[][] visited = new boolean[rows][cols];
        int[] tour = KnightsTour.warnsdorff(visited);
        if (tour != null)
            return tour;

        KnightsTour.path = new int[rows * cols];
        KnightsTour.path[0] = 0;
        KnightsTour.iters = 0;
        visited = new boolean[rows][cols];
        visited[0][0] = true;

        if (KnightsTour.backtrack(0, 0, 1, visited))
            return KnightsTour.path;

        return null;
    }

    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog(null,
                "Enter rows and columns (e.g. 8 5):", "Board Size",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty())
            return;

        String[] parts = input.trim().split("\\s+");
        if (parts.length < 2) {
            JOptionPane.showMessageDialog(null,
                    "Please enter two integers separated by a space.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int rows = Integer.parseInt(parts[0]);
        int cols = Integer.parseInt(parts[1]);
        int[] tour = solve(rows, cols);

        if (tour == null) {
            JOptionPane.showMessageDialog(null,
                    "No knight's tour exists on a " + rows + "\u00d7" + cols + " board.",
                    "No Solution", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new ChessboardGUI(rows, cols, tour).setVisible(true));
    }
}
