package csci576;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;

import static java.lang.Math.abs;

class SIFTFLANNbox {
    static int val1 = 0;
    static int val2 = 0;

    public static void setcounter1(Integer value) {
        val1 = value;
    }

    public static int getCount1(){
        return val1;
    }

    public static void setcounter2(Integer value) {
        val2 = value;
    }

    public static int getCount2(){
        return val2;
    }

    // Used for getting features to store for logos or frame
    public Mat img = new Mat();
    public Mat imgBI = new Mat();
    public MatOfKeyPoint keypoints;
    public Mat descriptors;

    public float[] boxCornerData;

    public static Mat drawBox(BufferedImage frame, float [] sceneCornersData, Scalar boxColor){
        Mat imgScene = Formulas.BufferedImage2Mat(frame);

        //-- Draw lines between the corners (the mapped object in the scene - image_2 )
        Imgproc.line(imgScene, new Point(sceneCornersData[0] , sceneCornersData[1]),
                new Point(sceneCornersData[2], sceneCornersData[3]), boxColor, 4);
        Imgproc.line(imgScene, new Point(sceneCornersData[2], sceneCornersData[3]),
                new Point(sceneCornersData[4], sceneCornersData[5]), boxColor, 4);
        Imgproc.line(imgScene, new Point(sceneCornersData[4], sceneCornersData[5]),
                new Point(sceneCornersData[6], sceneCornersData[7]), boxColor, 4);
        Imgproc.line(imgScene, new Point(sceneCornersData[6], sceneCornersData[7]),
                new Point(sceneCornersData[0], sceneCornersData[1]), boxColor, 4);

        return imgScene;
    }

    public Mat getFeatures(String img_path){
        //String img_path = ".\\src\\main\\java\\materials\\dataset\\Brand Images\\nfl_logo.bmp";

//        img = Imgcodecs.imread(img_path, Imgcodecs.IMREAD_COLOR);
        BufferedImage adBufferedImage = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);
        Formulas.readImageRGB(Constants.WIDTH, Constants.HEIGHT, img_path, adBufferedImage, 0);

        img = Formulas.BufferedImage2Mat(adBufferedImage);

        if (img.empty()) {
            System.err.println("Cannot read image!");
            System.exit(0);
        }

        SIFT detector = SIFT.create();

        MatOfKeyPoint _keypoints = new MatOfKeyPoint();
        Mat _descriptors = new Mat();

        detector.detectAndCompute(img, new Mat(), _keypoints, _descriptors);

        keypoints = _keypoints;
        descriptors = _descriptors;

