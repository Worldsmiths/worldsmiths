package io.github.worldsmiths.samples.snake;

import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.DrawMode;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Input;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.SceneWalk;
import io.github.worldsmiths.core.Sprite;
import io.github.worldsmiths.core.Transform2D;
import io.github.worldsmiths.core.WorldsmithsGame;
import java.util.ArrayList;
import java.util.List;

/**
 * Fuller Snake for D3: grid drawn in {@link #render()} via engine {@link Draw2D}, head + body
 * segment + food nodes from {@link #createScene()} with {@link Transform2D} + colored {@link
 * Sprite}, discrete grid move on a step timer, turns via {@link EngineServices#input()} while
 * alive, restart via R or Space when dead, score in the window title. Touch: swipe to turn while
 * alive; tap (or swipe) to restart when dead. Retained sprites still draw through {@link
 * SceneWalk}.
 */
public final class SnakeGame implements WorldsmithsGame {

    static final int GRID_WIDTH = 20;
    static final int GRID_HEIGHT = 15;
    /** Public cell size from {@link SceneWalk}; grid and sprite scale agree. */
    static final float CELL_SIZE = SceneWalk.CELL_SIZE;

    private static final float HEAD_R = 0.2f;
    private static final float HEAD_G = 0.85f;
    private static final float HEAD_B = 0.35f;
    private static final float BODY_R = 0.12f;
    private static final float BODY_G = 0.55f;
    private static final float BODY_B = 0.28f;
    private static final float FOOD_R = 0.9f;
    private static final float FOOD_G = 0.25f;
    private static final float FOOD_B = 0.2f;

    private final SnakeController controller;
    private EngineServices services;
    private Scene scene;
    private Transform2D headTransform;
    private Transform2D foodTransform;
    private final List<Node> bodyNodes = new ArrayList<Node>();
    private int lastTitleScore = Integer.MIN_VALUE;
    private boolean trackingTouch;
    private float touchDownX;
    private float touchDownY;

    public SnakeGame() {
        this(new SnakeController(GRID_WIDTH / 2, GRID_HEIGHT / 2, GRID_WIDTH, GRID_HEIGHT));
    }

    /** Package-visible for headless scene-sync tests with a seeded controller. */
    SnakeGame(SnakeController controller) {
        this.controller = controller;
    }

    @Override
    public Scene createScene() {
        bodyNodes.clear();
        scene = new Scene("snake");
        headTransform = new Transform2D(controller.x(), controller.y());
        scene.add(new Node("head").add(headTransform).add(coloredSprite(HEAD_R, HEAD_G, HEAD_B)));
        foodTransform = new Transform2D(controller.foodX(), controller.foodY());
        scene.add(new Node("food").add(foodTransform).add(coloredSprite(FOOD_R, FOOD_G, FOOD_B)));
        syncScene();
        return scene;
    }

    @Override
    public void create(EngineServices services) {
        if (services == null) {
            throw new NullPointerException("services");
        }
        this.services = services;
        publishTitle(controller.score());
    }

    @Override
    public void update(float delta) {
        applyInput();
        controller.update(delta);
        syncScene();
        publishTitleIfChanged();
    }

    @Override
    public void dispose() {
        if (services != null) {
            services.draw2d().dispose();
        }
    }

