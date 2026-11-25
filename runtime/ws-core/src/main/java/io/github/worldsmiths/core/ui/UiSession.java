package io.github.worldsmiths.core.ui;

import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.Font;
import java.util.ArrayList;
import java.util.List;

/** Shared hybrid-leaf session: layout, hit, focus, and draw for Default and Fake kits. */
final class UiSession {

  static final float MIN_HIT = 48f;
  private static final float TRAY_BAND = 72f;
  private static final float ACTION_BAND = 64f;
  private static final float PANEL_BAND = 40f;
  private static final float BUTTON_PAD = 16f;

  private final UiMeasure measure;
  private final UiRecorder recorder;
  private final SessionFocusScope focusScope;

  private Theme theme = Theme.defaults();
  private int viewportWidth;
  private int viewportHeight;
  private Insets insets = Insets.ZERO;
  private float uiScale = 1f;

  private final List<SessionRoot> roots = new ArrayList<SessionRoot>();
  private boolean laidOut;

  UiSession(UiMeasure measure, UiRecorder recorder) {
    this.measure = measure;
    this.recorder = recorder == null ? UiRecorder.NONE : recorder;
    this.focusScope = new SessionFocusScope();
  }

  SessionFocusScope focusScope() {
    return focusScope;
  }

  void setTheme(Theme theme) {
    if (theme != null) {
      this.theme = theme;
    }
  }

  void setViewport(int width, int height, Insets insets, float uiScale) {
    this.viewportWidth = Math.max(0, width);
    this.viewportHeight = Math.max(0, height);
    this.insets = insets == null ? Insets.ZERO : insets;
    this.uiScale = uiScale <= 0f ? 1f : uiScale;
    laidOut = false;
  }

  void clear() {
    for (SessionRoot root : roots) {
      recorder.onDispose(root.id);
    }
    roots.clear();
    focusScope.reset();
    laidOut = false;
  }

  SessionButton button(String id, String label) {
    SessionButton btn = new SessionButton(id, label == null ? "" : label);
    roots.add(btn);
    recorder.onCreate(id, UiLeafType.BUTTON);
    return btn;
  }

  SessionLabel label(String id, String text) {
    SessionLabel leaf = new SessionLabel(id, text == null ? "" : text);
    roots.add(leaf);
    recorder.onCreate(id, UiLeafType.LABEL);
    return leaf;
  }

  SessionPanel panel(String id) {
    SessionPanel panel = new SessionPanel(id);
    roots.add(panel);
    recorder.onCreate(id, UiLeafType.PANEL);
    return panel;
  }

  SessionTraySlot traySlot(String id) {
    SessionTraySlot tray = new SessionTraySlot(id);
    roots.add(tray);
    recorder.onCreate(id, UiLeafType.TRAY_SLOT);
    return tray;
  }

  SessionSwatch swatch(String id) {
    SessionSwatch sw = new SessionSwatch(id);
    roots.add(sw);
    recorder.onCreate(id, UiLeafType.SWATCH);
    return sw;
  }

  SessionModal modalOverlay(String id) {
    SessionModal modal = new SessionModal(id);
    roots.add(modal);
    recorder.onCreate(id, UiLeafType.MODAL);
    return modal;
  }

