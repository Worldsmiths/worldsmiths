package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Input.Keys;
import io.github.worldsmiths.core.Key;

/** Maps engine {@link Key} values onto LibGDX {@link Keys} codes. */
final class GdxKeys {
    private GdxKeys() {}

    static int code(Key key) {
        if (key == null) {
            return -1;
        }
        switch (key) {
            case LEFT:
                return Keys.LEFT;
            case RIGHT:
                return Keys.RIGHT;
            case UP:
                return Keys.UP;
            case DOWN:
                return Keys.DOWN;
            case A:
                return Keys.A;
            case B:
                return Keys.B;
            case C:
                return Keys.C;
            case D:
                return Keys.D;
            case E:
                return Keys.E;
            case F:
                return Keys.F;
            case G:
                return Keys.G;
            case H:
                return Keys.H;
            case I:
                return Keys.I;
            case J:
                return Keys.J;
            case K:
                return Keys.K;
            case L:
                return Keys.L;
            case M:
                return Keys.M;
            case N:
                return Keys.N;
            case O:
                return Keys.O;
            case P:
                return Keys.P;
            case Q:
                return Keys.Q;
            case R:
                return Keys.R;
            case S:
                return Keys.S;
            case T:
                return Keys.T;
            case U:
                return Keys.U;
            case V:
                return Keys.V;
            case W:
                return Keys.W;
            case X:
                return Keys.X;
            case Y:
                return Keys.Y;
            case Z:
                return Keys.Z;
            case SPACE:
                return Keys.SPACE;
            case ENTER:
                return Keys.ENTER;
            case ESCAPE:
                return Keys.ESCAPE;
            case SHIFT_LEFT:
                return Keys.SHIFT_LEFT;
            case SHIFT_RIGHT:
                return Keys.SHIFT_RIGHT;
            case CONTROL_LEFT:
                return Keys.CONTROL_LEFT;
            case CONTROL_RIGHT:
                return Keys.CONTROL_RIGHT;
            case TAB:
                return Keys.TAB;
            case BACKSPACE:
                return Keys.BACKSPACE;
            case NUM_0:
                return Keys.NUM_0;
            case NUM_1:
                return Keys.NUM_1;
            case NUM_2:
                return Keys.NUM_2;
            case NUM_3:
                return Keys.NUM_3;
            case NUM_4:
                return Keys.NUM_4;
            case NUM_5:
                return Keys.NUM_5;
            case NUM_6:
                return Keys.NUM_6;
            case NUM_7:
                return Keys.NUM_7;
            case NUM_8:
                return Keys.NUM_8;
            case NUM_9:
                return Keys.NUM_9;
            default:
                return -1;
        }
    }
}
