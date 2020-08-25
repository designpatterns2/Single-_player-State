package states;

import com.jme.input.InputHandler;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import main.DisplayGameOver;
import main.Main;

public class StateGameOver implements State {
    @Override
    public void display(StateContext context, Node rootNode, LightState lightState, Camera camera, InputHandler input) {
        int state = DisplayGameOver.display(rootNode, lightState, camera, input);
        switch (state) {
            case Main.INGAME:
                context.changeState(new StateInGame());
                break;
            case Main.INTRO:
                context.changeState(new StateIntro());
                break;
        }
    }
}
