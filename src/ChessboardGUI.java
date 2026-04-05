import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Swing GUI that draws an m x n chessboard and lets you step
 * through a completed Knight's Tour one move at a time.
 * Board dimensions can be changed from the input panel at the
 * top. The cell size adjusts automatically so that large boards
 * still fit on screen.
 */
public class ChessboardGUI extends JFrame {

    private static final Color LIGHT      = new Color(240, 217, 181);
    private static final Color DARK       = new Color(181, 136, 99);
    private static final Color BG         = new Color(48, 46, 43);
    private static final Color PANEL_BG   = new Color(38, 36, 33);
    private static final Color TEXT_COLOR = new Color(220, 220, 210);
    private static final int MAX_CELL     = 70;
    private static final int MIN_CELL     = 28;
    private static final int MAX_BOARD_PX = 620;
    private static final int MIN_WIDTH    = 480;

    private int rows;
    private int cols;
    private int cellSize;
    private int[] tour;
    private int[][] moveAt;
    private int currentStep;

    private final BoardPanel boardPanel;
    private final Timer autoPlayTimer;
    private JButton autoPlayBtn;
    private JLabel errorLabel;
    private JLabel stepLabel;

    public ChessboardGUI() {
        setTitle("Knight's Tour");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG);

        boardPanel = new BoardPanel();

        autoPlayTimer = new Timer(300, e -> {
            if (tour != null && currentStep < tour.length - 1) {
                currentStep++;
                updateStepLabel();
                boardPanel.repaint();
            } else {
                stopAutoPlay();
            }
        });

        add(createTopPanel(), BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);

