package states;

import com.jme.input.InputHandler;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import main.DisplayLoading;
import main.Main;

public class StateLoading implements State {
    @Override
    public void display(StateContext context, Node rootNode, LightState lightState, Camera camera, InputHandler input) {
        int state = DisplayLoading.display(rootNode, lightState, camera, input);
        switch (state) {
            case Main.INGAME:
                context.changeState(new StateInGame());
                break;
            case Main.GAME_DONE:
                context.changeState(new StateGameDone());
                break;
            default:
                context.changeState(new StateFinish());
                break;
        }
    }
}
