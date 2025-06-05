package csci576;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Player {

    static JFrame controlFrame;
    static JPanel buttonPanel;

    public static class ControlPanel extends JButton {
        public ControlPanel(String label) {
            this.setFont(new Font("Arial", Font.BOLD, 16));
            this.setText(label);
            this.addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            super.mousePressed(e);
                            mausKlick(getText());
                        }
                    }
            );
        }

        public void mausKlick(String label) {
            if (label.equals(Constants.PLAY)) {
                PlayVideo.resume();
                PlayAudio.resume();
            }
            if (label.equals(Constants.PAUSE)) {
                PlayVideo.pause();
                PlayAudio.pause();
            }
            if (label.equals(Constants.STOP)) {
                PlayVideo.stop();
                PlayAudio.stop();
            }
        }
    }


    public static void main(String[] args) throws FileNotFoundException {

        controlFrame = new JFrame();
        controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        controlFrame.setTitle("Advertising Blocker Magic");
        controlFrame.setSize(640, 310);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setPreferredSize(new Dimension(125, 125));

        controlFrame.getContentPane().add(buttonPanel, BorderLayout.EAST);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        ControlPanel button = new ControlPanel(Constants.PLAY);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(button);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        button = new ControlPanel(Constants.PAUSE);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(button);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        button = new ControlPanel(Constants.STOP);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(button);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        controlFrame.setVisible(true);
        System.out.println("Played Streams: " + args[0] + " " + args[1]);

        try {
            FileInputStream inputStream = new FileInputStream(args[1]);
            PlayAudio playAudio = new PlayAudio(inputStream);
            PlayVideo playVideo = new PlayVideo(args[0], playAudio);

            new Thread(playAudio).start();
            Thread.sleep(500); // helps with issues on loading the audio.
            new Thread(playVideo).start();

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