  void layout() {
    float left = insets.left;
    float top = insets.top;
    float contentW = Math.max(0, viewportWidth - insets.left - insets.right) * uiScale;
    float contentH = Math.max(0, viewportHeight - insets.top - insets.bottom) * uiScale;

    SessionModal modal = null;
    SessionTraySlot tray = null;
    SessionButton actionBtn = null;
    SessionPanel panel = null;
    SessionSwatch swatch = null;
  List<SessionLabel> labels = new ArrayList<SessionLabel>();

    for (SessionRoot root : roots) {
      if (root instanceof SessionModal) {
        modal = (SessionModal) root;
      } else if (root instanceof SessionTraySlot) {
        tray = (SessionTraySlot) root;
      } else if (root instanceof SessionButton) {
        actionBtn = (SessionButton) root;
      } else if (root instanceof SessionPanel) {
        panel = (SessionPanel) root;
      } else if (root instanceof SessionSwatch) {
        swatch = (SessionSwatch) root;
      } else if (root instanceof SessionLabel) {
        labels.add((SessionLabel) root);
      }
    }

    float yPanel = top;
    if (panel != null) {
      float h = PANEL_BAND * uiScale;
      panel.bounds = new UiBounds(left, yPanel, contentW, h);
      recorder.onLayout(panel.id, panel.bounds.x, panel.bounds.y, panel.bounds.width, panel.bounds.height);
      for (SessionLabel child : panel.labels) {
        float cw = Math.max(MIN_HIT, measure.advance(child.text) + BUTTON_PAD * uiScale);
        child.bounds =
            new UiBounds(
                panel.bounds.x + BUTTON_PAD,
                panel.bounds.y + (h - measure.lineHeight() * uiScale) * 0.5f,
                cw,
                Math.max(MIN_HIT, measure.lineHeight() * uiScale));
        recorder.onLayout(
            child.id, child.bounds.x, child.bounds.y, child.bounds.width, child.bounds.height);
      }
      yPanel += h;
    }

    float trayTop = top + contentH - TRAY_BAND * uiScale;
    if (tray != null) {
      tray.bounds = new UiBounds(left, trayTop, contentW, TRAY_BAND * uiScale);
      recorder.onLayout(tray.id, tray.bounds.x, tray.bounds.y, tray.bounds.width, tray.bounds.height);
      int count = tray.slotCount;
      if (count > 0) {
        float slotW = contentW / count;
        if (slotW < MIN_HIT) {
          slotW = MIN_HIT;
        }
        for (int i = 0; i < count; i++) {
          if (i >= tray.slots.size()) {
            break;
          }
          SessionButton slot = tray.slots.get(i);
          if (slot == null) {
            continue;
          }
          float sx = left + i * slotW;
          float sw = Math.max(MIN_HIT, slotW);
          float textW = measure.advance(slot.labelText) + BUTTON_PAD * uiScale;
          float bw = Math.max(MIN_HIT, Math.max(sw, textW));
          float bh = Math.max(MIN_HIT, TRAY_BAND * uiScale - 8f * uiScale);
          slot.bounds = new UiBounds(sx + (slotW - bw) * 0.5f, trayTop + 4f * uiScale, bw, bh);
          recorder.onLayout(
              slot.id, slot.bounds.x, slot.bounds.y, slot.bounds.width, slot.bounds.height);
        }
      }
    }

    float actionY = tray != null ? trayTop - ACTION_BAND * uiScale : top + contentH - ACTION_BAND * uiScale;
    if (actionBtn != null) {
      float textW = measure.advance(actionBtn.labelText) + BUTTON_PAD * 2f * uiScale;
      float bw = Math.max(MIN_HIT, textW);
      float bh = Math.max(MIN_HIT, ACTION_BAND * uiScale - 8f * uiScale);
      actionBtn.bounds = new UiBounds(left + 8f * uiScale, actionY + 4f * uiScale, bw, bh);
      recorder.onLayout(
          actionBtn.id,
          actionBtn.bounds.x,
          actionBtn.bounds.y,
          actionBtn.bounds.width,
          actionBtn.bounds.height);
    }

    if (swatch != null) {
      float size = Math.max(MIN_HIT, 32f * uiScale);
      swatch.bounds = new UiBounds(left + contentW - size - 8f, yPanel + 4f, size, size);
      recorder.onLayout(
          swatch.id, swatch.bounds.x, swatch.bounds.y, swatch.bounds.width, swatch.bounds.height);
    }

    for (SessionLabel free : labels) {
      float cw = Math.max(MIN_HIT, measure.advance(free.text) + BUTTON_PAD * uiScale);
      free.bounds =
          new UiBounds(
              left + BUTTON_PAD,
              yPanel + BUTTON_PAD,
              cw,
              Math.max(MIN_HIT, measure.lineHeight() * uiScale));
      recorder.onLayout(
          free.id, free.bounds.x, free.bounds.y, free.bounds.width, free.bounds.height);
    }

    if (modal != null) {
      float modalW = Math.min(contentW * 0.85f, 300f * uiScale);
      float modalH = Math.min(contentH * 0.45f, 220f * uiScale);
      modalW = Math.max(modalW, MIN_HIT);
      modalH = Math.max(modalH, MIN_HIT);
      float mx = left + (contentW - modalW) * 0.5f;
      float my = top + (contentH - modalH) * 0.5f;
      modal.bounds = new UiBounds(mx, my, modalW, modalH);
      recorder.onLayout(
          modal.id, modal.bounds.x, modal.bounds.y, modal.bounds.width, modal.bounds.height);
      if (modal.dismiss != null) {
        float dw = Math.max(MIN_HIT, measure.advance(modal.dismiss.labelText) + BUTTON_PAD * 2f * uiScale);
        float dh = Math.max(MIN_HIT, 48f * uiScale);
        modal.dismiss.bounds =
            new UiBounds(
                mx + (modalW - dw) * 0.5f,
                my + modalH - dh - 12f * uiScale,
                dw,
                dh);
        recorder.onLayout(
            modal.dismiss.id,
            modal.dismiss.bounds.x,
            modal.dismiss.bounds.y,
            modal.dismiss.bounds.width,
            modal.dismiss.bounds.height);
      }
    }

    laidOut = true;
  }

