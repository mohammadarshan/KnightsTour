import java.util.Arrays;

public class KnightsTour {

    static final int[][] MOVES = {
            { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 },
            { 1, -2 }, { 1, 2 }, { 2, -1 }, { 2, 1 }
    };

    static int ROWS, COLS;
    static int[] path;
    static int iters;
    static final int MAX_ITERS = 4_000_000;

    static boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    static int degree(int r, int c, boolean[][] visited) {
        int count = 0;
        for (int[] m : MOVES)
            if (inBounds(r + m[0], c + m[1]) && !visited[r + m[0]][c + m[1]])
                count++;
        return count;
    }

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

    static boolean backtrack(int r, int c, int step, boolean[][] visited) {
        if (iters++ > MAX_ITERS)
            return false;
        if (step == ROWS * COLS)
            return true;

        int[][] candidates = new int[8][3];
        int count = 0;
        for (int[] m : MOVES) {
            int nr = r + m[0], nc = c + m[1];
            if (inBounds(nr, nc) && !visited[nr][nc])
                candidates[count++] = new int[] { nr, nc, degree(nr, nc, visited) };
        }
        Arrays.sort(candidates, 0, count,
                (a, b) -> a[2] != b[2] ? a[2] - b[2] : (a[0] * COLS + a[1]) - (b[0] * COLS + b[1]));

        for (int i = 0; i < count; i++) {
            int nr = candidates[i][0], nc = candidates[i][1];
            visited[nr][nc] = true;
            path[step] = nr * COLS + nc;
            if (backtrack(nr, nc, step + 1, visited))
                return true;
            visited[nr][nc] = false;
        }
        return false;
    }
}