    /**
     * Syncs head / body / food node transforms and adds or removes body segment nodes to match
     * {@link SnakeController#bodyLength()}. Package-visible test seam.
     */
    void syncScene() {
        if (scene == null) {
            return;
        }
        headTransform.x = controller.x();
        headTransform.y = controller.y();
        foodTransform.x = controller.foodX();
        foodTransform.y = controller.foodY();

        int needed = controller.bodyLength() - 1;
        while (bodyNodes.size() < needed) {
            int index = bodyNodes.size() + 1;
            Transform2D transform =
                    new Transform2D(controller.bodyX(index), controller.bodyY(index));
            Node body =
                    new Node("body-" + index)
                            .add(transform)
                            .add(coloredSprite(BODY_R, BODY_G, BODY_B));
            int foodIndex = scene.nodes.size() - 1;
            scene.nodes.add(foodIndex, body);
            bodyNodes.add(body);
        }
        while (bodyNodes.size() > needed) {
            Node removed = bodyNodes.remove(bodyNodes.size() - 1);
            scene.nodes.remove(removed);
        }
        for (int i = 0; i < bodyNodes.size(); i++) {
            Transform2D transform = bodyNodes.get(i).get(Transform2D.class);
            transform.x = controller.bodyX(i + 1);
            transform.y = controller.bodyY(i + 1);
        }
    }

    /** Window title format for score display. Package-visible test seam. */
    static String formatScoreTitle(int score) {
        return "Snake — score: " + score;
    }

    @Override
    public void render() {
        if (services == null) {
            return;
        }
        Draw2D g = services.draw2d();
        g.setOrtho2D(0f, 0f, services.graphics().getWidth(), services.graphics().getHeight());
        g.begin(DrawMode.LINE);
        g.setColor(0.25f, 0.25f, 0.28f, 1f);
        for (int x = 0; x <= GRID_WIDTH; x++) {
            float px = x * CELL_SIZE;
            g.line(px, 0f, px, GRID_HEIGHT * CELL_SIZE);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            float py = y * CELL_SIZE;
            g.line(0f, py, GRID_WIDTH * CELL_SIZE, py);
        }
        g.end();
    }

    private void applyInput() {
        if (services == null) {
            return;
        }
        Input in = services.input();
        applyTouchInput(in);
        if (!controller.alive()) {
            if (in.isKeyDown(Key.R) || in.isKeyDown(Key.SPACE)) {
                restartRun();
            }
            return;
        }
        if (in.isKeyDown(Key.LEFT) || in.isKeyDown(Key.A)) {
            controller.turn(-1, 0);
        } else if (in.isKeyDown(Key.RIGHT) || in.isKeyDown(Key.D)) {
            controller.turn(1, 0);
        } else if (in.isKeyDown(Key.UP) || in.isKeyDown(Key.W)) {
            controller.turn(0, 1);
        } else if (in.isKeyDown(Key.DOWN) || in.isKeyDown(Key.S)) {
            controller.turn(0, -1);
        }
    }

    private void applyTouchInput(Input in) {
        if (in.justTouched()) {
            trackingTouch = true;
            touchDownX = in.getX();
            touchDownY = in.getY();
            return;
        }
        if (!trackingTouch || in.isTouched()) {
            return;
        }
        trackingTouch = false;
        SnakeTouchGestures.Gesture gesture =
                SnakeTouchGestures.classify(touchDownX, touchDownY, in.getX(), in.getY());
        if (!controller.alive()) {
            if (gesture != SnakeTouchGestures.Gesture.NONE) {
                restartRun();
            }
            return;
        }
        switch (gesture) {
            case LEFT:
                controller.turn(-1, 0);
                break;
            case RIGHT:
                controller.turn(1, 0);
                break;
            case UP:
                controller.turn(0, 1);
                break;
            case DOWN:
                controller.turn(0, -1);
                break;
            default:
                break;
        }
    }

    private void restartRun() {
        controller.restart();
        syncScene();
        publishTitle(controller.score());
    }

    private void publishTitleIfChanged() {
        int score = controller.score();
        if (score != lastTitleScore) {
            publishTitle(score);
        }
    }

    private void publishTitle(int score) {
        lastTitleScore = score;
        if (services != null) {
            services.graphics().setTitle(formatScoreTitle(score));
        }
    }

    private static Sprite coloredSprite(float r, float g, float b) {
        Sprite sprite = new Sprite();
        sprite.r = r;
        sprite.g = g;
        sprite.b = b;
        sprite.a = 1f;
        return sprite;
    }
}
