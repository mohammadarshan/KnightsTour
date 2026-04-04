import javax.swing.*;
import java.awt.*;

/**
 * Swing GUI that renders an m×n chessboard with alternating
 * light and dark squares.  Board dimensions are supplied via
 * an input dialog — the same two integers the console solver accepts.
 */
public class ChessboardGUI extends JFrame {

    private static final Color LIGHT = new Color(240, 217, 181);
    private static final Color DARK  = new Color(181, 136, 99);
    private static final int CELL_SIZE = 60;

    private final int rows;
    private final int cols;

    public ChessboardGUI(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        setTitle("Knight's Tour \u2013 " + rows + "\u00d7" + cols);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        add(new BoardPanel());
        pack();
        setLocationRelativeTo(null);
    }

    private class BoardPanel extends JPanel {

        BoardPanel() {
            setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    g.setColor((r + c) % 2 == 0 ? LIGHT : DARK);
                    g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
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

        SwingUtilities.invokeLater(() -> new ChessboardGUI(rows, cols).setVisible(true));
    }
}
