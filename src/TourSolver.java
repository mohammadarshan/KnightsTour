/**
 * Bridge between the GUI and the KnightsTour algorithm.
 * Tries Warnsdorff first, then falls back to backtracking.
 */
public final class TourSolver {

    private TourSolver() {}

    /**
     * Solves the Knight's Tour for the given board size.
     * Returns the tour as an array of board indices in visit order,
     * or null if no solution was found.
     * 
     * @author Mohammad Arshan Shaikh - Core Implementation
     * @author Kavitha Raavi - Documentation and Refactoring
     * @version 1.1
     * @since 1.0
     */
    public static int[] solve(int rows, int cols) {
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
}
