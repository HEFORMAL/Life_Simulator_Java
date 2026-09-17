import java.util.ArrayList;
import java.util.List;
public class World {
    private int width;
    private int height;
    private Agent[][] matrix;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.matrix = new Agent[width][height];
    }

    public Agent getAgent(int x, int y) { //получение агента по координатам
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return matrix[x][y];
        }
        return null;
    }
    public boolean setAgent(int x, int y, Agent agent) { //размещение/удаление агента
        if (x >= 0 && x < width && y >= 0 && y < height) { // проверим еще раз вдруг за границей
            if (agent == null) { // если агента убили то он убирается из клеки
                matrix[x][y] = null;
                return true;
            }
            if (matrix[x][y] == null) {
                matrix[x][y] = agent;
                agent.setCoords(x, y);
                return true;
            }
        }
        return false;
    }


    public List<Agent> getNeighbors(int x, int y) { // проверка на соседей
        List<Agent> neighbors = new ArrayList<>();
        int[][] zone = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int i = 0; i < zone.length; i++) {
            Agent neighbor = getAgent(x + zone[i][0], y + zone[i][1]);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }
    public List<Agent> getAllAgents() { // перебор агентов
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
        // Верхняя граница
        System.out.print("   +");
        for (int x = 0; x < width; x++) {
            System.out.print("---");
        }
        System.out.println("+");

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {
                Agent agent = matrix[x][y];
                if (agent == null) {
                    System.out.print(" . "); // 3 символа для выравнивания
                } else {
                    // Форматируем иконку с отступами
                    String icon = agent.getIcon();
                    if (icon.length() == 1) {
                        System.out.printf(" %s ", icon); // Обычный символ
                    } else {
                        System.out.printf("%s ", icon); // Эмодзи (уже 2 символа)
                    }
                }
            }
            System.out.println("|");
        }

        // Нижняя граница
        System.out.print("   +");
        for (int x = 0; x < width; x++) {
            System.out.print("---");
        }
        System.out.println("+");

        System.out.println();
    }
}
