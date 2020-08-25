package states;

import com.jme.input.InputHandler;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import main.DisplayLoad;
import main.Main;

public class StateLoad implements State {
    @Override
    public void display(StateContext context, Node rootNode, LightState lightState, Camera camera, InputHandler input) {
        int state = DisplayLoad.display(rootNode, lightState, camera, input);
        switch (state) {
            case Main.LOADING:
                context.changeState(new StateLoading());
                break;
            case Main.INTRO:
                context.changeState(new StateIntro());
                break;
        }
    }
}
