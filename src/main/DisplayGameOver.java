//	Copyright 2009 Nicolas Devere
//
//	This file is part of FLESH SNATCHER.
//
//	FLESH SNATCHER is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//
//	FLESH SNATCHER is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//
//	You should have received a copy of the GNU General Public License
//	along with FLESH SNATCHER; if not, write to the Free Software
//	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

package main;


import input.LoadHelper;

import java.util.StringTokenizer;

import script.Script;
import struct.Bitmap2D;
import world.World;

import jglcore.JGL;
import jglcore.JGL_3DMatrix;
import jglcore.JGL_3DVector;
import jglcore.JGL_Time;

import com.jme.image.Texture;
import com.jme.input.InputHandler;
import com.jme.input.InputSystem;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import com.jme.system.DisplaySystem;
import com.jmex.font2d.Font2D;
import com.jmex.font2d.Text2D;



/**
 * 
 * @author Nicolas Devere
 *
 */
public class DisplayGameOver {
	
	private static int action;
	
	private static Bitmap2D bloods[];
	
	private static Font2D my2dfont;
	private static Text2D game;
	private static Text2D over;
	private static Text2D restart;
	private static Text2D quit;
	
	private static Text2D texts[];
	private static ColorRGBA selected = new ColorRGBA(1f, 0f, 0f, 1f);
	private static ColorRGBA unselected = new ColorRGBA(1f, 1f, 1f, 1f);
	
	// Camera attributes
	private static JGL_3DMatrix matrix = new JGL_3DMatrix();
	
	private static JGL_3DVector baseLeft = new JGL_3DVector(-1f, 0f, 0f);
	private static JGL_3DVector baseUp = new JGL_3DVector(0f, 1f, 0f);
	private static JGL_3DVector baseDepth = new JGL_3DVector(0f, 0f, -1f);
	
	private static JGL_3DVector left = new JGL_3DVector(-1f, 0f, 0f);
	private static JGL_3DVector up = new JGL_3DVector(0f, 1f, 0f);
	private static JGL_3DVector depth = new JGL_3DVector(0f, 0f, -1f);

	private static Vector3f v_loc = new Vector3f();
	private static Vector3f v_left = new Vector3f();
	private static Vector3f v_up = new Vector3f();
	private static Vector3f v_depth = new Vector3f();
	
	
	public static void init() {
		
		Texture tb = LoadHelper.getTexture("menu_blood.png", true);
		bloods = new Bitmap2D[2];
		bloods[0] = new Bitmap2D(tb, 0.44f, 0.51f, 0.1f, 0.1f, Bitmap2D.ONE_MINUS_SOURCE_ALPHA);
		bloods[1] = new Bitmap2D(tb, 0.44f, 0.41f, 0.1f, 0.1f, Bitmap2D.ONE_MINUS_SOURCE_ALPHA);
		
		float width = DisplaySystem.getDisplaySystem().getRenderer().getWidth();
		float height = DisplaySystem.getDisplaySystem().getRenderer().getHeight();
		
		my2dfont = new Font2D("data/map/textures/font_fears.tga");
		my2dfont.getFontTextureState().getTexture().setApply(Texture.ApplyMode.Combine);
		
		game = my2dfont.createText("game", 10f, 0);
		game.setLocalTranslation(new Vector3f(width * 0.35f, height * 0.7f, 0f));
		game.setLocalScale(height / 200f);
		game.updateGeometricState( 0.0f, true );
		game.updateRenderState();
		
		over = my2dfont.createText("over", 10f, 0);
		over.setLocalTranslation(new Vector3f(width * 0.52f, height * 0.7f, 0f));
		over.setLocalScale(height / 200f);
		over.updateGeometricState( 0.0f, true );
		over.updateRenderState();
		
		restart = my2dfont.createText("restart", 10f, 0);
		restart.setLocalTranslation(new Vector3f(width * 0.47f, height * 0.55f, 0f));
		restart.setLocalScale(height / 600f);
		restart.updateGeometricState( 0.0f, true );
		restart.updateRenderState();
		
		quit = my2dfont.createText("quit", 10f, 0);
		quit.setLocalTranslation(new Vector3f(width * 0.47f, height * 0.45f, 0f));
		quit.setLocalScale(height / 600f);
		quit.updateGeometricState( 0.0f, true );
		quit.updateRenderState();
		
		texts = new Text2D[2];
		texts[0] = restart;
		texts[1] = quit;
	}
	
	
	
