import java.util.ArrayList;
import java.util.List;

public class Herbivore extends Agent implements Checkup {
    private boolean move;
    private boolean see;
    private int eat;
    private int run;
    private int cost_baby;
    private int childEnergy;
    private int plantX = -1, plantY = -1; // ← НОВОЕ: координаты замеченного растения

    public Herbivore(int[][] coords, int energy, String icon, int eat, int run, int cost_baby, int childEnergy) {
        super(coords, energy, icon);
        this.move = true;
        this.see = true;
        this.eat = eat;
        this.run = run;
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

    // ЗРЕНИЕ 5×5: ищем хищника И растение
    @Override
    public void scanner(World world) {
        plantX = -1;
        plantY = -1;

        int[][] cords = getCoords();
        int x = cords[0][0];
        int y = cords[0][1];

        int closestPredatorX = -1, closestPredatorY = -1;
        int minPredatorDist = Integer.MAX_VALUE;

        int closestPlantX = -1, closestPlantY = -1;
        int minPlantDist = Integer.MAX_VALUE;

        // Сканируем квадрат 5×5
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;
                Agent suspect = world.getAgent(nx, ny);

                if (suspect != null) {
                    int dist = Math.abs(dx) + Math.abs(dy);

                    // Ищем ближайшего хищника
                    if (suspect instanceof Predator && dist < minPredatorDist) {
                        minPredatorDist = dist;
                        closestPredatorX = nx;
                        closestPredatorY = ny;
                    }

                    // Ищем ближайшее растение
                    if (suspect instanceof Plant && dist < minPlantDist) {
                        minPlantDist = dist;
                        closestPlantX = nx;
                        closestPlantY = ny;
                    }
                }
            }
        }

        // ПРИОРИТЕТ 1: Хищник важнее растения
        if (closestPredatorX != -1) {
            boolean threatNear = (minPredatorDist == 1);

            if (this.energy >= run) {
                if (fleeFrom(world, x, y, closestPredatorX, closestPredatorY)) {
                    this.energy -= run;
                } else if (threatNear) {
                    world.setAgent(x, y, null);
                }
            } else if (threatNear) {
                world.setAgent(x, y, null);
            }
            return; // Хищник обработан, выходим
        }

        // Хищника нет — запоминаем растение (если нашли)
        if (closestPlantX != -1) {
            plantX = closestPlantX;
            plantY = closestPlantY;
        }
    }

    private boolean fleeFrom(World world, int x, int y, int px, int py) {
        int[][] adjacent = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        int bestX = -1, bestY = -1, bestDist = -1;

        for (int[] d : adjacent) {
            int cx = x + d[0];
            int cy = y + d[1];
            if (world.getAgent(cx, cy) == null) {
                int dist = Math.abs(cx - px) + Math.abs(cy - py);
                if (dist > bestDist) {
                    bestDist = dist;
                    bestX = cx;
                    bestY = cy;
                }
            }
        }

        if (bestX != -1) {
            world.setAgent(x, y, null);
            world.setAgent(bestX, bestY, this);
            return true;
        }
        return false;
    }

    public void Reproduction(World world) {
        int[][] cords = getCoords();
        int x = cords[0][0];
        int y = cords[0][1];
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int i = 0; i < directions.length; i++) {
            int newx = x + directions[i][0];
            int newy = y + directions[i][1];
            if (world.getAgent(newx, newy) == null) {
                Herbivore newHerbivore = new Herbivore(
                        new int[][]{{newx, newy}}, childEnergy, this.getIcon(),
                        this.eat, this.run, this.cost_baby, this.childEnergy
                );
                if (world.setAgent(newx, newy, newHerbivore)) {
                    energy -= cost_baby;
                    return;
                }
            }
        }
    }

    // ИДЁМ К РАСТЕНИЮ (аналогично погоне хищника)
    public void foodMining(World world) {
        int[][] cords = getCoords();
        int x = cords[0][0];
        int y = cords[0][1];
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        // 1) Если растение ВПЛОТНУЮ — съедаем
        for (int i = 0; i < directions.length; i++) {
            int newx = x + directions[i][0];
            int newy = y + directions[i][1];
            Agent suspect = world.getAgent(newx, newy);

            if (suspect != null && suspect instanceof Plant) {
                energy += eat;
                world.setAgent(newx, newy, null);
                world.setAgent(x, y, null);
                world.setAgent(newx, newy, this);
                return;
            }
        }

        // 2) Растение далеко — делаем шаг к нему
        if (plantX == -1 || plantY == -1) {
            return; // растения нет в поле зрения
        }

        int ddx = plantX - x;
        int ddy = plantY - y;
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

    @Override
    public void act(World world) {
        if (this.energy <= 0) {
            int[][] cords = getCoords();
            world.setAgent(cords[0][0], cords[0][1], null);
            return;
        }

        // Запоминаем координаты ДО действий
        int[][] cordsBefore = getCoords();
        int xBefore = cordsBefore[0][0];
        int yBefore = cordsBefore[0][1];

        // 1. Сканируем (ищем хищника И растение)
        scanner(world);

        if (this.energy <= 0) return; // умер от хищника

        // Проверяем, убежали ли от хищника
        int[][] cordsAfterScanner = getCoords();
        boolean fled = (xBefore != cordsAfterScanner[0][0] || yBefore != cordsAfterScanner[0][1]);

        if (fled) {
            // Убежали — ход закончен
            EnergyGeneration();
            return;
        }

        // 2. Пытаемся поесть или идём к растению
        foodMining(world);

        int[][] cordsAfterFood = getCoords();
        boolean movedToPlant = (xBefore != cordsAfterFood[0][0] || yBefore != cordsAfterFood[0][1]);

        if (movedToPlant) {
            // Съели или сделали шаг к растению — ход закончен
            EnergyGeneration();
            return;
        }

        // 3. Размножение (если много энергии)
        if (this.energy > cost_baby) {
            Reproduction(world);
            EnergyGeneration();
            return;
        }

        // 4. Иначе — бродим случайно
        randomWalk(world);
        EnergyGeneration();
    }
}