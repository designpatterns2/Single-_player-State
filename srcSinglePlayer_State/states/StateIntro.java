package states;

import com.jme.input.InputHandler;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import main.DisplayIntro;
import main.Main;

public class StateIntro implements State {

    @Override
    public void display(StateContext context, Node rootNode, LightState lightState,
                        Camera camera, InputHandler input) {
        int state = DisplayIntro.display(rootNode, lightState, camera, input);
        switch(state) {
            case Main.DIFFICULTY:
                context.changeState(new StateDifficulty());
                break;
            case Main.LOAD:
                context.changeState(new StateLoad());
                break;
            default:
                context.changeState(new StateFinish());
                break;
        }
    }
}
