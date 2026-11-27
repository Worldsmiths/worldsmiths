package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;
import io.github.worldsmiths.core.PadButton;

/**
 * Polls the first connected {@link Controller} via gdx-controllers. Digital face/bumper/d-pad/start
 * only — L2/R2 are axes on many desktop pads and are intentionally never read as digital.
 */
final class ControllersGamepadSource implements GamepadSource {

    @Override
    public boolean isPresent() {
        return firstController() != null;
    }

    @Override
    public boolean isButtonDown(PadButton button) {
        if (button == null) {
            return false;
        }
        Controller controller = firstController();
        if (controller == null) {
            return false;
        }
        ControllerMapping mapping = controller.getMapping();
        if (mapping == null) {
            return false;
        }
        int code = mappingCode(button, mapping);
        return code != ControllerMapping.UNDEFINED && controller.getButton(code);
    }

    private static Controller firstController() {
        try {
            Array<Controller> controllers = Controllers.getControllers();
            if (controllers == null || controllers.size == 0) {
                return null;
            }
            return controllers.first();
        } catch (Throwable ignored) {
            // No Controllers manager on classpath / init failed — soft-absent.
            return null;
        }
    }

    /**
     * Maps engine {@link PadButton} onto {@link ControllerMapping} fields. No L2/R2 digital path.
     */
    private static int mappingCode(PadButton button, ControllerMapping mapping) {
        switch (button) {
            case A:
                return mapping.buttonA;
            case B:
                return mapping.buttonB;
            case X:
                return mapping.buttonX;
            case Y:
                return mapping.buttonY;
            case START:
                return mapping.buttonStart;
            case SELECT:
                return mapping.buttonBack;
            case L1:
                return mapping.buttonL1;
            case R1:
                return mapping.buttonR1;
            case DPAD_UP:
                return mapping.buttonDpadUp;
            case DPAD_DOWN:
                return mapping.buttonDpadDown;
            case DPAD_LEFT:
                return mapping.buttonDpadLeft;
            case DPAD_RIGHT:
                return mapping.buttonDpadRight;
            default:
                return ControllerMapping.UNDEFINED;
        }
    }
}
