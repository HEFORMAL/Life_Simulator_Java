package org.example;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Базовый класс всех агентов экосистемы.
 * Хранит координаты, энергию, иконку и признак "жив/мёртв".
 */
public abstract class Agent {
    private int x;
    private int y;
    protected int energy;
    protected final int maxEnergy;   // потолок энергии (чтобы не было бесконечного роста)
    private final String icon;
    private boolean alive = true;    // ВАЖНО: съеденный агент помечается мёртвым

    /** 4 соседние клетки (без диагоналей) */
    protected static final int[][] DIRECTIONS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

    public Agent(int[][] coords, int energy, int maxEnergy, String icon) {
        this.x = coords[0][0];
        this.y = coords[0][1];
        this.energy = Math.min(energy, maxEnergy);
        this.maxEnergy = maxEnergy;
        this.icon = icon;
    }

    public int[][] getCoords() {
        return new int[][]{{x, y}};
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setCoords(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(energy, maxEnergy);
    }

    /** Прибавить энергию с учётом потолка */
    public void addEnergy(int delta) {
        this.energy = Math.min(this.energy + delta, maxEnergy);
    }

    public String getIcon() {
        return icon;
    }

    public boolean isAlive() {
        return alive;
    }

    /** Пометить агента мёртвым: он больше не будет ходить, даже если остался в списке дня */
    public void kill() {
        this.alive = false;
    }

    /** Перемешанный список направлений — убирает перекос роста популяции в одну сторону */
    protected List<int[]> shuffledDirections() {
        List<int[]> dirs = new ArrayList<>();
        for (int[] d : DIRECTIONS) {
            dirs.add(d);
        }
        Collections.shuffle(dirs, World.RANDOM);
        return dirs;
    }

    /** Обмен веществ: изменение энергии за шаг */
    public abstract void EnergyGeneration();

    /** Полный ход агента за один день */
    public abstract void act(World world);
}
