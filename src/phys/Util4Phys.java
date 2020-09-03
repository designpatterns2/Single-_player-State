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
import jglcore.JGL_3DMesh;
import jglcore.JGL_3DPlane;
import jglcore.JGL_3DTriangle;
import jglcore.JGL_3DVector;
import jglcore.JGL_Math;
import java.util.List;


/**
 * Physics functions and parameters.
 * 
 * @author Nicolas Devere
 *
 */
public final class Util4Phys {
	
	/**
	 * Minimum distance 2 collision-managed objects can have between them.
	 */
	public static float MIN_DISTANCE = 0.0015f;
	
	/**
	 * Plane projection correction.
	 */
	public static float PLANE_OVERCLIP = 1.001f;
	
	/**
	 * Minimum move length a collision-managed object can have.
	 */
	public static float MIN_MOVE = 0.003f;
	
	/**
	 * Maximum impacts for collision management
	 */
	public static short MAX_IMPACT 	= 10;
	
	/**
	 * Plane clipping epsilon
	 */
	public static float CLIP_EPSILON = 0.0005f;
	
	/**
	 * Distance for ground check
	 */
	public static float GROUND_CHECK = 0.003f;
	
	/**
	 * Min Y for a plane to be considered as the floor.
	 */
	public static float FLOOR_NORMAL = 0.7f;
	
	/**
	 * Max Y for a plane to be considered as the ceiling.
	 */
	public static float CEIL_NORMAL = -0.8f;
	
	/** Right-turned 3D plane */
	public static JGL_3DPlane right = new JGL_3DPlane(1f, 0f, 0f);
	
	/** Left-turned 3D plane */
	public static JGL_3DPlane left = new JGL_3DPlane(-1f, 0f, 0f);
	
	/** Far-turned 3D plane */
	public static JGL_3DPlane far = new JGL_3DPlane(0f, 0f, -1f);
	
	/** Near-turned 3D plane */
	public static JGL_3DPlane near = new JGL_3DPlane(0f, 0f, 1f);
	
	/** Down-turned 3D plane */
	public static JGL_3DPlane down = new JGL_3DPlane(0f, -1f, 0f);
	
	/** Up-turned 3D plane */
	public static JGL_3DPlane up = new JGL_3DPlane(0f, 1f, 0f);
	
	
	
	private static Trace s_trace1 = new Trace();
	private static Trace s_trace2 = new Trace();
	private static Trace s_trace3 = new Trace();
	private static Shape_sphere s_sphere = new Shape_sphere(new JGL_3DVector(), 0.0005f);
	
	
	/**
	 * Computes in front of what area of the triangle the point is.<br>
	 * - 0 : in the triangle<br>
	 * - 1 : point 1<br>
	 * - 2 : segment 1 - 2<br>
	 * - 3 : point 2<br>
	 * - 4 : segment 2 - 3<br>
	 * - 5 : point 3<br>
	 * - 6 : segment 3 - 1<br>
	 * 
	 * @param triangle : the triangle
	 * @param point : the point
	 * @return the point position according to the triangle
	 */
	public static short triangle_pointPosition(JGL_3DTriangle triangle, JGL_3DVector point) {
		
		float ux = triangle.point2.x - triangle.point1.x;
		float uy = triangle.point2.y - triangle.point1.y;
		float uz = triangle.point2.z - triangle.point1.z;
		
		float vx = triangle.point3.x - triangle.point1.x;
		float vy = triangle.point3.y - triangle.point1.y;
		float vz = triangle.point3.z - triangle.point1.z;
		
		float uv = (ux * vx) + (uy * vy) + (uz * vz);
		float uu = (ux * ux) + (uy * uy) + (uz * uz);
		float vv = (vx * vx) + (vy * vy) + (vz * vz);
		float ratio = ((uv * uv) - (uu * vv));
		
		if(Math.abs(ratio) < JGL_Math.EPSILON)
			return 1;
		
		float wx = point.x - triangle.point1.x;
		float wy = point.y - triangle.point1.y;
		float wz = point.z - triangle.point1.z;
		
		float wu = (wx * ux) + (wy * uy) + (wz * uz);
		float wv = (wx * vx) + (wy * vy) + (wz * vz);
		
		ratio = 1f / ratio;
		float s = ((uv * wv) - (vv * wu)) * ratio;
		float t = ((uv * wu) - (uu * wv)) * ratio;
		
		if (s<0f) {
			if (t<0f)
				// point 1 : result 1
				return 1;
			
			if (t>1f)
				// point 3 : result 5
				return 5;
			
			// segment 1 - 3 : result 6
			return 6;
		}
		
		if (t<0f) {
			if (s>1f)
				// point 2 : result 3
				return 3;
			
			// segment 1 - 2 : result 2
			return 2;
		}
		
		if ((s+t)>1f) {
			// segment 2 - 3 : result 4
			return 4;
		}
		
		// in front of the triangle : result 0
		return 0;
	}
	
	
	
