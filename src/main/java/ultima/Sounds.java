package ultima;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;

public class Sounds {
	
	public static Map<Sound, Music> sounds = new HashMap<Sound, Music>();
	
	public static List<Sound> backgroundList = new ArrayList<Sound>();
	public static boolean backgroundPlaying = true;
	
	static {
		backgroundList.add(Sound.OUTSIDE);
	}
	
	public static Music.OnCompletionListener listener = new Music.OnCompletionListener() {
		public void onCompletion(Music music) {
			backgroundPlaying = false;
		}
	};
	
	public static void startBackGroundMusic() {
		new Thread() {
			public void run() {
				while (true) {
					for (Sound sound : backgroundList) {
						Music m = play(sound);
						backgroundPlaying = true;
						m.setOnCompletionListener(listener);
						while (backgroundPlaying) {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
							}
						}
					}
				}
			}
		}.start();		
	}
	
	public static Music play(Sound sound) {
		Music m = sounds.get(sound);
		if (m == null) {
			m = Gdx.audio.newMusic(Gdx.files.internal("assets/sound/" + sound.getFile()));
			m.setVolume(sound.getVolume());
			m.setLooping(sound.getLooping());

			sounds.put(sound, m);
		}
		m.play();
		return m;
	}
	
	public static Music play(Sound sound, OnCompletionListener ocl) {
		Music m = sounds.get(sound);
		if (m == null) {
			m = Gdx.audio.newMusic(Gdx.files.internal("assets/sound/" + sound.getFile()));
			m.setVolume(sound.getVolume());
			m.setLooping(sound.getLooping());

			sounds.put(sound, m);
		}
		
		m.setOnCompletionListener(ocl);
		m.play();
		
		return m;
	}
	

}