  UiHit hit(float x, float y) {
    if (!laidOut) {
      layout();
    }
    List<HitTarget> targets = collectHitTargets();
    for (int i = targets.size() - 1; i >= 0; i--) {
      HitTarget target = targets.get(i);
      UiBounds hitBounds = target.bounds.expandToMinSize(MIN_HIT);
      if (hitBounds.contains(x, y)) {
        boolean activated = target.activatable && target.enabled;
        recorder.onHit(target.id, activated);
        return new UiHit(target.id, activated);
      }
    }
    recorder.onHit(null, false);
    return UiHit.none();
  }

  void draw(Draw2D draw2d, Font font) {
    if (!laidOut) {
      layout();
    }
    if (draw2d == null || font == null) {
      return;
    }
    draw2d.setOrtho2D(0f, 0f, viewportWidth, viewportHeight);
    float flip = viewportHeight;

    for (SessionRoot root : roots) {
      if (root instanceof SessionModal) {
        drawModal(draw2d, font, (SessionModal) root, flip);
      }
    }
    for (SessionRoot root : roots) {
      if (root instanceof SessionPanel) {
        drawPanel(draw2d, font, (SessionPanel) root, flip);
      } else if (root instanceof SessionTraySlot) {
        drawTray(draw2d, font, (SessionTraySlot) root, flip);
      } else if (root instanceof SessionButton) {
        drawButton(draw2d, font, (SessionButton) root, flip, isFocused((SessionButton) root));
      } else if (root instanceof SessionSwatch) {
        drawSwatch(draw2d, (SessionSwatch) root, flip);
      } else if (root instanceof SessionLabel) {
        drawLabel(draw2d, font, (SessionLabel) root, flip);
      }
    }
  }

  private boolean isFocused(SessionButton button) {
    return button.id.equals(focusScope.focusedId());
  }

  private void drawModal(Draw2D draw2d, Font font, SessionModal modal, float flipH) {
    if (modal.bounds == null) {
      return;
    }
    recorder.onDraw(UiLeafType.MODAL, modal.id);
    float x = modal.bounds.x;
    float y = toDrawY(modal.bounds, flipH);
    draw2d.setColor(theme.panelR, theme.panelG, theme.panelB, theme.panelA);
    UiGlyphs.beginFilled(draw2d);
    UiGlyphs.roundedPanel(draw2d, x, y, modal.bounds.width, modal.bounds.height, 8f);
    draw2d.end();
    if (modal.dismiss != null && modal.dismiss.bounds != null) {
      drawButton(draw2d, font, modal.dismiss, flipH, isFocused(modal.dismiss));
    }
  }

  private void drawPanel(Draw2D draw2d, Font font, SessionPanel panel, float flipH) {
    if (panel.bounds == null) {
      return;
    }
    recorder.onDraw(UiLeafType.PANEL, panel.id);
    float x = panel.bounds.x;
    float y = toDrawY(panel.bounds, flipH);
    draw2d.setColor(theme.panelR, theme.panelG, theme.panelB, theme.panelA * 0.6f);
    UiGlyphs.beginFilled(draw2d);
    UiGlyphs.roundedPanel(draw2d, x, y, panel.bounds.width, panel.bounds.height, 6f);
    draw2d.end();
    for (SessionLabel child : panel.labels) {
      drawLabel(draw2d, font, child, flipH);
    }
  }

