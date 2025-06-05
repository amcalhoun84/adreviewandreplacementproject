package csci576;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class videoProcessor {

    public static Shot[] analyzeVideo(String videoFileName, boolean isComplex) throws IOException {
        File file = new File(videoFileName);
        InputStream videoStream = new FileInputStream(file);

        double prevY = 0, prevR = 0, prevG = 0, prevB = 0;
        double prevDiffY = 0, prevDiffR = 0, prevDiffG = 0, prevDiffB = 0;

        ArrayList<Integer> borders = new ArrayList<>();

        int numRead = 0;
        int frame = 1;
        byte[] shotBuffer = new byte[(int) Constants.PPF];


        for (frame = 1; numRead != -1; frame++) {

            int offset = 0;
            while (offset < shotBuffer.length && (numRead = videoStream.read(shotBuffer, offset, shotBuffer.length - offset)) >= 0) {
                offset += numRead;
            }

            double sumY = 0;
            double sumR = 0;
            double sumG = 0;
            double sumB = 0;

            int[] YSpace = new int[256];
            int[] RSpace = new int[256];
            int[] GSpace = new int[256];
            int[] BSpace = new int[256];

            int ind = 0;
            for (int y = 0; y < Constants.HEIGHT; y++) {
                for (int x = 0; x < Constants.WIDTH; x++) {
                    // Read RGB from buffer for frame
                    int r = shotBuffer[ind];
                    int g = shotBuffer[ind + Constants.HEIGHT * Constants.WIDTH];
                    int b = shotBuffer[ind + Constants.HEIGHT * Constants.WIDTH * 2];

                    int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                    // Normalize values
                    Y = Math.max(0, Math.min(255, Y));
                    r = Math.max(0, r);
                    g = Math.max(0, g);
                    b = Math.max(0, b);

                    // Increase frequency for those values in their respective spaces
                    YSpace[Y]++;
                    RSpace[r]++;
                    GSpace[g]++;
                    BSpace[b]++;

                    ind++;
                }
            }

            // see if we can simplify this.
            for (int index : YSpace) {
                if (index != 0) {
                    double probability = index * 1.0 / (Constants.WIDTH * Constants.HEIGHT);
                    sumY += probability * Math.log(index) / Math.log(2);
                }
            }
            for (int index : RSpace) {
                if (index != 0) {
                    double probability = index * 1.0 / (Constants.WIDTH * Constants.HEIGHT);
                    sumR += probability * Math.log(index) / Math.log(2);
                }
            }
            for (int index : GSpace) {
                if (index != 0) {
                    double probability = index * 1.0 / (Constants.WIDTH * Constants.HEIGHT);
                    sumG += probability * Math.log(index) / Math.log(2);
                }
            }
            for (int index : BSpace) {
                if (index != 0) {
                    double probability = index * 1.0 / (Constants.WIDTH * Constants.HEIGHT);
                    sumB += probability * Math.log(index) / Math.log(2);
                }
            }

            double diffY = Math.abs(prevY - sumY);
            double diffR = Math.abs(prevR - sumR);
            double diffG = Math.abs(prevG - sumG);
            double diffB = Math.abs(prevB - sumB);

            boolean check = true;

            // entropy checks
            if (diffY > 0.4 || (diffR > 0.35 || diffG > 0.35 || diffB > 0.35)) {
                if (prevDiffY == 0 || diffY != 0 && diffY / prevDiffY > 100
                        || (prevDiffR == 0 || diffR != 0 && diffR / prevDiffR > 100)
                        || (prevDiffG == 0 || diffG != 0 && diffG / prevDiffG > 100)
                        || (prevDiffB == 0 || diffB != 0 && diffB / prevDiffB > 100)) {
                    check = false;
                    borders.add(frame);
                }
            }
            if (diffY > 0.5 || (diffR > 0.5 || diffG > 0.5 || diffB > 0.5)) {
                if ((prevDiffY == 0 || diffY != 0 && diffY / prevDiffY > 50)
                        || (prevDiffR == 0 || diffR != 0 && diffR / prevDiffR > 50)
                        || (prevDiffG == 0 || diffG != 0 && diffG / prevDiffG > 50)
                        || (prevDiffB == 0 || diffB != 0 && diffB / prevDiffB > 50)) {
                    if (check) {
                        check = false;
                        borders.add(frame);
                    }
                }
            }

            if (diffY > 0.7 || (diffR > 0.7 || diffG > 0.7 || diffB > 0.7)) {
                if ((prevDiffY == 0 || diffY != 0 && diffY / prevDiffY > 10)
                        || (prevDiffR == 0 || diffR != 0 && diffR / prevDiffR > 10)
                        || (prevDiffG == 0 || diffG != 0 && diffG / prevDiffG > 10)
                        || (prevDiffB == 0 || diffB != 0 && diffB / prevDiffB > 10)) {
                    if (check) {
                        check = false;
                        borders.add(frame);
                    }
                }
            }

            prevY = sumY;
            prevDiffY = diffY;
            prevR = sumR;
            prevDiffR = diffR;
            prevG = sumG;
            prevDiffG = diffG;
            prevB = sumB;
            prevDiffB = diffB;
        }

        videoStream.close();
        borders.add(frame);

        Shot[] shots = new Shot[borders.size() - 1];

        int start = borders.get(0);
        for (int i = 1; i < borders.size(); i++) {
            int end = borders.get(i);
            shots[(i - 1)] = new Shot(start, end - 1);
            start = end;
        }

        boolean falsePositiveCheck = false;


        for (Shot shot : shots) {
            if (shot.length() < 120) {
                shot.category = Category.YES;   // advertisement
            } else if (shot.length() > 300) {
                shot.category = Category.NO;   // no advertisement
            } else {
                shot.category = Category.EITHER;
            }
        }

        return shots;
    }


    public static List<String> videoProcess(String[] paths, List<Integer> addpoints, boolean isComplex) {

        String vidpath = paths[0];
        String ad1SourcePath = paths[2];
        String ad2SourcePath = paths[3];
        String ad1VidPath = paths[4];
        String ad2VidPath = paths[5];
        String ad1AudPath = paths[6];
        String ad2AudPath = paths[7];
        String outPath = paths[8];
        BufferedImage bufferedImage;
        float[] toDrawCorners = new float [0];
        Scalar boxColor = new Scalar(255,255,255);
        int ad1count = 0;
        int ad2count = 0;
        int MIN_MATCH_COUNT1 = Integer.parseInt(paths[10]);
        int AREA_MINIMUM1 = Integer.parseInt(paths[11]);
        int MIN_MATCH_COUNT2 = Integer.parseInt(paths[12]);
        int AREA_MINIMUM2 = Integer.parseInt(paths[13]);
        List<String> commercialAudioOrder = new ArrayList<>();


        bufferedImage = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);
        File file = new File(vidpath);

        long framenumber = 1;
        long positionbit = 0;
        long filelength = file.length();
        long increment = Constants.PPF;
        boolean beginningCommercial = false;
        boolean specialCase = false;
        boolean originalCommercial = false;

        SIFTFLANNbox logo1 = new SIFTFLANNbox();
        SIFTFLANNbox logo2 = new SIFTFLANNbox();
        Mat logoMat1 = logo1.getFeatures(ad1SourcePath);
        Mat logoMat2 = logo2.getFeatures(ad2SourcePath);

        try {
            OutputStream outputStream = new FileOutputStream(outPath);

            if (addpoints.size() == 6) {

                System.out.println("Video Processing Start:");
                while (positionbit < filelength) {
                    Formulas.readImageRGB(Constants.WIDTH, Constants.HEIGHT, vidpath, bufferedImage, positionbit);

                    if (framenumber > addpoints.get(0) && framenumber < addpoints.get(1) ||
                            framenumber > addpoints.get(2) && framenumber < addpoints.get(3) ||
                            framenumber > addpoints.get(4) && framenumber < addpoints.get(5)) {
                        if (beginningCommercial) {
                            outputStream.write(Formulas.getByteArray(bufferedImage));
                        }
                        if (beginningCommercial && framenumber == addpoints.get(1)) {
                            beginningCommercial = false;
                            specialCase = true;
                        }

                        if (originalCommercial) {
                            outputStream.write(Formulas.getByteArray(bufferedImage));
                        }


                    } else if (framenumber == addpoints.get(0)) {
                        if (framenumber == 1 && addpoints.get(0) == 1) {
                            outputStream.write(Formulas.getByteArray(bufferedImage));
                            beginningCommercial = true;
                        } else {
                            System.out.println("ad1count: " + ad1count + " ad2count: " + ad2count);
                            if (ad1count > ad2count && Math.abs(ad1count - ad2count) > Constants.COMPLEXITY_THRESHOLD) {
                                byte[] ad1 = Formulas.adProcessing(ad1VidPath);
                                outputStream.write(ad1);
                                commercialAudioOrder.add(ad1AudPath);
                                System.out.println("Inserting commercial1: " + ad1VidPath);

                            } else if (ad1count < ad2count && Math.abs(ad1count - ad2count) > Constants.COMPLEXITY_THRESHOLD) {
                                byte[] ad2 = Formulas.adProcessing(ad2VidPath);
                                outputStream.write(ad2);
                                commercialAudioOrder.add(ad2AudPath);
                                System.out.println("Inserting commercial1: " + ad2VidPath);
                            } else {
                                originalCommercial = true;
                                commercialAudioOrder.add("SKIPME");
                                System.out.println("SKIPME");
                            }
                            ad1count = 0;
                            ad2count = 0;
                        }
                    } else if (framenumber == addpoints.get(1)) {
                        if (originalCommercial) {
                            originalCommercial = false;
                        }
                    } else if (framenumber == addpoints.get(2)) {
                        System.out.println("ad1count: " + ad1count + " ad2count: " + ad2count);
                        if (ad1count > ad2count && Math.abs(ad1count - ad2count) > Constants.COMPLEXITY_THRESHOLD) {
                            byte[] ad1 = Formulas.adProcessing(ad1VidPath);
                            outputStream.write(ad1);
                            commercialAudioOrder.add(ad1AudPath);
                            System.out.println("Inserting commercial2: " + ad1VidPath);
                        } else if (ad1count < ad2count && Math.abs(ad1count - ad2count) > Constants.COMPLEXITY_THRESHOLD) {
                            byte[] ad2 = Formulas.adProcessing(ad2VidPath);
                            outputStream.write(ad2);
                            commercialAudioOrder.add(ad2AudPath);
                            System.out.println("Inserting commercial2: " + ad2VidPath);
                        } else {
                            originalCommercial = true;
                            commercialAudioOrder.add("SKIPME");
                            System.out.println("SKIPME");
                        }

                        if (specialCase) {
                            if (ad1count > ad2count) {
                                commercialAudioOrder.add(ad1AudPath);
                            } else {
                                commercialAudioOrder.add(ad2AudPath);
                            }
                        }
                        ad1count = 0;
                        ad2count = 0;
                    } else if (framenumber == addpoints.get(3)) {
                        if (originalCommercial) {
                            originalCommercial = false;
                        }
                    } else if (framenumber == addpoints.get(4)) {
                        System.out.println("ad1count: " + ad1count + " ad2count: " + ad2count);
                        if (ad1count > ad2count && Math.abs(ad1count - ad2count) > Constants.COMPLEXITY_THRESHOLD) {
                            byte[] ad1 = Formulas.adProcessing(ad1VidPath);
                            outputStream.write(ad1);
                            commercialAudioOrder.add(ad1AudPath);
                            System.out.println("Inserting commercial2: " + ad1VidPath);
                        } else if (ad1count < ad2count && Math.abs(ad1count - ad2count) > Constants.COMPLEXITY_THRESHOLD) {
                            byte[] ad2 = Formulas.adProcessing(ad2VidPath);
                            outputStream.write(ad2);
                            commercialAudioOrder.add(ad2AudPath);
                            System.out.println("Inserting commercial2: " + ad2VidPath);
                        } else {
                            originalCommercial = true;
                            commercialAudioOrder.add("SKIPME");
                            System.out.println("SKIPME");
                        }

                        if (specialCase) {
                            if (ad1count > ad2count) {
                                commercialAudioOrder.add(ad1AudPath);
                            } else {
                                commercialAudioOrder.add(ad2AudPath);
                            }
                        }
                        ad1count = 0;
                        ad2count = 0;
                    } else if (framenumber == addpoints.get(5)) {
                        if (originalCommercial) {
                            originalCommercial = false;
                        }
                    } else {
                        if (framenumber % 2 == 0) {
                            if (toDrawCorners != null && toDrawCorners.length > 0){
                                Mat frameMat = SIFTFLANNbox.drawBox(bufferedImage,toDrawCorners, boxColor);
                                toDrawCorners = new float [0];
                                boxColor = new Scalar(255,255,255);
                                BufferedImage output = Formulas.Mat2BufferedImage(frameMat);
                                byte[] currentframe = Formulas.getByteArray(output);
                                outputStream.write(currentframe);

                            }else{
                                byte[] currentframe = Formulas.getByteArray(bufferedImage);
                                outputStream.write(currentframe);
                            }


                        } else {

                            SIFTFLANNbox framelogo1 = new SIFTFLANNbox();
                            SIFTFLANNbox framelogo2 = new SIFTFLANNbox();
                            SIFTFLANNbox match1 = new SIFTFLANNbox();
                            SIFTFLANNbox match2 = new SIFTFLANNbox();
                            Mat frameMat1 = framelogo1.getFeaturesBI(bufferedImage);
                            Mat matoutput = match1.getMatches1(logoMat1, frameMat1, logo1.keypoints,
                                    framelogo1.keypoints, logo1.descriptors,
                                    framelogo1.descriptors, MIN_MATCH_COUNT1, AREA_MINIMUM1);
                            Mat matoutput2 = match2.getMatches2(logoMat2, frameMat1, logo2.keypoints,
                                    framelogo1.keypoints, logo2.descriptors,
                                    framelogo1.descriptors, MIN_MATCH_COUNT2, AREA_MINIMUM2);

                            BufferedImage output = Formulas.Mat2BufferedImage(matoutput);
                            BufferedImage output2 = Formulas.Mat2BufferedImage(matoutput2);

                            ad1count += framelogo1.getCount1();
                            ad2count += framelogo2.getCount2();


                            try {
                                if (framelogo1.getCount1() > framelogo2.getCount2()) {
                                    byte[] currentframe = Formulas.getByteArray(output);
                                    toDrawCorners = match1.boxCornerData;
                                    boxColor = new Scalar(0, 255, 0);
                                    outputStream.write(currentframe);

                                } else {
                                    byte[] currentframe = Formulas.getByteArray(output2);
                                    toDrawCorners = match2.boxCornerData;
                                    boxColor = new Scalar(255, 0, 0);
                                    outputStream.write(currentframe);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    positionbit += increment;
                    framenumber++;

                }

            } else {

                System.out.println("Video Processing Start:");
                while (positionbit < filelength) {
                    Formulas.readImageRGB(Constants.WIDTH, Constants.HEIGHT, vidpath, bufferedImage, positionbit);

                    if (framenumber > addpoints.get(0) && framenumber <= addpoints.get(1) ||
                            framenumber > addpoints.get(2) && framenumber <= addpoints.get(3)) {
                        if (beginningCommercial) {
                            outputStream.write(Formulas.getByteArray(bufferedImage));
                        }
                        if (beginningCommercial && framenumber == addpoints.get(1)) {
                            beginningCommercial = false;
                            specialCase = true;
                        }
                    } else if (framenumber == addpoints.get(0)) {
                        if (framenumber == 1 && addpoints.get(0) == 1) {
                            outputStream.write(Formulas.getByteArray(bufferedImage));
                            beginningCommercial = true;
                        } else {
                            System.out.println("ad1count: " + ad1count + " ad2count: " + ad2count);
                            if (ad1count > ad2count) {
                                byte[] ad1 = Formulas.adProcessing(ad1VidPath);
                                outputStream.write(ad1);
                                commercialAudioOrder.add(ad1AudPath);
                                commercialAudioOrder.add(ad2AudPath);
                                System.out.println("Inserting commercial1: " + ad1VidPath);

                            } else {
                                byte[] ad2 = Formulas.adProcessing(ad2VidPath);
                                outputStream.write(ad2);
                                commercialAudioOrder.add(ad2AudPath);
                                commercialAudioOrder.add(ad1AudPath);
                                System.out.println("Inserting commercial1: " + ad2VidPath);
                            }
                            ad1count = 0;
                            ad2count = 0;
                        }
                    } else if (framenumber == addpoints.get(2)) {
                        System.out.println("ad1count: " + ad1count + " ad2count: " + ad2count);
                        if (ad1count > ad2count) {
                            byte[] ad1 = Formulas.adProcessing(ad1VidPath);
                            outputStream.write(ad1);
                            System.out.println("Inserting commercial2: " + ad1VidPath);
                        } else {
                            byte[] ad2 = Formulas.adProcessing(ad2VidPath);
                            outputStream.write(ad2);
                            System.out.println("Inserting commercial2: " + ad2VidPath);
                        }

                        if (specialCase) {
                            if (ad1count > ad2count) {
                                commercialAudioOrder.add(ad1AudPath);
                            } else {
                                commercialAudioOrder.add(ad2AudPath);
                            }
                        }
                        ad1count = 0;
                        ad2count = 0;
                    } else {
                        if (framenumber % 2 == 0) {
                            if (toDrawCorners != null && toDrawCorners.length > 0){
                                Mat frameMat = SIFTFLANNbox.drawBox(bufferedImage,toDrawCorners, boxColor);
                                toDrawCorners = new float [0];
                                boxColor = new Scalar(255,255,255);
                                BufferedImage output = Formulas.Mat2BufferedImage(frameMat);
                                byte[] currentframe = Formulas.getByteArray(output);
                                outputStream.write(currentframe);

                            }else{
                                byte[] currentframe = Formulas.getByteArray(bufferedImage);
                                outputStream.write(currentframe);
                            }


                        } else {

                            SIFTFLANNbox framelogo1 = new SIFTFLANNbox();
                            SIFTFLANNbox framelogo2 = new SIFTFLANNbox();
                            SIFTFLANNbox match1 = new SIFTFLANNbox();
                            SIFTFLANNbox match2 = new SIFTFLANNbox();
                            Mat frameMat1 = framelogo1.getFeaturesBI(bufferedImage);
                            Mat matoutput = match1.getMatches1(logoMat1, frameMat1, logo1.keypoints,
                                    framelogo1.keypoints, logo1.descriptors,
                                    framelogo1.descriptors, MIN_MATCH_COUNT1, AREA_MINIMUM1);
                            Mat matoutput2 = match2.getMatches2(logoMat2, frameMat1, logo2.keypoints,
                                    framelogo1.keypoints, logo2.descriptors,
                                    framelogo1.descriptors, MIN_MATCH_COUNT2, AREA_MINIMUM2);

                            BufferedImage output = Formulas.Mat2BufferedImage(matoutput);
                            BufferedImage output2 = Formulas.Mat2BufferedImage(matoutput2);

                            ad1count += framelogo1.getCount1();
                            ad2count += framelogo2.getCount2();


                            try {
                                if (framelogo1.getCount1() > framelogo2.getCount2()) {
                                    byte[] currentframe = Formulas.getByteArray(output);
                                    toDrawCorners = match1.boxCornerData;
                                    boxColor = new Scalar(0, 255, 0);
                                    outputStream.write(currentframe);

                                } else {
                                    byte[] currentframe = Formulas.getByteArray(output2);
                                    toDrawCorners = match2.boxCornerData;
                                    boxColor = new Scalar(255, 0, 0);
                                    outputStream.write(currentframe);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    positionbit += increment;
                    framenumber++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Video Processing Ended");
        return commercialAudioOrder;
    }
}