        return img;
    }
    public Mat getFeaturesBI(BufferedImage frameimg){
        //String img_path = ".\\src\\main\\java\\materials\\dataset\\Brand Images\\nfl_logo.bmp";

//        img = Imgcodecs.imread(img_path, Imgcodecs.IMREAD_COLOR);

        imgBI = Formulas.BufferedImage2Mat(frameimg);

        if (imgBI.empty()) {
            System.err.println("Cannot read image!");
            System.exit(0);
        }

        SIFT detector = SIFT.create();

        MatOfKeyPoint _keypoints = new MatOfKeyPoint();
        Mat _descriptors = new Mat();

        detector.detectAndCompute(imgBI, new Mat(), _keypoints, _descriptors);

        keypoints = _keypoints;
        descriptors = _descriptors;

        return imgBI;
    }

    public Mat getMatches1(Mat imgObject, Mat imgScene, MatOfKeyPoint keypointsObject,
                                  MatOfKeyPoint keypointsScene, Mat descriptorsObject,
                                  Mat descriptorsScene, int MIN_MATCH_COUNT,int AREA_MINIMUM){
//        int MIN_MATCH_COUNT = 20;
//        int AREA_MINIMUM = 2000; //subway for dat1d was ~23300, starbucks frame8 was ~ 12000

        if (descriptorsObject.empty() || descriptorsScene.empty()){
            setcounter1(0);
            return imgScene;
        }
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        List<MatOfDMatch> knnMatches = new ArrayList<>();
        matcher.knnMatch(descriptorsObject, descriptorsScene, knnMatches, 2);

        //-- Filter matches using the Lowe's ratio test
        float ratioThresh = 0.7f;   // change this to determine how many matches are found
        List<DMatch> listOfGoodMatches = new ArrayList<>();

        for (MatOfDMatch knnMatch : knnMatches) {
            if (knnMatch.rows() > 1) {
                DMatch[] matches = knnMatch.toArray();
                if (matches[0].distance < ratioThresh * matches[1].distance) {
                    listOfGoodMatches.add(matches[0]);
                }
            }
        }

        if (listOfGoodMatches.size() < MIN_MATCH_COUNT){
            setcounter1(0);
            return imgScene;
        }

        //-- Localize the object
        List<Point> obj = new ArrayList<>();
        List<Point> scene = new ArrayList<>();

        List<KeyPoint> listOfKeypointsObject = keypointsObject.toList();
        List<KeyPoint> listOfKeypointsScene = keypointsScene.toList();
        for (DMatch listOfGoodMatch : listOfGoodMatches) {
            //-- Get the keypoints from the good matches
            obj.add(listOfKeypointsObject.get(listOfGoodMatch.queryIdx).pt);
            scene.add(listOfKeypointsScene.get(listOfGoodMatch.trainIdx).pt);
        }

        MatOfPoint2f objMat = new MatOfPoint2f(), sceneMat = new MatOfPoint2f();
        objMat.fromList(obj);
        sceneMat.fromList(scene);
        double ransacReprojThreshold = 5.0; // tolerance of distance between obj pts and scene pts * H.
        // change threshold to determine how many matches

        Mat mask = new Mat();
        Mat H = Calib3d.findHomography(objMat, sceneMat, Calib3d.LMEDS, ransacReprojThreshold, mask);

        if (H.empty()){
            setcounter1(0);
            return imgScene;
        }

        // to remove bad boxes that stretch way off screen
        // remove to speed up
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (abs(H.get(i, j)[0]) > 2000){
                    setcounter1(0);
                    return imgScene;
                }
            }
        }
        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromList(listOfGoodMatches);

        //-- Get the corners from the image_1 ( the object to be "detected" )
        Mat objCorners = new Mat(4, 1, CvType.CV_32FC2), sceneCorners = new Mat();
        float[] objCornersData = new float[(int) (objCorners.total() * objCorners.channels())];
        objCorners.get(0, 0, objCornersData);
        objCornersData[0] = 0;
        objCornersData[1] = 0;
        objCornersData[2] = imgObject.cols();
        objCornersData[3] = 0;
        objCornersData[4] = imgObject.cols();
        objCornersData[5] = imgObject.rows();
        objCornersData[6] = 0;
        objCornersData[7] = imgObject.rows();
        objCorners.put(0, 0, objCornersData);

        Core.perspectiveTransform(objCorners, sceneCorners, H);
        float[] sceneCornersData = new float[(int) (sceneCorners.total() * sceneCorners.channels())];
        sceneCorners.get(0, 0, sceneCornersData);

        for (int i = 0; i < 8; i++){
            //System.out.println(sceneCornersData[i] + " "  + sceneCornersData[i+1]);
            if (abs(sceneCornersData[i]) > 1000){
                setcounter1(0);
                return imgScene;
            }
        }

        double x1 = new Float(sceneCornersData[0]).doubleValue();
        double x2 = new Float(sceneCornersData[2]).doubleValue();
        double x3 = new Float(sceneCornersData[4]).doubleValue();
        double x4 = new Float(sceneCornersData[6]).doubleValue();

        double y1 = new Float(sceneCornersData[1]).doubleValue();
        double y2 = new Float(sceneCornersData[3]).doubleValue();
        double y3 = new Float(sceneCornersData[5]).doubleValue();
        double y4 = new Float(sceneCornersData[7]).doubleValue();

        double lengthA = distance(x1, x2, y1, y2);
        double lengthB = distance(x2, x3, y2, y3);
        double lengthC = distance(x3, x1, y3, y1);

        int area = (int)triangleArea(lengthA, lengthB, lengthC);

        lengthA = distance(x3, x4, y3, y4);
        lengthB = distance(x4, x1, y4, y1);
        lengthC = distance(x3, x1, y3, y1);

        area = area + (int)triangleArea(lengthA, lengthB, lengthC);

        if (area < AREA_MINIMUM){
            setcounter1(0);
            return imgScene;
        }

        boxCornerData = sceneCornersData;


        //-- Draw lines between the corners (the mapped object in the scene - image_2 )
        Imgproc.line(imgScene, new Point(sceneCornersData[0] , sceneCornersData[1]),
                new Point(sceneCornersData[2], sceneCornersData[3]), new Scalar(0, 255, 0), 4);
        Imgproc.line(imgScene, new Point(sceneCornersData[2], sceneCornersData[3]),
                new Point(sceneCornersData[4], sceneCornersData[5]), new Scalar(0, 255, 0), 4);
        Imgproc.line(imgScene, new Point(sceneCornersData[4], sceneCornersData[5]),
                new Point(sceneCornersData[6], sceneCornersData[7]), new Scalar(0, 255, 0), 4);
        Imgproc.line(imgScene, new Point(sceneCornersData[6], sceneCornersData[7]),
                new Point(sceneCornersData[0], sceneCornersData[1]), new Scalar(0, 255, 0), 4);


        //return frame with box drawn
        setcounter1(1);
        return imgScene;
    }

    public Mat getMatches2(Mat imgObject, Mat imgScene, MatOfKeyPoint keypointsObject,
                                  MatOfKeyPoint keypointsScene, Mat descriptorsObject,
                                  Mat descriptorsScene, int MIN_MATCH_COUNT,int AREA_MINIMUM){

//        int MIN_MATCH_COUNT = 8;
//        int AREA_MINIMUM = 2000; //subway for dat1d was ~23300, starbucks frame8 was ~ 12000

        if (descriptorsObject.empty() || descriptorsScene.empty()){
            setcounter2(0);
            return imgScene;
        }

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        List<MatOfDMatch> knnMatches = new ArrayList<>();
        matcher.knnMatch(descriptorsObject, descriptorsScene, knnMatches, 2);


        //-- Filter matches using the Lowe's ratio test
        float ratioThresh = 0.7f;   // change this to determine how many matches are found
        List<DMatch> listOfGoodMatches = new ArrayList<>();

        for (MatOfDMatch knnMatch : knnMatches) {
            if (knnMatch.rows() > 1) {
                DMatch[] matches = knnMatch.toArray();
                if (matches[0].distance < ratioThresh * matches[1].distance) {
                    listOfGoodMatches.add(matches[0]);
                }
            }
        }

        if (listOfGoodMatches.size() < MIN_MATCH_COUNT){
            setcounter2(0);
            return imgScene;
        }

        //-- Localize the object
        List<Point> obj = new ArrayList<>();
        List<Point> scene = new ArrayList<>();

        List<KeyPoint> listOfKeypointsObject = keypointsObject.toList();
        List<KeyPoint> listOfKeypointsScene = keypointsScene.toList();
        for (DMatch listOfGoodMatch : listOfGoodMatches) {
            //-- Get the keypoints from the good matches
            obj.add(listOfKeypointsObject.get(listOfGoodMatch.queryIdx).pt);
            scene.add(listOfKeypointsScene.get(listOfGoodMatch.trainIdx).pt);
        }

        MatOfPoint2f objMat = new MatOfPoint2f(), sceneMat = new MatOfPoint2f();
        objMat.fromList(obj);
        sceneMat.fromList(scene);
        double ransacReprojThreshold = 5.0; // tolerance of distance between obj pts and scene pts * H.
        // change threshold to determine how many matches

        Mat mask = new Mat();
        Mat H = Calib3d.findHomography(objMat, sceneMat, Calib3d.LMEDS, ransacReprojThreshold, mask);

        if (H.empty()){
            setcounter2(0);
            return imgScene;
        }

        // to remove bad boxes that stretch way off screen
        // remove to speed up
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (abs(H.get(i, j)[0]) > 2000){
                    setcounter2(0);
                    return imgScene;
                }
            }
        }

        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromList(listOfGoodMatches);

        //-- Get the corners from the image_1 ( the object to be "detected" )
        Mat objCorners = new Mat(4, 1, CvType.CV_32FC2), sceneCorners = new Mat();
        float[] objCornersData = new float[(int) (objCorners.total() * objCorners.channels())];
        objCorners.get(0, 0, objCornersData);
        objCornersData[0] = 0;
        objCornersData[1] = 0;
        objCornersData[2] = imgObject.cols();
        objCornersData[3] = 0;
        objCornersData[4] = imgObject.cols();
        objCornersData[5] = imgObject.rows();
        objCornersData[6] = 0;
        objCornersData[7] = imgObject.rows();
        objCorners.put(0, 0, objCornersData);

        Core.perspectiveTransform(objCorners, sceneCorners, H);
        float[] sceneCornersData = new float[(int) (sceneCorners.total() * sceneCorners.channels())];
        sceneCorners.get(0, 0, sceneCornersData);

        double x1 = new Float(sceneCornersData[0]).doubleValue();
        double x2 = new Float(sceneCornersData[2]).doubleValue();
        double x3 = new Float(sceneCornersData[4]).doubleValue();
        double x4 = new Float(sceneCornersData[6]).doubleValue();

        double y1 = new Float(sceneCornersData[1]).doubleValue();
        double y2 = new Float(sceneCornersData[3]).doubleValue();
        double y3 = new Float(sceneCornersData[5]).doubleValue();
        double y4 = new Float(sceneCornersData[7]).doubleValue();

        double lengthA = distance(x1, x2, y1, y2);
        double lengthB = distance(x2, x3, y2, y3);
        double lengthC = distance(x3, x1, y3, y1);

        int area = (int)triangleArea(lengthA, lengthB, lengthC);

        lengthA = distance(x3, x4, y3, y4);
        lengthB = distance(x4, x1, y4, y1);
        lengthC = distance(x3, x1, y3, y1);

        area = area + (int)triangleArea(lengthA, lengthB, lengthC);

        if (area < AREA_MINIMUM){
            setcounter2(0);
            return imgScene;
        }

        boxCornerData = sceneCornersData;

        //-- Draw lines between the corners (the mapped object in the scene - image_2 )
        Imgproc.line(imgScene, new Point(sceneCornersData[0] , sceneCornersData[1]),
                new Point(sceneCornersData[2], sceneCornersData[3]), new Scalar(255, 0, 0), 4);
        Imgproc.line(imgScene, new Point(sceneCornersData[2], sceneCornersData[3]),
                new Point(sceneCornersData[4], sceneCornersData[5]), new Scalar(255, 0, 0), 4);
        Imgproc.line(imgScene, new Point(sceneCornersData[4], sceneCornersData[5]),
                new Point(sceneCornersData[6], sceneCornersData[7]), new Scalar(255, 0, 0), 4);
        Imgproc.line(imgScene, new Point(sceneCornersData[6], sceneCornersData[7]),
                new Point(sceneCornersData[0], sceneCornersData[1]), new Scalar(255, 0, 0), 4);

        //return frame with box drawn
        setcounter2(1);
        return imgScene;
    }

    public static double distance(double _x1, double _x2, double _y1, double _y2){
        return Math.sqrt(Math.pow(_x2-_x1, 2) + Math.pow(_y2-_y1, 2));
    }

    public static double triangleArea(double lengthA, double lengthB, double lengthC){
        return 0.25 * Math.sqrt(
                (lengthA + lengthB + lengthC) *
                        (lengthB + lengthC - lengthA) *
                        (lengthA + lengthC - lengthB) *
                        (lengthA + lengthB - lengthC)
        );
    }

}