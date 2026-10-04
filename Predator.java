package org.example;
import java.util.ArrayList;
import java.util.List;

/**
 * Хищник. Зрение 5x5 (агент в центре).
 * Логика хода: съесть травоядное рядом -> преследовать замеченное -> размножиться -> бродить.
 */
public class Predator extends Agent implements Checkup {
    private final int eat;         // энергия от добычи
    private final int huntCost;    // цена охоты (шаг преследования)
    private final int costBaby;
    private final int childEnergy;
    private final int metabolism;
    private final int hunger;      // порог голода: сытый хищник не охотится

    private boolean hunting = false;
    private int preyX = -1, preyY = -1;

    public Predator(int[][] coords, int energy, int maxEnergy, String icon,
                    int eat, int huntCost, int costBaby, int childEnergy, int metabolism, int hunger) {
        super(coords, energy, maxEnergy, icon);
        this.eat = eat;
        this.huntCost = huntCost;
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

    /** ЗРЕНИЕ 5x5: ищем ближайшее травоядное */
    @Override
    public void scanner(World world) {
        hunting = false;
        preyX = -1;
        preyY = -1;

        int x = getX();
        int y = getY();
        int minDist = Integer.MAX_VALUE;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;

                Agent suspect = world.getAgent(x + dx, y + dy);
                if (suspect instanceof Herbivore && suspect.isAlive()) {
                    int dist = Math.abs(dx) + Math.abs(dy);
                    if (dist < minDist) {
                        minDist = dist;
                        preyX = x + dx;
                        preyY = y + dy;
                    }
                }
            }
        }

        hunting = (preyX != -1);
    }

    @Override
    public void foodMining(World world) {
        if (!hunting) {
            return;
        }

        int x = getX();
        int y = getY();

        // 1) добыча вплотную — съедаем
        for (int[] d : shuffledDirections()) {
            int nx = x + d[0];
            int ny = y + d[1];
            Agent suspect = world.getAgent(nx, ny);

            if (suspect instanceof Herbivore && suspect.isAlive()) {
                addEnergy(eat);
                world.killAgent(nx, ny);
                world.moveAgent(this, nx, ny);
                hunting = false;
                return;
            }
        }

        // 2) добыча далеко — шаг в её сторону, охота стоит энергии
        int ddx = preyX - x;
        int ddy = preyY - y;
        int sx = Integer.compare(ddx, 0);
        int sy = Integer.compare(ddy, 0);

        boolean moved;
        if (Math.abs(ddx) >= Math.abs(ddy)) {
            moved = (sx != 0) && world.moveAgent(this, x + sx, y);
            if (!moved && sy != 0) moved = world.moveAgent(this, x, y + sy);
        } else {
            moved = (sy != 0) && world.moveAgent(this, x, y + sy);
            if (!moved && sx != 0) moved = world.moveAgent(this, x + sx, y);
        }

        if (moved) {
            energy -= huntCost;
        } else {
            hunting = false;
        }
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
                Predator child = new Predator(new int[][]{{nx, ny}}, childEnergy, maxEnergy,
                        getIcon(), eat, huntCost, costBaby, childEnergy, metabolism, hunger);
                if (world.setAgent(nx, ny, child)) {
                    energy -= costBaby;   // БЫЛО: energy -= childEnergy (размножение было слишком дешёвым)
                    return;
                }
            }
        }
    }

    @Override
    public void act(World world) {
        if (!isAlive()) {
            return;
        }

        int beforeX = getX(), beforeY = getY();

        if (isHungry()) {
            scanner(world);
            foodMining(world);
        }

        boolean moved = (beforeX != getX() || beforeY != getY());

        if (energy > costBaby) {
            Reproduction(world);
        } else if (!moved) {
            randomWalk(world);
        }

        EnergyGeneration();

        if (energy <= 0) {
            world.killAgent(getX(), getY());
        }
    }
}
