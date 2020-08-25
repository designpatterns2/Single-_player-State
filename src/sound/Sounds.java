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

package sound;

import java.util.Vector;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.codecs.CodecJOrbis;
import input.LoadHelper;


/**
 * Class storing the game sounds list and the current music.
 * 
 * @author Nicolas Devere
 *
 */
public final class Sounds {
	
	private static boolean soundOK = false;
	private static SoundSystem soundSystem = null;
	
	private static String DUMMY_SOUND = "dum";
	private static String musicPath = DUMMY_SOUND;
	
	private static Vector sourceNames = new Vector();
	
	
	
	public static void init() {
		try {
			SoundSystemConfig.setCodec( "ogg", CodecJOrbis.class );
			soundSystem = new SoundSystem(LibraryLWJGLOpenAL.class);
			soundOK = true;
		}
		catch(Exception ex) {
			soundOK = false;
		}
	}
	
	
	/**
	 * Adds a sound to the list given its file path, its ID and its copies number.
	 * 
	 * @param path : the sound path
	 * @param id : the sound ID
	 * @param copies : the number of copies
	 */
	public static void add(String path, String id, int copies, float volume) {
		try {
			if (soundOK && !id.equals(DUMMY_SOUND)) {
				soundSystem.newSource(false, id, LoadHelper.getURL(path), path, false, 0f, 0f, 0f, SoundSystemConfig.ATTENUATION_NONE, 0f);
				soundSystem.setVolume(id, volume);
				sourceNames.add(id);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return;
		}
	}
	
	
	/**
	 * Plays the specified sound.
	 * 
	 * @param id : the sound ID
	 */
	public static void play(String id) {
		if (soundOK && !id.equals(DUMMY_SOUND)) {
			if (soundSystem.playing(id))
				soundSystem.stop(id);
			soundSystem.play(id);
		}
	}
	
	
	/**
	 * Plays the specified sound in an infinite loop.
	 * 
	 * @param index : the sound ID
	 */
	public static void loop(String id) {
		
	}
	
	
	/**
	 * Stops the specified sound.
	 * 
	 * @param index : the sound ID
	 */
	public static void stop(String id) {
		if (soundOK && !id.equals(DUMMY_SOUND))
			soundSystem.stop(id);
	}
	
	
	/**
	 * Plays the specified music.
	 * @param path : the music file path
	 * @param volume : the music volume
	 */
	public static void playMusic(String path, float volume) {
		
		try {
			if (soundOK) {
				if (!path.equals(musicPath)) {
					stopMusic();
					soundSystem.newStreamingSource(false, path, LoadHelper.getURL(path), path, true, 0f, 0f, 0f, SoundSystemConfig.ATTENUATION_NONE, 0f);
					soundSystem.setVolume(path, volume);
					soundSystem.play(path);
					musicPath = path;
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace(System.out);
			return;
		}
	}
	
	
	/**
	 * Stops the music currently played.
	 */
	public static void stopMusic() {
		if (soundOK) {
			if (!musicPath.equals(DUMMY_SOUND)) {
				String path = "";
				path = path.concat(musicPath);
				soundSystem.stop(path);
				soundSystem.removeSource(path);
				musicPath = DUMMY_SOUND;
			}
		}
	}
	
	
	
	
	/**
	 * Clears the sounds list.
	 */
	public static void clear() {
		if (soundOK) {
			for (int i=0; i<sourceNames.size(); i++) {
				String path = "";
				path = path.concat((String)sourceNames.get(i));
				//soundSystem.stop(path);
				soundSystem.removeSource(path);
			}
			sourceNames.clear();
			stopMusic();
		}
	}
	
	
	public static void cleanup() {
		if (soundOK)
			soundSystem.cleanup();
		sourceNames.clear();
		soundSystem = null;
		soundOK = false;
	}
}
