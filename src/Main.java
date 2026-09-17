import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {

    // === 🌿 НАСТРОЙКИ РАСТЕНИЙ ===
    public static final int PLANT_ENERGY = 10;
    public static final String PLANT_ICON = "\uD83C\uDF3F";
    public static final int PLANT_SUN_ENERGY = 3;
    public static final int PLANT_COST_BABY = 40;
    public static final int PLANT_CHILD_ENERGY = 15;

    // === 🐇 НАСТРОЙКИ ТРАВЯОДНЫХ ===
    public static final int HERBIVORE_ENERGY = 40;
    public static final String HERBIVORE_ICON = "\uD83D\uDC07";
    public static final int HERBIVORE_EAT = 15;
    public static final int HERBIVORE_RUN = 6;
    public static final int HERBIVORE_COST_BABY = 30;
    public static final int HERBIVORE_CHILD_ENERGY = 10;

    // === 🐺 НАСТРОЙКИ ХИЩНИКОВ ===
    public static final int PREDATOR_ENERGY = 80;        // ↑ больше стартовой энергии
    public static final String PREDATOR_ICON = "\uD83D\uDC3A";
    public static final int PREDATOR_EAT = 50;           // ↑ больше энергии от жертвы (было 30)
    public static final int PREDATOR_HUNT_COST = 1;      // ↓ дешевле охота (было 2)
    public static final int PREDATOR_COST_BABY = 80;     // ↓ легче размножаться (было 90)
    public static final int PREDATOR_CHILD_ENERGY = 40;

    public static void main(String[] args) {
        System.out.println("🌍 Запуск симуляции экосистемы (Этап 1: Консоль)");

        World world = new World(20, 20);

        // === КЛАСТЕР 1: Левый верхний угол (с хищником) ===
        world.setAgent(2, 2, new Plant(new int[][]{{2, 2}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(3, 3, new Plant(new int[][]{{3, 3}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(2, 4, new Plant(new int[][]{{2, 4}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(3, 2, new Herbivore(new int[][]{{3, 2}}, HERBIVORE_ENERGY, HERBIVORE_ICON, HERBIVORE_EAT, HERBIVORE_RUN, HERBIVORE_COST_BABY, HERBIVORE_CHILD_ENERGY));
        world.setAgent(4, 3, new Herbivore(new int[][]{{4, 3}}, HERBIVORE_ENERGY, HERBIVORE_ICON, HERBIVORE_EAT, HERBIVORE_RUN, HERBIVORE_COST_BABY, HERBIVORE_CHILD_ENERGY));
        world.setAgent(4, 2, new Predator(new int[][]{{4, 2}}, PREDATOR_ENERGY, PREDATOR_ICON, PREDATOR_EAT, PREDATOR_HUNT_COST, PREDATOR_COST_BABY, PREDATOR_CHILD_ENERGY));

        // === КЛАСТЕР 2: Правый верхний угол (БЕЗ хищника - мирная зона) ===
        world.setAgent(15, 2, new Plant(new int[][]{{15, 2}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(16, 3, new Plant(new int[][]{{16, 3}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(14, 4, new Plant(new int[][]{{14, 4}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(15, 4, new Herbivore(new int[][]{{15, 4}}, HERBIVORE_ENERGY, HERBIVORE_ICON, HERBIVORE_EAT, HERBIVORE_RUN, HERBIVORE_COST_BABY, HERBIVORE_CHILD_ENERGY));
        world.setAgent(16, 5, new Herbivore(new int[][]{{16, 5}}, HERBIVORE_ENERGY, HERBIVORE_ICON, HERBIVORE_EAT, HERBIVORE_RUN, HERBIVORE_COST_BABY, HERBIVORE_CHILD_ENERGY));

        // === КЛАСТЕР 3: Левый нижний угол (БЕЗ хищника - мирная зона) ===
        world.setAgent(2, 15, new Plant(new int[][]{{2, 15}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(3, 16, new Plant(new int[][]{{3, 16}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(4, 14, new Plant(new int[][]{{4, 14}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(3, 15, new Herbivore(new int[][]{{3, 15}}, HERBIVORE_ENERGY, HERBIVORE_ICON, HERBIVORE_EAT, HERBIVORE_RUN, HERBIVORE_COST_BABY, HERBIVORE_CHILD_ENERGY));
        world.setAgent(4, 16, new Herbivore(new int[][]{{4, 16}}, HERBIVORE_ENERGY, HERBIVORE_ICON, HERBIVORE_EAT, HERBIVORE_RUN, HERBIVORE_COST_BABY, HERBIVORE_CHILD_ENERGY));

        // === КЛАСТЕР 4: Правый нижний угол (с хищником) ===
        world.setAgent(15, 15, new Plant(new int[][]{{15, 15}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(16, 16, new Plant(new int[][]{{16, 16}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(14, 17, new Plant(new int[][]{{14, 17}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(15, 16, new Herbivore(new int[][]{{15, 16}}, HERBIVORE_ENERGY, HERBIVORE_ICON, HERBIVORE_EAT, HERBIVORE_RUN, HERBIVORE_COST_BABY, HERBIVORE_CHILD_ENERGY));
        world.setAgent(16, 17, new Herbivore(new int[][]{{16, 17}}, HERBIVORE_ENERGY, HERBIVORE_ICON, HERBIVORE_EAT, HERBIVORE_RUN, HERBIVORE_COST_BABY, HERBIVORE_CHILD_ENERGY));
        world.setAgent(17, 15, new Predator(new int[][]{{17, 15}}, PREDATOR_ENERGY, PREDATOR_ICON, PREDATOR_EAT, PREDATOR_HUNT_COST, PREDATOR_COST_BABY, PREDATOR_CHILD_ENERGY));

        // === ДОПОЛНИТЕЛЬНЫЕ РАСТЕНИЯ: центр и рассеянные по карте ===
        world.setAgent(10, 10, new Plant(new int[][]{{10, 10}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(11, 9, new Plant(new int[][]{{11, 9}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(9, 3, new Plant(new int[][]{{9, 3}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(5, 7, new Plant(new int[][]{{5, 7}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(7, 6, new Plant(new int[][]{{7, 6}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(2, 9, new Plant(new int[][]{{2, 9}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(9, 14, new Plant(new int[][]{{9, 14}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(6, 11, new Plant(new int[][]{{6, 11}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(13, 7, new Plant(new int[][]{{13, 7}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(17, 9, new Plant(new int[][]{{17, 9}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(8, 17, new Plant(new int[][]{{8, 17}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(12, 13, new Plant(new int[][]{{12, 13}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));

        // === НОВЫЕ РАСТЕНИЯ (добавлено 15 штук для плотного покрова) ===
        world.setAgent(6, 2, new Plant(new int[][]{{6, 2}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(8, 4, new Plant(new int[][]{{8, 4}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(12, 5, new Plant(new int[][]{{12, 5}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(18, 7, new Plant(new int[][]{{18, 7}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(3, 8, new Plant(new int[][]{{3, 8}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(7, 10, new Plant(new int[][]{{7, 10}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(14, 10, new Plant(new int[][]{{14, 10}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(11, 12, new Plant(new int[][]{{11, 12}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(4, 11, new Plant(new int[][]{{4, 11}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(18, 13, new Plant(new int[][]{{18, 13}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(8, 14, new Plant(new int[][]{{8, 14}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(11, 16, new Plant(new int[][]{{11, 16}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(13, 18, new Plant(new int[][]{{13, 18}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(6, 18, new Plant(new int[][]{{6, 18}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));
        world.setAgent(18, 18, new Plant(new int[][]{{18, 18}}, PLANT_ENERGY, PLANT_ICON, PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY));

        Scanner scanner = new Scanner(System.in);
        int day = 1;
        boolean isRunning = true;

        while (isRunning) {
            System.out.print("\033[H\033[2J");
            System.out.flush();

            System.out.println("=== ДЕНЬ " + day + " ===");
            world.printMap();

            int plants = 0, herbivores = 0, predators = 0;
            for (Agent agent : world.getAllAgents()) {
                if (agent instanceof Plant) plants++;
                else if (agent instanceof Herbivore) herbivores++;
                else if (agent instanceof Predator) predators++;
            }

            System.out.printf("Статистика: [🌿] Растений: %-3d | [🐇] Травоядных: %-3d | [🐺] Хищников: %-3d%n",
                    plants, herbivores, predators);

            System.out.print("\nНажмите Enter для следующего дня (или введите 'stop' для выхода): ");
            String input = scanner.nextLine();

            if (input.trim().equalsIgnoreCase("stop")) {
                isRunning = false;
                break;
            }

            List<Agent> agents = world.getAllAgents();
            for (Agent agent : agents) {
                agent.act(world);
            }

            day++;

            List<String> survivingSpecies = new ArrayList<>();
            if (plants > 0) survivingSpecies.add("🌿 Растения");
            if (herbivores > 0) survivingSpecies.add("🐇 Травоядные");
            if (predators > 0) survivingSpecies.add("🐺 Хищники");

            if (survivingSpecies.size() <= 1) {
                System.out.println("\n🏁 Экосистема разрушена!");

                if (survivingSpecies.isEmpty()) {
                    System.out.println("   💀 Все виды полностью вымерли.");
                } else {
                    System.out.println("   Остался только один вид: " + survivingSpecies.get(0));
                }
                break;
            }
        }

        System.out.println("\n✅ Симуляция завершена. Всего дней: " + (day - 1));
        scanner.close();
    }
}