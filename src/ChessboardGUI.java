import javax.swing.*;
import java.awt.*;

/**
 * Swing GUI that draws an m x n chessboard and lets you step
 * through a completed Knight's Tour one move at a time.
 * Board dimensions can be changed at any time from the input
 * panel at the top. Errors are shown inside the GUI.
 */
public class ChessboardGUI extends JFrame {

    private static final Color LIGHT = new Color(240, 217, 181);
    private static final Color DARK  = new Color(181, 136, 99);
    private static final int CELL_SIZE = 60;

    /** Current board dimensions. Updated when a new tour is loaded. */
    private int rows;
    private int cols;
    /** Full tour from the solver. Null if no tour is loaded. */
    private int[] tour;
    /** Move number for each cell. moveAt[r][c] gives the 1-based visit order. */
    private int[][] moveAt;
    /** How far we have stepped through the tour (0-based index into tour). */
    private int currentStep;

    private final BoardPanel boardPanel;
    private final Timer autoPlayTimer;
    private JButton autoPlayBtn;
    private JLabel errorLabel;

    public ChessboardGUI() {
        setTitle("Knight's Tour");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        boardPanel = new BoardPanel();

        // Timer advances one step per tick and stops at the end
        autoPlayTimer = new Timer(300, e -> {
            if (tour != null && currentStep < tour.length - 1) {
                currentStep++;
                boardPanel.repaint();
            } else {
                stopAutoPlay();
            }
        });

        add(createInputPanel(), BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);

        // Start with a default 8x8 board
        loadTour(8, 8);
    }

    /**
     * Validates the given dimensions, runs the solver, and refreshes
     * the board. Shows an error message if something goes wrong.
     */
    private void loadTour(int newRows, int newCols) {
        stopAutoPlay();
        errorLabel.setText(" ");

        if (newRows < 1 || newCols < 1) {
            showError("Rows and columns must be at least 1.");
            return;
        }

        int[] result = solve(newRows, newCols);
        if (result == null) {
            showError("No knight's tour exists on a " + newRows + "x" + newCols + " board.");
            return;
        }

        this.rows = newRows;
        this.cols = newCols;
        this.tour = result;
        this.currentStep = 0;
        this.moveAt = new int[rows][cols];
        for (int i = 0; i < tour.length; i++)
            moveAt[tour[i] / cols][tour[i] % cols] = i + 1;

        setTitle("Knight's Tour - " + rows + "x" + cols);
        boardPanel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        pack();
        setLocationRelativeTo(null);
        boardPanel.repaint();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setForeground(new Color(180, 30, 30));
    }

    private JPanel createInputPanel() {
        JTextField rowsField = new JTextField("8", 3);
        JTextField colsField = new JTextField("8", 3);
        JButton startBtn = new JButton("Start");
        errorLabel = new JLabel(" ");

        startBtn.addActionListener(e -> {
            String rowText = rowsField.getText().trim();
            String colText = colsField.getText().trim();

            int r, c;
            try {
                r = Integer.parseInt(rowText);
                c = Integer.parseInt(colText);
            } catch (NumberFormatException ex) {
                showError("Please enter valid integers for rows and columns.");
                return;
            }

            loadTour(r, c);
        });

        JPanel fields = new JPanel();
        fields.add(new JLabel("Rows:"));
        fields.add(rowsField);
        fields.add(new JLabel("Cols:"));
        fields.add(colsField);
        fields.add(startBtn);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(fields, BorderLayout.CENTER);
        panel.add(errorLabel, BorderLayout.SOUTH);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        return panel;
    }

    private void stopAutoPlay() {
        autoPlayTimer.stop();
        if (autoPlayBtn != null)
            autoPlayBtn.setText("Auto-Play");
    }

    private JPanel createControlPanel() {
        JButton prevBtn  = new JButton("<< Previous");
        JButton nextBtn  = new JButton("Next >>");
        JButton resetBtn = new JButton("Reset");
        autoPlayBtn      = new JButton("Auto-Play");

        prevBtn.addActionListener(e -> {
            stopAutoPlay();
            if (tour != null && currentStep > 0) {
                currentStep--;
                boardPanel.repaint();
            }
        });

        nextBtn.addActionListener(e -> {
            stopAutoPlay();
            if (tour != null && currentStep < tour.length - 1) {
                currentStep++;
                boardPanel.repaint();
            }
        });

        resetBtn.addActionListener(e -> {
            stopAutoPlay();
            currentStep = 0;
            boardPanel.repaint();
        });

        autoPlayBtn.addActionListener(e -> {
            if (tour == null) return;
            if (autoPlayTimer.isRunning()) {
                stopAutoPlay();
            } else {
                // If already at the end, restart from the beginning
                if (currentStep >= tour.length - 1)
                    currentStep = 0;
                autoPlayBtn.setText("Pause");
                autoPlayTimer.start();
            }
        });

        // Speed slider: left is slow (800ms), right is fast (50ms)
        JSlider speedSlider = new JSlider(50, 800, 300);
        speedSlider.setInverted(true);
        speedSlider.setPreferredSize(new Dimension(120, 26));
        speedSlider.addChangeListener(e ->
                autoPlayTimer.setDelay(speedSlider.getValue()));

        JPanel buttons = new JPanel();
        buttons.add(prevBtn);
        buttons.add(resetBtn);
        buttons.add(nextBtn);
        buttons.add(autoPlayBtn);

        JPanel speedPanel = new JPanel();
        speedPanel.add(new JLabel("Slow"));
        speedPanel.add(speedSlider);
        speedPanel.add(new JLabel("Fast"));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buttons, BorderLayout.CENTER);
        panel.add(speedPanel, BorderLayout.SOUTH);
        return panel;
    }

    private class BoardPanel extends JPanel {

        BoardPanel() {
            setPreferredSize(new Dimension(8 * CELL_SIZE, 8 * CELL_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (tour == null) return;

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
        SwingUtilities.invokeLater(() -> new ChessboardGUI().setVisible(true));
    }
}
