import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.ArrayDeque;
import java.util.ArrayList;

class Solver {

    private final static int SIZE = 3;

    private static class Board {
        byte[][] board;
        byte x, y;

        public Board(byte[][] board) {
            this.board = board;
            for (byte y = 0; y < this.board.length; y++) {
                for (byte x = 0; x < this.board[y].length; x++) {
                    if (this.board[y][x] == 0) {
                        this.x = x;
                        this.y = y;
                        return;
                    }
                }
            }
        }

        public Board(byte[][] board, byte x, byte y) {
            this.board = board;
            this.x = x;
            this.y = y;
        }

        public Board(byte[][] board, int x, int y) {
            this(board, (byte) x, (byte) y);
        }

        public Board[] nextBoards() {
            boolean inCenter = this.x == 1 && this.y == 1;
            boolean onEdge = this.x == 1 ^ this.y == 1;
            byte n = (byte) (inCenter ? 4 : (onEdge ? 3 : 2));
            Board[] toReturn = new Board[n];
            byte i = 0;

            if (this.y - 1 >= 0) {
                toReturn[i] = this.copy();
                toReturn[i].swap(this.x, this.y, this.x, this.y - 1);
                toReturn[i].y -= 1;
                i++;
            }
            if (this.y + 1 < SIZE) {
                toReturn[i] = this.copy();
                toReturn[i].swap(this.x, this.y, this.x, this.y + 1);
                toReturn[i].y += 1;
                i++;
            }

            if (this.x - 1 >= 0) {
                toReturn[i] = this.copy();
                toReturn[i].swap(this.x, this.y, this.x - 1, this.y);
                toReturn[i].x -= 1;
                i++;
            }
            if (this.x + 1 < SIZE) {
                toReturn[i] = this.copy();
                toReturn[i].swap(this.x, this.y, this.x + 1, this.y);
                toReturn[i].x += 1;
                i++;
            }

            return toReturn;
        }

        public void swap(int x1, int y1, int x2, int y2) {
            byte temp = this.board[y1][x1];
            this.board[y1][x1] = this.board[y2][x2];
            this.board[y2][x2] = temp;
        }

