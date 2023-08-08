package de.maxhenkel.wiretap.utils;

import java.util.List;

public class AudioUtils {

    public static final short SIZE = 960;

    public static short[] combineAudio(List<short[]> audioParts) {
        short[] result = new short[SIZE];
        int sample;
        for (int i = 0; i < result.length; i++) {
            sample = 0;
            for (short[] audio : audioParts) {
                if (audio == null) {
                    sample += 0;
                } else {
                    sample += audio[i];
                }
            }
            if (sample > Short.MAX_VALUE) {
                result[i] = Short.MAX_VALUE;
            } else if (sample < Short.MIN_VALUE) {
                result[i] = Short.MIN_VALUE;
            } else {
                result[i] = (short) sample;
            }
        }
        return result;
    }

    public static double getDistanceVolume(double maxDistance, double distance) {
        distance = Math.min(distance, maxDistance);
        return (1D - distance / maxDistance);
    }

    public static short[] setVolume(short[] audio, double volume) {
        for (int i = 0; i < audio.length; i++) {
            audio[i] = (short) (audio[i] * volume);
        }
        return audio;
    }

}
