package states;

import com.jme.input.InputHandler;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;

public interface State {
    void display(StateContext context, Node rootNode, LightState lightState,
                 Camera camera, InputHandler input);
}
