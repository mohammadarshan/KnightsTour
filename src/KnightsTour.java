public class KnightsTour {

    static final int[][] MOVES = {
            { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 },
            { 1, -2 }, { 1, 2 }, { 2, -1 }, { 2, 1 }
    };

    static int ROWS, COLS;

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
}