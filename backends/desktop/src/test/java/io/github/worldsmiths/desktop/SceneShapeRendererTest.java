package io.github.worldsmiths.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.worldsmiths.core.Node;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.Sprite;
import io.github.worldsmiths.core.Transform2D;
import java.util.List;
import org.junit.jupiter.api.Test;

class SceneShapeRendererTest {

    @Test
    void collectsFilledRectForVisibleSpriteUsingCellCoordsAndRgba() {
        Sprite sprite = new Sprite("ignored.png");
        sprite.r = 0.2f;
        sprite.g = 0.4f;
        sprite.b = 0.6f;
        sprite.a = 0.8f;

        Scene scene = new Scene("test");
        scene.add(new Node("head").add(new Transform2D(3f, 5f)).add(sprite));

        List<ShapeDrawCommand> draws = SceneShapeRenderer.collectDrawCommands(scene);

        assertEquals(1, draws.size());
        ShapeDrawCommand draw = draws.get(0);
        assertEquals(3f * SceneShapeRenderer.CELL_SIZE, draw.x, 0.001f);
        assertEquals(5f * SceneShapeRenderer.CELL_SIZE, draw.y, 0.001f);
        assertEquals(SceneShapeRenderer.CELL_SIZE, draw.width, 0.001f);
        assertEquals(SceneShapeRenderer.CELL_SIZE, draw.height, 0.001f);
        assertEquals(0.2f, draw.r, 0.001f);
        assertEquals(0.4f, draw.g, 0.001f);
        assertEquals(0.6f, draw.b, 0.001f);
        assertEquals(0.8f, draw.a, 0.001f);
    }

    @Test
    void skipsInvisibleSprites() {
        Sprite hidden = new Sprite("hidden.png");
        hidden.visible = false;

        Scene scene = new Scene("test");
        scene.add(new Node("ghost").add(new Transform2D(1f, 1f)).add(hidden));

        List<ShapeDrawCommand> draws = SceneShapeRenderer.collectDrawCommands(scene);

        assertEquals(0, draws.size());
    }

    @Test
    void skipsNodesMissingSpriteOrTransform() {
        Scene scene = new Scene("test");
        scene.add(new Node("no-sprite").add(new Transform2D(0f, 0f)));
        scene.add(new Node("no-transform").add(new Sprite("only.png")));

        List<ShapeDrawCommand> draws = SceneShapeRenderer.collectDrawCommands(scene);

        assertEquals(0, draws.size());
    }

    @Test
    void walksChildrenInTreeOrder() {
        Sprite rootSprite = new Sprite("root.png");
        rootSprite.r = 1f;
        Sprite childSprite = new Sprite("child.png");
        childSprite.r = 0.5f;

        Node root =
                new Node("root")
                        .add(new Transform2D(0f, 0f))
                        .add(rootSprite)
                        .addChild(new Node("child").add(new Transform2D(2f, 0f)).add(childSprite));
        Scene scene = new Scene("test").add(root);

        List<ShapeDrawCommand> draws = SceneShapeRenderer.collectDrawCommands(scene);

        assertEquals(2, draws.size());
        assertEquals(0f, draws.get(0).x, 0.001f);
        assertEquals(1f, draws.get(0).r, 0.001f);
        assertEquals(2f * SceneShapeRenderer.CELL_SIZE, draws.get(1).x, 0.001f);
        assertEquals(0.5f, draws.get(1).r, 0.001f);
    }
}
