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

package world;

import java.util.Vector;
import phys.Bsp_tree;
import phys.Shape_aabb;
import phys.Shape_sphere;
import phys.Trace;
import jglcore.JGL_Math;
import jglcore.JGL_3DMesh;
import jglcore.JGL_3DVector;


/**
 * BSP area that can be insert in a height-map land.
 * 
 * @author Nicolas Devere
 *
 */
public final class CollisionBSP implements CollisionNode {
	
	private static Shape_sphere eyeSphere = new Shape_sphere(new JGL_3DVector(), 0.05f);
	private static Trace testTrace = new Trace();
	
	private String name;
	private Bsp_tree bsp_phys;
	private Shape_aabb[] viewShapes;
	private CollisionNode pvs[];
	
	private boolean ingame;
	
	
	/**
	 * Constructs a BSP area.
	 * 
	 * @param _meshes : the 3D meshes
	 * @param _pos : the area position
	 * @param _aabb : the area AABB
	 * @param map : the height-map land which contains the BSP area
	 */
	public CollisionBSP(String id, Vector _meshes, Vector _viewShapes, JGL_3DVector _pos) {
		
		name = id;
		
		int i, j;
		
		viewShapes = new Shape_aabb[_viewShapes.size()];
		for (i=0; i<_viewShapes.size(); i++)
			viewShapes[i] = (Shape_aabb)_viewShapes.get(i);
		
		JGL_3DMesh m;
		JGL_3DVector v;
		JGL_3DMesh[] brushes = new JGL_3DMesh[_meshes.size()];
		
		for (i=0; i<brushes.length; i++) {
			m = (JGL_3DMesh)_meshes.get(i);
			for (j=0; j<m.getPoints().size(); j++) {
				v = (JGL_3DVector)m.getPoints().get(j);
				JGL_Math.vector_add(v, _pos, v);
			}
			
			brushes[i] = m;
		}
		bsp_phys = new Bsp_tree(brushes);
		
		pvs = new CollisionNode[0];
		
		ingame = false;
	}
	
	
	public String getName() {
		return name;
	}
	
	
	/**
	 * Returns the BSP in this area.
	 * 
	 * @return the BSP in this area
	 */
	public Bsp_tree getBsp() {
		return bsp_phys;
	}
	
	
	
	
	public Shape_aabb[] getViewShapes() {
		return viewShapes;
	}
	
	
	public void setInGame(boolean arg) {
		ingame = arg;
	}
	
	
	public void addPvs(CollisionNode arg) {
		CollisionNode newStruct[] = new CollisionNode[pvs.length + 1];
		for (int i=0; i<pvs.length; i++)
			newStruct[i] = pvs[i];
		newStruct[pvs.length] = arg;
		pvs = newStruct;
	}
	
	
	/**
	 * Traces the shape across the BSP area.
	 * 
	 * @param trace : the trace result
	 * @return if an impact occurs nearer than the trace's stored one
	 */
	public boolean collideRecursive(Trace trace) {
		
		boolean result = collideSimple(trace, true);
		for (int i=0; i<pvs.length; i++)
			result |= pvs[i].collideSimple(trace, false);
		return result;
	}
	
	
	/**
	 * Traces the shape across the BSP area.
	 * 
	 * @param trace : the trace result
	 * @return if an impact occurs nearer than the trace's stored one
	 */
	public boolean collideSimple(Trace trace, boolean test) {
		
		if (!ingame)
			return false;
		
		if (trace.dummy)
			return false;
		
		if (!test) test = isIn(trace);
		if (!test) test = isCrossing(trace);
		
		if (test)
			return bsp_phys.trace(trace);
		return false;
	}
	
	
	/**
	 * Returns if the specified segment  
	 * intersects the map (height-map + BSP's).
	 * 
	 * @param p1 : segment start point
	 * @param p2 : segment end point
	 * @return if the segment intersects the map
	 */
	public boolean intersect(JGL_3DVector p1, JGL_3DVector p2) {
		
		eyeSphere.setPosition(p1);
		testTrace.reset(eyeSphere, p1, p2);
		
		boolean test = isIn(testTrace);
		if (!test) test = isCrossing(testTrace);
		
		if (test) {
			testTrace.clearImpact();
			bsp_phys.trace(testTrace);
			return testTrace.isImpact();
		}
		return false;
	}
	
	
	
	public boolean isIn(Trace trace) {
		
		boolean test = false;
		for (int i=0; i<viewShapes.length && !test; i++)
			test |= viewShapes[i].isIn(trace.cshape);
		return test;
	}
	
	
	public boolean isCrossing(Trace trace) {
		
		boolean test = false;
		testTrace.reset(trace.cshape, trace.start, trace.end);
		for (int i=0; i<viewShapes.length && !test; i++) {
			viewShapes[i].trace(testTrace);
			test |= testTrace.isImpact();
			testTrace.clearImpact();
		}
		return test;
	}
	
}
