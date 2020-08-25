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

package entity;

import jglcore.JGL_3DVector;
import jglcore.JGL_Math;
import jglcore.JGL_Time;
import phys.Motion;
import phys.Mover;
import phys.Shape;
import phys.Trace;
import phys.Util4Phys;
import world.World;

import com.jme.scene.Node;
import com.jme.system.DisplaySystem;
import com.jmex.model.animation.KeyframeController;


/**
 * 
 * @author Nicolas Devere
 *
 */
public class Factory implements Entity, Scriptable {
	
	private String name;
	private int state;
	private int team;
	
	private Mover cmover;
	private Motion cmotion;
	private Shape cshape;
	private JGL_3DVector angles;
	
	private Node objNode;
	
	private Node hitNode;
	private boolean isHit;
	private static long hitLapse = 40l;
	
	private Explosion expNode;
	
	private Explosion genNode;
	
	private Node curNode;
	private KeyframeController kc;
	private float kspeed;
	private int nbFrames;
	private float t;
	private int minKey;
	private int maxKey;
	private int length;
	
	private Entity proto;
	
	private Entity[] ennemies;
	private int ennemyCursor;
	private int ennemiesNb;
	
	private float oLife;
	private float life;
	private float sp;
	private long pSpeed;
	private long cumulTimeGen;
	private long cumulTimeHit;
	
	private ScriptBox scriptbox;
	
	
	public Factory(	String id, int team, float posX, float posY, float posZ, 
					float life, float speed, long productionSpeed, int ennemyNumber, 
					Node node, Node hit, Explosion explode, 
					Explosion generateNode, Entity prototype, 
					Shape shape, Mover mover, Motion motion) {
		
		name = id;
		state = ACTIVE;
		this.team = team;
		
		cmover = mover;
		cmover.setSpeed(speed);
		cmotion = motion;
		cshape = shape;
		cshape.setPosition(new JGL_3DVector(posX, posY, posZ));
		angles = new JGL_3DVector();
		
		oLife = this.life = life;
		sp = speed;
		pSpeed = productionSpeed;
		
		objNode = node;
		objNode.setLocalTranslation(posX, posY, posZ);
		
		hitNode = hit;
		hitNode.setLocalTranslation(posX, posY, posZ);
		isHit = false;
		
		expNode = explode;
		
		genNode = generateNode;
		
		curNode = objNode;
		
		proto = prototype;
		
		ennemiesNb = ennemyNumber;
		ennemies = new Entity[ennemyNumber];
		for (int i=0; i<ennemyNumber; i++) {
			ennemies[i] = (Entity)prototype.clone();
			float s = ennemies[i].getMover().getSpeed();
			ennemies[i].setSpeed(s - (s * (0.3f - (0.3f*JGL_Math.rnd()) )));
		}
		
		ennemyCursor = 0;
	}
	
	

	private void setCurrentNode(Node n) {
		curNode = n;
		if (n.getChild(0).getControllerCount()>0) {
			kc = (KeyframeController)n.getChild(0).getController(0);
			kspeed = kc.getSpeed();
			nbFrames = kc.keyframes.size();
		}
		else {
			kc = null;
			kspeed = 0f;
			nbFrames = 1;
		}
		setAnimationFrames(0, nbFrames - 1);
	}
	
	
	/**
	 * Sets the min and max keyframes for the animation to play.
	 * 
	 * @param minKeyframe : the min keyframe
	 * @param maxKeyframe : the max keyframe
	 */
	public void setAnimationFrames(int minKeyframe, int maxKeyframe) {
		if (maxKeyframe<minKeyframe) return;
		if (minKeyframe<0) minKeyframe = 0;
		if (maxKeyframe<0) maxKeyframe = 0;
		if (minKeyframe>nbFrames - 1) minKeyframe = nbFrames - 1;
		if (maxKeyframe>nbFrames - 1) maxKeyframe = nbFrames - 1;
		
		minKey = minKeyframe;
		maxKey = maxKeyframe;
		length = (maxKey - minKey);
		
		t = minKey;
	
	}
	
	
	public void reset() {
		state = ACTIVE;
		life = oLife;
		setCurrentNode(objNode);
		isHit = false;
		cmover.setSpeed(sp);
		for (int i=0; i<ennemiesNb; i++) {
			ennemies[i] = (Entity)proto.clone();
			float s = ennemies[i].getMover().getSpeed();
			ennemies[i].setSpeed(s - (s * (0.3f - (0.3f*JGL_Math.rnd()) )));
		}
		if (scriptbox!=null)
			scriptbox.reset();
	}
	
	
	@Override
	public Shape getCShape() {
		// TODO Auto-generated method stub
		return cshape;
	}

	@Override
	public Motion getCollider() {
		// TODO Auto-generated method stub
		return cmotion;
	}

