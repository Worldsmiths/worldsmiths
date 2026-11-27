package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Binding table + edge sampling over a shared {@link DeviceState}. Used by {@link FakeActions} and
 * the GDX bag ({@code GdxActionDevices} syncs Input/Controllers into the port each frame).
 */
public final class DefaultActions implements Actions {
    static final String UI_FOCUS_NEXT = "ui_focus_next";
    static final String UI_FOCUS_PREV = "ui_focus_prev";
    static final String UI_ACCEPT = "ui_accept";

    private final DeviceState devices;
    private final Map<String, List<Control>> bindings = new HashMap<String, List<Control>>();
    private final Map<String, Boolean> heldLastTick = new HashMap<String, Boolean>();
    private final Set<String> justPressedThisTick = new HashSet<String>();
    private final Set<String> forcedActive = new HashSet<String>();

    public DefaultActions(DeviceState devices) {
        if (devices == null) {
            throw new EngineException("DeviceState must not be null");
        }
        this.devices = devices;
        seedUiDefaults();
    }

    DeviceState devices() {
        return devices;
    }

    private void seedUiDefaults() {
        bind(UI_FOCUS_NEXT, Controls.key(Key.TAB));
        bind(
                UI_FOCUS_PREV,
                Controls.keys(Key.SHIFT_LEFT, Key.TAB),
                Controls.keys(Key.SHIFT_RIGHT, Key.TAB));
        bind(UI_ACCEPT, Controls.key(Key.ENTER), Controls.key(Key.SPACE));
    }

    @Override
    public void bind(String action, Control... controls) {
        requireActionName(action);
        if (controls == null) {
            throw new EngineException("Controls must not be null");
        }
        for (int i = 0; i < controls.length; i++) {
            if (controls[i] == null) {
                throw new EngineException("Control must not be null");
            }
        }
        bindings.put(action, new ArrayList<Control>(Arrays.asList(controls)));
    }

    @Override
    public void load(Files files, String path) {
        if (files == null) {
            throw new EngineException("Files must not be null");
        }
        if (path == null || path.trim().isEmpty()) {
            throw new EngineException("Path must not be null or blank");
        }
        String text = files.readString(path);
        Map<String, List<Control>> loaded = parseBindingTable(text);
        for (Map.Entry<String, List<Control>> entry : loaded.entrySet()) {
            List<Control> controls = entry.getValue();
            bind(entry.getKey(), controls.toArray(new Control[controls.size()]));
        }
    }

    /** Line table: {@code action kind TOKEN}; blank/# ignored. ~40 LOC; keep tiny (HLD Q2). */
    private static Map<String, List<Control>> parseBindingTable(String text) {
        Map<String, List<Control>> loaded = new LinkedHashMap<String, List<Control>>();
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 3) {
                throw new EngineException("Bad binding line: " + line);
            }
            Control control = parseControl(parts[1], parts[2], line);
            List<Control> list = loaded.get(parts[0]);
            if (list == null) {
                list = new ArrayList<Control>();
                loaded.put(parts[0], list);
            }
            list.add(control);
        }
        return loaded;
    }

    private static Control parseControl(String kind, String token, String line) {
        try {
            if ("key".equals(kind)) {
                return Controls.key(Key.valueOf(token));
            }
            if ("pad".equals(kind)) {
                return Controls.gamepadButton(PadButton.valueOf(token));
            }
            if ("pointer".equals(kind)) {
                return Controls.pointerButton(PointerButton.valueOf(token));
            }
        } catch (IllegalArgumentException e) {
            throw new EngineException("Unknown control token in: " + line);
        }
        throw new EngineException("Unknown control kind in: " + line);
    }

    @Override
    public boolean isDown(String action) {
        if (action == null || action.trim().isEmpty()) {
            return false;
        }
        if (forcedActive.contains(action)) {
            return true;
        }
        List<Control> controls = bindings.get(action);
        if (controls == null || controls.isEmpty()) {
            return false;
        }
        for (int i = 0; i < controls.size(); i++) {
            if (controls.get(i).isDown(devices)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean justPressed(String action) {
        if (action == null || action.trim().isEmpty()) {
            return false;
        }
        return justPressedThisTick.contains(action);
    }

    @Override
    public void tick() {
        justPressedThisTick.clear();
        Set<String> names = new HashSet<String>(bindings.keySet());
        names.addAll(forcedActive);
        for (String name : names) {
            boolean down = isDown(name);
            boolean was = Boolean.TRUE.equals(heldLastTick.get(name));
            if (down && !was) {
                justPressedThisTick.add(name);
            }
            heldLastTick.put(name, Boolean.valueOf(down));
        }
    }

    /**
     * Fake-only shortcut: force an action active without actuating a bound control. Dual-device and
     * remap proofs must use the real Control device path, not only this method.
     */
    void setActionActive(String action, boolean active) {
        requireActionName(action);
        if (active) {
            forcedActive.add(action);
        } else {
            forcedActive.remove(action);
        }
    }

    private static void requireActionName(String action) {
        if (action == null || action.trim().isEmpty()) {
            throw new EngineException("Action name must not be null or blank");
        }
    }
}
