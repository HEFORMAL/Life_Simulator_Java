import java.util.ArrayList;
import java.util.List;

public class Predator extends Agent implements Checkup {
    private boolean move;
    private boolean see;
    private int huntCost;
    private int eat;
    private int cost_baby;
    private int childEnergy;
    private boolean hunting = false;
    private int preyX = -1, preyY = -1;

    public Predator(int[][] coords, int energy, String icon, int eat, int huntCost, int cost_baby, int childEnergy) {
        super(coords, energy, icon);
        this.move = true;
        this.see = true;
        this.eat = eat;
        this.huntCost = huntCost;
        this.cost_baby = cost_baby;
        this.childEnergy = childEnergy;
    }

    @Override
    public void EnergyGeneration() {
        this.energy -= 1;
    }

    public void randomWalk(World world) {
        int[][] cords = getCoords();
        int x = cords[0][0];
        int y = cords[0][1];
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        List<int[]> freeCells = new ArrayList<>();

        for (int i = 0; i < directions.length; i++) {
            int newx = x + directions[i][0];
            int newy = y + directions[i][1];
            if (world.getAgent(newx, newy) == null) {
                freeCells.add(new int[]{newx, newy});
            }
        }

        if (!freeCells.isEmpty()) {
            int randomIndex = (int)(Math.random() * freeCells.size());
            int[] target = freeCells.get(randomIndex);
            world.setAgent(x, y, null);
            world.setAgent(target[0], target[1], this);
        }
    }

    @Override
    public void scanner(World world) {
        hunting = false;
        preyX = -1;
        preyY = -1;
        int[][] cords = getCoords();
        int x = cords[0][0];
        int y = cords[0][1];
        int closestX = -1, closestY = -1;
        int minDist = Integer.MAX_VALUE;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;
                Agent suspect = world.getAgent(nx, ny);

                if (suspect != null && suspect instanceof Herbivore) {
                    int dist = Math.abs(dx) + Math.abs(dy);
                    if (dist < minDist) {
                        minDist = dist;
                        closestX = nx;
                        closestY = ny;
                    }
                }
            }
        }

        if (closestX != -1 && this.energy >= huntCost) {
            this.energy -= huntCost;
            hunting = true;
            preyX = closestX;
            preyY = closestY;
        }
    }

    @Override
    public void foodMining(World world) {
        if (!hunting) {
            return;
        }

        int[][] cords = getCoords();
        int x = cords[0][0];
        int y = cords[0][1];
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        // 1) Проверяем 4 соседние клетки — если добыча ВПЛОТНУЮ, съедаем
        for (int i = 0; i < directions.length; i++) {
            int newx = x + directions[i][0];
            int newy = y + directions[i][1];
            Agent suspect = world.getAgent(newx, newy);

            if (suspect != null && suspect instanceof Herbivore) {
                energy += eat;
                world.setAgent(newx, newy, null);
                world.setAgent(x, y, null);
                world.setAgent(newx, newy, this);
                hunting = false;
                return;
            }
        }

        // 2) Добыча далеко — делаем шаг в её сторону (ПОГОНЯ)
        if (preyX == -1 || preyY == -1) {
            hunting = false;
            return;
        }

        int ddx = preyX - x;
        int ddy = preyY - y;
        int sx = Integer.compare(ddx, 0);
        int sy = Integer.compare(ddy, 0);

        boolean moved = false;
        if (Math.abs(ddx) >= Math.abs(ddy)) {
            if (sx != 0) moved = tryStep(world, x, y, x + sx, y);
            if (!moved && sy != 0) moved = tryStep(world, x, y, x, y + sy);
        } else {
            if (sy != 0) moved = tryStep(world, x, y, x, y + sy);
            if (!moved && sx != 0) moved = tryStep(world, x, y, x + sx, y);
        }
    }

    private boolean tryStep(World world, int x, int y, int nx, int ny) {
        if (world.getAgent(nx, ny) == null) {
            world.setAgent(x, y, null);
            world.setAgent(nx, ny, this);
            return true;
        }
        return false;
    }

    public void Reproduction(World world) {
        if (this.energy > cost_baby) {
            int[][] cords = getCoords();
            int x = cords[0][0];
            int y = cords[0][1];
            int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

            for (int i = 0; i < directions.length; i++) {
                int newx = x + directions[i][0];
                int newy = y + directions[i][1];
                if (world.getAgent(newx, newy) == null) {
                    Predator newPredator = new Predator(
                            new int[][]{{newx, newy}}, childEnergy, this.getIcon(),
                            this.eat, this.huntCost, this.cost_baby, this.childEnergy
                    );
                    if (world.setAgent(newx, newy, newPredator)) {
                        energy -= childEnergy;
                        break;
                    }
                }
            }
        }
    }

    // ИСПРАВЛЕНО: правильная последовательность действий
    @Override
    public void act(World world) {
        // Проверка на смерть в начале дня
        if (this.energy <= 0) {
            int[][] cords = getCoords();
            world.setAgent(cords[0][0], cords[0][1], null);
            return;
        }

        // 1. Сканируем окружение
        scanner(world);

        // 2. Пытаемся поесть (если нашли добычу)
        foodMining(world);

        // 3. Если не охотились (hunting == false), бродим случайно
        if (!hunting) {
            randomWalk(world);
        }

        // 4. Размножение (если достаточно энергии)
        Reproduction(world);

        // 5. Метаболизм (тратим энергию на жизнь)
        EnergyGeneration();
    }
}