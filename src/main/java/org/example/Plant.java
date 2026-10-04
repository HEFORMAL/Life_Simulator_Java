package org.example;
/** Растение: не двигается, копит энергию от солнца, размножается в соседнюю пустую клетку. */
public class Plant extends Agent {
    private final int energyWithSun;
    private final int costBaby;
    private final int childEnergy;
    private final int minFreeNeighbors; // конкуренция за свет: в тесноте растение не размножается

    public Plant(int[][] coords, int energy, int maxEnergy, String icon,
                 int energyWithSun, int costBaby, int childEnergy, int minFreeNeighbors) {
        super(coords, energy, maxEnergy, icon);
        this.energyWithSun = energyWithSun;
        this.costBaby = costBaby;
        this.childEnergy = childEnergy;
        this.minFreeNeighbors = minFreeNeighbors;
    }

    private int freeNeighbors(World world) {
        int free = 0;
        for (int[] d : DIRECTIONS) {
            if (world.isFree(getX() + d[0], getY() + d[1])) free++;
        }
        return free;
    }

    @Override
    public void EnergyGeneration() {
        addEnergy(energyWithSun);   // фотосинтез, но не выше потолка
    }

    public void Reproduction(World world) {
        if (freeNeighbors(world) < minFreeNeighbors) {
            return;   // слишком тесно — семени негде прорасти
        }
        int x = getX();
        int y = getY();

        for (int[] d : shuffledDirections()) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (world.isFree(nx, ny)) {
                Plant child = new Plant(new int[][]{{nx, ny}}, childEnergy, maxEnergy,
                        getIcon(), energyWithSun, costBaby, childEnergy, minFreeNeighbors);
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
            return;   // растение уже съели в этом же дне
        }

        EnergyGeneration();

        if (energy > costBaby) {
            Reproduction(world);
        }

        if (energy <= 0) {
            world.killAgent(getX(), getY());
        }
    }
}
