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
import jglcore.JGL_Time;
import jglcore.JGL_Math;
import phys.Motion;
import phys.Mover;
import phys.Shape;
import phys.Shape_sphere;
import phys.Trace;
import world.World;

import com.jme.scene.Node;
import com.jme.system.DisplaySystem;
import com.jmex.model.animation.KeyframeController;

import java.util.Vector;


/**
 * 
 * @author Nicolas Devere
 *
 */
public final class Explosible implements Entity {
	
	
	private static Shape_sphere pPoint = new Shape_sphere(new JGL_3DVector(), 0f);
	private static Trace trace = new Trace();
	
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
	private long cumulHitTime;
	private static long hitLapse = 40l;
	
	private Explosion expNode;
	
	private Node curNode;
	private KeyframeController kc;
	private float kspeed;
	private int nbFrames;
	private float t;
	private int minKey;
	private int maxKey;
	private int length;
	
	private float life;
	private float dam;
	private float damOffset2;
	
	
	public Explosible(	String id, int team, float posX, float posY, float posZ, 
						float life, float damage, float damageOffset, 
						Node node, Node hit, Explosion explode, 
						Shape shape, Mover mover, Motion motion) {
		
		name = id;
		state = ACTIVE;
		this.team = team;
		
		cmover = mover;
		cmotion = motion;
		cshape = shape;
		cshape.setPosition(new JGL_3DVector(posX, posY, posZ));
		angles = new JGL_3DVector();
		
		this.life = life;
		dam = damage;
		damOffset2 = damageOffset * damageOffset;
		
		objNode = node;
		objNode.setLocalTranslation(posX, posY, posZ);
		
		hitNode = hit;
		hitNode.setLocalTranslation(posX, posY, posZ);
		isHit = false;
		cumulHitTime = 0l;
		
		expNode = explode;
		curNode = objNode;
		setCurrentNode(objNode);
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
		setActive();
		setCurrentNode(objNode);
		isHit = false;
		cumulHitTime = 0l;
		expNode.reset(getPosition().x, getPosition().y, getPosition().z);
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
		return dam;
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
		dam = arg;
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
		
		Vector v = World.map.characters;
		Entity e;
		for (int i=0; i<v.size(); i++) {
			e = (Entity)v.get(i);
			if (e!=this && JGL_Math.vector_squareDistance(getPosition(), e.getPosition())<damOffset2) {
				trace.reset(pPoint, getPosition(), e.getPosition());
				e.getCShape().trace(trace);
				e.touchReact(this, trace);
			}
		}
		
		v = World.map.objects;
		for (int i=0; i<v.size(); i++) {
			e = (Entity)v.get(i);
			if (e!=this && JGL_Math.vector_squareDistance(getPosition(), e.getPosition())<damOffset2) {
				trace.reset(pPoint, getPosition(), e.getPosition());
				e.getCShape().trace(trace);
				e.touchReact(this, trace);
			}
		}
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
		team = arg;
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
			cumulHitTime = 0l;
			life -= entity.getDamage();
		}
		if (life<=0f && isActive())
			setDying();
		return true;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		if (isHit) {
			if (cumulHitTime > hitLapse) {
				setCurrentNode(objNode);
				isHit = false;
			}
			cumulHitTime += JGL_Time.getTimePerFrame() * 1000f;
		}
		if (isDying())
			setDead();
	}
	
	public Object clone() {
			return new Explosible(name, getTeam(), getPosition().x, getPosition().y, getPosition().z, 
	        					life, dam, (float)Math.sqrt(damOffset2), objNode, hitNode, (Explosion)expNode.clone(), 
	        					(Shape)cshape.clone(), (Mover)cmover.clone(), (Motion)cmotion.clone());
		
	}

}