        loadTour(8, 8);
    }

    /**
     * Computes cell size so that the board fits within MAX_BOARD_PX,
     * clamped between MIN_CELL and MAX_CELL.
     */
    private int computeCellSize(int r, int c) {
        int size = Math.min(MAX_BOARD_PX / Math.max(r, 1),
                            MAX_BOARD_PX / Math.max(c, 1));
        return Math.max(MIN_CELL, Math.min(MAX_CELL, size));
    }

    private void loadTour(int newRows, int newCols) {
        stopAutoPlay();
        errorLabel.setText(" ");

        if (newRows < 1 || newCols < 1) {
            showError("Rows and columns must be at least 1.");
            return;
        }

        int[] result = solve(newRows, newCols);
        if (result == null) {
            showError("No tour exists on a " + newRows + "x" + newCols + " board.");
            return;
        }

        this.rows = newRows;
        this.cols = newCols;
        this.tour = result;
        this.currentStep = 0;
        this.cellSize = computeCellSize(rows, cols);
        this.moveAt = new int[rows][cols];
        for (int i = 0; i < tour.length; i++)
            moveAt[tour[i] / cols][tour[i] % cols] = i + 1;

        setTitle("Knight's Tour - " + rows + "x" + cols);
        updateStepLabel();

        int boardW = Math.max(cols * cellSize, MIN_WIDTH);
        int boardH = rows * cellSize;
        boardPanel.setPreferredSize(new Dimension(boardW, boardH));
        pack();
        setLocationRelativeTo(null);
        boardPanel.repaint();
    }

    private void updateStepLabel() {
        if (tour == null) {
            stepLabel.setText(" ");
            return;
        }
        stepLabel.setText("Move " + (currentStep + 1) + " / " + tour.length);
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    // ---- Top panel: title, input fields, error and step labels ----

    private JPanel createTopPanel() {
        JLabel title = new JLabel("Knight's Tour");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(8, 0, 4, 0));

        JTextField rowsField = new JTextField("8", 3);
        JTextField colsField = new JTextField("8", 3);
        JButton startBtn = new JButton("Start");

        startBtn.addActionListener(e -> {
            try {
                int r = Integer.parseInt(rowsField.getText().trim());
                int c = Integer.parseInt(colsField.getText().trim());
                loadTour(r, c);
            } catch (NumberFormatException ex) {
                showError("Please enter valid integers for rows and columns.");
            }
        });

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.add(makeLabel("Rows:"));
        fields.add(rowsField);
        fields.add(Box.createHorizontalStrut(6));
        fields.add(makeLabel("Cols:"));
        fields.add(colsField);
        fields.add(Box.createHorizontalStrut(6));
        fields.add(startBtn);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(220, 80, 70));
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        stepLabel = new JLabel(" ");
        stepLabel.setForeground(TEXT_COLOR);
        stepLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        stepLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        info.add(errorLabel);
        info.add(stepLabel);

        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(4, 12, 6, 12));
        panel.add(title, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        panel.add(info, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_COLOR);
        return lbl;
    }

    // ---- Playback controls ----

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
                updateStepLabel();
                boardPanel.repaint();
            }
        });

        nextBtn.addActionListener(e -> {
            stopAutoPlay();
            if (tour != null && currentStep < tour.length - 1) {
                currentStep++;
                updateStepLabel();
                boardPanel.repaint();
            }
        });

        resetBtn.addActionListener(e -> {
            stopAutoPlay();
            currentStep = 0;
            updateStepLabel();
            boardPanel.repaint();
        });

        autoPlayBtn.addActionListener(e -> {
            if (tour == null) return;
            if (autoPlayTimer.isRunning()) {
                stopAutoPlay();
            } else {
                if (currentStep >= tour.length - 1)
                    currentStep = 0;
                autoPlayBtn.setText("Pause");
                autoPlayTimer.start();
            }
        });

        JSlider speedSlider = new JSlider(50, 800, 300);
        speedSlider.setInverted(true);
        speedSlider.setPreferredSize(new Dimension(120, 26));
        speedSlider.setOpaque(false);
        speedSlider.addChangeListener(e ->
                autoPlayTimer.setDelay(speedSlider.getValue()));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        buttons.setOpaque(false);
        buttons.add(prevBtn);
        buttons.add(resetBtn);
        buttons.add(nextBtn);
        buttons.add(autoPlayBtn);

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        speedPanel.setOpaque(false);
        speedPanel.add(makeLabel("Slow"));
        speedPanel.add(speedSlider);
        speedPanel.add(makeLabel("Fast"));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(4, 12, 8, 12));
        panel.add(buttons, BorderLayout.CENTER);
        panel.add(speedPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ---- Board rendering ----

    private class BoardPanel extends JPanel {

        BoardPanel() {
            setBackground(BG);
            setPreferredSize(new Dimension(8 * MAX_CELL, 8 * MAX_CELL));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (tour == null) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Center the board if the panel is wider than the grid
            int gridW = cols * cellSize;
            int gridH = rows * cellSize;
            int ox = (getWidth() - gridW) / 2;
            int oy = (getHeight() - gridH) / 2;

            Font numFont = new Font("SansSerif", Font.BOLD, cellSize / 3);
            g2.setFont(numFont);
            FontMetrics fm = g2.getFontMetrics();

            int knightRow = tour[currentStep] / cols;
            int knightCol = tour[currentStep] % cols;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int x = ox + c * cellSize;
                    int y = oy + r * cellSize;

                    boolean light = (r + c) % 2 == 0;
                    g2.setColor(light ? LIGHT : DARK);
                    g2.fillRect(x, y, cellSize, cellSize);

                    int move = moveAt[r][c];
                    if (move > 0 && move <= currentStep) {
                        String num = String.valueOf(move);
                        int tx = x + (cellSize - fm.stringWidth(num)) / 2;
                        int ty = y + (cellSize - fm.getHeight()) / 2
                                + fm.getAscent();
                        g2.setColor(light ? DARK : LIGHT);
                        g2.drawString(num, tx, ty);
                    }
                }
            }

            // Draw the knight on its current square
            Font knightFont = new Font("Serif", Font.PLAIN, (int) (cellSize * 0.7));
            g2.setFont(knightFont);
            FontMetrics kfm = g2.getFontMetrics();
            String knight = "\u265E";
            int kx = ox + knightCol * cellSize
                    + (cellSize - kfm.stringWidth(knight)) / 2;
            int ky = oy + knightRow * cellSize
                    + (cellSize - kfm.getHeight()) / 2 + kfm.getAscent();
            boolean knightOnLight = (knightRow + knightCol) % 2 == 0;
            g2.setColor(knightOnLight ? new Color(40, 40, 40) : Color.WHITE);
            g2.drawString(knight, kx, ky);
        }
    }

    // ---- Solver bridge ----

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
