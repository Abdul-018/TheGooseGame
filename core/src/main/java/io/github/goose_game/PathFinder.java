package io.github.goose_game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.PriorityQueue;

public class PathFinder {

    // individual grid cell used for path calculation
    private static class Node implements Comparable<Node> {
        int x, y;
        boolean walkable;
        float gCost; // distance from start
        float hCost; // estimated distance to target heuristic
        Node parent; // previous node in the path trail

        public Node(int x, int y, boolean walkable) {
            this.x = x;
            this.y = y;
            this.walkable = walkable;
        }

        // total cost g plus h used to figure out which node to check next
        public float getFCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Float.compare(this.getFCost(), other.getFCost());
        }
    }

    private Node[][] grid;
    private final int gridWidth = 64;
    private final int gridHeight = 36;
    private float mapWidth;
    private float mapHeight;
    private float cellWidth;
    private float cellHeight;

    // sets up the grid size and builds it using collision data
    public PathFinder(MapCollision mapCollision, float mapWidth, float mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.cellWidth = mapWidth / gridWidth;
        this.cellHeight = mapHeight / gridHeight;
        this.grid = new Node[gridWidth][gridHeight];

        updateGrid(mapCollision);
    }

    // scans the map and marks which grid cells are blocked by walls
    public void updateGrid(MapCollision mapCollision) {
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                float centerX = (x + 0.5f) * cellWidth;
                float centerY = (y + 0.5f) * cellHeight;
                
                Rectangle testBox = new Rectangle(
                    centerX - 0.15f, centerY - 0.15f, 0.3f, 0.3f
                );
                
                boolean blocked = mapCollision != null && mapCollision.collides(testBox);
                grid[x][y] = new Node(x, y, !blocked);
            }
        }
    }

    // main a star pathfinding method finds a sequence of coordinates from start to target
    public Array<Vector2> findPath(Vector2 startWorld, Vector2 targetWorld) {
        Array<Vector2> path = new Array<>();

        int startX = MathUtils.clamp((int) (startWorld.x / cellWidth), 0, gridWidth - 1);
        int startY = MathUtils.clamp((int) (startWorld.y / cellHeight), 0, gridHeight - 1);
        int targetX = MathUtils.clamp((int) (targetWorld.x / cellWidth), 0, gridWidth - 1);
        int targetY = MathUtils.clamp((int) (targetWorld.y / cellHeight), 0, gridHeight - 1);

        Node startNode = grid[startX][startY];
        Node targetNode = grid[targetX][targetY];

        // if start or target is stuck inside a wall find the closest valid spot nearby
        if (!startNode.walkable) {
            startNode = getNearestWalkableNode(startX, startY);
        }
        if (!targetNode.walkable) {
            targetNode = getNearestWalkableNode(targetX, targetY);
        }

        if (startNode == null || targetNode == null) {
            return path;
        }

        // reset costs and parents before running search
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                grid[x][y].gCost = Float.MAX_VALUE;
                grid[x][y].parent = null;
            }
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[gridWidth][gridHeight];

        startNode.gCost = 0;
        startNode.hCost = getHeuristic(startNode, targetNode);
        openSet.add(startNode);

        // loop through open nodes until we find the target or run out of options
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.x == targetNode.x && current.y == targetNode.y) {
                return reconstructPath(startNode, targetNode);
            }

            closedSet[current.x][current.y] = true;

            // check all 4 neighboring directions
            int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};
            for (int[] dir : directions) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                if (nx < 0 || nx >= gridWidth || ny < 0 || ny >= gridHeight) continue;

                Node neighbor = grid[nx][ny];
                if (!neighbor.walkable || closedSet[nx][ny]) continue;

                float newGCost = current.gCost + 1f;
                if (newGCost < neighbor.gCost) {
                    neighbor.gCost = newGCost;
                    neighbor.hCost = getHeuristic(neighbor, targetNode);
                    neighbor.parent = current;

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        // returns empty path if no route was found
        return path; 
    }

    // find a valid walkable tile nearby if someone starts inside a wall
    private Node getNearestWalkableNode(int cx, int cy) {
        for (int radius = 1; radius <= 3; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int nx = cx + dx;
                    int ny = cy + dy;
                    if (nx >= 0 && nx < gridWidth && ny >= 0 && ny < gridHeight) {
                        if (grid[nx][ny].walkable) {
                            return grid[nx][ny];
                        }
                    }
                }
            }
        }
        return null;
    }

    // manhattan distance heuristic formula for a star
    private float getHeuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    // traces back from the target node to the start node to build the final list of waypoints
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