package io.github.worldsmiths.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.DrawMode;
import io.github.worldsmiths.core.EngineException;
import io.github.worldsmiths.core.FakeDraw2D;
import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.FakeUi;
import io.github.worldsmiths.core.Font;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UiKitTest {

  private static void seedAllText(FakeUiKit kit, String... texts) {
    for (String t : texts) {
      kit.seedMeasureAdvance(t, Math.max(UiSession.MIN_HIT, t.length() * 8f));
    }
  }

  private static void assertOnScreen(UiBounds b, int viewportW, int viewportH) {
    assertTrue(b.x >= 0f && b.y >= 0f, "top-left on screen");
    assertTrue(b.x + b.width <= viewportW + 1f, "width on screen");
    assertTrue(b.y + b.height <= viewportH + 1f, "height on screen");
  }

  private static void assertMinHit(UiBounds b) {
    assertTrue(b.width >= UiSession.MIN_HIT - 0.01f);
    assertTrue(b.height >= UiSession.MIN_HIT - 0.01f);
  }

  private static void assertNoOverlap(UiBounds a, UiBounds b) {
    assertFalse(a.overlaps(b), a + " vs " + b);
  }

  private static MatrixTree buildMatrixTree(FakeUiKit kit, int w, int h) {
    kit.setTheme(Theme.defaults());
    kit.setViewport(w, h, Insets.ZERO, 1f);
    seedAllText(kit, "Go", "Slot0", "Slot1", "Slot2", "Slot3", "Slot4", "OK");
    Button go = kit.button("go", "Go");
    TraySlot tray = kit.traySlot("tray").length(5);
    List<Button> slots = new ArrayList<Button>();
    for (int i = 0; i < 5; i++) {
      slots.add(tray.slot(i, "slot-" + i, "Slot" + i));
    }
    ModalOverlay modal = kit.modalOverlay("modal");
    Button dismiss = modal.dismiss("dismiss", "OK");
    kit.focusScope().add(go);
    kit.layout();
    return new MatrixTree(go, slots, dismiss, modal);
  }

  private static final class MatrixTree {
    final Button go;
    final List<Button> slots;
    final Button dismiss;
    final ModalOverlay modal;

    MatrixTree(Button go, List<Button> slots, Button dismiss, ModalOverlay modal) {
      this.go = go;
      this.slots = slots;
      this.dismiss = dismiss;
      this.modal = modal;
    }
  }

  @ParameterizedTest
  @CsvSource({"360,640", "1280,720"})
  void layoutHitMatrixCentersResolveIds(int w, int h) {
    FakeUiKit kit = new FakeUiKit();
    MatrixTree tree = buildMatrixTree(kit, w, h);

    UiBounds goB = tree.go.bounds();
    UiBounds dismissB = tree.dismiss.bounds();
    UiBounds modalB = tree.modal.bounds();
    assertOnScreen(goB, w, h);
    assertOnScreen(dismissB, w, h);
    assertOnScreen(modalB, w, h);
    assertMinHit(goB);
    assertMinHit(dismissB);

    kit.clearCommands();

    UiHit hitGo = kit.hit(goB.centerX(), goB.centerY());
    assertEquals("go", hitGo.id());
    assertTrue(hitGo.activated());
    assertTrue(containsKind(kit.commands(), FakeUiKit.Command.Kind.HIT));

    UiHit hitDismiss = kit.hit(dismissB.centerX(), dismissB.centerY());
    assertEquals("dismiss", hitDismiss.id());
    assertTrue(hitDismiss.activated());
    assertTrue(containsKind(kit.commands(), FakeUiKit.Command.Kind.HIT));

    for (int i = 0; i < tree.slots.size(); i++) {
      Button slot = tree.slots.get(i);
      UiBounds slotB = slot.bounds();
      assertOnScreen(slotB, w, h);
      assertMinHit(slotB);
      assertNoOverlap(goB, slotB);
      assertNoOverlap(modalB, goB);
      assertNoOverlap(modalB, slotB);
      assertNoOverlap(dismissB, goB);
      assertNoOverlap(dismissB, slotB);
      UiHit slotHit = kit.hit(slotB.centerX(), slotB.centerY());
      assertEquals("slot-" + i, slotHit.id());
      assertTrue(slotHit.activated());
      assertTrue(containsKind(kit.commands(), FakeUiKit.Command.Kind.HIT));
    }
  }

  @Test
  void traySlotLengthZeroLayoutAndHitWithoutNpe() {
    FakeUiKit kit = new FakeUiKit();
    kit.setViewport(360, 640, Insets.ZERO, 1f);
    seedAllText(kit, "Go");
    Button go = kit.button("go", "Go");
    kit.traySlot("empty").length(0);
    kit.layout();
    assertNotNull(go.bounds());
    assertEquals("go", kit.hit(go.bounds().centerX(), go.bounds().centerY()).id());
  }

  @Test
  void unseededMeasureFailsClosedOnLayout() {
    FakeUiKit kit = new FakeUiKit();
    kit.setViewport(400, 600, Insets.ZERO, 1f);
    kit.label("lbl", "missing-seed");
    assertThrows(EngineException.class, kit::layout);
  }

  @Test
  void drawThrowsWhenDraw2dOrFontNull() {
    FakeUiKit kit = new FakeUiKit();
    kit.setViewport(400, 600, Insets.ZERO, 1f);
    seedAllText(kit, "Go");
    kit.button("go", "Go");
    kit.layout();

    FakeDraw2D draw2d = new FakeDraw2D();
    FakeUi ui = new FakeUi();
    ui.putFont("fonts/hud.fnt");
    Font font = ui.createFont("fonts/hud.fnt");

    assertThrows(NullPointerException.class, () -> kit.draw(null, font));
    assertThrows(NullPointerException.class, () -> kit.draw(draw2d, null));
  }

  @Test
  void drawFlipsYOnceForDraw2DRects() {
    DefaultUiKit kit = new DefaultUiKit();
    int h = 640;
    kit.setViewport(360, h, Insets.ZERO, 1f);
    Button go = kit.button("go", "Go");
    kit.layout();
    UiBounds b = go.bounds();

    FakeDraw2D draw2d = new FakeDraw2D();
    FakeUi ui = new FakeUi();
    ui.putFont("fonts/hud.fnt");
    Font font = ui.createFont("fonts/hud.fnt");

    kit.draw(draw2d, font);

    float expectedY = h - b.y - b.height;
    boolean sawRect = false;
    DrawMode currentMode = null;
    for (io.github.worldsmiths.core.FakeDraw2D.Command cmd : draw2d.commands()) {
      if (cmd.kind == io.github.worldsmiths.core.FakeDraw2D.Command.Kind.BEGIN) {
        currentMode = cmd.mode;
      } else if (cmd.kind == io.github.worldsmiths.core.FakeDraw2D.Command.Kind.END) {
        currentMode = null;
      } else if (cmd.kind == io.github.worldsmiths.core.FakeDraw2D.Command.Kind.RECT
          && currentMode == DrawMode.FILLED) {
        if (Math.abs(cmd.y1 - expectedY) < 2f) {
          sawRect = true;
        }
      }
    }
    assertTrue(sawRect, "filled rect at Y-flipped coordinate");
  }

  @Test
  void focusNextPrevAndActivateRecordsKinds() {
    FakeUiKit kit = new FakeUiKit();
    kit.setViewport(800, 600, Insets.ZERO, 1f);
    seedAllText(kit, "A", "B");
    Button a = kit.button("a", "A");
    Button b = kit.button("b", "B");
    kit.focusScope().add(a);
    kit.focusScope().add(b);
    kit.layout();

    kit.clearCommands();
    kit.focusScope().focusNext();
    kit.focusScope().activateFocused();
    kit.focusScope().focusPrev();
    kit.focusScope().activateFocused();

    assertTrue(
        containsKind(kit.commands(), FakeUiKit.Command.Kind.FOCUS),
        "FOCUS recorded");
    assertTrue(
        containsKind(kit.commands(), FakeUiKit.Command.Kind.ACTIVATE),
        "ACTIVATE recorded");
  }

  @Test
  void disabledButtonDoesNotRecordActivate() {
    FakeUiKit kit = new FakeUiKit();
    kit.setViewport(400, 600, Insets.ZERO, 1f);
    seedAllText(kit, "Off");
    Button off = kit.button("off", "Off").enabled(false);
    kit.focusScope().add(off);
    kit.layout();
    kit.clearCommands();
    kit.focusScope().activateFocused();
    assertFalse(containsKind(kit.commands(), FakeUiKit.Command.Kind.ACTIVATE));
    UiHit hit = kit.hit(off.bounds().centerX(), off.bounds().centerY());
    assertEquals("off", hit.id());
    assertFalse(hit.activated());
  }

  @Test
  void clearIsIdempotentAndDisposesLeaves() {
    FakeUiKit kit = new FakeUiKit();
    kit.setViewport(400, 600, Insets.ZERO, 1f);
    seedAllText(kit, "X");
    kit.button("x", "X");
    kit.layout();
    kit.clearCommands();
    kit.clear();
    assertTrue(containsKind(kit.commands(), FakeUiKit.Command.Kind.DISPOSE));
    kit.clearCommands();
    kit.clear();
    assertTrue(kit.commands().isEmpty());
  }

  @Test
  void defaultUiKitLayoutAtTwoViewports() {
    for (int[] size : new int[][] {{360, 640}, {1280, 720}}) {
      DefaultUiKit kit = new DefaultUiKit();
      kit.setViewport(size[0], size[1], Insets.ZERO, 1f);
      Button go = kit.button("go", "Go");
      TraySlot tray = kit.traySlot("tray").length(5);
      for (int i = 0; i < 5; i++) {
        tray.slot(i, "s" + i, "S" + i);
      }
      kit.modalOverlay("m").dismiss("d", "OK");
      kit.layout();
      assertMinHit(go.bounds());
      assertOnScreen(go.bounds(), size[0], size[1]);
    }
  }

  @Test
  void fakeDrawEmitsLeafKindsAndSparsePaintMarkers() {
    FakeUiKit kit = new FakeUiKit();
    kit.setViewport(400, 600, Insets.ZERO, 1f);
    seedAllText(kit, "Go");
    kit.button("go", "Go");
    kit.layout();
    kit.clearCommands();

    FakeDraw2D draw2d = new FakeDraw2D();
    FakeUi ui = new FakeUi();
    ui.putFont("fonts/hud.fnt");
    Font font = ui.createFont("fonts/hud.fnt");
    kit.draw(draw2d, font);

    assertTrue(containsKind(kit.commands(), FakeUiKit.Command.Kind.DRAW_BUTTON));
    assertTrue(
        draw2d.commands().stream()
            .anyMatch(c -> c.kind == io.github.worldsmiths.core.FakeDraw2D.Command.Kind.RECT));
    assertTrue(
        ui.commands().stream()
            .anyMatch(c -> c.kind == FakeUi.Command.Kind.DRAW_STRING));
  }

  @Test
  void fakeBagUiKitIsStable() {
    FakeEngineServices services = new FakeEngineServices();
    assertSame(services.uiKit(), services.uiKit());
  }

  private static void assertSame(Object a, Object b) {
    assertTrue(a == b);
  }

  private static boolean containsKind(List<FakeUiKit.Command> commands, FakeUiKit.Command.Kind kind) {
    for (FakeUiKit.Command cmd : commands) {
      if (cmd.kind == kind) {
        return true;
      }
    }
    return false;
  }
}