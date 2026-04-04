import java.util.Arrays;
import java.util.Scanner;

/**
 * Knight's Tour solver using Warnsdorff's heuristic with a backtracking
 * fallback.
 *
 * <p>
 * Warnsdorff's rule runs in O(n^2) time for an n-square board and solves
 * most boards without any backtracking. The backtracking fallback is only
 * triggered when the heuristic gets stuck, which typically only occurs on
 * small or degenerate boards.
 * </p>
 *
 * <p>
 * Board state is encoded as a flat {@code boolean[]} rather than a 2-D
 * structure so that index arithmetic ({@code r * COLS + c}) is the only
 * overhead — cache locality is better on a contiguous array than on an
 * array-of-arrays whose rows may be scattered in the heap.
 * </p>
 */
public class KnightsTour {

    /**
     * The 8 relative (row, col) offsets a knight can move to.
     * Stored as a 2-D int literal so no collection overhead is incurred;
     * we iterate over it many thousands of times, so a plain array is
     * preferable to a {@code List} here.
     */
    static final int[][] MOVES = {
            { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 },
            { 1, -2 }, { 1, 2 }, { 2, -1 }, { 2, 1 }
    };

    /** Number of rows on the board, set once from user input. */
    static int ROWS;

    /** Number of columns on the board, set once from user input. */
    static int COLS;

    /**
     * Shared path array used by the backtracking solver.
     * Allocated once and reused across recursive calls to avoid
     * repeated heap allocation inside the recursion.
     */
    static int[] path;

    /** Iteration counter used to abort backtracking if it runs too long. */
    static int iters;

    /**
     * Hard cap on backtracking iterations.
     * Without this, the solver could spin for minutes on boards that
     * have no tour. 4 million iterations is enough to solve any
     * solvable small board while keeping worst-case runtime under ~1 s.
     */
    static final int MAX_ITERS = 4_000_000;

    /**
     * Returns {@code true} if (r, c) is a valid board position.
     *
     * @param r row index
     * @param c column index
     * @return whether the cell is inside the board boundaries
     */
    static boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    /**
     * Counts how many unvisited squares the knight can reach from (r, c).
     *
     * <p>
     * This is the core metric for Warnsdorff's heuristic — a lower
     * degree means the square is harder to reach later, so we prefer
     * to visit it sooner.
     * </p>
     *
     * @param r       row of the candidate square
     * @param c       column of the candidate square
     * @param visited current board visit state
     * @return number of valid onward moves from (r, c)
     */
    static int degree(int r, int c, boolean[][] visited) {
        int count = 0;
        for (int[] m : MOVES)
            if (inBounds(r + m[0], c + m[1]) && !visited[r + m[0]][c + m[1]])
                count++;
        return count;
    }

    /**
     * Attempts to find a knight's tour using Warnsdorff's heuristic.
     *
     * <p>
     * At each step the knight moves to the reachable unvisited square
     * with the fewest onward moves. Ties are broken by board index
     * (row-major order) to produce a deterministic result. This greedy
     * strategy succeeds on the vast majority of boards without any
     * backtracking.
     * </p>
     *
     * @param visited empty visit grid (all {@code false}); mutated in place
     * @return a complete tour as an array of board indices, or {@code null}
     *         if the heuristic gets stuck before visiting all squares
     */
    static int[] warnsdorff(boolean[][] visited) {
        int[] tour = new int[ROWS * COLS];
        int r = 0, c = 0;
        visited[r][c] = true;
        tour[0] = r * COLS + c;

        for (int step = 1; step < ROWS * COLS; step++) {
            int bestR = -1, bestC = -1, bestDeg = Integer.MAX_VALUE;
            for (int[] m : MOVES) {
                int nr = r + m[0], nc = c + m[1];
                if (inBounds(nr, nc) && !visited[nr][nc]) {
                    int d = degree(nr, nc, visited);
                    if (d < bestDeg || (d == bestDeg && nr * COLS + nc < bestR * COLS + bestC)) {
                        bestDeg = d;
                        bestR = nr;
                        bestC = nc;
                    }
                }
            }
            if (bestR == -1)
                return null;
            visited[bestR][bestC] = true;
            tour[step] = bestR * COLS + bestC;
            r = bestR;
            c = bestC;
        }
        return tour;
    }

