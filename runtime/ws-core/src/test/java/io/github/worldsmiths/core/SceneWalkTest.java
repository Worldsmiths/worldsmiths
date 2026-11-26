package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class SceneWalkTest {

    @Test
    void texturedSpriteYieldsCommandWithTextureKeyAndTint() {
        Sprite sprite = new Sprite("hero.png");
        sprite.r = 0.2f;
        sprite.g = 0.4f;
        sprite.b = 0.6f;
        sprite.a = 0.8f;

        Scene scene = new Scene("test");
        scene.add(new Node("head").add(new Transform2D(3f, 5f)).add(sprite));

        List<RectDraw> draws = SceneWalk.collectDrawCommands(scene);

        assertEquals(1, draws.size());
        RectDraw draw = draws.get(0);
        assertEquals("hero.png", draw.textureKey);
        assertEquals(3f * SceneWalk.CELL_SIZE, draw.x, 0.001f);
        assertEquals(5f * SceneWalk.CELL_SIZE, draw.y, 0.001f);
        assertEquals(SceneWalk.CELL_SIZE, draw.width, 0.001f);
        assertEquals(SceneWalk.CELL_SIZE, draw.height, 0.001f);
        assertEquals(0.2f, draw.r, 0.001f);
        assertEquals(0.4f, draw.g, 0.001f);
        assertEquals(0.6f, draw.b, 0.001f);
        assertEquals(0.8f, draw.a, 0.001f);
    }

    @Test
    void nullTextureYieldsNullKeyAndRgbaRect() {
        Sprite sprite = new Sprite(null);
        sprite.r = 0.1f;
        sprite.g = 0.3f;
        sprite.b = 0.5f;
        sprite.a = 0.7f;

        Scene scene = new Scene("test");
        scene.add(new Node("plain").add(new Transform2D(1f, 2f)).add(sprite));

        List<RectDraw> draws = SceneWalk.collectDrawCommands(scene);

        assertEquals(1, draws.size());
        RectDraw draw = draws.get(0);
        assertNull(draw.textureKey);
        assertEquals(1f * SceneWalk.CELL_SIZE, draw.x, 0.001f);
        assertEquals(2f * SceneWalk.CELL_SIZE, draw.y, 0.001f);
        assertEquals(0.1f, draw.r, 0.001f);
        assertEquals(0.3f, draw.g, 0.001f);
        assertEquals(0.5f, draw.b, 0.001f);
        assertEquals(0.7f, draw.a, 0.001f);
    }

    @Test
    void blankTextureYieldsNullKey() {
        Sprite empty = new Sprite("");
        Sprite whitespace = new Sprite("   ");

        Scene scene = new Scene("test");
        scene.add(new Node("empty").add(new Transform2D(0f, 0f)).add(empty));
        scene.add(new Node("ws").add(new Transform2D(1f, 0f)).add(whitespace));

        List<RectDraw> draws = SceneWalk.collectDrawCommands(scene);

        assertEquals(2, draws.size());
        assertNull(draws.get(0).textureKey);
        assertNull(draws.get(1).textureKey);
    }

    @Test
    void skipsInvisibleSprites() {
        Sprite hidden = new Sprite("hidden.png");
        hidden.visible = false;

        Scene scene = new Scene("test");
        scene.add(new Node("ghost").add(new Transform2D(1f, 1f)).add(hidden));

        List<RectDraw> draws = SceneWalk.collectDrawCommands(scene);

        assertEquals(0, draws.size());
    }

    @Test
    void skipsNodesMissingSpriteOrTransform() {
        Scene scene = new Scene("test");
        scene.add(new Node("no-sprite").add(new Transform2D(0f, 0f)));
        scene.add(new Node("no-transform").add(new Sprite("only.png")));

        List<RectDraw> draws = SceneWalk.collectDrawCommands(scene);

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

        List<RectDraw> draws = SceneWalk.collectDrawCommands(scene);

        assertEquals(2, draws.size());
        assertEquals("root.png", draws.get(0).textureKey);
        assertEquals(0f, draws.get(0).x, 0.001f);
        assertEquals(1f, draws.get(0).r, 0.001f);
        assertEquals("child.png", draws.get(1).textureKey);
        assertEquals(2f * SceneWalk.CELL_SIZE, draws.get(1).x, 0.001f);
        assertEquals(0.5f, draws.get(1).r, 0.001f);
    }

    @Test
    void emptySceneYieldsEmptyList() {
        Scene scene = new Scene("empty");

        List<RectDraw> draws = SceneWalk.collectDrawCommands(scene);

        assertEquals(0, draws.size());
    }

    @Test
    void cellSizeIsThirtyTwoPixels() {
        assertEquals(32f, SceneWalk.CELL_SIZE, 0.001f);
    }
}
