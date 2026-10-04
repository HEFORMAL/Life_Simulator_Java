package org.example;
import java.util.ArrayList;
import java.util.List;


public class Herbivore extends Agent implements Checkup {
    private final int eat;         // сколько энергии даёт растение
    private final int run;         // цена бегства
    private final int costBaby;    // сколько энергии тратит родитель
    private final int childEnergy; // с какой энергией рождается детёныш
    private final int metabolism;  // трата энергии за день
    private final int hunger;      // порог голода: сытое травоядное не трогает растения

    // то, что увидел scanner
    private int plantX = -1, plantY = -1;
    private int predatorX = -1, predatorY = -1;
    private int predatorDist = Integer.MAX_VALUE;

    public Herbivore(int[][] coords, int energy, int maxEnergy, String icon,
                     int eat, int run, int costBaby, int childEnergy, int metabolism, int hunger) {
        super(coords, energy, maxEnergy, icon);
        this.eat = eat;
        this.run = run;
        this.costBaby = costBaby;
        this.childEnergy = childEnergy;
        this.metabolism = metabolism;
        this.hunger = hunger;
    }

    /** Логическое условие "агент голоден" */
    public boolean isHungry() {
        return energy < hunger;
    }

    @Override
    public void EnergyGeneration() {
        this.energy -= metabolism;
    }

    /** ЗРЕНИЕ 5x5: ищем ближайшего хищника и ближайшее растение */
    @Override
    public void scanner(World world) {
        plantX = -1;
        plantY = -1;
        predatorX = -1;
        predatorY = -1;
        predatorDist = Integer.MAX_VALUE;

        int x = getX();
        int y = getY();
        int minPlantDist = Integer.MAX_VALUE;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;

                Agent suspect = world.getAgent(x + dx, y + dy);
                if (suspect == null || !suspect.isAlive()) continue;

                int dist = Math.abs(dx) + Math.abs(dy);

                if (suspect instanceof Predator && dist < predatorDist) {
                    predatorDist = dist;
                    predatorX = x + dx;
                    predatorY = y + dy;
                } else if (suspect instanceof Plant && dist < minPlantDist) {
                    minPlantDist = dist;
                    plantX = x + dx;
                    plantY = y + dy;
                }
            }
        }
    }

    /** Бегство: шаг в клетку, максимально далёкую от хищника */
    private boolean fleeFrom(World world, int px, int py) {
        int x = getX();
        int y = getY();
        int curDist = Math.abs(x - px) + Math.abs(y - py);
        int bestX = -1, bestY = -1, bestDist = curDist;

        for (int[] d : shuffledDirections()) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (world.isFree(nx, ny)) {
                int dist = Math.abs(nx - px) + Math.abs(ny - py);
                if (dist > bestDist) {
                    bestDist = dist;
                    bestX = nx;
                    bestY = ny;
                }
            }
        }

        if (bestX != -1 && world.moveAgent(this, bestX, bestY)) {
            energy -= run;
            return true;
        }
        return false;
    }

    /** Поедание растения рядом или шаг в его сторону */
    @Override
    public void foodMining(World world) {
        int x = getX();
        int y = getY();

        // 1) растение вплотную — съедаем и занимаем его клетку
        for (int[] d : shuffledDirections()) {
            int nx = x + d[0];
            int ny = y + d[1];
            Agent suspect = world.getAgent(nx, ny);

            if (suspect instanceof Plant && suspect.isAlive()) {
                addEnergy(eat);
                world.killAgent(nx, ny);      // растение помечено мёртвым, клетка свободна
                world.moveAgent(this, nx, ny);
                return;
            }
        }

        // 2) растение далеко — шаг в его сторону
        if (plantX == -1) {
            return;
        }
        stepTowards(world, plantX, plantY);
    }

    private boolean stepTowards(World world, int tx, int ty) {
        int x = getX();
        int y = getY();
        int ddx = tx - x;
        int ddy = ty - y;
        int sx = Integer.compare(ddx, 0);
        int sy = Integer.compare(ddy, 0);

        boolean moved = false;
        if (Math.abs(ddx) >= Math.abs(ddy)) {
            if (sx != 0) moved = world.moveAgent(this, x + sx, y);
            if (!moved && sy != 0) moved = world.moveAgent(this, x, y + sy);
        } else {
            if (sy != 0) moved = world.moveAgent(this, x, y + sy);
            if (!moved && sx != 0) moved = world.moveAgent(this, x + sx, y);
        }
        return moved;
    }

    public void randomWalk(World world) {
        int x = getX();
        int y = getY();
        List<int[]> freeCells = new ArrayList<>();

        for (int[] d : DIRECTIONS) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (world.isFree(nx, ny)) {
                freeCells.add(new int[]{nx, ny});
            }
        }

        if (!freeCells.isEmpty()) {
            int[] target = freeCells.get(World.RANDOM.nextInt(freeCells.size()));
            world.moveAgent(this, target[0], target[1]);
        }
    }

    public void Reproduction(World world) {
        int x = getX();
        int y = getY();

        for (int[] d : shuffledDirections()) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (world.isFree(nx, ny)) {
                Herbivore child = new Herbivore(new int[][]{{nx, ny}}, childEnergy, maxEnergy,
                        getIcon(), eat, run, costBaby, childEnergy, metabolism, hunger);
                if (world.setAgent(nx, ny, child)) {
                    energy -= costBaby;
                    return;
                }
            }
        }
    }

    @Override
    public void act(World world) {
        if (!isAlive()) {
            return;   // съеден раньше в этом же дне
        }

        scanner(world);

        boolean busy = false;

        // 1. Хищник близко и есть силы бежать — убегаем
        if (predatorX != -1 && energy > run) {
            busy = fleeFrom(world, predatorX, predatorY);
        }

        // 2. Иначе, если голодны — едим / идём к растению
        if (!busy && isHungry()) {
            int beforeX = getX(), beforeY = getY(), beforeEnergy = energy;
            foodMining(world);
            busy = (beforeX != getX() || beforeY != getY() || beforeEnergy != energy);
        }

        // 3. Иначе размножаемся
        if (!busy && energy > costBaby) {
            Reproduction(world);
            busy = true;
        }

        // 4. Иначе бродим
        if (!busy) {
            randomWalk(world);
        }

        EnergyGeneration();

        if (energy <= 0) {
            world.killAgent(getX(), getY());
        }
    }
}
