import javax.swing.*;
import java.awt.*;

/**
 * Custom JPanel that draws the chessboard grid, move numbers,
 * and the knight piece. Reads all tour state from the parent
 * ChessboardGUI instance.
 */
public class BoardPanel extends JPanel {

    private final ChessboardGUI gui;

    public BoardPanel(ChessboardGUI gui) {
        this.gui = gui;
        setBackground(Theme.BG);
        setPreferredSize(new Dimension(8 * Theme.MAX_CELL, 8 * Theme.MAX_CELL));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gui.getTour() == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int rows     = gui.getBoardRows();
        int cols     = gui.getBoardCols();
        int cellSize = gui.getCellSize();
        int[] tour   = gui.getTour();
        int[][] moveAt = gui.getMoveAt();
        int step     = gui.getCurrentStep();

        // Center the board if the panel is wider than the grid
        int gridW = cols * cellSize;
        int gridH = rows * cellSize;
        int ox = (getWidth() - gridW) / 2;
        int oy = (getHeight() - gridH) / 2;

        drawSquares(g2, rows, cols, cellSize, moveAt, step, ox, oy);
        drawKnight(g2, tour[step], cols, cellSize, ox, oy);
    }

    private void drawSquares(Graphics2D g2, int rows, int cols, int cellSize,
                             int[][] moveAt, int step, int ox, int oy) {
        Font numFont = new Font("SansSerif", Font.BOLD, cellSize / 3);
        g2.setFont(numFont);
        FontMetrics fm = g2.getFontMetrics();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = ox + c * cellSize;
                int y = oy + r * cellSize;

                boolean light = (r + c) % 2 == 0;
                g2.setColor(light ? Theme.LIGHT_SQUARE : Theme.DARK_SQUARE);
                g2.fillRect(x, y, cellSize, cellSize);

                // Only show the number on squares the knight has already left
                int move = moveAt[r][c];
                if (move > 0 && move <= step) {
                    String num = String.valueOf(move);
                    int tx = x + (cellSize - fm.stringWidth(num)) / 2;
                    int ty = y + (cellSize - fm.getHeight()) / 2 + fm.getAscent();
                    g2.setColor(light ? Theme.DARK_SQUARE : Theme.LIGHT_SQUARE);
                    g2.drawString(num, tx, ty);
                }
            }
        }
    }

    private void drawKnight(Graphics2D g2, int boardIndex, int cols,
                            int cellSize, int ox, int oy) {
        int knightRow = boardIndex / cols;
        int knightCol = boardIndex % cols;

        Font knightFont = new Font("Serif", Font.PLAIN, (int) (cellSize * 0.7));
        g2.setFont(knightFont);
        FontMetrics kfm = g2.getFontMetrics();

        String knight = "\u265E";
        int kx = ox + knightCol * cellSize
                + (cellSize - kfm.stringWidth(knight)) / 2;
        int ky = oy + knightRow * cellSize
                + (cellSize - kfm.getHeight()) / 2 + kfm.getAscent();

        boolean onLight = (knightRow + knightCol) % 2 == 0;
        g2.setColor(onLight ? Theme.KNIGHT_DARK : Theme.KNIGHT_LIGHT);
        g2.drawString(knight, kx, ky);
    }
}
