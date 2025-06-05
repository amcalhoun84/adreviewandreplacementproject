package csci576;

import org.opencv.core.Core;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Processor {

    public static List<Integer> getShotEndPoints(Shot[] shots, boolean isComplex) {

        for (int i = 0; i < shots.length; i++) {
            Set<Category> neighbors = new HashSet<>(); // see if we can fudge this as a set.
            if (i != 0) {
                neighbors.add(shots[i - 1].category);
            }
            if (i != shots.length - 1) {
                neighbors.add(shots[i + 1].category);
            }


            if (shots[i].category == Category.EITHER || shots[i].category == Category.YES) {
                if (neighbors.contains(Category.NO) && neighbors.size() == 1) {
                    shots[i].category = Category.NO;
                } else if (neighbors.contains(Category.YES) && neighbors.size() == 1) {
                    shots[i].category = Category.YES;
                }
            }
        }

        // Compare the average amplitude with neighbors.
        for (int i = 0; i < shots.length; i++) {
            if (i != 0 && shots[i].category == Category.EITHER) {
                if (Math.abs(shots[i].avgAmp - shots[i - 1].avgAmp) <= 0.01) {
                    shots[i].category = shots[i - 1].category;
                }
            }

            if (i != shots.length - 1 && shots[i].category == Category.EITHER) {
                if (Math.abs(shots[i].avgAmp - shots[i + 1].avgAmp) <= 0.01) {
                    shots[i].category = shots[i + 1].category;
                }
            }
        }

        // Figure the ODD frame.
        for (int i = 0; i < shots.length - 1; i++) {
            if (shots[i].category == Category.EITHER) {
                if (Math.abs(shots[i].signChangeFreq - shots[i - 1].signChangeFreq) <
                        Math.abs(shots[i].signChangeFreq - shots[i + 1].signChangeFreq)) {
                    shots[i].category = shots[i - 1].category;
                } else {
                    shots[i].category = shots[i + 1].category;
                }
            }
        }

        for (int i = 0; i < shots.length; i++) {
            if (shots[i].category == Category.EITHER) {
                for (int j = 1; j <= shots.length; j++) {
                    if (i - j >= 0) {
                        if (shots[j].category != Category.EITHER) {
                            shots[i].category = shots[j].category;
                            break;
                        }
                    }
                    if (i + j < shots.length) {
                        if (shots[j].category != Category.EITHER) {
                            shots[i].category = shots[j].category;
                            break;
                        }
                    }
                }
            }
        }

        for (Shot s : shots) {
            System.out.println("Shot: " + s.start + "-" + s.end + ", " + s.category.name());
        }

        List<Integer> shotEndpoints = new ArrayList<>();
        for (int i = 0; i < shots.length; i++) {

            if (i == 0) {
                if (shots[i].category.name().equals("YES") && shots[i + 1].category.name().equals("YES")) {
                    shotEndpoints.add(1);
                    System.out.println("Shot start: " + shots[i].start);
                }
            }

            if (i == shots.length - 1) {
                if (shots[i].category.name().equals("YES") && shots[i - 1].category.name().equals("YES")) {
                    shotEndpoints.add(shots[i].end);
                    System.out.println("Shot end: " + shots[i].end);
                }
            } else {
                if (shots[i].category.name().equals("NO") && shots[i + 1].category.name().equals("YES")) {
                    shotEndpoints.add(shots[i + 1].start);
                    System.out.println("Shot start: " + shots[i + 1].start);
                }
                if (shots[i].category.name().equals("YES") && shots[i + 1].category.name().equals("NO")) {
                    shotEndpoints.add(shots[i].end);
                    System.out.println("Shot end: " + shots[i].end);
                }
            }
        }
        return shotEndpoints;
    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String[] sourcePaths = DataSets.getPaths(args[0]);

        String[] paths = {
                args[0],
                args[1],
                sourcePaths[0],
                sourcePaths[1],
                sourcePaths[2],
                sourcePaths[3],
                sourcePaths[4],
                sourcePaths[5],
                args[2],
                args[3],
                sourcePaths[6],
                sourcePaths[7],
                sourcePaths[8],
                sourcePaths[9]
        };

        boolean isComplex = Boolean.parseBoolean(sourcePaths[10]);
        System.out.println(isComplex);


        long start = System.currentTimeMillis();

        // check for shots on the basis of entropy -- also adds Danny and Andrew's complicated edge case code
        Shot[] shots = videoProcessor.analyzeVideo(paths[0], isComplex);
        audioProcessor.analyzeAudio(paths[1], shots, isComplex);
        List<Integer> shotEnds = getShotEndPoints(shots, isComplex);

        //process video using Dereks code and return ad placement path for audio {ad1AudioPath, ad2AudioPath}
        List<String> adAudioPath = videoProcessor.videoProcess(paths, shotEnds, isComplex);

        //process audio
        audioProcessor.processAudio(paths[1], paths[9], shots, adAudioPath, isComplex);

        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        System.out.println("Total Process Ended");
        System.out.println("Total Process took: " + elapsedTime / 1000);

    }
}
