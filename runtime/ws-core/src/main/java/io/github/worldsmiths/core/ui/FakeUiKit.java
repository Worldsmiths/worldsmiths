package io.github.worldsmiths.core.ui;

import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recording {@link UiKit} for headless tests. Leaf Kind recording plus sparse Draw2D/Font paint when
 * drawing through fakes.
 */
public final class FakeUiKit implements UiKit {

  private final List<Command> commands = new ArrayList<Command>();
  private final Map<String, Float> measureSeeds = new HashMap<String, Float>();
  private final FakeUiMeasure measure = new FakeUiMeasure();
  private final UiSession session;

  public FakeUiKit() {
    session =
        new UiSession(
            measure,
            new UiRecorder() {
              @Override
              public void onCreate(String id, UiLeafType type) {
                commands.add(Command.create(id, type));
              }

              @Override
              public void onLayout(String id, float x, float y, float w, float h) {
                commands.add(Command.layout(id, x, y, w, h));
              }

              @Override
              public void onHit(String id, boolean activated) {
                commands.add(Command.hit(id, activated));
              }

              @Override
              public void onFocus(String id) {
                commands.add(Command.focus(id));
              }

              @Override
              public void onActivate(String id) {
                commands.add(Command.activate(id));
              }

              @Override
              public void onDispose(String id) {
                commands.add(Command.dispose(id));
              }

              @Override
              public void onDraw(UiLeafType type, String id) {
                commands.add(Command.draw(type, id));
              }
            });
  }

  /**
   * Seeds horizontal text advance for {@code text} on the toolkit layout path (Fake-only).
   *
   * @throws EngineException if {@code text} is already seeded
   */
  public void seedMeasureAdvance(String text, float advance) {
    if (text == null) {
      throw new IllegalArgumentException("text must be non-null");
    }
  if (measureSeeds.containsKey(text)) {
      throw new EngineException("measure already seeded for: " + text);
    }
    measureSeeds.put(text, advance);
  }

  @Override
  public void setTheme(Theme theme) {
    session.setTheme(theme);
  }

  @Override
  public void setViewport(int width, int height, Insets insets, float uiScale) {
    session.setViewport(width, height, insets, uiScale);
  }

  @Override
  public void clear() {
    session.clear();
  }

  @Override
  public Button button(String id, String label) {
    return session.button(id, label);
  }

  @Override
  public Label label(String id, String text) {
    return session.label(id, text);
  }

  @Override
  public Panel panel(String id) {
    return session.panel(id);
  }

  @Override
  public TraySlot traySlot(String id) {
    return session.traySlot(id);
  }

  @Override
  public Swatch swatch(String id) {
    return session.swatch(id);
  }

  @Override
  public ModalOverlay modalOverlay(String id) {
    return session.modalOverlay(id);
  }

  @Override
  public FocusScope focusScope() {
    return session.focusScope();
  }

  @Override
  public void layout() {
    session.layout();
  }

  @Override
  public UiHit hit(float x, float y) {
    return session.hit(x, y);
  }

  @Override
  public void draw(Draw2D draw2d, Font font) {
    session.draw(draw2d, font);
  }

  /** Append-only interaction log. */
  public List<Command> commands() {
    return Collections.unmodifiableList(commands);
  }

  /** Clears the recording log only (not a full session reset). */
  public void clearCommands() {
    commands.clear();
  }

  /** Immutable Fake recording entry. */
  public static final class Command {
    public enum Kind {
      CREATE,
      LAYOUT,
      HIT,
      FOCUS,
      ACTIVATE,
      DISPOSE,
      DRAW_BUTTON,
      DRAW_LABEL,
      DRAW_PANEL,
      DRAW_TRAY_SLOT,
      DRAW_SWATCH,
      DRAW_MODAL
    }

    public final Kind kind;
    public final String id;
    public final UiLeafType leafType;
    public final float x;
    public final float y;
    public final float width;
    public final float height;
    public final boolean activated;

    private Command(
        Kind kind,
        String id,
        UiLeafType leafType,
        float x,
        float y,
        float width,
        float height,
        boolean activated) {
      this.kind = kind;
      this.id = id;
      this.leafType = leafType;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.activated = activated;
    }

    static Command create(String id, UiLeafType type) {
      return new Command(Kind.CREATE, id, type, Float.NaN, Float.NaN, Float.NaN, Float.NaN, false);
    }

    static Command layout(String id, float x, float y, float w, float h) {
      return new Command(Kind.LAYOUT, id, null, x, y, w, h, false);
    }

    static Command hit(String id, boolean activated) {
      return new Command(Kind.HIT, id, null, Float.NaN, Float.NaN, Float.NaN, Float.NaN, activated);
    }

    static Command focus(String id) {
      return new Command(Kind.FOCUS, id, null, Float.NaN, Float.NaN, Float.NaN, Float.NaN, false);
    }

    static Command activate(String id) {
      return new Command(Kind.ACTIVATE, id, null, Float.NaN, Float.NaN, Float.NaN, Float.NaN, false);
    }

    static Command dispose(String id) {
      return new Command(Kind.DISPOSE, id, null, Float.NaN, Float.NaN, Float.NaN, Float.NaN, false);
    }

    static Command draw(UiLeafType type, String id) {
      Kind kind;
      switch (type) {
        case BUTTON:
          kind = Kind.DRAW_BUTTON;
          break;
        case LABEL:
          kind = Kind.DRAW_LABEL;
          break;
        case PANEL:
          kind = Kind.DRAW_PANEL;
          break;
        case TRAY_SLOT:
          kind = Kind.DRAW_TRAY_SLOT;
          break;
        case SWATCH:
          kind = Kind.DRAW_SWATCH;
          break;
        case MODAL:
          kind = Kind.DRAW_MODAL;
          break;
        default:
          kind = Kind.DRAW_BUTTON;
          break;
      }
      return new Command(kind, id, type, Float.NaN, Float.NaN, Float.NaN, Float.NaN, false);
    }
  }

  private final class FakeUiMeasure implements UiMeasure {

    @Override
    public float advance(String text) {
      if (text == null || text.isEmpty()) {
        return 0f;
      }
      Float seeded = measureSeeds.get(text);
      if (seeded == null) {
        throw new EngineException("unseeded measure for text: " + text);
      }
      return seeded;
    }

    @Override
    public float lineHeight() {
      Float seeded = measureSeeds.get("\u0000lineHeight");
      return seeded == null ? DefaultUiMeasure.LINE_HEIGHT : seeded;
    }
  }
}