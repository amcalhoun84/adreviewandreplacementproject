package csci576;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;


import javax.swing.*;

// Derived from the first two assignments
public class PlayVideo implements Runnable {

    private final PlayAudio playAudio;
    private static InputStream inputStream;
    private byte[] buffer; // because the read requires bytes. We need to be careful here else our colors may overflow

    private final String videoFileName;

    private static boolean paused;

    private static final Object lock = new Object();
    static BufferedImage bufferedImage;


    //todo(): Check that these are auto set in the local object space, otherwise put make them discrete class members
    public PlayVideo(String videoFileName, PlayAudio playAudio) {
        this.playAudio = playAudio;
        this.videoFileName = videoFileName;
    }

    // based off the reads of the first two assignments
    private void readImageRGB() {
        synchronized (this) {
            while (paused) {
                Thread.interrupted();
            }
        }

        try {
            int offset = 0;
            int numRead = 0;

            while (offset < buffer.length && (numRead = inputStream.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
            int ind = 0;
            for (int y = 0; y < Constants.HEIGHT; y++) {
                for (int x = 0; x < Constants.WIDTH; x++) {
                    byte r = buffer[ind];
                    byte g = buffer[ind + Constants.HEIGHT * Constants.WIDTH];
                    byte b = buffer[ind + Constants.HEIGHT * Constants.WIDTH * 2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    bufferedImage.setRGB(x, y, pix);
                    ind++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void playVideo() {

        bufferedImage = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);

        try {
            File videoFile = new File(videoFileName);
            inputStream = new FileInputStream(videoFile);
            long numberOfFrames = videoFile.length() / Constants.PPF;
            buffer = new byte[(int) Constants.PPF];

            PlayVideoComponent component = new PlayVideoComponent();
            double SPF = playAudio.getSampledRate() / 30; // samples per frame

            int offset = 0;
            int counter = 0;
            while (counter < Math.round(playAudio.getFramePosition() / SPF)) {
                readImageRGB();
                component.setImg(bufferedImage);
                Player.controlFrame.add(component);
                Player.controlFrame.repaint();
                Player.controlFrame.setVisible(true);
                counter++;
            }
            // Sync audio
            while (counter > Math.round(offset + playAudio.getFramePosition() / SPF)) {
            }

            for (int i = counter; i < numberOfFrames; i++) {

                // Sync Video
                while (i > Math.round(offset + playAudio.getFramePosition() / SPF)) {
                }

                // Sync Audio
                while (i < Math.round(playAudio.getFramePosition() / SPF)) {
                    readImageRGB();
                    component.setImg(bufferedImage);
                    Player.controlFrame.add(component);
                    Player.controlFrame.repaint();
                    Player.controlFrame.setVisible(true);
                    i++;
                }

                readImageRGB();
                component.setImg(bufferedImage);
                Player.controlFrame.add(component);
                Player.controlFrame.repaint();
                Player.controlFrame.setVisible(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void stop() {
        System.exit(0);
    }

    public static void pause() {
        paused = true;
    }

    public static void resume() {
        synchronized (lock) {
            paused = false;
            lock.notify();
        }
    }

    @Override
    public void run() {
        playVideo();

    }

    // similar to what we did for HW2 with the frame, only this time, we need a player.
    public static class PlayVideoComponent extends JComponent {
        private BufferedImage img;

        public void paintComponent(Graphics g) {
            Graphics2D playVideo = (Graphics2D) g;
            playVideo.drawImage(img, 0, 0, this);
        }

        public void setImg(BufferedImage img) {
            this.img = img;
        }

    }
}