	public static int display(Node rootNode, LightState lightState, 
								Camera camera, InputHandler input) {
		
		rootNode.detachAllChildren();
		rootNode.attachChild(game);
		rootNode.attachChild(over);
		rootNode.attachChild(restart);
		rootNode.attachChild(quit);
		lightState.detachAll();
        rootNode.updateGeometricState( 0.0f, true );
        rootNode.updateRenderState();
        
        Script.execute(new StringTokenizer("stopmusic"));
		Script.execute(new StringTokenizer("playmusic data/map/music/vers_les_dieux.ogg 1"));
        
        JGL_Time.reset();
        
        KeyBindingManager.getKeyBindingManager().removeAll();
        input = new DummyHandler();
		KeyBindingManager.getKeyBindingManager().set( "action", KeyInput.KEY_RETURN );
		KeyBindingManager.getKeyBindingManager().set( "up", KeyInput.KEY_UP );
		KeyBindingManager.getKeyBindingManager().set( "down", KeyInput.KEY_DOWN );
		
		Renderer r = DisplaySystem.getDisplaySystem().getRenderer();
		
        camera.setFrustumPerspective( Player.FRUSTUM_NORMAL, (float) DisplaySystem.getDisplaySystem().getWidth()
                / (float) DisplaySystem.getDisplaySystem().getHeight(), 1f, 3000f );
		
		boolean game = false;
		boolean intro = false;
		while (!game && !intro) {
			
			InputSystem.update();
			
			/** Recalculate the framerate. */
	    	JGL_Time.update();
	        /** Update tpf to time per frame according to the Timer. */
	        float tpf = JGL_Time.getTimePerFrame();
	        
	        input.update( tpf );
	        
	        // World update
			World.map.update();
	        
	        /** Clears the previously rendered information. */
	        r.clearBuffers();
	        
			// Camera update
			JGL_3DVector pos = Player.entity.getPosition();
			JGL_3DVector or = Player.entity.getOrientation();
			
			matrix.identity();
			matrix.rotate(or.x, or.y, or.z, JGL.YXZ);
			
			left.x = (matrix.m11*baseLeft.x) + (matrix.m12*baseLeft.y) + (matrix.m13*baseLeft.z);
			left.y = (matrix.m21*baseLeft.x) + (matrix.m22*baseLeft.y) + (matrix.m23*baseLeft.z);
			left.z = (matrix.m31*baseLeft.x) + (matrix.m32*baseLeft.y) + (matrix.m33*baseLeft.z);
			
			up.x = (matrix.m11*baseUp.x) + (matrix.m12*baseUp.y) + (matrix.m13*baseUp.z);
			up.y = (matrix.m21*baseUp.x) + (matrix.m22*baseUp.y) + (matrix.m23*baseUp.z);
			up.z = (matrix.m31*baseUp.x) + (matrix.m32*baseUp.y) + (matrix.m33*baseUp.z);
			
			depth.x = (matrix.m11*baseDepth.x) + (matrix.m12*baseDepth.y) + (matrix.m13*baseDepth.z);
			depth.y = (matrix.m21*baseDepth.x) + (matrix.m22*baseDepth.y) + (matrix.m23*baseDepth.z);
			depth.z = (matrix.m31*baseDepth.x) + (matrix.m32*baseDepth.y) + (matrix.m33*baseDepth.z);
			
			v_loc.set(pos.x, pos.y, pos.z);
			v_left.set(left.x, left.y, left.z);
			v_up.set(up.x, up.y, up.z);
			v_depth.set(depth.x, depth.y, depth.z);
			camera.setFrame(v_loc, v_left, v_up, v_depth);
			camera.update();
	        
			World.map.render(Player.entity.getPosition());
	        
			v_loc.set(0f, 0f, 0f);
			v_left.set(baseLeft.x, baseLeft.y, baseLeft.z);
			v_up.set(baseUp.x, baseUp.y, baseUp.z);
			v_depth.set(baseDepth.x, baseDepth.y, baseDepth.z);
			camera.setFrame(v_loc, v_left, v_up, v_depth);
			camera.update();
			/** Update controllers/render states/transforms/bounds for rootNode. */
            //root.updateGeometricState(tpf, true);
            
            //AudioSystem.getSystem().update();
	        
	        /** Draw the rootNode and all its children. */
			for (int i=0; i<texts.length; i++)
	        	texts[i].setTextColor(unselected);
	        texts[action].setTextColor(selected);
	        r.draw(rootNode);
	        bloods[action].render();
	        r.displayBackBuffer();
			
			
	        if ( KeyBindingManager.getKeyBindingManager().isValidCommand("up", false ) ) {
	        	action--;
	        	if (action<0)
	        		action = 1;
	        }
	        
	        if ( KeyBindingManager.getKeyBindingManager().isValidCommand("down", false ) ) {
	        	action++;
	        	if (action>1)
	        		action = 0;
	        }
	        
			if ( KeyBindingManager.getKeyBindingManager().isValidCommand("action", false ) ) {
				if (action==0)
					game = true;
				if (action==1)
					intro = true;
			}
		}
		
		Script.execute(new StringTokenizer("stopmusic"));
		KeyBindingManager.getKeyBindingManager().removeAll();
		
		if (game) {
			World.map.applyCheckpoint();
			return Main.INGAME;
		}
		
		return Main.INTRO;
	}
	
	
}
