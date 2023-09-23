package ultima;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;

public class Sounds {

    public static Map<Sound, Music> sounds = new HashMap<>();

    public static final List<Sound> backgroundList = new ArrayList<>();
    public static boolean backgroundPlaying = true;

    static {
        backgroundList.add(Sound.OUTSIDE);
    }

    public static class BackgroundMusicJukeBox implements Runnable {

        @Override
        public void run() {
            while (true) {
                for (Sound sound : backgroundList) {
                    
                    Music m = play(sound);
                    backgroundPlaying = true;
                    
                    m.setOnCompletionListener(new Music.OnCompletionListener() {
                        @Override
                        public void onCompletion(Music music) {
                            backgroundPlaying = false;
                        }
                    });
                    
                    while (backgroundPlaying) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    public static Music play(Sound sound) {
        return play(sound, sound.getVolume());
    }
    
    public static Music play(Sound sound, float volume) {
        Music m = sounds.get(sound);
        if (m == null) {
            m = Gdx.audio.newMusic(Gdx.files.classpath("assets/sound/" + sound.getFile()));
            sounds.put(sound, m);
        }
        m.setLooping(sound.getLooping());
        m.setVolume(volume);
        m.play();
        return m;
    }

    public static Music play(Sound sound, OnCompletionListener ocl) {
        Music m = sounds.get(sound);
        if (m == null) {
            m = Gdx.audio.newMusic(Gdx.files.classpath("assets/sound/" + sound.getFile()));
            m.setVolume(sound.getVolume());
            m.setLooping(sound.getLooping());

            sounds.put(sound, m);
        }

        m.setOnCompletionListener(ocl);
        m.play();

        return m;
    }

}
