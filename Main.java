package org.example;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;

import java.util.Collections;
import java.util.List;

public class Main extends Application {

    // === РАЗМЕР МИРА И СТАРТОВОЕ НАСЕЛЕНИЕ ===
    public static int WORLD_WIDTH = 30;
    public static int WORLD_HEIGHT = 30;
    public static int START_PLANTS = 135;
    public static int START_HERBIVORES = 36;
    public static int START_PREDATORS = 10;

    // === НАСТРОЙКИ РАСТЕНИЙ ===
    public static int PLANT_ENERGY = 2;
    public static int PLANT_MAX_ENERGY = 15;
    public static String PLANT_ICON = "\uD83C\uDF3F";
    public static int PLANT_SUN_ENERGY = 5;
    public static int PLANT_COST_BABY = 5;
    public static int PLANT_CHILD_ENERGY = 2;
    public static int PLANT_MIN_FREE_NEIGHBORS = 2;

    // === НАСТРОЙКИ ТРАВОЯДНЫХ ===
    public static int HERBIVORE_ENERGY = 100;
    public static int HERBIVORE_MAX_ENERGY = 150;
    public static String HERBIVORE_ICON = "\uD83D\uDC07";
    public static int HERBIVORE_EAT = 20;
    public static int HERBIVORE_RUN = 3;
    public static int HERBIVORE_COST_BABY = 100;
    public static int HERBIVORE_CHILD_ENERGY = 10;
    public static int HERBIVORE_METABOLISM = 1;
    public static int HERBIVORE_HUNGER = 115;

    // === НАСТРОЙКИ ХИЩНИКОВ ===
    public static int PREDATOR_ENERGY = 110;
    public static int PREDATOR_MAX_ENERGY = 180;
    public static String PREDATOR_ICON = "\uD83D\uDC3A";
    public static int PREDATOR_EAT = 90;
    public static int PREDATOR_HUNT_COST = 1;
    public static int PREDATOR_COST_BABY = 130;
    public static int PREDATOR_CHILD_ENERGY = 50;
    public static int PREDATOR_METABOLISM = 2;
    public static int PREDATOR_HUNGER = 120;

    private World world;
    private int day = 1;

    private final GridPane grid = new GridPane();
    private final Label infoLabel = new Label();
    private Timeline timeline;
    private Stage mainStage;
    private Button autoBtn;
    private Slider speedSlider;
    private Label speedLabel;

    public static void main(String[] args) {
        launch(args);
    }

    // ================= ГЛАВНОЕ ОКНО =================
    @Override
    public void start(Stage stage) {
        mainStage = stage;
        world = buildWorld();

        // === КНОПКИ ===
        Button stepBtn = new Button("Шаг ▶");
        autoBtn = new Button("Авто ▶▶");
        Button resetBtn = new Button("Заново ↺");
        Button settingsBtn = new Button("⚙ Настройки");

        stepBtn.setOnAction(e -> doStep());

        stage.setOnCloseRequest(e -> {
            if (timeline != null) timeline.stop();  // останавливаем авто-режим перед выходом
        });

        autoBtn.setOnAction(e -> {
            if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING) {
                timeline.stop();
                autoBtn.setText("Авто ▶▶");
            } else {
                timeline = createTimeline();
                timeline.play();
                autoBtn.setText("Пауза ⏸");
            }
        });

        resetBtn.setOnAction(e -> {
            if (timeline != null) timeline.stop();
            autoBtn.setText("Авто ▶▶");
            world = buildWorld();
            day = 1;
            redraw();
        });

        settingsBtn.setOnAction(e -> openSettings());

