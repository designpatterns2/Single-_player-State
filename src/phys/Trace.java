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

import jglcore.JGL_3DBsp;
import jglcore.JGL_3DTriangle;
import jglcore.JGL_3DVector;
import jglcore.JGL_3DPlane;


/**
 * Stores a 3D segment and some impact data on this segment.
 * 
 * @author Nicolas Devere
 *
 */
public final class Trace {
	
	/** The collision shape */
	public Shape cshape;
	
	/** The trace start point */
	public JGL_3DVector start;
	
	/** The trace end point */
	public JGL_3DVector end;
	
	/** The trace segment */
	public JGL_3DVector segment;
	
	/** The trajectory correction. */
	public JGL_3DPlane correction;
	
	/** The impact fraction of the full distance. */
	public float fractionImpact;
	
	/** The true intersection fraction of the full distance */
	public float fractionReal;
	
	/** State of the trace */
	public boolean dummy;


	public static short VOLUME_PRECISION 	= 0;
	public static short FACE_PRECISION		= 1;
	private static short s_precision;

	// trace method
	private static Trace s_impact;
	private static Trace s_impact_t;

	// triangleTrace method
	private static JGL_3DVector s_intersect;

	// nodeSphereIntersection method
	private static JGL_3DVector s_impactPoint_sav;

	static {
		s_precision = VOLUME_PRECISION;

		s_impact = new Trace();
		s_impact_t = new Trace();

		s_intersect = new JGL_3DVector();
		s_impactPoint_sav = new JGL_3DVector();
	}
	
	
	/**
	 * Constructs a new trace with no impact.
	 */
	public Trace() {
		cshape = null;
		start = new JGL_3DVector();
		end = new JGL_3DVector();
		segment = new JGL_3DVector();
		clearImpact();
	}
	
	
	/**
	 * Resets the trace as if there's no impact.
	 * the <code>isImpact()</code> method will return <code>false</code>.
	 * 
	 * @param _cshape : the collision shape
	 * @param _start : the start trace segment point
	 * @param _end : the end trace segment point
	 */
	public void reset(Shape _cshape, JGL_3DVector _start, JGL_3DVector _end) {
		cshape = _cshape;
		start.assign(_start);
		end.assign(_end);
		segment.x = end.x - start.x;
		segment.y = end.y - start.y;
		segment.z = end.z - start.z;
		clearImpact();
	}
	
	
	/**
	 * Stores the specified impact data.
	 *  
	 * @param _correction : the impact plane to correct the segment
	 * @param fraction_impact : the impact fraction compared to the full segment
	 * @param fraction_real : the real intersection fraction compared to the full segment
	 */
	public void setImpact(JGL_3DPlane _correction, float fraction_impact, float fraction_real) {
		correction = _correction;
		fractionImpact = fraction_impact;
		fractionReal = fraction_real;
	}
	
	
	/**
	 * Stores the specified impact data, only if the specified real fraction 
	 * is lesser than the object's one.
	 * 
	 * @param _correction : the impact plane to correct the segment
	 * @param fraction_impact : the impact fraction compared to the full segment
	 * @param fraction_real : the real intersection fraction compared to the full segment
	 * @return if the impact is stored or not
	 */
	public boolean setNearerImpact(JGL_3DPlane _correction, float fraction_impact, float fraction_real) {
		
		if(fraction_real < fractionReal) {
			correction = _correction;
			fractionReal = fraction_real;
			fractionImpact = fraction_impact;
			return true;
		}
		return false;
	}
	
	
	/**
	 * Stores the specified impact data, only if the specified real fraction 
	 * is bigger than the object's one.
	 * 
	 * @param _correction : the impact plane to correct the segment
	 * @param fraction_impact : the impact fraction compared to the full segment
	 * @param fraction_real : the real intersection fraction compared to the full segment
	 * @return if the impact is stored or not
	 */
	public boolean setFarerImpact(JGL_3DPlane _correction, float fraction_impact, float fraction_real) {
		
		if (isImpact()) {
			if(fraction_real > fractionReal) {
				correction = _correction;
				fractionReal = fraction_real;
				fractionImpact = fraction_impact;
				return true;
			}
			return false;
		}
		else {
			correction = _correction;
			fractionReal = fraction_real;
			fractionImpact = fraction_impact;
			return true;
		}
	}
	
	
	/**
	 * Clears the trace as if there's no impact.
	 */
	public void clearImpact() {
		correction = null;
		fractionImpact = fractionReal = 1f;
		dummy = false;
	}
	
	
	/**
	 * Returns if the trace stores an impact point.
	 * 
	 * @return if the trace stores an impact point
	 */
	public boolean isImpact() {
		return fractionReal < 1f;
	}


