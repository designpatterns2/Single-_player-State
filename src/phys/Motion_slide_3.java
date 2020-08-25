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

package phys;

import jglcore.JGL_3DVector;
import jglcore.JGL_Math;


public final class Motion_slide_3 implements Motion {

	//private static float MIN_MOVE_2;
	
	private float step;
	private JGL_3DVector pos2;
	private JGL_3DVector prim_v;
	private JGL_3DVector end_v;
	private JGL_3DVector endClip_v;
	private JGL_3DVector clip_v;
	private JGL_3DVector tmp_v;
	private JGL_3DVector move;
	private JGL_3DVector crease;
	private JGL_3DVector[] normals;
	private Trace trace;
	//private Trace tr;
	//private JGL_3DVector stepTest;
	private JGL_3DVector act_pos;
	private JGL_3DVector old_pos;
	
	
	/**
	 * Constructor
	 */
	public Motion_slide_3(float stepHeight) {
		
		//MIN_MOVE_2 = Util4Phys.MIN_MOVE * Util4Phys.MIN_MOVE;
		
		step = stepHeight;
		pos2 = new JGL_3DVector();
		prim_v = new JGL_3DVector();
		end_v = new JGL_3DVector();
		endClip_v = new JGL_3DVector();
		clip_v = new JGL_3DVector();
		tmp_v = new JGL_3DVector();
		move = new JGL_3DVector();
		crease = new JGL_3DVector();
		trace = new Trace();
		//tr = new Trace();
		normals = new JGL_3DVector[Util4Phys.MAX_IMPACT];
		for (int i=0; i<normals.length; i++)
			normals[i] = new JGL_3DVector();
		//stepTest = new JGL_3DVector();
		act_pos = new JGL_3DVector();
		old_pos = new JGL_3DVector();
	}
	
	
	
	/**
	 * Slides the specified bounding shape, according to the specified mover, 
	 * against the specified tracable object.
	 * 
	 * @param cshape : the collision shape to slide
	 * @param mover : the mover object
	 * @param tracable : the object to slide against
	 * @return if a collision occurs
	 */
	public boolean process(Shape cshape, Mover mover, Tracable tracable) {
		
		short loops = 0;
		short normals_index = 0;
		float frac = 1f;
		float path;
		float into;
		float dot;
		int i, j, k;
		
		JGL_3DVector pos = cshape.getPosition();
		act_pos.assign(pos);
		
		prim_v.assign(mover.getMove());
		end_v.assign(mover.getMove());
		tmp_v.assign(mover.getMove());
		
		for (loops=0; loops<Util4Phys.MAX_IMPACT; loops++) {
			
			pos2.x = pos.x + (tmp_v.x * frac);
			pos2.y = pos.y + (tmp_v.y * frac);
			pos2.z = pos.z + (tmp_v.z * frac);
			
			trace.reset(cshape, pos, pos2);
			tracable.trace(trace);

			if (trace.dummy) {
				pos.assign(old_pos);
				old_pos.assign(act_pos);
				System.out.println("DUMMY !!");
				return true;
			}
			
			mover.impactReaction(trace, tracable);
			
			path = frac * trace.fractionImpact;
			frac -= path;
			move.x = trace.segment.x * trace.fractionImpact;
			move.y = trace.segment.y * trace.fractionImpact;
			move.z = trace.segment.z * trace.fractionImpact;
			
			pos.x += move.x;
			pos.y += move.y;
			pos.z += move.z;
			
			if (trace.fractionImpact>=1f)
				break;
			
			if (normals_index>=Util4Phys.MAX_IMPACT) {
				old_pos.assign(act_pos);
				return true;
			}
			
			//
			// if this is the same plane we hit before, nudge velocity
			// out along it, which fixes some epsilon issues with
			// non-axial planes
			//
			/*for ( i = 0; i < normals_index; i++ ) {
				if ( JGL_Math.vector_dotProduct(trace.correction.normal, normals[i]) > 0.999f ) {
					tmp_v.x += trace.correction.normal.x;
					tmp_v.y += trace.correction.normal.y;
					tmp_v.z += trace.correction.normal.z;
					break;
				}
			}
			if ( i < normals_index ) {
				continue;
			}*/
			normals[normals_index] = trace.correction.normal;
			normals_index++;
			
			
			for (i=0; i<normals_index; i++) {
				
				into = JGL_Math.vector_dotProduct(tmp_v, normals[i]);
				if (into>=0.1f)
					continue;
				
				clip_v.assign(tmp_v);
				JGL_Math.vector_projectOnPlane(clip_v, normals[i], Util4Phys.PLANE_OVERCLIP, clip_v);
				
				endClip_v.assign(end_v);
				JGL_Math.vector_projectOnPlane(endClip_v, normals[i], Util4Phys.PLANE_OVERCLIP, endClip_v);
				
				// see if there is a second plane that the new move enters
				for (j=0; j<normals_index; j++) {
					
					if (j==i)
						continue;
					
					if (JGL_Math.vector_dotProduct(clip_v, normals[j])>=0.1f)
						continue;
					
					// try clipping the move to the plane
					JGL_Math.vector_projectOnPlane(clip_v, normals[j], Util4Phys.PLANE_OVERCLIP, clip_v);
					JGL_Math.vector_projectOnPlane(endClip_v, normals[j], Util4Phys.PLANE_OVERCLIP, endClip_v);
					
					if (JGL_Math.vector_dotProduct(clip_v, normals[i])>=0f) {
						continue;
					}
					
					// slide the original velocity along the crease
					JGL_Math.vector_crossProduct(normals[i], normals[j], crease);
					crease.normalize();
					dot = JGL_Math.vector_dotProduct(crease, tmp_v);
					clip_v.x = crease.x * dot;
					clip_v.y = crease.y * dot;
					clip_v.z = crease.z * dot;
					
					dot = JGL_Math.vector_dotProduct(crease, end_v);
					endClip_v.x = crease.x * dot;
					endClip_v.y = crease.y * dot;
					endClip_v.z = crease.z * dot;
					
					// see if there is a third plane the the new move enters
					for ( k = 0; k < normals_index; k++ ) {
						if ( k == i || k == j )
							continue;
						
						if (JGL_Math.vector_dotProduct(clip_v, normals[k])>=0.1f)
							continue;		// move doesn't interact with the plane
						
						// stop dead at a tripple plane interaction
						tmp_v.assign(0f, 0f, 0f);
						return true;
					}
				}
				
				tmp_v.assign(clip_v);
				end_v.assign(endClip_v);
				break;
			}
			//tmp_v.assign(clip_v);
			//end_v.assign(endClip_v);
			
			if (JGL_Math.vector_dotProduct(prim_v, tmp_v)<=0f) {
				old_pos.assign(act_pos);
				return true;
			}
		}
		
		//System.out.println("MAX IMPACT !");
		old_pos.assign(act_pos);
		return loops == 0;
	}
	
	
	public Trace getTrace() {
		return trace;
	}
	

	public Object clone() {
		return new Motion_slide_3(step);
	}

}
