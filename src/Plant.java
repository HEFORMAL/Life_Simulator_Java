public class Plant extends Agent {
    private boolean move;
    private int energi_with_sun;
    private int cost_baby; //трата энергии за размножение
    private int childEnergy;

    // Конструктор (обязателен, чтобы передать параметры в Agent)
    public Plant(int[][] coords, int energy, String icon, int energi_with_sun, int cost_baby, int childEnergy) {
        super(coords, energy, icon);
        this.move = false;
        this.energi_with_sun = energi_with_sun;
        this.cost_baby = cost_baby;
        this.childEnergy = childEnergy;
    }
    @Override
    public void EnergyGeneration(){
        this.energy += energi_with_sun;

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
                Plant newPlant = new Plant(new int[][]{{newx, newy}}, childEnergy, this.getIcon(), this.energi_with_sun, this.cost_baby, this.childEnergy);
                                                                //возможно можно поменять чтобы при рожденнии была энергия не 0
                if (world.setAgent(newx, newy, newPlant)) {
                    energy -= cost_baby;
                    break;
                }

            }
        }
    }
    @Override
    public void act(World world) {
        if (this.energy <= 0) {
            int[][] cords = getCoords();
            int x = cords[0][0];
            int y = cords[0][1];
            world.setAgent(x, y, null);
            return;
        }
        EnergyGeneration();
        if (this.energy <= 0) {
            int[][] cords = getCoords();
            world.setAgent(cords[0][0], cords[0][1], null);
            return;
        }


        if (energy > cost_baby) {
            Reproduction(world);
        }
    }




}