	/**
	 * Tests the intersection between a collision shape and a BSP,
	 * and stores the result in the specified trace.
	 *
	 * @param bsp : the BSP
	 * @param convex : if the BSP is convex or not
	 * @return if an intersection occurs with the BSP
	 */
	public boolean trace(JGL_3DBsp bsp, boolean convex) {

		s_impact.reset(this.cshape, this.start, this.end);

		if (s_precision == VOLUME_PRECISION) {
			s_impact_t.reset(this.cshape, this.start, this.end);
			if (convex)
				convexTrace(bsp);
			else
				solidTrace(bsp);
		}
		else
			triangleTrace(bsp);

		if (s_impact.dummy)
			this.dummy = true;

		if (s_impact.isImpact())
			return this.setNearerImpact(s_impact.correction, s_impact.fractionImpact, s_impact.fractionReal);
		return false;
	}



	/**
	 * Tests the intersection with the specified BSP.
	 *
	 * @param bsp : the BSP
	 */
	private void solidTrace(JGL_3DBsp bsp) {

		float offset;
		float d1, d2, dDiff;
		float fReal, fImpact;
		JGL_3DPlane vect_i;

		// empty leaf
		if (bsp.type == JGL_3DBsp.EMPTY_LEAF) {
			s_impact_t.clearImpact();
			return;
		}

		// solid leaf
		if (bsp.type == JGL_3DBsp.SOLID_LEAF) {
			if (s_impact_t.isImpact())
				s_impact.setNearerImpact(s_impact_t.correction, s_impact_t.fractionImpact, s_impact_t.fractionReal);
			else
				s_impact.dummy = true;
			s_impact_t.clearImpact();
			return;
		}

		// node

		offset = s_impact_t.cshape.getOffset(bsp.plane.normal);

		d1 = bsp.plane.distance(s_impact.start);
		d2 = bsp.plane.distance(s_impact.end);

		// before the node
		if (d1>offset && d2>offset) {
			solidTrace(bsp.front);
			return;
		}

		bsp.plane.normal.invert();
		float offset2 = s_impact.cshape.getOffset(bsp.plane.normal);
		bsp.plane.normal.invert();

		// behind the node
		if (d1<=-offset2 && d2<=-offset2) {
			solidTrace(bsp.rear);
			return;
		}

		// Crosses the node

		if (d1>offset && d2<=offset) {
			d1 -= offset;
			d2 -= offset;
			dDiff = 1f / (d1 - d2);
			fReal = d1 * dDiff;
			if (fReal>=s_impact.fractionReal)
				return;

			if (fReal<s_impact_t.fractionReal) {
				vect_i = bsp.plane;
				fImpact = (d1 - Util4Phys.MIN_DISTANCE) * dDiff;
				s_impact_t.setImpact(vect_i, fImpact, fReal);
			}
			else {
				vect_i = s_impact_t.correction;
				fReal = s_impact_t.fractionReal;
				fImpact = s_impact_t.fractionImpact;
			}
		}
		else {
			vect_i = s_impact_t.correction;
			fReal = s_impact_t.fractionReal;
			fImpact = s_impact_t.fractionImpact;
		}

		solidTrace(bsp.rear);
		s_impact_t.setImpact(vect_i, fImpact, fReal);
		solidTrace(bsp.front);
	}



