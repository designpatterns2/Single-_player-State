package states;

import com.jme.app.AbstractGame;
import com.jme.input.InputHandler;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;

public class StateContext {

    private State state;
    private Node rootNode;
    private LightState lightState;
    private Camera camera;
    private InputHandler input;
    private AbstractGame game;

    public StateContext(Node rootNode, LightState lightState,
                        Camera camera, InputHandler input, AbstractGame game) {
        this.rootNode = rootNode;
        this.lightState = lightState;
        this.camera = camera;
        this.input = input;
        this.game = game;
        changeState(new StateIntro());
    }

    public void changeState(State state) {
        this.state = state;
        state.display(this, rootNode, lightState, camera, input);
    }

    public void finish() {
        game.finish();
    }
}
