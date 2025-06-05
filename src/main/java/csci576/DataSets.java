package csci576;

public class DataSets {

    public static final String[] advertLogos = {
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Brand Images\\subway_logo.rgb",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Brand Images\\starbucks_logo.rgb",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Brand Images\\nfl_logo.rgb",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Brand Images\\Mcdonalds_logo.rgb",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Brand Images\\ae_logo.rgb",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Brand Images\\hrc_logo.rgb",

    };

    public static final String[] advertVideos = {
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\Subway_Ad_15s.rgb",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\Starbucks_Ad_15s.rgb",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\nfl_Ad_15s.rgb",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\mcd_Ad_15s.rgb",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\ae_ad_15s.rgb",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\hrc_ad_15s.rgb"
    };

    public static final String[] advertAudios = {
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\Subway_Ad_15s.wav",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\Starbucks_Ad_15s.wav",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\nfl_Ad_15s.wav",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\mcd_Ad_15s.wav",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\ae_ad_15s.wav",
            "C:\\CSCI576\\ABD\\src\\main\\resources\\dataset\\Ads\\hrc_ad_15s.wav"
    };

    public static final String[] minMatches = {
            "8", "10", "8", "25", "8", "20", "8", "10","8", "10"
    };

    public static final String[] areaMinimum = {
            "8000", "3000", "2000", "2000", "2000", "2000", "2000", "2000", "8000", "3000"
    };


    public static String[] getPaths(String video) {
        System.out.println(video);
        String[] paths = new String[11];
        if (video.contains("data_test1.rgb")) {
            System.out.println("VIDEO 1");
            paths[0] = advertLogos[0];
            paths[1] = advertLogos[1];
            paths[2] = advertVideos[0];
            paths[3] = advertVideos[1];
            paths[4] = advertAudios[0];
            paths[5] = advertAudios[1];
            paths[6] = minMatches[0];
            paths[7] = areaMinimum[0];
            paths[8] = minMatches[1];
            paths[9] = areaMinimum[1];
            paths[10] = String.valueOf(false);
        } else if (video.contains("data_test2.rgb")) {
            System.out.println("VIDEO 2");
            paths[0] = advertLogos[2];
            paths[1] = advertLogos[3];
            paths[2] = advertVideos[2];
            paths[3] = advertVideos[3];
            paths[4] = advertAudios[2];
            paths[5] = advertAudios[3];
            paths[6] = minMatches[2];
            paths[7] = areaMinimum[2];
            paths[8] = minMatches[3];
            paths[9] = areaMinimum[3];
            paths[10] = String.valueOf(false);
        } else if (video.contains("data_test3.rgb")) {
            System.out.println("VIDEO 3");
            paths[0] = advertLogos[4];
            paths[1] = advertLogos[5];
            paths[2] = advertVideos[4];
            paths[3] = advertVideos[5];
            paths[4] = advertAudios[4];
            paths[5] = advertAudios[5];
            paths[6] = minMatches[4];
            paths[7] = areaMinimum[4];
            paths[8] = minMatches[5];
            paths[9] = areaMinimum[5];
            paths[10] = String.valueOf(false);
        } else if (video.contains("test1.rgb")) {
            System.out.println("TEST 1");
            paths[0] = advertLogos[0];
            paths[1] = advertLogos[1];
            paths[2] = advertVideos[0];
            paths[3] = advertVideos[1];
            paths[4] = advertAudios[0];
            paths[5] = advertAudios[1];
            paths[6] = minMatches[6];
            paths[7] = areaMinimum[6];
            paths[8] = minMatches[7];
            paths[9] = areaMinimum[7];
            paths[10] = String.valueOf(true);
        } else if (video.contains("test2.rgb")) {
            System.out.println("TEST 2");
            paths[0] = advertLogos[0];
            paths[1] = advertLogos[1];
            paths[2] = advertVideos[0];
            paths[3] = advertVideos[1];
            paths[4] = advertAudios[0];
            paths[5] = advertAudios[1];
            paths[6] = minMatches[8];
            paths[7] = areaMinimum[8];
            paths[8] = minMatches[9];
            paths[9] = areaMinimum[9];
            paths[10] = String.valueOf(true);
        } else {
            System.out.println("Data Set not found");
            System.exit(-1);
        }


        return paths;
    }

}