        public Board copy() {
            byte[][] boardCopy = new byte[SIZE][SIZE];
            for (int y = 0; y < SIZE; y++) {
                boardCopy[y] = this.board[y].clone();
            }
            return new Board(boardCopy, this.x, this.y);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    if (this.board[y][x] == 0) {
                        sb.append(' ');
                    } else {
                        sb.append(this.board[y][x]);
                    }
                    sb.append(' ');
                }
                sb.append('\n');
            }
            return sb.toString();
        }

        @Override
        public int hashCode() {
            int toReturn = 0;
            byte i = 0;
            byte empty_index = -1;
            for (byte y = 0; y < SIZE; y++) {
                for (byte x = 0; x < SIZE; x++) {
                    if (this.board[y][x] != 0) {
                        toReturn |= ((this.board[y][x] - 1) << (i * 3));
                        i++;
                    } else {
                        empty_index = i;
                    }
                }
            }
            toReturn |= (empty_index << (8 * 3));
            return toReturn;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof Board) {
                Board otherBoard = (Board) (o);
                return this.hashCode() == otherBoard.hashCode();
            }
            return false;
        }

    }

    final static Board solved = new Board(new byte[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 0 } });

    private static class BoardEntry {
        public Board b;
        public int myIndex;
        public int parentIndex;

        public BoardEntry(Board b, int my, int parent) {
            this.b = b;
            this.myIndex = my;
            this.parentIndex = parent;
        }

        public BoardEntry(Board b) {
            this(b, 0, -1);
        }

        @Override
        public boolean equals(Object o) {
            return this.b.equals(o);
        }
    }

    private static BoardEntry searchForBoard(Board b, ArrayList<BoardEntry> boards) {
        for (int i = 0; i < boards.size(); i++) {
            Board currBoard = boards.get(i).b;
            boolean possible = true;
            for (int y = 0; y < SIZE && possible; y++) {
                for (int x = 0; x < SIZE; x++) {
                    if (b.board[y][x] != currBoard.board[y][x]) {
                        possible = false;
                        break;
                    }
                }
            }
            if (possible) {
                return boards.get(i);
            }
        }
        return null;
    }

    private static ArrayList<BoardEntry> getSolveState() {
        HashSet<Integer> solvedBoards = new HashSet<Integer>();
        solvedBoards.add(solved.hashCode());

        ArrayList<BoardEntry> toReturn = new ArrayList<BoardEntry>(/* possible orientations = */181440);
        toReturn.add(new BoardEntry(solved));

        ArrayDeque<BoardEntry> q = new ArrayDeque<BoardEntry>();
        q.addLast(toReturn.get(0));

        while (!q.isEmpty()) {
            BoardEntry base = q.pollFirst();
            for (Board b : base.b.nextBoards()) {
                if (!solvedBoards.contains(b.hashCode())) {
                    solvedBoards.add(b.hashCode());
                    toReturn.add(new BoardEntry(b, toReturn.size(), base.myIndex));
                    q.addLast(toReturn.get(toReturn.size() - 1));
                }
            }
        }

        return toReturn;
    }

    private static int computeMovesToSolveAtIndex(int i, Integer[] movesToSolve, ArrayList<BoardEntry> solved) {
        if (movesToSolve[i] != null) {
            return movesToSolve[i];
        }
        if (i == 0) {
            return 0;
        }
        return 1 + computeMovesToSolveAtIndex(solved.get(i).parentIndex, movesToSolve, solved);
    }

    private static void printFrequencies() {
        ArrayList<BoardEntry> solved = getSolveState();
        int n = solved.size();
        Integer[] movesToSolve = new Integer[n];
        // The maximum number of moves (if playing optimally) is 31
        int[] frequencies = new int[32];
        for (int i = n - 1; i >= 0; i--) {
            movesToSolve[i] = computeMovesToSolveAtIndex(i, movesToSolve, solved);
            frequencies[movesToSolve[i]]++;
        }
        System.out.println("Moves | Frequency");
        System.out.println("-----------------");
        for (int i = 0; i < frequencies.length; i++) {
            System.out.printf("%-6d|%10d\n", i, frequencies[i]);
        }
    }

    /**
     * Give the board in simplified notation. For example...
     *
     * ╭───╮╭───╮╭───╮
     * │ 5 ││ 8 ││ 4 │
     * ╰───╯╰───╯╰───╯
     * ╭───╮╭───╮╭───╮
     * │ 2 ││ 1 ││ 3 │
     * ╰───╯╰───╯╰───╯
     * ╭───╮╭───╮
     * │ 6 ││ 7 │
     * ╰───╯╰───╯
     * 
     * becomes...
     * 
     * 5 8 4
     * 2 1 3
     * 6 7 0
     */
    private static Board getBoardFromStandardIn() {
        Scanner in = new Scanner(System.in);
        byte[][] board = new byte[SIZE][SIZE];
        try {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    board[y][x] = (byte) in.nextInt();
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("Error parsing board");
            System.out.println("Ensure that the board you gave is valid");
            System.exit(2);
        }
        in.close();
        return new Board(board);
    }

    private static void printSolveBoard() {
        ArrayList<BoardEntry> solved = getSolveState();

        Board given = getBoardFromStandardIn();

        BoardEntry found = searchForBoard(given, solved);
        if (found == null) {
            System.out.println("Board is not reachable!");
            System.exit(1);
        }

        int moves = 0;
        while (found.parentIndex != -1) {
            System.out.println(found.b);
            found = solved.get(found.parentIndex);
            moves++;
        }
        System.out.println("Board can be solved in " + moves + " move" + (moves > 1 ? "s" : ""));
    }

    private static void printUsage() {
        System.err.println("\nUsage:");
        System.err.println("    To print frequencies:");
        System.err.println("    > $ java Solver -f\n");
        System.err.println("    To parse a board from stdin:");
        System.err.println("    > $ java Solver\n");
        System.exit(1);
    }

    private static void printUnknownArg(String flag) {
        System.err.println("Unknown argument: " + flag);
        System.err.println("Did you mean to use \"-f\"?");
        System.exit(3);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printSolveBoard();
        } else if (args.length == 1) {
            if (args[0].equals("-f")) {
                printFrequencies();
            } else {
                printUnknownArg(args[0]);
            }
        } else {
            printUsage();
        }
    }
}