  private void drawTray(Draw2D draw2d, Font font, SessionTraySlot tray, float flipH) {
    if (tray.bounds == null) {
      return;
    }
    recorder.onDraw(UiLeafType.TRAY_SLOT, tray.id);
    for (SessionButton slot : tray.slots) {
      if (slot.bounds != null) {
        drawButton(draw2d, font, slot, flipH, isFocused(slot));
      }
    }
  }

  private void drawButton(
      Draw2D draw2d, Font font, SessionButton button, float flipH, boolean focused) {
    if (button.bounds == null) {
      return;
    }
    recorder.onDraw(UiLeafType.BUTTON, button.id);
    float x = button.bounds.x;
    float y = toDrawY(button.bounds, flipH);
    float alpha = button.enabled ? theme.buttonA : theme.buttonA * 0.4f;
    draw2d.setColor(theme.buttonR, theme.buttonG, theme.buttonB, alpha);
    UiGlyphs.beginFilled(draw2d);
    UiGlyphs.roundedPanel(draw2d, x, y, button.bounds.width, button.bounds.height, 6f);
    draw2d.end();
    if (focused) {
      draw2d.setColor(theme.focusR, theme.focusG, theme.focusB, theme.focusA);
      UiGlyphs.beginLine(draw2d);
      float pad = 2f;
      draw2d.rect(
          x - pad,
          y - pad,
          button.bounds.width + 2f * pad,
          button.bounds.height + 2f * pad);
      draw2d.end();
    }
    float tx = x + BUTTON_PAD * 0.5f;
    float ty = y + button.bounds.height * 0.35f;
    font.drawString(button.labelText, tx, ty);
  }

  private void drawSwatch(Draw2D draw2d, SessionSwatch swatch, float flipH) {
    if (swatch.bounds == null) {
      return;
    }
    recorder.onDraw(UiLeafType.SWATCH, swatch.id);
    float x = swatch.bounds.x;
    float y = toDrawY(swatch.bounds, flipH);
    draw2d.setColor(swatch.r, swatch.g, swatch.b, swatch.a);
    UiGlyphs.beginFilled(draw2d);
    UiGlyphs.swatchFill(draw2d, x, y, swatch.bounds.width);
    draw2d.end();
  }

  private void drawLabel(Draw2D draw2d, Font font, SessionLabel label, float flipH) {
    if (label.bounds == null) {
      return;
    }
    recorder.onDraw(UiLeafType.LABEL, label.id);
    float x = label.bounds.x;
    float y = toDrawY(label.bounds, flipH);
    font.drawString(label.text, x, y + label.bounds.height * 0.2f);
  }

  private static float toDrawY(UiBounds bounds, float viewportHeight) {
    return viewportHeight - bounds.y - bounds.height;
  }

  private List<HitTarget> collectHitTargets() {
    List<HitTarget> out = new ArrayList<HitTarget>();
    for (SessionRoot root : roots) {
      if (root instanceof SessionModal) {
        SessionModal modal = (SessionModal) root;
        if (modal.bounds != null) {
          out.add(new HitTarget(modal.id, modal.bounds, false, false));
          if (modal.dismiss != null && modal.dismiss.bounds != null) {
            out.add(
                new HitTarget(
                    modal.dismiss.id,
                    modal.dismiss.bounds,
                    modal.dismiss.enabled,
                    true));
          }
        }
      } else if (root instanceof SessionTraySlot) {
        for (SessionButton slot : ((SessionTraySlot) root).slots) {
          if (slot.bounds != null) {
            out.add(new HitTarget(slot.id, slot.bounds, slot.enabled, true));
          }
        }
      } else if (root instanceof SessionButton) {
        SessionButton btn = (SessionButton) root;
        if (btn.bounds != null) {
          out.add(new HitTarget(btn.id, btn.bounds, btn.enabled, true));
        }
      }
    }
    return out;
  }

  private static final class HitTarget {
    final String id;
    final UiBounds bounds;
    final boolean enabled;
    final boolean activatable;

    HitTarget(String id, UiBounds bounds, boolean enabled, boolean activatable) {
      this.id = id;
      this.bounds = bounds;
      this.enabled = enabled;
      this.activatable = activatable;
    }
  }

  abstract static class SessionRoot {
    final String id;
    UiBounds bounds;

    SessionRoot(String id) {
      this.id = id;
    }
  }

