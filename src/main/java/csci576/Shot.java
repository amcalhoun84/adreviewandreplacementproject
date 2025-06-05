package csci576;

public class Shot {
    public int start;
    public int end;
    int time;
    public Category category;
    double avgAmp;
    int sampleCount;
    double signChangeFreq;


    public Shot(int startShot, int endShot) {
        start = startShot;
        end = endShot;
        time = endShot - startShot;
        category = Category.EITHER;
        avgAmp = 0;
        sampleCount = 0;

    }

    public int length() {
        return time;
    }

    public void addSample(double s) {
        avgAmp += s;
        sampleCount++;
    }

    public void avgSample() {
        if (sampleCount != 0) {
            avgAmp /= sampleCount;
        }
    }
}