        // === ВЕРТИКАЛЬНЫЙ СЛАЙДЕР СКОРОСТИ ===
        speedSlider = new Slider(30, 1000, 120);
        speedSlider.setOrientation(Orientation.VERTICAL);
        speedSlider.setPrefHeight(160);
        speedLabel = new Label("120 мс/день");
        speedLabel.setWrapText(true);
        speedSlider.valueProperty().addListener((obs, o, n) -> {
            speedLabel.setText(n.intValue() + " мс/день");
            if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING) {
                timeline.stop();
                timeline = createTimeline();
                timeline.play();
            }
        });

        // === ПРАВАЯ ПАНЕЛЬ УПРАВЛЕНИЯ ===
        stepBtn.setMaxWidth(Double.MAX_VALUE);
        autoBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        settingsBtn.setMaxWidth(Double.MAX_VALUE);

        VBox rightPanel = new VBox(10,
                stepBtn, autoBtn, resetBtn, settingsBtn,
                new Separator(),
                speedLabel, speedSlider);
        rightPanel.setPadding(new Insets(12));
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPrefWidth(150);

        // === ВЕРХНЯЯ СТАТИСТИКА ===
        infoLabel.setFont(Font.font("Segoe UI", 15));
        HBox top = new HBox(infoLabel);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(8));

        // === СБОРКА ОКНА: поле в центре, кнопки СПРАВА ===
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(grid);
        root.setRight(rightPanel);

        stage.setTitle("Экосистема — JavaFX");
        stage.setScene(new Scene(root, 1000, 800));
        stage.show();
        redraw();
    }

    private Timeline createTimeline() {
        Timeline t = new Timeline(new KeyFrame(Duration.millis(speedSlider.getValue()), e -> doStep()));
        t.setCycleCount(Animation.INDEFINITE);
        return t;
    }

    private void doStep() {
        simulateDay(world);
        day++;
        redraw();
        checkEnd();
    }

    // ================= ОКНО НАСТРОЕК =================
    private void openSettings() {
        Stage st = new Stage();
        st.setTitle("⚙ Настройки симуляции");
        st.initOwner(mainStage);

        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(6);
        g.setPadding(new Insets(15));

        int row = 0;
        g.add(new Label("— МИР —"), 0, row++);
        Spinner<Integer> spWidth  = addRow(g, row++, "Ширина мира", 5, 60, WORLD_WIDTH);
        Spinner<Integer> spHeight = addRow(g, row++, "Высота мира", 5, 60, WORLD_HEIGHT);
        Spinner<Integer> spPlants = addRow(g, row++, "Старт растений", 0, 500, START_PLANTS);
        Spinner<Integer> spHerb   = addRow(g, row++, "Старт травоядных", 0, 300, START_HERBIVORES);
        Spinner<Integer> spPred   = addRow(g, row++, "Старт хищников", 0, 100, START_PREDATORS);

        g.add(new Label("— 🌿 РАСТЕНИЯ —"), 0, row++);
        Spinner<Integer> plSun   = addRow(g, row++, "Энергия солнца", 0, 20, PLANT_SUN_ENERGY);
        Spinner<Integer> plCost  = addRow(g, row++, "Цена размножения", 0, 100, PLANT_COST_BABY);
        Spinner<Integer> plChild = addRow(g, row++, "Энергия ребёнка", 0, 50, PLANT_CHILD_ENERGY);
        Spinner<Integer> plMin   = addRow(g, row++, "Мин. своб. соседей", 0, 4, PLANT_MIN_FREE_NEIGHBORS);

        g.add(new Label("— 🐇 ТРАВОЯДНЫЕ —"), 0, row++);
        Spinner<Integer> hEat   = addRow(g, row++, "Энергия от еды", 0, 100, HERBIVORE_EAT);
        Spinner<Integer> hRun   = addRow(g, row++, "Цена побега", 0, 50, HERBIVORE_RUN);
        Spinner<Integer> hCost  = addRow(g, row++, "Цена размножения", 0, 300, HERBIVORE_COST_BABY);
        Spinner<Integer> hChild = addRow(g, row++, "Энергия ребёнка", 0, 100, HERBIVORE_CHILD_ENERGY);
        Spinner<Integer> hMet   = addRow(g, row++, "Метаболизм", 0, 10, HERBIVORE_METABOLISM);
        Spinner<Integer> hHun   = addRow(g, row++, "Порог голода", 0, 300, HERBIVORE_HUNGER);

        g.add(new Label("— 🐺 ХИЩНИКИ —"), 0, row++);
        Spinner<Integer> pEat   = addRow(g, row++, "Энергия от добычи", 0, 200, PREDATOR_EAT);
        Spinner<Integer> pHunt  = addRow(g, row++, "Цена охоты", 0, 50, PREDATOR_HUNT_COST);
        Spinner<Integer> pCost  = addRow(g, row++, "Цена размножения", 0, 400, PREDATOR_COST_BABY);
        Spinner<Integer> pChild = addRow(g, row++, "Энергия ребёнка", 0, 150, PREDATOR_CHILD_ENERGY);
        Spinner<Integer> pMet   = addRow(g, row++, "Метаболизм", 0, 10, PREDATOR_METABOLISM);
        Spinner<Integer> pHun   = addRow(g, row++, "Порог голода", 0, 300, PREDATOR_HUNGER);

        Button apply = new Button("✔ Применить и перезапустить");
        apply.setOnAction(e -> {
            WORLD_WIDTH  = spWidth.getValue();
            WORLD_HEIGHT = spHeight.getValue();
            START_PLANTS = spPlants.getValue();
            START_HERBIVORES = spHerb.getValue();
            START_PREDATORS  = spPred.getValue();

            PLANT_SUN_ENERGY = plSun.getValue();
            PLANT_COST_BABY  = plCost.getValue();
            PLANT_CHILD_ENERGY = plChild.getValue();
            PLANT_MIN_FREE_NEIGHBORS = plMin.getValue();

            HERBIVORE_EAT = hEat.getValue();
            HERBIVORE_RUN = hRun.getValue();
            HERBIVORE_COST_BABY = hCost.getValue();
            HERBIVORE_CHILD_ENERGY = hChild.getValue();
            HERBIVORE_METABOLISM = hMet.getValue();
            HERBIVORE_HUNGER = hHun.getValue();

            PREDATOR_EAT = pEat.getValue();
            PREDATOR_HUNT_COST = pHunt.getValue();
            PREDATOR_COST_BABY = pCost.getValue();
            PREDATOR_CHILD_ENERGY = pChild.getValue();
            PREDATOR_METABOLISM = pMet.getValue();
            PREDATOR_HUNGER = pHun.getValue();

            if (timeline != null) timeline.stop();
            autoBtn.setText("Авто ▶▶");
            world = buildWorld();
            day = 1;
            redraw();
            st.close();
        });

        VBox box = new VBox(12, g, apply);
        box.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);

        st.setScene(new Scene(scroll, 400, 650));
        st.show();
    }

    private Spinner<Integer> addRow(GridPane g, int row, String label, int min, int max, int value) {
        Spinner<Integer> sp = new Spinner<>(min, max, value);
        sp.setEditable(true);
        sp.setPrefWidth(110);
        g.add(new Label(label), 0, row);
        g.add(sp, 1, row);
        return sp;
    }

    // ================= ИНФО-ОКОШКО ОБ АГЕНТЕ =================
    private void showAgentInfo(Agent a) {
        String type;
        if (a instanceof Plant) type = "🌿 Растение";
        else if (a instanceof Herbivore) type = "🐇 Травоядное";
        else type = "🐺 Хищник";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация об агенте");
        alert.setHeaderText(type);
        alert.setContentText(String.format(
                "Координаты: (%d, %d)%nЭнергия: %d%nСостояние: %s",
                a.getX(), a.getY(), a.getEnergy(), a.isAlive() ? "жив ✔" : "мёртв ✖"));
        alert.initOwner(mainStage);
        alert.showAndWait();
    }

    // ================= ЛОГИКА СИМУЛЯЦИИ =================
    public World buildWorld() {
        World w = new World(WORLD_WIDTH, WORLD_HEIGHT);
        place(w, START_PLANTS, 'p');
        place(w, START_HERBIVORES, 'h');
        place(w, START_PREDATORS, 'x');
        return w;
    }

    private void place(World w, int count, char type) {
        int placed = 0, attempts = 0;
        while (placed < count && attempts < count * 200) {
            attempts++;
            int x = World.RANDOM.nextInt(w.getWidth());
            int y = World.RANDOM.nextInt(w.getHeight());
            if (!w.isFree(x, y)) continue;

            int[][] c = new int[][]{{x, y}};
            Agent agent;
            if (type == 'p') {
                agent = new Plant(c, PLANT_ENERGY, PLANT_MAX_ENERGY, PLANT_ICON,
                        PLANT_SUN_ENERGY, PLANT_COST_BABY, PLANT_CHILD_ENERGY, PLANT_MIN_FREE_NEIGHBORS);
            } else if (type == 'h') {
                agent = new Herbivore(c, HERBIVORE_ENERGY, HERBIVORE_MAX_ENERGY, HERBIVORE_ICON,
                        HERBIVORE_EAT, HERBIVORE_RUN, HERBIVORE_COST_BABY,
                        HERBIVORE_CHILD_ENERGY, HERBIVORE_METABOLISM, HERBIVORE_HUNGER);
            } else {
                agent = new Predator(c, PREDATOR_ENERGY, PREDATOR_MAX_ENERGY, PREDATOR_ICON,
                        PREDATOR_EAT, PREDATOR_HUNT_COST, PREDATOR_COST_BABY,
                        PREDATOR_CHILD_ENERGY, PREDATOR_METABOLISM, PREDATOR_HUNGER);
            }
            w.setAgent(x, y, agent);
            placed++;
        }
    }

    public void simulateDay(World w) {
        List<Agent> agents = w.getAllAgents();
        Collections.shuffle(agents, World.RANDOM);
        for (Agent agent : agents) {
            if (agent.isAlive()) {
                agent.act(w);
            }
        }
    }

    // ================= ПЕРЕРИСОВКА =================
    private void redraw() {
        grid.getChildren().clear();
        for (int y = 0; y < world.getHeight(); y++) {
            for (int x = 0; x < world.getWidth(); x++) {
                final Agent agent = world.getAgent(x, y);
                Label cell = new Label(agent == null ? "·" : agent.getIcon());
                cell.setFont(Font.font(14));
                cell.setMinSize(26, 26);
                cell.setAlignment(Pos.CENTER);
                if (agent != null) {
                    cell.setStyle("-fx-cursor: hand;");
                    cell.setOnMouseClicked(e -> showAgentInfo(agent)); // клик = инфо-окошко
                }
                grid.add(cell, x, y);
            }
        }
        int[] c = countSpecies();
        infoLabel.setText(String.format(
                "День: %d    |    🌿 Растений: %d    |    🐇 Травоядных: %d    |    🐺 Хищников: %d",
                day, c[0], c[1], c[2]));
    }

    private int[] countSpecies() {
        int p = 0, h = 0, x = 0;
        for (Agent a : world.getAllAgents()) {
            if (a instanceof Plant) p++;
            else if (a instanceof Herbivore) h++;
            else if (a instanceof Predator) x++;
        }
        return new int[]{p, h, x};
    }

    private void checkEnd() {
        int[] c = countSpecies();
        int species = (c[0] > 0 ? 1 : 0) + (c[1] > 0 ? 1 : 0) + (c[2] > 0 ? 1 : 0);
        if (species <= 1 && timeline != null) {
            timeline.stop();
            autoBtn.setText("Авто ▶▶");
        }
    }
}