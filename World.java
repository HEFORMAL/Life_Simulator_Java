package org.example;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/** Среда обитания: двумерный массив клеток. В клетке либо один агент, либо null. */
public class World {

    /** Общий генератор случайных чисел (можно задать seed для воспроизводимости) */
    public static final Random RANDOM = new Random();

    private final int width;
    private final int height;
    private final Agent[][] matrix;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.matrix = new Agent[width][height];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean inside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /** Получить агента по координатам (за границей карты — null) */
    public Agent getAgent(int x, int y) {
        if (inside(x, y)) {
            return matrix[x][y];
        }
        return null;
    }

    public boolean isFree(int x, int y) {
        return inside(x, y) && matrix[x][y] == null;
    }

    /** Поставить агента в пустую клетку. null — очистить клетку. */
    public boolean setAgent(int x, int y, Agent agent) {
        if (!inside(x, y)) {
            return false;
        }
        if (agent == null) {
            matrix[x][y] = null;
            return true;
        }
        if (matrix[x][y] == null) {
            matrix[x][y] = agent;
            agent.setCoords(x, y);
            return true;
        }
        return false;
    }

    /**
     * Убить агента в клетке: клетка очищается И агент помечается мёртвым.
     * Без второго действия съеденный агент продолжал ходить ("зомби").
     */
    public void killAgent(int x, int y) {
        Agent victim = getAgent(x, y);
        if (victim != null) {
            victim.kill();
            matrix[x][y] = null;
        }
    }

    /** Безопасное перемещение: агент уходит из старой клетки только если реально в неё попал */
    public boolean moveAgent(Agent agent, int nx, int ny) {
        if (!isFree(nx, ny)) {
            return false;
        }
        int ox = agent.getX();
        int oy = agent.getY();
        if (inside(ox, oy) && matrix[ox][oy] == agent) {
            matrix[ox][oy] = null;
        }
        matrix[nx][ny] = agent;
        agent.setCoords(nx, ny);
        return true;
    }

    public List<Agent> getNeighbors(int x, int y) {
        List<Agent> neighbors = new ArrayList<>();
        for (int[] d : Agent.DIRECTIONS) {
            Agent neighbor = getAgent(x + d[0], y + d[1]);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    public List<Agent> getAllAgents() {
        List<Agent> agents = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix[x][y] != null) {
                    agents.add(matrix[x][y]);
                }
            }
        }
        return agents;
    }

    public void printMap() {
        System.out.print("  +");
        for (int x = 0; x < width; x++) {
            System.out.print("--");
        }
        System.out.println("+");

        for (int y = 0; y < height; y++) {
            System.out.print("  |");
            for (int x = 0; x < width; x++) {
                Agent agent = matrix[x][y];
                if (agent == null) {
                    System.out.print(" .");
                } else {
                    System.out.print(agent.getIcon());
                }
            }
            System.out.println("|");
        }

        System.out.print("  +");
        for (int x = 0; x < width; x++) {
            System.out.print("--");
        }
        System.out.println("+");
        System.out.println();
    }
}
