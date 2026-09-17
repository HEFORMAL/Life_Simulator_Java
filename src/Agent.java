public abstract class Agent {
    private int[][] coords;
    protected int energy;
    private String icon;


    public Agent(int[][] coords, int energy, String icon) {
        this.coords = coords;
        this.energy = energy;
        this.icon = icon;

    }

    public int[][] getCoords() {
        return coords;
    }


    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }
    public void setCoords(int x, int y) {
        this.coords = new int[][]{{x, y}};
    }
    public String getIcon() {
        return icon;
    }
    public abstract void EnergyGeneration();
    public abstract void act(World world);

}