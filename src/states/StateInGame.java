package states;

import com.jme.input.InputHandler;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import main.DisplayInGame;
import main.Main;

public class StateInGame implements State {
    @Override
    public void display(StateContext context, Node rootNode, LightState lightState, Camera camera, InputHandler input) {
        int state = DisplayInGame.display(rootNode, lightState, camera, input);
        switch (state) {
            case Main.PAUSE:
                context.changeState(new StatePause());
                break;
            case Main.GAME_OVER:
                context.changeState(new StateGameOver());
                break;
            case Main.LOADING:
                context.changeState(new StateLoading());
                break;
            case Main.KINEMATIC:
                context.changeState(new StateKinematic());
                break;
            default:
                context.changeState(new StateFinish());
                break;
        }
    }
}