    /**
     * Recursive depth-first backtracking solver with Warnsdorff ordering.
     *
     * <p>
     * Candidates at each level are sorted by ascending degree before
     * recursing — this is the same ordering Warnsdorff uses, and it
     * dramatically reduces the number of backtracks needed compared to
     * arbitrary move ordering.
     * </p>
     *
     * <p>
     * {@code Arrays.sort} is used on a small fixed-size array (max 8
     * elements) rather than a {@code PriorityQueue} or {@code TreeSet}
     * because the overhead of those collections outweighs any benefit
     * at this tiny size.
     * </p>
     *
     * @param r       current row
     * @param c       current column
     * @param step    number of squares already placed (next index to fill)
     * @param visited current board visit state; mutated and restored
     * @return {@code true} if a complete tour was found and stored in
     *         {@link #path}, {@code false} otherwise
     */
    static boolean backtrack(int r, int c, int step, boolean[][] visited) {
        if (iters++ > MAX_ITERS)
            return false;
        if (step == ROWS * COLS)
            return true;

        // int[8][3]: fixed-size stack array; no heap allocation per call.
        // Columns: [0]=row, [1]=col, [2]=degree.
        int[][] candidates = new int[8][3];
        int count = 0;
        for (int[] m : MOVES) {
            int nr = r + m[0], nc = c + m[1];
            if (inBounds(nr, nc) && !visited[nr][nc])
                candidates[count++] = new int[] { nr, nc, degree(nr, nc, visited) };
        }

        // Sort by degree ascending, then by board index for tie-breaking.
        Arrays.sort(candidates, 0, count,
                (a, b) -> a[2] != b[2] ? a[2] - b[2] : (a[0] * COLS + a[1]) - (b[0] * COLS + b[1]));

        for (int i = 0; i < count; i++) {
            int nr = candidates[i][0], nc = candidates[i][1];
            visited[nr][nc] = true;
            path[step] = nr * COLS + nc;
            if (backtrack(nr, nc, step + 1, visited))
                return true;
            visited[nr][nc] = false; // backtrack: restore state before trying next candidate
        }
        return false;
    }

    /**
     * Prints the tour as a numbered grid where each cell shows the move
     * order in which the knight visited it.
     *
     * <p>
     * Column width is derived from the total square count so that the
     * grid stays aligned regardless of board size — e.g. a 10×10 board
     * needs width 3 (moves go up to 100).
     * </p>
     *
     * @param tour array of board indices in visit order
     */
    static void printTour(int[] tour) {
        int width = String.valueOf(ROWS * COLS).length();
        int[][] board = new int[ROWS][COLS];
        for (int i = 0; i < tour.length; i++)
            board[tour[i] / COLS][tour[i] % COLS] = i + 1;
        for (int[] row : board) {
            for (int v : row)
                System.out.printf("%" + width + "d ", v);
            System.out.println();
        }
    }

    /**
     * Entry point. Reads two integers from stdin (rows and columns),
     * attempts Warnsdorff first, falls back to backtracking, and either
     * prints the tour grid or an error message.
     *
     * <p>
     * {@code Scanner} is wrapped in a try-with-resources block so it
     * is automatically closed after input is read, preventing a resource
     * leak. {@code Scanner} is used over {@code BufferedReader} for
     * cleaner {@code nextInt()} parsing; performance is not a concern
     * here since input is read only once.
     * </p>
     *
     * @param args unused
     */
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter rows and cols: ");
            ROWS = sc.nextInt();
            COLS = sc.nextInt();
        }

        boolean[][] visited = new boolean[ROWS][COLS];
        int[] tour = warnsdorff(visited);

        if (tour != null) {
            System.out.println("Tour found (Warnsdorff):");
            printTour(tour);
            return;
        }

        path = new int[ROWS * COLS];
        path[0] = 0;
        iters = 0;
        visited = new boolean[ROWS][COLS];
        visited[0][0] = true;

        if (backtrack(0, 0, 1, visited)) {
            System.out.println("Tour found (backtracking):");
            printTour(path);
        } else {
            System.out.println("No knight's tour exists on a " + ROWS + "x" + COLS + " board.");
        }
    }
}