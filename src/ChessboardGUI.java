import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Main frame that wires together the input panel, board panel,
 * and playback controls. Holds the current tour state and exposes
 * it through getters so that BoardPanel can read it during painting.
 */
public class ChessboardGUI extends JFrame {

    // ---- Tour state ----

    private int rows;
    private int cols;
    private int cellSize;
    private int[] tour;
    private int[][] moveAt;
    private int currentStep;

    // ---- UI components ----

    private final BoardPanel boardPanel;
    private final Timer autoPlayTimer;
    private JButton autoPlayBtn;
    private JLabel errorLabel;
    private JLabel stepLabel;

    public ChessboardGUI() {
        setTitle("Knight's Tour");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Theme.BG);

        boardPanel = new BoardPanel(this);

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

    // ---- State access for BoardPanel ----

    public int   getBoardRows()    { return rows; }
    public int   getBoardCols()    { return cols; }
    public int   getCellSize()     { return cellSize; }
    public int[] getTour()         { return tour; }
    public int[][] getMoveAt()     { return moveAt; }
    public int   getCurrentStep()  { return currentStep; }

    // ---- Tour loading ----

    private int computeCellSize(int r, int c) {
        int size = Math.min(Theme.MAX_BOARD_PX / Math.max(r, 1),
                            Theme.MAX_BOARD_PX / Math.max(c, 1));
        return Math.max(Theme.MIN_CELL, Math.min(Theme.MAX_CELL, size));
    }

    private void loadTour(int newRows, int newCols) {
        stopAutoPlay();
        errorLabel.setText(" ");

        if (newRows < 1 || newCols < 1) {
            showError("Rows and columns must be at least 1.");
            return;
        }

        int[] result = TourSolver.solve(newRows, newCols);
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

        int boardW = Math.max(cols * cellSize, Theme.MIN_WIDTH);
        int boardH = rows * cellSize;
        boardPanel.setPreferredSize(new Dimension(boardW, boardH));
        pack();
        setLocationRelativeTo(null);
        boardPanel.repaint();
    }

    // ---- Labels ----

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
        title.setForeground(Theme.TEXT);
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
        errorLabel.setForeground(Theme.ERROR_TEXT);
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        stepLabel = new JLabel(" ");
        stepLabel.setForeground(Theme.TEXT);
        stepLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        stepLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        info.add(errorLabel);
        info.add(stepLabel);

        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setBackground(Theme.PANEL_BG);
        panel.setBorder(new EmptyBorder(4, 12, 6, 12));
        panel.add(title, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        panel.add(info, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Theme.TEXT);
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
        panel.setBackground(Theme.PANEL_BG);
        panel.setBorder(new EmptyBorder(4, 12, 8, 12));
        panel.add(buttons, BorderLayout.CENTER);
        panel.add(speedPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ---- Entry point ----

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChessboardGUI().setVisible(true));
    }
}