  final class SessionButton extends SessionRoot implements Button {
    private final String labelText;
    private boolean enabled = true;

    SessionButton(String id, String labelText) {
      super(id);
      this.labelText = labelText;
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public String label() {
      return labelText;
    }

    @Override
    public Button enabled(boolean value) {
      this.enabled = value;
      return this;
    }

    @Override
    public boolean enabled() {
      return enabled;
    }

    @Override
    public UiBounds bounds() {
      return bounds;
    }
  }

  final class SessionLabel extends SessionRoot implements Label {
    private final String text;

    SessionLabel(String id, String text) {
      super(id);
      this.text = text;
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public String text() {
      return text;
    }

    @Override
    public UiBounds bounds() {
      return bounds;
    }
  }

  final class SessionPanel extends SessionRoot implements Panel {
    final List<SessionLabel> labels = new ArrayList<SessionLabel>();

    SessionPanel(String id) {
      super(id);
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public Label label(String labelId, String text) {
      SessionLabel child = new SessionLabel(labelId, text == null ? "" : text);
      labels.add(child);
      recorder.onCreate(labelId, UiLeafType.LABEL);
      return child;
    }

    @Override
    public UiBounds bounds() {
      return bounds;
    }
  }

  final class SessionTraySlot extends SessionRoot implements TraySlot {
    int slotCount;
    final List<SessionButton> slots = new ArrayList<SessionButton>();

    SessionTraySlot(String id) {
      super(id);
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public TraySlot length(int count) {
      this.slotCount = Math.max(0, count);
      return this;
    }

    @Override
    public int length() {
      return slotCount;
    }

    @Override
    public Button slot(int index, String slotId, String label) {
      if (index < 0 || index >= slotCount) {
        throw new IllegalArgumentException("slot index out of range");
      }
      SessionButton btn = new SessionButton(slotId, label == null ? "" : label);
      while (slots.size() <= index) {
        slots.add(null);
      }
      slots.set(index, btn);
      recorder.onCreate(slotId, UiLeafType.BUTTON);
      return btn;
    }

    @Override
    public UiBounds bounds() {
      return bounds;
    }
  }

  final class SessionSwatch extends SessionRoot implements Swatch {
    float r = 0.8f;
    float g = 0.2f;
    float b = 0.2f;
    float a = 1f;

    SessionSwatch(String id) {
      super(id);
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public Swatch color(float r, float g, float b, float a) {
      this.r = r;
      this.g = g;
      this.b = b;
      this.a = a;
      return this;
    }

    @Override
    public UiBounds bounds() {
      return bounds;
    }
  }

  final class SessionModal extends SessionRoot implements ModalOverlay {
    SessionButton dismiss;

    SessionModal(String id) {
      super(id);
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public Button dismiss(String dismissId, String label) {
      dismiss = new SessionButton(dismissId, label == null ? "" : label);
      recorder.onCreate(dismissId, UiLeafType.BUTTON);
      return dismiss;
    }

    @Override
    public UiBounds bounds() {
      return bounds;
    }
  }

  final class SessionFocusScope implements FocusScope {
    private final List<SessionButton> ring = new ArrayList<SessionButton>();
    private int index = -1;

    void reset() {
      ring.clear();
      index = -1;
    }

    @Override
    public void add(Button button) {
      if (button instanceof SessionButton) {
        SessionButton sb = (SessionButton) button;
        if (!ring.contains(sb)) {
          ring.add(sb);
          if (index < 0) {
            index = 0;
            recorder.onFocus(sb.id);
          }
        }
      }
    }

    @Override
    public void focusNext() {
      if (ring.isEmpty()) {
        return;
      }
      index = (index + 1) % ring.size();
      recorder.onFocus(ring.get(index).id);
    }

    @Override
    public void focusPrev() {
      if (ring.isEmpty()) {
        return;
      }
      index = (index - 1 + ring.size()) % ring.size();
      recorder.onFocus(ring.get(index).id);
    }

    @Override
    public void activateFocused() {
      if (ring.isEmpty() || index < 0) {
        return;
      }
      SessionButton focused = ring.get(index);
      if (focused.enabled) {
        recorder.onActivate(focused.id);
      }
    }

    @Override
    public String focusedId() {
      if (ring.isEmpty() || index < 0) {
        return null;
      }
      return ring.get(index).id;
    }
  }
}