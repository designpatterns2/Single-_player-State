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
import jglcore.JGL_Time;


/**
 * collision-managed movement processor implementation for sliding objects.
 * 
 * @author Nicolas Devere
 *
 */
public final class Motion_slide implements Motion {
	
	private float step;
	private float minMove;
	private JGL_3DVector pos2;
	private JGL_3DVector ref_v;
	private JGL_3DVector tmp_v;
	private JGL_3DVector crease;
	private JGL_3DVector[] normals;
	private Trace trace;
	private Trace tr;
	private Trace trDown;
	private JGL_3DVector stepStart;
	private JGL_3DVector stepDown;
	private JGL_3DVector stepSeg;
	private JGL_3DVector act_pos;
	private JGL_3DVector old_pos;
	
	
	/**
	 * Constructor
	 */
	public Motion_slide(float stepHeight) {
		
		step = stepHeight;
		minMove = Util4Phys.MIN_MOVE * Util4Phys.MIN_MOVE;
		pos2 = new JGL_3DVector();
		ref_v = new JGL_3DVector();
		tmp_v = new JGL_3DVector();
		crease = new JGL_3DVector();
		trace = new Trace();
		tr = new Trace();
		trDown = new Trace();
		normals = new JGL_3DVector[Util4Phys.MAX_IMPACT];
		for (int i=0; i<normals.length; i++)
			normals[i] = new JGL_3DVector();
		stepStart = new JGL_3DVector();
		stepDown = new JGL_3DVector();
		stepSeg = new JGL_3DVector();
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
		
		boolean is_collision = false;
		boolean near_ground = false;
		float step_speed;
		short normals_index = 0;
		float frac = 1f;
		float path;
		float dot;
		float offset;
		float d1;
		float d2;
		float fReal;
		int i, j;
		
		JGL_3DVector pos = cshape.getPosition();
		act_pos.assign(pos);
		ref_v.assign(mover.getMove());
		tmp_v.assign(ref_v);
		
		// Check the ground
		tr.reset(cshape, pos, pos);
		tr.end.y -= Util4Phys.GROUND_CHECK;
		tr.segment.y -= Util4Phys.GROUND_CHECK;
		tracable.trace(tr);
		if (!tr.dummy && tr.isImpact() && tr.correction.normal.y > Util4Phys.FLOOR_NORMAL)
			near_ground = true;
		
		// Slide loop
		for (short loops=0; loops<Util4Phys.MAX_IMPACT; loops++) {
			
			pos2.x = pos.x + (tmp_v.x * frac);
			pos2.y = pos.y + (tmp_v.y * frac);
			pos2.z = pos.z + (tmp_v.z * frac);
			
			trace.reset(cshape, pos, pos2);
			
			if (trace.segment.norm2()<minMove) {
				old_pos.assign(act_pos);
				return is_collision;
			}
			
			tracable.trace(trace);
			
			mover.impactReaction(trace, tracable);
			path = frac * trace.fractionImpact;
			frac -= path;
			
			if (!trace.dummy) {
				if (trace.isImpact() && Math.abs(trace.correction.normal.y)<0.00005f && !mover.isJumping() && near_ground) {
					tr.reset(trace.cshape, pos, pos);
					tr.end.y += step; tr.segment.y += step;
					tracable.trace(tr);
					if (!tr.isImpact()) {
						tr.reset(trace.cshape, trace.start, trace.end);
						tr.start.y += step; tr.end.y += step;
						if (tr.segment.y<0f) {
							tr.end.y -= tr.segment.y;
							tr.segment.y -= tr.segment.y;
						}
						tracable.trace(tr);
						if (!tr.dummy && (!tr.isImpact() || !trace.correction.eq(tr.correction))) {
							
							stepSeg.x = tr.start.x + (tr.segment.x * tr.fractionImpact);
							stepSeg.y = tr.start.y + (tr.segment.y * tr.fractionImpact);
							stepSeg.z = tr.start.z + (tr.segment.z * tr.fractionImpact);
							trDown.reset(trace.cshape, stepSeg, stepSeg);
							trDown.end.y -= step; trDown.segment.y -= step;
							tracable.trace(trDown);
							stepDown.x = trDown.start.x + (trDown.segment.x * trDown.fractionImpact);
							stepDown.y = trDown.start.y + (trDown.segment.y * trDown.fractionImpact);
							stepDown.z = trDown.start.z + (trDown.segment.z * trDown.fractionImpact);
							
							step_speed = (mover.getSpeed() * JGL_Time.getTimer() * frac * 0.52f);
							if (step_speed > (stepDown.y - trace.start.y))
								step_speed = stepDown.y - trace.start.y;
							if (trace.segment.y<0f)
								step_speed -= trace.segment.y;
							trace.end.y += step_speed;
							trace.segment.y += step_speed;
							
							offset = trace.cshape.getOffset(trace.correction.normal);
							d1 = trace.correction.distance(trace.start) - offset;
							d2 = trace.correction.distance(trace.end) - offset;
							fReal = (d1 + 0.0005f) / (d1 - d2);
							stepStart.x = trace.start.x + (trace.segment.x * fReal);
							stepStart.y = trace.start.y + (trace.segment.y * fReal);
							stepStart.z = trace.start.z + (trace.segment.z * fReal);
							
							tr.reset(trace.cshape, stepStart, trace.end);
							tracable.trace(tr);
							trace.fractionImpact += ((1f - trace.fractionImpact) * tr.fractionImpact);
							trace.fractionReal += ((1f - trace.fractionReal) * tr.fractionReal);
							trace.correction = tr.correction;
							trace.dummy = false;
						}
					}
				}
			}
			else {
				tr.reset(trace.cshape, pos, pos);
				tr.start.y += step; tr.end.y += step;
				if (tr.segment.y<0f) {
					tr.end.y -= tr.segment.y;
					tr.segment.y -= tr.segment.y;
				}
				tracable.trace(tr);
				if (!tr.dummy) {
					stepSeg.x = tr.start.x + (tr.segment.x * tr.fractionImpact);
					stepSeg.y = tr.start.y + (tr.segment.y * tr.fractionImpact);
					stepSeg.z = tr.start.z + (tr.segment.z * tr.fractionImpact);
					trDown.reset(trace.cshape, stepSeg, stepSeg);
					trDown.end.y -= step; trDown.segment.y -= step;
					tracable.trace(trDown);
					stepDown.x = trDown.start.x + (trDown.segment.x * trDown.fractionImpact);
					stepDown.y = trDown.start.y + (trDown.segment.y * trDown.fractionImpact);
					stepDown.z = trDown.start.z + (trDown.segment.z * trDown.fractionImpact);
					
					step_speed = (mover.getSpeed() * JGL_Time.getTimer() * path * 0.52f);
					if (step_speed > (stepDown.y - trace.start.y))
						step_speed = stepDown.y - trace.start.y;
					if (trace.segment.y<0f)
						step_speed -= trace.segment.y;
					
					trace.end.y += step_speed;
					trace.segment.y += step_speed;
					trace.clearImpact();
					tracable.trace(trace);
					trace.dummy = false;
				}
			}
			
			if (trace.dummy) {
				pos.assign(old_pos);
				old_pos.assign(act_pos);
				return true;
			}
			
			pos.x += trace.segment.x * trace.fractionImpact;
			pos.y += trace.segment.y * trace.fractionImpact;
			pos.z += trace.segment.z * trace.fractionImpact;
			
			if (!trace.isImpact()) {
				pos.assign(trace.end);
				old_pos.assign(act_pos);
				return is_collision;
			}
			else
				is_collision = true;
			
			normals[normals_index].assign(trace.correction.normal);
			normals_index++;
			
			for (i=0; i<normals_index; i++) {
				
				dot = JGL_Math.vector_dotProduct(ref_v, normals[i]);
				tmp_v.x = (ref_v.x - (normals[i].x * dot));
				tmp_v.y = (ref_v.y - (normals[i].y * dot));
				tmp_v.z = (ref_v.z - (normals[i].z * dot));
				
				for (j=0; j<normals_index; j++) {
					if (j!=i)
						if (JGL_Math.vector_dotProduct(tmp_v, normals[j]) < 0f)
							break;
				}
				if (j == normals_index)
					break;
			}
			
			if (i == normals_index) {
				if (normals_index == 2) {
					JGL_Math.vector_crossProduct(normals[0], normals[1], crease);
					crease.normalize();
					dot = JGL_Math.vector_dotProduct(crease, tmp_v);
					tmp_v.x = crease.x * dot;
					tmp_v.y = crease.y * dot;
					tmp_v.z = crease.z * dot;
				}
				else {
					old_pos.assign(act_pos);
					return is_collision;
				}
			}
			
			if (JGL_Math.vector_dotProduct(ref_v, tmp_v)<=0f) {
				old_pos.assign(act_pos);
				return is_collision;
			}
		}
		
		pos.assign(old_pos);
		old_pos.assign(act_pos);
		return is_collision;
	}
	
	
	public void simpleSlide() {
		
	}
	
	
	public Trace getTrace() {
		return trace;
	}
	

	public Object clone() {
		return new Motion_slide(step);
	}
	
}
