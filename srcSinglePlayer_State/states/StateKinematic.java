package states;

import com.jme.input.InputHandler;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import main.DisplayKinematic;
import main.Main;

public class StateKinematic implements State {
    @Override
    public void display(StateContext context, Node rootNode, LightState lightState, Camera camera, InputHandler input) {
        int state = DisplayKinematic.display(rootNode, lightState, camera, input);
        switch (state) {
            case Main.INGAME:
                context.changeState(new StateInGame());
        }
    }
}
