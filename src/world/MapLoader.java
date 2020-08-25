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

import com.jme.scene.Node;

import entity.ScriptBox;

import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Observable;

import ai.PathGraph;

import phys.Shape;

import jglcore.JGL_3DVector;
import jglanim.JGL_KeyframesArray;
import jglanim.JGL_Keyframe;
import input.LoadHelper;

import script.Script;



/**
 * Class providing static methods to load a complete map.
 * 
 * @author Nicolas Devere
 *
 */
public final class MapLoader extends Observable {
	
	private int lines;
	
	
	public MapLoader() {
		lines = 0;
	}
	
	
	/**
	 * Loads a map according to the specified file and stores it in the World class.
	 * 
	 * @param path : the map file
	 * @return the map
	 * @throws Exception
	 */
	public final void loadMap(String path) throws Exception {
		
		try {
			
			if (World.map!=null) {
				World.map.clear();
				World.map = null;
			}
			
			// Map instantiation
			World.map = new Map();
			
			lines = 0;
			
			StringTokenizer st;
			
			BufferedReader br = LoadHelper.getBufferedReader(path);
			
			// Characters loading
			while (br.ready()) {
				
				st = new StringTokenizer(br.readLine()); lines++; this.setChanged(); this.notifyObservers();
				if (st.countTokens()==0)
					continue;
				
			    String charToken = st.nextToken();
			    if (charToken.startsWith("!"))
			    	continue;
			    
			    if (charToken.equals(Script.SCRIPT))
			    	Script.execute(st);
			    
			    if (charToken.equals("loadpathgraph")) {
			    	String id = st.nextToken();
			    	int nbPoints = Integer.parseInt(st.nextToken());
					JGL_3DVector points[] = new JGL_3DVector[nbPoints];
					for (int i=0; i<nbPoints; i++) {
						st = new StringTokenizer(br.readLine()); lines++; this.setChanged(); this.notifyObservers();
						points[i] = new JGL_3DVector(Float.parseFloat(st.nextToken()), 
													Float.parseFloat(st.nextToken()), 
													Float.parseFloat(st.nextToken()));
					}
					
					boolean links[][] = new boolean[nbPoints][nbPoints];
					for (int i=0; i<nbPoints; i++) {
						st = new StringTokenizer(br.readLine()); lines++; this.setChanged(); this.notifyObservers();
						for (int j=0; j<nbPoints; j++)
							links[i][j] = !st.nextToken().equals("0");
					}
					
					Resources.addPathGraph(new PathGraph(id, points, links));
			    }
			    
			    if (charToken.equals("loadscriptbox")) {
			    	String id = st.nextToken();
			    	JGL_3DVector pos = new JGL_3DVector(Float.parseFloat(st.nextToken()), 
														Float.parseFloat(st.nextToken()), 
														Float.parseFloat(st.nextToken()));
			    	Shape cshape = Script.getCShape(st);
			    	cshape.setPosition(pos);
			    	int nb = Integer.parseInt(st.nextToken());
			    	boolean checkpoint = st.hasMoreTokens();
			    	Vector scripts = new Vector();
			    	
			    	for (int i=0; i<nb; i++) {
			    		scripts.add(br.readLine());
			    		lines++; this.setChanged(); this.notifyObservers();
			    	}
			    	
			    	Resources.addEntity(new ScriptBox(id, cshape, scripts, checkpoint));
			    }
			    
			    if (charToken.equals("loadkinematic")) {
			    	String id = st.nextToken();
			    	float speed = Float.parseFloat(st.nextToken());
			    	float endDate = Float.parseFloat(st.nextToken());
			    	
			    	int nbKeys = Integer.parseInt(new StringTokenizer(br.readLine()).nextToken()); 
			    	lines++; this.setChanged(); this.notifyObservers();
			    	JGL_KeyframesArray kfs = new JGL_KeyframesArray();
			    	for (int i=0; i<nbKeys; i++) {
			    		StringTokenizer st2 = new StringTokenizer(br.readLine());
			    		lines++; this.setChanged(); this.notifyObservers();
			    		kfs.add(new JGL_Keyframe(new JGL_3DVector(	Float.parseFloat(st2.nextToken()), 
																	Float.parseFloat(st2.nextToken()), 
																	Float.parseFloat(st2.nextToken())), 
												new JGL_3DVector(	Float.parseFloat(st2.nextToken()), 
																	Float.parseFloat(st2.nextToken()), 
																	Float.parseFloat(st2.nextToken()))));
			    	}
			    	
			    	int nbNds = Integer.parseInt(new StringTokenizer(br.readLine()).nextToken());
			    	lines++; this.setChanged(); this.notifyObservers();
			    	Vector nds = new Vector();
			    	for (int i=0; i<nbNds; i++) {
			    		StringTokenizer st2 = new StringTokenizer(br.readLine());
			    		lines++; this.setChanged(); this.notifyObservers();
			    		Node node = Script.getNodeCopy(st2.nextToken());
			    		node.setLocalTranslation(	Float.parseFloat(st2.nextToken()), 
			    									Float.parseFloat(st2.nextToken()), 
			    									Float.parseFloat(st2.nextToken()));
			    		nds.add(node);
			    	}
			    	
			    	int nbScripts = Integer.parseInt(new StringTokenizer(br.readLine()).nextToken());
			    	lines++; this.setChanged(); this.notifyObservers();
			    	Vector scripts = new Vector();
			    	for (int i=0; i<nbScripts; i++) {
			    		scripts.add(br.readLine());
			    		lines++; this.setChanged(); this.notifyObservers();
			    	}
			    	
			    	Resources.addKinematic(new Kinematic(id, nds, kfs, speed, endDate, scripts));
			    }
			}
			
			br.close();
			World.map.applyCheckpoint();
		}
		catch (Exception ex) {
			throw ex;
		}
	}
	
	
	/**
	 * Returns the number of lines currently read.
	 * 
	 * @return the number of lines currently read
	 */
	public int getLines() {
		return lines;
	}
	
}
