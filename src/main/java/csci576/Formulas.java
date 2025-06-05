package csci576;

import org.apache.commons.lang.ArrayUtils;
//import org.apache.commons.lang3.ArrayUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class Formulas {

    public static BufferedImage convertTo3ByteBGRType(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        return convertedImage;
    }

    public static Mat BufferedImage2Mat(BufferedImage image) {
        image = convertTo3ByteBGRType(image);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    public static BufferedImage Mat2BufferedImage(Mat matrix)throws IOException {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".bmp", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }

    public static byte[] getByteArray(BufferedImage img) {

        byte[] bytes = new byte[388800];
        int ind = 0;
        for(int y = 0; y < Constants.HEIGHT; y++) {
            for(int x = 0; x < Constants.WIDTH; x++) {
                //write out to Byte Array
                Color color = new Color(img.getRGB(x,y));
                bytes[ind] = (byte) color.getRed();
                bytes[ind+Constants.HEIGHT*Constants.WIDTH] = (byte) color.getGreen();
                bytes[ind+Constants.HEIGHT*Constants.WIDTH*2] = (byte) color.getBlue();
                ind++;
            }
        }
        return bytes;
    }

    static void readImageRGB(int width, int height, String imgPath, BufferedImage img, long pos) {
        try
        {
            int frameLength = width*height*3;

            File file = new File(imgPath);

            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(pos);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    img.setRGB(x,y,pix);
                    ind++;
                }
            }
        } catch (IOException e){e.printStackTrace();}
    }

    static byte[] adProcessing(String adPath) {
        System.out.println("Ad Video Processing Start:");
        BufferedImage adBufferedImage = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);
        File file = new File(adPath);

        long framenumber = 1;
        long positionbit = 0;
        long filelength = file.length();
        long increment = Constants.PPF;
        byte[] adByte = new byte[(int) increment];
        while (positionbit < filelength) {
            readImageRGB(Constants.WIDTH, Constants.HEIGHT, adPath, adBufferedImage, positionbit);

            if (framenumber == 1) {
                adByte = Formulas.getByteArray(adBufferedImage);
            } else{
                adByte = ArrayUtils.addAll(adByte, Formulas.getByteArray(adBufferedImage));
            }
            //System.out.println("Ad Process Frame: " + framenumber);
            positionbit+=increment;
            framenumber++;
        }
        System.out.println("Ad Video Processing Ended");
        return adByte;
    }

    public static int getAlpha(int color) {
        int alpha = color & 0xff000000;   // bitwise - a/r/g/b
        alpha >>= 24;
        return alpha;
    }

    public static int getRed(int color) {
        int red = color & 0x00ff0000;   // bitwise - a/r/g/b
        red >>= 16;
        return red;
    }

    public static int getGreen(int color) {
        int green = color & 0x0000ff00;   // bitwise - a/r/g/b
        green >>= 16;
        return green;
    }

    public static int getBlue(int color) {
        return color & 0x00ff0000;
    }

}