	private void convexTrace(JGL_3DBsp bsp) {

		float offset;
		float d1, d2, dDiff;

		// Leaf reached
		if (bsp.type == JGL_3DBsp.SOLID_LEAF) {
			if (s_impact_t.isImpact())
				s_impact.setImpact(s_impact_t.correction, s_impact_t.fractionImpact, s_impact_t.fractionReal);
			else
				s_impact.dummy = true;
			return;
		}

		// node

		offset = s_impact.cshape.getOffset(bsp.plane.normal);

		d1 = bsp.plane.distance(s_impact.start);
		d2 = bsp.plane.distance(s_impact.end);

		// before the node
		if (d1>offset && d2>offset)
			return;

		// Crosses the node
		if (d1>offset && d2<=offset) {
			d1 -= offset;
			d2 -= offset;
			dDiff = 1f / (d1 - d2);
			s_impact_t.setFarerImpact(bsp.plane, (d1 - Util4Phys.MIN_DISTANCE) * dDiff, d1 * dDiff);
		}
		convexTrace(bsp.rear);
	}


	/**
	 * Tests the intersection with the triangles from the specified BSP.
	 *
	 * @param bsp : the BSP
	 */
	private void triangleTrace(JGL_3DBsp bsp) {

		if(bsp.type != JGL_3DBsp.NODE)
			return;

		float offset = s_impact.cshape.getOffset(bsp.plane.normal);

		float d1 = bsp.plane.distance(s_impact.start);
		float d2 = bsp.plane.distance(s_impact.end);

		// before the node
		if (d1>offset && d2>offset)
			triangleTrace(bsp.front);

			// behind the node
		else if (d1<=offset && d2<=offset) {
			triangleTrace(bsp.rear);
			bsp.plane.normal.invert();
			float offset2 = s_impact.cshape.getOffset(bsp.plane.normal);
			bsp.plane.normal.invert();
			if (d1>=-offset2 && d2>=-offset2)
				triangleTrace(bsp.front);
		}

		// crosses the node
		else {

			float nx = bsp.plane.normal.x * offset;
			float ny = bsp.plane.normal.y * offset;
			float nz = bsp.plane.normal.z * offset;

			float ex = s_impact.start.x - nx;
			float ey = s_impact.start.y - ny;
			float ez = s_impact.start.z - nz;

			if (d1 > 0f) {
				d1 -= offset;
				d2 -= offset;
				float d_diff = d1 - d2;
				if(d_diff != 0f) {

					float frac = d1 / d_diff;
					s_intersect.x = ex + (((s_impact.end.x - nx) - ex) * frac);
					s_intersect.y = ey + (((s_impact.end.y - ny) - ey) * frac);
					s_intersect.z = ez + (((s_impact.end.z - nz) - ez) * frac);

					if (nodeSphereIntersection(bsp, s_intersect, offset))
						s_impact.setNearerImpact(bsp.plane, (d1 - Util4Phys.MIN_DISTANCE) / d_diff, frac);
				}
			}

			triangleTrace(bsp.front);
			if(!s_impact.isImpact())
				triangleTrace(bsp.rear);
		}

	}




	/**
	 * Returns if there is intersection between the BSP node triangles and the sphere.
	 *
	 * @param node : the BSP node
	 * @param center : the sphere center
	 * @param offset : the sphere offset
	 * @return if there is intersection between the BSP node triangles and the sphere.
	 */
	private boolean nodeSphereIntersection(JGL_3DBsp node, JGL_3DVector center, float offset) {

		float d;
		short pos;
		JGL_3DTriangle t;
		float sqrOffset = offset * offset;

		for (int i=0; i<node.facesSize; i++) {
			t = (JGL_3DTriangle)node.faces.get(i);
			pos = Util4Phys.triangle_pointPosition(t, center);

			if (pos==0 && node.plane.distance(center)<offset)
				return true;

			else if (pos==1 || pos==2) {
				d = Util4Phys.segmentSphereIntersection(center, offset, t.point1, t.point2, s_impactPoint_sav);
				if (d < sqrOffset)
					return true;
			}

			else if (pos==3 || pos==4) {
				d = Util4Phys.segmentSphereIntersection(center, offset, t.point2, t.point3, s_impactPoint_sav);
				if (d < sqrOffset)
					return true;
			}

			else if (pos==5 || pos==6) {
				d = Util4Phys.segmentSphereIntersection(center, offset, t.point3, t.point1, s_impactPoint_sav);
				if (d < sqrOffset)
					return true;
			}
		}

		return false;
	}
}
