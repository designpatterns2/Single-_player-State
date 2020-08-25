package states;

import com.jme.input.InputHandler;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import main.DisplaySave;
import main.Main;

public class StateSave implements State {
    @Override
    public void display(StateContext context, Node rootNode, LightState lightState, Camera camera, InputHandler input) {
        int state = DisplaySave.display(rootNode, lightState, camera, input);
        switch (state) {
            case Main.PAUSE:
                context.changeState(new StatePause());
                break;
        }
    }
}
