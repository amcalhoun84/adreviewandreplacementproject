package csci576;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;

public class PlayAudio implements Runnable, PlayerInterface {


    // This follows patterns found in the PlaySound and audio files given to us.
    // For Danny: final is used to imply it does not change and cannot be overwritten, sort of like constant. It's used to restrict change when change may be undesirable.
    private InputStream inputStream;
    private SourceDataLine sourceDataLine;
    private AudioFormat audioFormat;
    private static Clip clip;

    public PlayAudio(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public static void resume() {
        clip.start();
    }

    public static void pause() {
        clip.stop();
    }

    public long getFramePosition() {
        return clip.getLongFramePosition();
    }

    // check if we can do a double for this, otherwise float okay
    public float getSampledRate() {
        //System.out.println(audioFormat);
        return audioFormat.getFrameRate();
    }

    public void playAudioVideo() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        // derived from PlaySound
        AudioInputStream audioInputStream = null;
        try {
            InputStream bufferedIn = new BufferedInputStream(this.inputStream);
            audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch(Exception e) {
            e.printStackTrace();
        }

        assert audioInputStream != null;
        audioFormat = audioInputStream.getFormat();
    }


    @Override
    public void run() {
        try {
            this.playAudioVideo();
            // For Danny: If you have common errors/exceptions, you can collapse the blocks together.
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        clip.stop();
        clip.setFramePosition(0);
        System.exit(0);
    }
}
