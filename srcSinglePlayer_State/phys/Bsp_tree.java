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

import jglcore.JGL_3DPlane;
import jglcore.JGL_3DTriangle;
import jglcore.JGL_3DMesh;
import jglcore.JGL_3DBsp;
import jglcore.JGL_Math;
import java.util.Vector;


/**
 * Class representing a brush-based collision BSP.
 * 
 * @author Nicolas Devere
 *
 */
public final class Bsp_tree {
	
	
	private static Trace s_impact = new Trace();
	
	private Bsp_brush[] brushes;
	private Bsp_node root;
	
	
	/**
	 * Constructs a collision BSP with an array of convex brushes.
	 * 
	 * @param _brushes : the array of convex brushes
	 */
	public Bsp_tree(JGL_3DMesh[] _meshes) {
		
		int i;
		int length = _meshes.length;
		
		for (i=0; i<length; i++)
			_meshes[i].markFaces();
		
		
		JGL_3DMesh[] m = new JGL_3DMesh[length];
		Bsp_brush[] b = new Bsp_brush[length];
		
		for (i=0; i<length; i++) {
			b[i] = new Bsp_brush(_meshes[i]);
			m[i] = new JGL_3DMesh();
			b[i].bsp.getMesh(m[i]);
			m[i].markFaces();
		}
		
		brushes = new Bsp_brush[length];
		for (i=0; i<length; i++)
			brushes[i] = new Bsp_brush(m[i]);
		
		JGL_3DBsp bsp1 = new JGL_3DBsp();
		for (i=0; i<length; i++)
			bsp1.addMesh(m[i]);
		
		
		root = new Bsp_node();
		buildNodes(bsp1, root);
		initEmptyLeaves(root, new Vector(), new Vector());
		//initSolidLeaves(root, new Vector());
		
		emptyfy(root);
		
		for (i=0; i<length; i++)
			addBrush(root, brushes[i]);
		
		for (i=0; i<length; i++)
			removeWrongBrushes(root, brushes[i]);
	}
	
	
	public static void initEmptyLeaves(Bsp_node bsp,Vector parentPlanes, Vector parentFaces) {
		
		if (bsp.type==Bsp_node.SOLID_LEAF)
			return;
		
		if (bsp.type==Bsp_node.EMPTY_LEAF) {
			bsp.b_planes = parentPlanes;
			bsp.b_faces = parentFaces;
			return;
		}
		
		parentPlanes.add(bsp.plane);
		parentFaces.add(bsp.faces);
		initEmptyLeaves(bsp.front, parentPlanes, parentFaces);
		initEmptyLeaves(bsp.rear, new Vector(), new Vector());
	}
	
	public static void initSolidLeaves(Bsp_node bsp, Vector parentFaces) {
		
		if (bsp.type==Bsp_node.EMPTY_LEAF)
			return;
		
		if (bsp.type==Bsp_node.SOLID_LEAF) {
			bsp.faces.addAll(parentFaces);
			return;
		}
		
		parentFaces.addAll(bsp.faces);
		initSolidLeaves(bsp.rear, parentFaces);
		initSolidLeaves(bsp.front, new Vector());
	}
	
	
	public static void removeWrongBrushes(Bsp_node bsp, Bsp_brush brush) {
		
		if (bsp.type==Bsp_node.EMPTY_LEAF)
			return;
		
		if (bsp.type==Bsp_node.SOLID_LEAF) {
			boolean toRemove = false;
			for (int i=0; i<bsp.b_faces.size(); i++) {
				Vector subFaces = (Vector)bsp.b_faces.get(i);
				for (int j=0; j<subFaces.size(); j++)
					if (!Util4Phys.isInBrush((JGL_3DTriangle)subFaces.get(j), brush)) {
						toRemove = true;
					}
			}
			if (!toRemove)
				for (int i=0; i<bsp.b_faces.size(); i++) {
					Vector subFaces = (Vector)bsp.b_faces.get(i);
					JGL_3DPlane plane = (JGL_3DPlane)bsp.b_planes.get(i);
					if (Util4Phys.isBrushPlane(plane, brush)) {
						for (int j=0; j<subFaces.size(); j++)
							if (!Util4Phys.isStrictlyInBrush((JGL_3DTriangle)subFaces.get(j), brush)) {
								toRemove = true;
							}
					}
				}
			if (!toRemove)
				for (int i=0; i<bsp.b_faces.size(); i++) {
					Vector subFaces = (Vector)bsp.b_faces.get(i);
					JGL_3DPlane plane = (JGL_3DPlane)bsp.b_planes.get(i);
					if (!Util4Phys.isBrushPlane(plane, brush)) {
						for (int j=0; j<subFaces.size(); j++)
							if (Util4Phys.isBrushEdge((JGL_3DTriangle)subFaces.get(j), brush)) {
								toRemove = true;
							}
					}
				}
			
			if (toRemove)
				bsp.brushes = removeBrush(bsp.brushes, brush);
			if (bsp.brushes.length==0)
				bsp.type = Bsp_node.EMPTY_LEAF;
			return;
		}
		removeWrongBrushes(bsp.rear, brush);
		removeWrongBrushes(bsp.front, brush);
	}
	
	
	public static void buildNodes(JGL_3DBsp bsp1, Bsp_node bsp2) {
		
		if (bsp1.rear != null) {
			if (bsp2.rear == null)
				bsp2.rear = new Bsp_node(Bsp_node.SOLID_LEAF);
			buildNodes(bsp1.rear, bsp2.rear);
		}
		
		if (bsp1.type == JGL_3DBsp.NODE) {
			bsp2.type = Bsp_node.NODE;
			bsp2.plane = bsp1.plane;
			bsp2.faces = bsp1.faces;
		}
		
		if (bsp1.front != null) {
			if (bsp2.front == null)
				bsp2.front = new Bsp_node(Bsp_node.EMPTY_LEAF);
			buildNodes(bsp1.front, bsp2.front);
		}
	}
	
	
	public static void solidify(Bsp_node bsp, Bsp_brush b, JGL_3DMesh m, boolean trapped) {
		
		boolean this_trapped;
		int nb_trapped;
		JGL_3DTriangle t;
		
		if (bsp.type!=Bsp_node.NODE) {
			if (trapped) {
				bsp.type = Bsp_node.SOLID_LEAF;
			}
			return;
		}
		
		this_trapped = false;
		nb_trapped = 0;
		for (int j=0; j<bsp.faces.size(); j++) {
			t = (JGL_3DTriangle)bsp.faces.get(j);
			if (t.owner!=m)
				if (Util4Phys.isInBrush(t, b))
					nb_trapped++;
		}
		if (nb_trapped==bsp.faces.size())
			this_trapped = true;
		
		solidify(bsp.rear, b, m, true);
		
		if (bsp.front.type!=Bsp_node.NODE)
			solidify(bsp.front, b, m, this_trapped);
		else
			solidify(bsp.front, b, m, false);
	}
	
	
	public static void emptyfy(Bsp_node bsp) {
		
		if (bsp.type!=Bsp_node.NODE) {
			if (bsp.brushes.length==0)
				bsp.type = Bsp_node.EMPTY_LEAF;
			return;
		}
		emptyfy(bsp.rear);
		emptyfy(bsp.front);
	}
	
	
	