	/**
	 * Computes the intersection between the sphere and the segment, puts it 
	 * in the <code>result</code> parameter and returns the square distance 
	 * from the sphere center to the segment.
	 * 
	 * @param center : the sphere center
	 * @param offset : the sphere offset
	 * @param a : the segment start point
	 * @param b : the segment end point
	 * @param result : the intersection point (only valid if intersection)
	 * @return the square distance from the sphere center to the segment
	 */
	public static float segmentSphereIntersection(JGL_3DVector center, float offset, 
													JGL_3DVector a, JGL_3DVector b, 
													JGL_3DVector result) {
		float dot, dist2;
		
		float abx = b.x - a.x;
		float aby = b.y - a.y;
		float abz = b.z - a.z;
		
		float acx = center.x - a.x;
		float acy = center.y - a.y;
		float acz = center.z - a.z;
		
		dot = (abx * acx) + (aby * acy) + (abz * acz);
		
		if (dot > 0f) {
			dist2 = (abx * abx) + (aby * aby) + (abz * abz);
			if(dot < dist2) {
				dot /= dist2;
				abx *= dot;
				aby *= dot;
				abz *= dot;
				
				acx -= abx;
				acy -= aby;
				acz -= abz;
				
				result.x = a.x + abx;
				result.y = a.y + aby;
				result.z = a.z + abz;
			}
			else {
				acx -= abx;
				acy -= aby;
				acz -= abz;
				result.assign(b);
			}
		}
		else
			result.assign(a);
		
		return (acx * acx) + (acy * acy) + (acz * acz);
	}
	
	
	
	
	/**
	 * Returns the AABB containing the specified 3D mesh.
	 * 
	 * @param mesh : the 3D mesh
	 * @return the AABB
	 */
	public static Shape_aabb getAABB(JGL_3DMesh mesh, float lapse) {
		
		JGL_3DVector min = new JGL_3DVector(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		JGL_3DVector max = new JGL_3DVector(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		
		JGL_3DVector v;
		for (int i=0; i<mesh.getPoints().size(); i++) {
			v = (JGL_3DVector)mesh.getPoints().get(i);
			
			if (v.x<min.x) min.x = v.x;
			if (v.y<min.y) min.y = v.y;
			if (v.z<min.z) min.z = v.z;
			
			if (v.x>max.x) max.x = v.x;
			if (v.y>max.y) max.y = v.y;
			if (v.z>max.z) max.z = v.z;
		}
		min.x -= lapse; min.y -= lapse; min.z -= lapse;
		max.x += lapse; max.y += lapse; max.z += lapse;
		
		return new Shape_aabb(new JGL_3DVector(), min, max);
	}
	
	
	
	/**
	 * Scans the specified BSP and deletes the faces which 
	 * are inside one of the specified brushes.
	 * 
	 * @param bsp : the bsp to clear
	 * @param brushes : the brushes
	 */
	public static void clearBsp(JGL_3DBsp bsp, Bsp_brush[] brushes) {
		
		if (bsp.type==JGL_3DBsp.NODE) {
			clearTriangles(bsp.faces, brushes);
			if (bsp.faces.isEmpty()) {
				bsp.type = JGL_3DBsp.SOLID_LEAF;
				bsp.rear = bsp.front = null;
			}
		}
		if(bsp.rear != null) clearBsp(bsp.rear, brushes);
		if(bsp.front != null) clearBsp(bsp.front, brushes);
	}
	
	
	
	/**
	 * Scans the specified BSP and deletes the faces which 
	 * are inside one of the specified brushes.
	 * 
	 * @param bsp : the bsp to clear
	 * @param brushes : the brushes
	 */
	public static void clearBsp(Bsp_node bsp, Bsp_brush[] brushes) {
		
		if (bsp.type==Bsp_node.NODE) {
			clearTriangles(bsp.faces, brushes);
			if (bsp.faces.isEmpty()) {
				bsp.type = Bsp_node.SOLID_LEAF;
				bsp.rear = bsp.front = null;
			}
		}
		if(bsp.rear != null) clearBsp(bsp.rear, brushes);
		if(bsp.front != null) clearBsp(bsp.front, brushes);
	}
	
	
	
	
	/**
	 * Scans the specified triangles and deletes those which 
	 * are inside one of the specified brushes.
	 * 
	 * @param triangles : the triangles list to clear
	 * @param brushes : the brushes
	 */
	public static void clearTriangles(List triangles, Bsp_brush[] brushes) {
		int removed = 0;
		//int start = triangles.size();
		for (int i=0; i<triangles.size(); i++)
			if ( isInBrush((JGL_3DTriangle)triangles.get(i), brushes) ) {
				triangles.remove(i);
				i--;
				removed++;
			}
		//System.out.println("Ratio : " + (((float)removed / (float)start) * 100f));
	}
	
	
	/**
	 * Returns if the specified triangle is inside one of the brushes.
	 * 
	 * @param triangle : the triangle to test
	 * @param brushes : the brushes
	 * @return if the triangle is inside one of the brushes
	 */
	public static boolean isInBrush(JGL_3DTriangle triangle, Bsp_brush[] brushes) {
		
		int nbBrushes = 0;
		for (int i=0; i<brushes.length; i++) {
			if (isInBrush(triangle, brushes[i]))
				nbBrushes++;
			if (nbBrushes>=2)
				return true;
		}
		return false;
	}
	
	
	/**
	 * Returns if the specified triangle is inside the brush.
	 * 
	 * @param triangle : the triangle to test
	 * @param brush : the brush
	 * @return if the triangle is inside the brush
	 */
	public static boolean isInBrush(JGL_3DTriangle triangle, Bsp_brush brush) {
		
		s_trace1.reset(s_sphere, triangle.point1, triangle.point1);
		s_trace2.reset(s_sphere, triangle.point2, triangle.point2);
		s_trace3.reset(s_sphere, triangle.point3, triangle.point3);
		Tracer.trace(brush.bsp, false, s_trace1);
		Tracer.trace(brush.bsp, false, s_trace2);
		Tracer.trace(brush.bsp, false, s_trace3);
		if (s_trace1.dummy && s_trace2.dummy && s_trace3.dummy) {
			JGL_3DBsp bsp = brush.bsp;
			while (bsp.type==JGL_3DBsp.NODE) {
				int p = JGL_Math.plane_trianglePosition(bsp.plane, triangle);
				if (p==9)
					return false;
				bsp = bsp.rear;
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * Returns if the specified triangle is inside the brush.
	 * 
	 * @param triangle : the triangle to test
	 * @param brush : the brush
	 * @return if the triangle is inside the brush
	 */
	public static boolean isStrictlyInBrush(JGL_3DTriangle triangle, Bsp_brush brush) {
		
		s_trace1.reset(s_sphere, triangle.point1, triangle.point1);
		s_trace2.reset(s_sphere, triangle.point2, triangle.point2);
		s_trace3.reset(s_sphere, triangle.point3, triangle.point3);
		Tracer.trace(brush.bsp, false, s_trace1);
		Tracer.trace(brush.bsp, false, s_trace2);
		Tracer.trace(brush.bsp, false, s_trace3);
		if (s_trace1.dummy && s_trace2.dummy && s_trace3.dummy) {
			JGL_3DBsp bsp = brush.bsp;
			while (bsp.type==JGL_3DBsp.NODE) {
				int p = JGL_Math.plane_trianglePosition(bsp.plane, triangle);
				if (p==9 || p==10)
					return false;
				bsp = bsp.rear;
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * Returns if the plane is a brush plane.
	 * 
	 * @param plane : the plane to test
	 * @param brush : the brush
	 * @return if the plane is a brush plane
	 */
	public static boolean isBrushPlane(JGL_3DPlane plane, Bsp_brush brush) {
		JGL_3DBsp bsp = brush.bsp;
		while (bsp.type==JGL_3DBsp.NODE) {
			if (plane.eq(bsp.plane))
				return true;
			bsp = bsp.rear;
		}
		return false;
	}
	
	
	/**
	 * Returns if the specified triangle is a brush edge.
	 * 
	 * @param triangle : the triangle to test
	 * @param brush : the brush
	 * @return if the triangle is a brush edge
	 */
	public static boolean isBrushEdge(JGL_3DTriangle triangle, Bsp_brush brush) {
		
		s_trace1.reset(s_sphere, triangle.point1, triangle.point1);
		s_trace2.reset(s_sphere, triangle.point2, triangle.point2);
		s_trace3.reset(s_sphere, triangle.point3, triangle.point3);
		Tracer.trace(brush.bsp, false, s_trace1);
		Tracer.trace(brush.bsp, false, s_trace2);
		Tracer.trace(brush.bsp, false, s_trace3);
		if (s_trace1.dummy && s_trace2.dummy && s_trace3.dummy) {
			JGL_3DBsp bsp = brush.bsp;
			while (bsp.type==JGL_3DBsp.NODE) {
				int p = JGL_Math.plane_trianglePosition(bsp.plane, triangle);
				if (p==10)
					return true;
				bsp = bsp.rear;
			}
			return false;
		}
		return false;
	}
	
	
	/**
	 * Returns a BSP given a list of triangles.
	 * 
	 * @param triangles : the triangles list
	 * @return the BSP
	 */
	public static JGL_3DBsp buildBSP(List triangles) {
		
		JGL_3DTriangle t;
		JGL_3DBsp result = new JGL_3DBsp();
		while (triangles.size()!=0) {
			t = findBestSplitter(triangles);
			if (t==null) return result;
			result.addFace(t);
			triangles.remove(t);
		}
		return result;
	}
	
	
	/**
	 * Returns the triangle which splits the less the others, or null if empty list.
	 * 
	 * @param triangles : the triangles list
	 * @return the triangle which splits the less the others, or null
	 */
	public static JGL_3DTriangle findBestSplitter(List triangles) {
		
		JGL_3DTriangle triangleToTest;
		JGL_3DPlane trianglePlane = new JGL_3DPlane(0f, 1f, 0f);
		JGL_3DTriangle triangleResult = null;
		int nbSplit = Integer.MAX_VALUE;
		int tSplit;
		
		for (int i=0; i<triangles.size(); i++) {
			tSplit = 0;
			triangleToTest = (JGL_3DTriangle)triangles.get(i);
			trianglePlane.assign(triangleToTest.point1, triangleToTest.point2, triangleToTest.point3);
			for (int j=0; j<triangles.size(); j++)
				if (i != j)
					if (JGL_Math.plane_trianglePosition(trianglePlane, (JGL_3DTriangle)triangles.get(j))==0)
						tSplit++;
			
			if (tSplit<nbSplit) {
				nbSplit = tSplit;
				triangleResult = triangleToTest;
			}
		}
		return triangleResult;
	}
	
	
	
	/**
	 * 
	 * @param mesh
	 * @param plane
	 * @param resultRear
	 * @param resultFront
	 * @return
	 */
	public static int splitMesh(JGL_3DMesh mesh, JGL_3DPlane plane, JGL_3DMesh resultRear, JGL_3DMesh resultFront) {
		
		JGL_3DTriangle split[] = new JGL_3DTriangle[3];
		split[0] = new JGL_3DTriangle(new JGL_3DVector(), new JGL_3DVector(), new JGL_3DVector());
		split[1] = new JGL_3DTriangle(new JGL_3DVector(), new JGL_3DVector(), new JGL_3DVector());
		split[2] = new JGL_3DTriangle(new JGL_3DVector(), new JGL_3DVector(), new JGL_3DVector());
		JGL_3DTriangle t;
		int place;
		int split_nb;
		int split_place;
		boolean split_allowed;
		
		List faces = mesh.getFaces();
		split_allowed = true;
		for (int i=0; i<faces.size(); i++) {
			t = (JGL_3DTriangle)faces.get(i);
			place = JGL_Math.plane_trianglePosition(plane, t);
			if (place==9 || place==10)
				split_allowed = false;
			if (place==-1 || place==10) {
				resultRear.addFace(t);
			}
			else if (place==1 || place==9) {
				resultFront.addFace(t);
			}
			else if (place==0 && split_allowed) {
				split_nb = JGL_Math.triangle_split(t, plane, split[0], split[1], split[2]);
				for (int j=0; j<split_nb; j++) {
					split_place = JGL_Math.plane_trianglePosition(plane, split[j]);
					if (split_place==-1)
						resultRear.addFace(split[j]);
					else if (split_place==1)
						resultFront.addFace(split[j]);
				}
			}
		}
		
		if (resultRear.getFaces().size()>0 && resultFront.getFaces().size()>0) {
			return 0;
		}
		
		if (resultRear.getFaces().size()>0)
			return -1;
		
		return 1;
	}
	
	
	
}
