/*
JFrame and JPanel GUI
0 = walkable cell (white)
1 = wall (black)
Start = green square
Goal = red square
Path = blue squares
 */

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AStarVisualizer extends JPanel {

    static final int CELL_SIZE = 40;
    static final int ROWS = 10;
    static final int COLS = 10;

    // Grid: 0 = empty, 1 = wall
    static final int[][] grid = new int[ROWS][COLS];

    // Start and goal positions [row, col]
    static final int[] start = {0, 0};
    static final int[] goal = {9, 9};

    List<Node> path;

    // Node class for each position in the grid
    static class Node {
        int row, col;
        int g, h;
        Node parent;

        Node(int row, int col, int g, int h, Node parent) {
            this.row = row;
            this.col = col;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        int f() {
            return g + h;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) return false;
            Node other = (Node) obj;
            return this.row == other.row && this.col == other.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    // 4-directional movement: up, down, left, right
    static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public AStarVisualizer() {
        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));

        // Add a horizontal wall across row 5, columns 2–7
        for (int col = 2; col < 8; col++) {
            grid[5][col] = 1;
        }

        // Compute path
        path = aStar(grid, start, goal);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the grid
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                // Wall = black
                if (grid[row][col] == 1) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.GRAY); // grid lines
                g.drawRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // Draw path in blue
        if (path != null) {
            for (Node node : path) {
                g.setColor(Color.CYAN);
                g.fillRect(node.col * CELL_SIZE, node.row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // Draw start in green
        g.setColor(Color.GREEN);
        g.fillRect(start[1] * CELL_SIZE, start[0] * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        // Draw goal in red
        g.setColor(Color.RED);
        g.fillRect(goal[1] * CELL_SIZE, goal[0] * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    // A* pathfinding algorithm
    static List<Node> aStar(int[][] grid, int[] start, int[] goal) {
        int rows = grid.length, cols = grid[0].length;
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(Node::f));
        Set<String> closed = new HashSet<>();
        Map<String, Integer> gScore = new HashMap<>();

        Node startNode = new Node(start[0], start[1], 0, heuristic(start, goal), null);
        open.add(startNode);
        gScore.put(key(start[0], start[1]), 0);

        while (!open.isEmpty()) {
            Node current = open.poll();

            // Goal check
            if (current.row == goal[0] && current.col == goal[1]) {
                return reconstructPath(current);
            }

            closed.add(key(current.row, current.col));

            for (int[] dir : DIRECTIONS) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];

                // Bounds check and wall check
                if (newRow < 0 || newCol < 0 || newRow >= rows || newCol >= cols) continue;
                if (grid[newRow][newCol] == 1) continue;

                String neighborKey = key(newRow, newCol);
                if (closed.contains(neighborKey)) continue;

                int tentativeG = current.g + 1;

                if (!gScore.containsKey(neighborKey) || tentativeG < gScore.get(neighborKey)) {
                    Node neighbor = new Node(newRow, newCol, tentativeG, heuristic(new int[]{newRow, newCol}, goal), current);
                    gScore.put(neighborKey, tentativeG);
                    open.add(neighbor);
                }
            }
        }

        return null; // No path found
    }

    // Manhattan distance heuristic
    static int heuristic(int[] a, int[] b) {
        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]);
    }

    // Unique key for each grid cell
    static String key(int row, int col) {
        return row + "," + col;
    }

    // Trace back the path from goal to start
    static List<Node> reconstructPath(Node node) {
        List<Node> path = new ArrayList<>();
        while (node != null) {
            path.add(node);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    // Launch the window
    public static void main(String[] args) {
        JFrame frame = new JFrame("A* Pathfinding Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new AStarVisualizer());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