	public static void addBrush(Bsp_node bsp, Bsp_brush brush) {
		
		//if (bsp.type==Bsp_node.EMPTY_LEAF)
		//	return;
		
		if (bsp.type!=Bsp_node.NODE) {
			bsp.brushes = addBrush(bsp.brushes, brush);
			bsp.type = Bsp_node.SOLID_LEAF;
			return;
		}
		
		int place = JGL_Math.plane_meshPosition(bsp.plane, brush.mesh);
		
		if (place==-1)
			addBrush(bsp.rear, brush);
		else if (place==1)
			addBrush(bsp.front, brush);
		else {
			addBrush(bsp.rear, brush);
			addBrush(bsp.front, brush);
		}
		
	}
	
	
	
	public static void clearAllBrushes(Bsp_node bsp) {
		if (bsp.type!=Bsp_node.NODE) {
			bsp.brushes = new Bsp_brush[0];
			return;
		}
		clearAllBrushes(bsp.rear);
		clearAllBrushes(bsp.front);
	}
	
	
	
	private static Bsp_brush[] addBrush(Bsp_brush[] _brushes, Bsp_brush _brush) {
		for (int i=0; i<_brushes.length; i++)
			if (_brushes[i]==_brush)
				return _brushes;
		Bsp_brush[] newBrushes = new Bsp_brush[_brushes.length+1];
		for (int i=0; i<_brushes.length; i++)
			newBrushes[i] = _brushes[i];
		newBrushes[_brushes.length] = _brush;
		_brushes = newBrushes;
		return _brushes;
	}
	
	
	private static Bsp_brush[] removeBrush(Bsp_brush[] _brushes, Bsp_brush _brush) {
		Vector br = new Vector();
		for (int i=0; i<_brushes.length; i++)
			if (_brushes[i]!=_brush)
				br.add(_brushes[i]);
		_brushes = new Bsp_brush[br.size()];
		_brushes = (Bsp_brush[])br.toArray(_brushes);
		return _brushes;
	}
	
	
	/**
	 * Tests the intersection between a collision shape and the BSP, 
	 * and stores the result in the specified trace.
	 * 
	 * @param trace : describes the shape movement and stores the impact result
	 * @return if an intersection occurs with the BSP
	 */
	public boolean trace(Trace trace) {
		
		s_impact.reset(trace.cshape, trace.start, trace.end);
		
		for (int i=0; i<brushes.length; i++)
			brushes[i].collision_tested = false;
		
		root.traceThroughTree(s_impact, 0f, 1f, 
				s_impact.start.x, s_impact.start.y, s_impact.start.z, 
				s_impact.end.x, s_impact.end.y, s_impact.end.z);
		
		if (s_impact.dummy)
			trace.dummy = true;
		
		if (s_impact.isImpact())
			return trace.setNearerImpact(s_impact.correction, s_impact.fractionImpact, s_impact.fractionReal);
		
		return false;
	}
	
}
