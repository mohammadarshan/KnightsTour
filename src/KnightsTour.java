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
}