	@Override
	public float getDamage() {
		// TODO Auto-generated method stub
		return 0f;
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public float getLife() {
		// TODO Auto-generated method stub
		return life;
	}

	@Override
	public Mover getMover() {
		// TODO Auto-generated method stub
		return cmover;
	}

	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return curNode;
	}

	@Override
	public JGL_3DVector getOrientation() {
		// TODO Auto-generated method stub
		return angles;
	}

	@Override
	public JGL_3DVector getPosition() {
		// TODO Auto-generated method stub
		return cshape.getPosition();
	}

	@Override
	public int getTeam() {
		// TODO Auto-generated method stub
		return team;
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return state == ACTIVE;
	}

	@Override
	public boolean isCollidable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isDead() {
		// TODO Auto-generated method stub
		return state == DEAD;
	}

	@Override
	public boolean isDying() {
		// TODO Auto-generated method stub
		return state == DYING;
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		if (kc!=null)
			kc.setCurTime(t);
		curNode.setLocalTranslation(getPosition().x, getPosition().y, getPosition().z);
		curNode.updateGeometricState(0f, true);
		DisplaySystem.getDisplaySystem().getRenderer().draw(curNode);
	}

	@Override
	public void setActive() {
		// TODO Auto-generated method stub
		state = ACTIVE;
	}

	@Override
	public void setCShape(Shape arg) {
		// TODO Auto-generated method stub
		cshape = arg;
	}

	@Override
	public void setCollidable(boolean arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCollider(Motion arg) {
		// TODO Auto-generated method stub
		cmotion = arg;
	}

	@Override
	public void setDamage(float arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDead() {
		// TODO Auto-generated method stub
		state = DEAD;
		cmover.setSpeed(0f);
		expNode.reset(getPosition().x, getPosition().y, getPosition().z);
		World.map.addObject(expNode);
	}

	@Override
	public void setDying() {
		// TODO Auto-generated method stub
		state = DYING;
		isHit = false;
	}

	@Override
	public void setLife(float arg) {
		// TODO Auto-generated method stub
		life = arg;
	}

	@Override
	public void setMover(Mover arg) {
		// TODO Auto-generated method stub
		cmover = arg;
	}

	@Override
	public void setSpeed(float arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTeam(int arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void synchronizeNode() {
		// TODO Auto-generated method stub
		if (length>0) {
			t += JGL_Time.getTimePerFrame() * kspeed;
			while (t >= maxKey)
				t -= length;
		}
	}

	@Override
	public boolean touchReact(Entity entity, Trace trace) {
		// TODO Auto-generated method stub
		if (team!=entity.getTeam() && entity.getDamage()>0f) {
			isHit = true;
			setCurrentNode(hitNode);
			cumulTimeHit = 0l;
			life -= entity.getDamage();
		}
		if (life<=0f)
			setDying();
		return true;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		if (isActive()) {
			cumulTimeGen += (long)(JGL_Time.getTimePerFrame() * 1000f);
			if ( (cumulTimeGen)>pSpeed) {
				ennemies[ennemyCursor].getPosition().assign(getPosition().x, getPosition().y + ennemies[ennemyCursor].getCShape().getOffset(Util4Phys.up.normal) + Util4Phys.MIN_DISTANCE, getPosition().z);
				ennemies[ennemyCursor].setTeam(team);
				boolean spawnOk = true;
				for (int i=0; i<World.map.characters.size() && spawnOk; i++)
					spawnOk = !(ennemies[ennemyCursor].getCShape().isIn(((Entity)World.map.characters.get(i)).getCShape()));
				if (spawnOk) {
					World.map.addCharacter(ennemies[ennemyCursor]);
					genNode.reset(ennemies[ennemyCursor].getPosition().x, ennemies[ennemyCursor].getPosition().y, ennemies[ennemyCursor].getPosition().z);
					World.map.addObject(genNode);
					ennemyCursor++;
					cumulTimeGen = 0l;
					if (ennemyCursor>=ennemies.length)
						setDying();
				}
			}
			if (isHit) {
				if (cumulTimeHit>hitLapse) {
					curNode = objNode;
					isHit = false;
				}
				cumulTimeHit += (long)(JGL_Time.getTimePerFrame() * 1000f);
			}
		}
		else if (isDying())
			setDead();
		
		cmover.update();
	}
	
	public Object clone() {
	        return new Factory(name, getTeam(), getPosition().x, getPosition().y, getPosition().z, life, sp, 
	        		pSpeed, ennemies.length, objNode, hitNode, 
	        		(Explosion)expNode.clone(), (Explosion)genNode.clone(), proto, 
	        		(Shape)cshape.clone(), (Mover)cmover.clone(), (Motion)cmotion.clone());
	}
	
	public void storeScriptBox(ScriptBox arg) {
		scriptbox = arg;
	}
	
	public void executeScripts() {
		if (scriptbox!=null)
			scriptbox.executeScripts();
	}

}
