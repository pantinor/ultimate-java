package util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

public class MidiMusic implements Music {

    private Sequencer sequencer;

    public MidiMusic(String fname) {
        try {
            InputStream is = Gdx.files.classpath("assets/sound/" + fname).read();

            this.sequencer = MidiSystem.getSequencer();
            this.sequencer.setSequence(MidiSystem.getSequence(is));
            this.sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            this.sequencer.open();
        } catch (IOException | InvalidMidiDataException | MidiUnavailableException t) {
        }
    }

    public void resetGain(int gain) {

    }

    @Override
    public void play() {
        sequencer.start();
    }

    @Override
    public void pause() {
    }

    @Override
    public void stop() {
        this.sequencer.stop();
    }

    @Override
    public void dispose() {
        this.sequencer.close();
    }

    @Override
    public boolean isPlaying() {
        return this.sequencer.isRunning();
    }

    @Override
    public void setLooping(boolean bln) {
    }

    @Override
    public boolean isLooping() {
        return true;
    }

    @Override
    public void setVolume(float vol) {
        //cannot figure out how to set volume on MIDI 
    }

    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public void setPan(float f, float f1) {
    }

    @Override
    public void setPosition(float f) {
    }

    @Override
    public float getPosition() {
        return 0;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener ol) {
    }

}
