package io.github.worldsmiths.samples.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.Sprite;
import io.github.worldsmiths.core.Transform2D;
import io.github.worldsmiths.core.WorldsmithsGame;
import java.util.ArrayList;
import java.util.List;

/**
 * Fuller Snake for D3: grid drawn in {@link #render()}, head + body segment + food nodes from
 * {@link #createScene()} with {@link Transform2D} + colored {@link Sprite}, discrete grid move on a
 * step timer, turns via {@link Gdx#input} while alive, restart via R or Space when dead, score in
 * the window title. Touch: swipe to turn while alive; tap (or swipe) to restart when dead.
 */
public final class SnakeGame implements WorldsmithsGame {

    static final int GRID_WIDTH = 20;
    static final int GRID_HEIGHT = 15;
    /** Matches desktop {@code SceneShapeRenderer.CELL_SIZE}. */
    static final float CELL_SIZE = 32f;

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
    private Scene scene;
    private Transform2D headTransform;
    private Transform2D foodTransform;
    private final List<Node> bodyNodes = new ArrayList<Node>();
    private ShapeRenderer shapes;
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
    public void create() {
        shapes = new ShapeRenderer();
        publishTitle(controller.score());
    }

    @Override
    public void update(float delta) {
        applyInput();
        controller.update(delta);
        syncScene();
        publishTitleIfChanged();
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

    /**
     * Screen-space ortho for pixel coordinates (origin bottom-left). Package-visible test seam;
     * grid draw applies this via {@link ShapeRenderer#setProjectionMatrix(Matrix4)}.
     */
    static Matrix4 screenOrthoMatrix(float width, float height) {
        return new Matrix4().setToOrtho2D(0f, 0f, width, height);
    }

    @Override
    public void render() {
        if (shapes == null) {
            return;
        }
        if (Gdx.graphics != null) {
            shapes.setProjectionMatrix(
                    screenOrthoMatrix(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        }
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.25f, 0.25f, 0.28f, 1f);
        for (int x = 0; x <= GRID_WIDTH; x++) {
            float px = x * CELL_SIZE;
            shapes.line(px, 0f, px, GRID_HEIGHT * CELL_SIZE);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            float py = y * CELL_SIZE;
            shapes.line(0f, py, GRID_WIDTH * CELL_SIZE, py);
        }
        shapes.end();
    }

    private void applyInput() {
        if (Gdx.input == null) {
            return;
        }
        applyTouchInput();
        if (!controller.alive()) {
            if (Gdx.input.isKeyPressed(Input.Keys.R) || Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                restartRun();
            }
            return;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            controller.turn(-1, 0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            controller.turn(1, 0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            controller.turn(0, 1);
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            controller.turn(0, -1);
        }
    }

    private void applyTouchInput() {
        if (Gdx.input.justTouched()) {
            trackingTouch = true;
            touchDownX = Gdx.input.getX();
            touchDownY = Gdx.input.getY();
            return;
        }
        if (!trackingTouch || Gdx.input.isTouched()) {
            return;
        }
        trackingTouch = false;
        SnakeTouchGestures.Gesture gesture =
                SnakeTouchGestures.classify(
                        touchDownX, touchDownY, Gdx.input.getX(), Gdx.input.getY());
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
        if (Gdx.graphics != null) {
            Gdx.graphics.setTitle(formatScoreTitle(score));
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
