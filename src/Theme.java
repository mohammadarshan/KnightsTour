import java.awt.Color;

/**
 * Shared color and sizing constants used across the GUI.
 */
public final class Theme {

    // Board square colors
    public static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    public static final Color DARK_SQUARE  = new Color(181, 136, 99);

    // Window and panel backgrounds
    public static final Color BG       = new Color(48, 46, 43);
    public static final Color PANEL_BG = new Color(38, 36, 33);

    // Text colors
    public static final Color TEXT       = new Color(220, 220, 210);
    public static final Color ERROR_TEXT = new Color(220, 80, 70);

    // Knight contrast colors
    public static final Color KNIGHT_DARK  = new Color(40, 40, 40);
    public static final Color KNIGHT_LIGHT = Color.WHITE;

    // Cell size limits and board fitting
    public static final int MAX_CELL     = 70;
    public static final int MIN_CELL     = 28;
    public static final int MAX_BOARD_PX = 620;
    public static final int MIN_WIDTH    = 480;

    private Theme() {}
}
