package io.github.worldsmiths.samples.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.Sprite;
import io.github.worldsmiths.core.Transform2D;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Partial Snake for D2: grid drawn in {@link #render()}, one head from {@link #createScene()}
 * with {@link Transform2D} + colored {@link Sprite}, discrete grid move on a step timer, turns via
 * {@link Gdx#input} (arrows or WASD).
 */
public final class SnakeGame implements WorldsmithsGame {

    static final int GRID_WIDTH = 20;
    static final int GRID_HEIGHT = 15;
    /** Matches desktop {@code SceneShapeRenderer.CELL_SIZE}. */
    static final float CELL_SIZE = 32f;

    private final SnakeController controller =
            new SnakeController(GRID_WIDTH / 2, GRID_HEIGHT / 2, GRID_WIDTH, GRID_HEIGHT);
    private Transform2D headTransform;
    private ShapeRenderer shapes;

    @Override
    public Scene createScene() {
        headTransform = new Transform2D(controller.x(), controller.y());
        Sprite sprite = new Sprite();
        sprite.r = 0.2f;
        sprite.g = 0.85f;
        sprite.b = 0.35f;
        sprite.a = 1f;
        Node head = new Node("head").add(headTransform).add(sprite);
        return new Scene("snake").add(head);
    }

    @Override
    public void create() {
        shapes = new ShapeRenderer();
    }

    @Override
    public void update(float delta) {
        applyInput();
        controller.update(delta);
        headTransform.x = controller.x();
        headTransform.y = controller.y();
    }

    @Override
    public void render() {
        if (shapes == null) {
            return;
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
}
