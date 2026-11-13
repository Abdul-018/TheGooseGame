package io.github.goose_game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.PriorityQueue;

public class PathFinder {

    private static class Node implements Comparable<Node> {
        int x, y;
        boolean walkable;
        float gCost;
        float hCost;
        Node parent;

        public Node(int x, int y, boolean walkable) {
            this.x = x;
            this.y = y;
            this.walkable = walkable;
        }

        public float getFCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Float.compare(this.getFCost(), other.getFCost());
        }
    }

    private Node[][] grid;
    private final int gridWidth;
    private final int gridHeight;
    private final float cellWidth;
    private final float cellHeight;

    public PathFinder(Array<Rectangle> collisionRects, float mapWidth, float mapHeight, float tileSize) {
        this.gridWidth = (int) (mapWidth / tileSize);
        this.gridHeight = (int) (mapHeight / tileSize);
        this.cellWidth = tileSize;
        this.cellHeight = tileSize;
        this.grid = new Node[gridWidth][gridHeight];
        buildGrid(collisionRects);
    }

    private void buildGrid(Array<Rectangle> collisionRects) {
        Rectangle testBox = new Rectangle(0, 0, cellWidth * 0.8f, cellHeight * 0.8f);
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                float centerX = (x + 0.5f) * cellWidth;
                float centerY = (y + 0.5f) * cellHeight;
                testBox.setPosition(centerX - testBox.width / 2f, centerY - testBox.height / 2f);
                boolean blocked = false;
                for (Rectangle r : collisionRects) {
                    if (testBox.overlaps(r)) { blocked = true; break; }
                }
                grid[x][y] = new Node(x, y, !blocked);
            }
        }
    }

    public Array<Vector2> findPath(Vector2 startWorld, Vector2 targetWorld) {
        Array<Vector2> path = new Array<>();

        int startX = MathUtils.clamp((int) (startWorld.x / cellWidth), 0, gridWidth - 1);
        int startY = MathUtils.clamp((int) (startWorld.y / cellHeight), 0, gridHeight - 1);
        int targetX = MathUtils.clamp((int) (targetWorld.x / cellWidth), 0, gridWidth - 1);
        int targetY = MathUtils.clamp((int) (targetWorld.y / cellHeight), 0, gridHeight - 1);

        Node startNode = grid[startX][startY];
        Node targetNode = grid[targetX][targetY];

        if (!startNode.walkable) startNode = getNearestWalkable(startX, startY);
        if (!targetNode.walkable) targetNode = getNearestWalkable(targetX, targetY);
        if (startNode == null || targetNode == null) return path;

        // Reset costs
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                grid[x][y].gCost = Float.MAX_VALUE;
                grid[x][y].parent = null;
            }
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[gridWidth][gridHeight];

        startNode.gCost = 0;
        startNode.hCost = heuristic(startNode, targetNode);
        openSet.add(startNode);

        int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.x == targetNode.x && current.y == targetNode.y) {
                return reconstructPath(startNode, targetNode);
            }

            closedSet[current.x][current.y] = true;

            for (int[] dir : directions) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                if (nx < 0 || nx >= gridWidth || ny < 0 || ny >= gridHeight) continue;

                Node neighbor = grid[nx][ny];
                if (!neighbor.walkable || closedSet[nx][ny]) continue;

                float newGCost = current.gCost + 1f;
                if (newGCost < neighbor.gCost) {
                    neighbor.gCost = newGCost;
                    neighbor.hCost = heuristic(neighbor, targetNode);
                    neighbor.parent = current;
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        return path;
    }

    private Node getNearestWalkable(int cx, int cy) {
        for (int radius = 1; radius <= 5; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int nx = cx + dx;
                    int ny = cy + dy;
                    if (nx >= 0 && nx < gridWidth && ny >= 0 && ny < gridHeight) {
                        if (grid[nx][ny].walkable) return grid[nx][ny];
                    }
                }
            }
        }
        return null;
    }

    private float heuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private Array<Vector2> reconstructPath(Node startNode, Node targetNode) {
        Array<Vector2> waypoints = new Array<>();
        Node current = targetNode;
        while (current != null && current != startNode) {
            float worldX = (current.x + 0.5f) * cellWidth;
            float worldY = (current.y + 0.5f) * cellHeight;
            waypoints.add(new Vector2(worldX, worldY));
            current = current.parent;
        }
        waypoints.reverse();
        return waypoints;
    }
}
