package csci576;

public enum Logo {
    AE(0), HRC(1), MCDS(2), NFL(3), STARBUCKS(4), SUBWAY(5), NONE(-1);
    int key;

    Logo(int k) {
        key = k;
    }
}
