package csci576;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class audioProcessor {

    // once more, we derive a lot of this from HW1
    // this will analyze the entropies and divide the video into shots.

    public static void analyzeAudio(String audioFilePath, Shot[] shots, boolean isComplex) throws UnsupportedAudioFileException, IOException {
        File audioFile = new File(audioFilePath);

        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioStream.getFormat();
        double frameRate = format.getFrameRate();
        int frameSize = format.getFrameSize();
        boolean bigEnd = format.isBigEndian();

        System.out.println(shots.length);

        byte[] buffer = new byte[frameSize];
        int read;
        int offset;
        int x, y;
        int shotOffset = 0;

        // either positive or negative, a common flag in math based problems
        int sign = 1;
        // count the number of sign changes.
        int signCount = 0;
        for (int curr = 0; (read = audioStream.read(buffer)) > 0; curr++) {
            if (read != frameSize) {
                offset = read;
                while (offset < frameSize && (read = audioStream.read(buffer, offset, frameSize - offset)) >= 0) {
                    offset += read;
                }
            }


            if (!bigEnd) {
                x = buffer[1] << 8;
                y = buffer[0];
                // in case the sound file is big endian (AMD or a big endian driver)
            } else {
                x = buffer[0] << 8;
                y = buffer[1];
            }
            double xy = x | y;

            // map sample frames to frame 31
            int videoFrame = (int) ((curr / frameRate) * 30) + 1;

            // There might be a desync between a frame or two, but that isn't a big deal
            if (videoFrame > shots[shotOffset].end) {

                shots[shotOffset].signChangeFreq = signCount * 1.0 / shots[shotOffset].length();
                signCount = 0;
                sign = 1;
                shotOffset++;
                // because we need to terminate if we meet or exceed the shots length
                if (shotOffset == shots.length && isComplex) {
                    shotOffset = shots.length - 1;
                    break;
                }
            }

            shots[shotOffset].addSample(Math.abs(xy / Short.MAX_VALUE));

            if (sign * xy < 0) {
                sign = (xy < 0 ? -1 : 1);
                signCount++;
            }
        }

        shots[shotOffset].signChangeFreq = signCount * 1.0 / shots[shotOffset].length();

        for (Shot s : shots) {
            s.avgSample();
        }

        if (isComplex) {
            analyzeComplexAudio(shots);
        }

    }

    private static void analyzeComplexAudio(Shot[] shots) {

        Shot[] newShots = shots;
        List<Shot> errShots = new ArrayList<>();
        List<Integer> errIndex = new ArrayList<>();
        int errCount = 0;
        for (int i = 0; i < shots.length; i++) {

            if (i == 0) {
                if (shots[i].category.name().equals("YES") && shots[i + 1].category.name().equals("YES")) {
                    errShots.add(shots[i]);
                    errIndex.add(i);
                }
            }

            if (i == shots.length - 1) {
                if (shots[i].category.name().equals("YES") && shots[i - 1].category.name().equals("YES")) {
                    errShots.add(shots[i]);
                    errIndex.add(i);
                    for (int j = 0; j < errShots.size(); j++) {
                        errCount += errShots.get(j).length();
                    }
                    if (errCount < 250) {
                        for (int k = 0; k < errIndex.size(); k++) {
                            newShots[errIndex.get(k)].category = Category.NO;
                        }
                    }
                    errCount = 0;
                }
            } else {
                if (shots[i].category.name().equals("YES") && shots[i + 1].category.name().equals("YES")) {
                    errShots.add(shots[i]);
                    errIndex.add(i);
                } else if (shots[i].category.name().equals("YES") && shots[i + 1].category.name().equals("NO")) {
                    errShots.add(shots[i]);
                    errIndex.add(i);
                    for (int j = 0; j < errShots.size(); j++) {
                        errCount += errShots.get(j).length();
                    }
                    if (errCount < 250) {
                        for (int k = 0; k < errIndex.size(); k++) {
                            newShots[errIndex.get(k)].category = Category.NO;
                        }
                    }
                    errCount = 0;
                    errShots = new ArrayList<>();
                    errIndex = new ArrayList<>();
                }
            }
        }
        shots = newShots;

        // Shallow copy is PROBABLY okay, keep this until we know for sure.
//        for (int i = 0; i < shots.length; i++) {
//            shots[i] = newshots[i];
//        }

    }

    public static void processAudio(String audioIn, String audioOut, Shot[] shots, List<String> adpaths, boolean isComplex) throws UnsupportedAudioFileException, IOException {
        File audio = new File(audioIn);
        AudioInputStream stream = AudioSystem.getAudioInputStream(audio);
        AudioFormat format = stream.getFormat();
        double frameRate = format.getFrameRate();
        int frameSize = format.getFrameSize();
        System.out.println("Audio Processing Start:");
        byte[] buffer = new byte[(int) (frameRate * frameSize / Constants.FPS)];
        FileOutputStream output = new FileOutputStream(audioOut + ".tmp");

        int read, offset, curr = -1, length = 0, lastAdReplaced = -1;
        boolean currAdReplaced = false;
        boolean startCommercial = false;

        // Commercial checks. This is for Complex ONLY
        boolean keepCommercial = false;
        boolean secondCommercial = false;

        for (int frame = 1; (read = stream.read(buffer)) > 0; frame++) {
            if (read != frameSize) {
                offset = read;
                while (offset < frameSize && (read = stream.read(buffer, offset, frameSize - offset)) >= 0) {
                    offset += read;
                }
            }

            // reset ad replaced because if we have more than one ad, it will leave the other alone.
            if (lastAdReplaced != curr) {
                currAdReplaced = false;
            }

            // check if we have a starter commercial
            if (frame == 1 && shots[curr + 1].category == Category.YES && length == 0) {
                startCommercial = true;
            }

            if (curr == -1 || startCommercial && shots[curr].category == Category.YES) {
                curr++;
                output.write(buffer);
                length += (int) (frameRate / 30);

            } else {
                // turn off start commercial because we don't need to check for that anymore
                startCommercial = false;
                // check if it's complex and if we are keeping a commercial or not
                if (adpaths.size() > 0 && isComplex) {
                    keepCommercial = adpaths.get(0).length() <= 10;
                } else {
                    keepCommercial = false;
                }


                if (shots[curr].end < frame) {
                    curr++;
                    if (curr == shots.length) {
                        break;
                    }

                    // if we have ads to replace and there isn't a starting commercial, we need to insert the audio based on the length replaced in the video processor
                    if (shots[curr - 1].category == Category.YES && (shots[curr].category == Category.NO || curr == shots.length - 1) && adpaths.size() > 0 && !keepCommercial) {
                        if (!currAdReplaced) {
                            currAdReplaced = true;
                            lastAdReplaced = curr;
                            System.out.println("Adding audio  " + adpaths.get(0) + " current shot: " + curr);
                            length += insertAdAudio(adpaths.get(0), output);
                            adpaths.remove(0); // pop 0
                        }
                    }
                    if (shots[curr].category == Category.NO && shots[curr - 1].category == Category.YES && keepCommercial) {
                        adpaths.remove(0); // pop 0
                    }
                }

                if (shots[curr].category == Category.YES && keepCommercial) {
                    output.write(buffer);
                    length += (int) (frameRate / 30);
                }

                if (shots[curr].category == Category.NO) {
                    output.write(buffer);
                    length += (int) (frameRate / 30);
                }
            }
        }
        stream.close();
        output.close();

        File out = new File(audioOut);
        FileInputStream temp = new FileInputStream(audioOut + ".tmp");

        AudioInputStream audioStream = new AudioInputStream(temp, format, length);
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, out);
        temp.close();
        audioStream.close();

        // Delete additional file
        Files.delete(Paths.get(audioOut + ".tmp"));
        System.out.println("Audio Processing Ended");

    }

    public static int insertAdAudio(String adpath, FileOutputStream audioOutput) throws
            IOException, UnsupportedAudioFileException {
        File audioOut = new File(adpath);

        AudioInputStream stream = AudioSystem.getAudioInputStream(audioOut);
        AudioFormat format = stream.getFormat();
        double frameRate = format.getFrameRate();
        int frameSize = format.getFrameSize();

        byte[] buffer = new byte[(int) frameRate * frameSize / Constants.FPS];
        int read;
        int offset;
        int length = 0;

        while ((read = stream.read(buffer)) > 0) {
            if (read != frameSize) {
                offset = read;
                while (offset < frameSize && (read = stream.read(buffer, offset, frameSize - offset)) >= 0) {
                    offset += read;
                }
            }
            audioOutput.write(buffer);
            length += (int) (frameRate / Constants.FPS);

        }
        stream.close();
        return length;
    }
}