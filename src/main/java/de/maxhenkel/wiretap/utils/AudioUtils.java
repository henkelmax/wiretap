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
                sample += audio[i];
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

    private static final double AM_MODULATION_FACTOR = 0.3D;
    private static final double AM_MODULATION_FREQUENCY = 600D;
    private static final double FILTER_CUTOFF_FREQUENCY = 4000D;

    public static short[] applyRadioEffect(short[] audioData) {
        int numSamples = audioData.length;
        short[] processedAudio = new short[numSamples];

        // Apply AM modulation
        double modulationPeriod = (1D / AM_MODULATION_FREQUENCY) * 48000D;
        for (int i = 0; i < numSamples; i++) {
            double modulation = Math.sin(2D * Math.PI * (i % modulationPeriod) / modulationPeriod);
            double modulatedSample = audioData[i] * (1D + AM_MODULATION_FACTOR * modulation);
            processedAudio[i] = (short) Math.round(modulatedSample);
        }

        // Apply low-pass filter
        double RC = 1D / (2D * Math.PI * FILTER_CUTOFF_FREQUENCY);
        double dt = 1D / 48000D;
        double alpha = dt / (RC + dt);
        double previousSample = processedAudio[0];

        for (int i = 0; i < numSamples; i++) {
            double currentSample = processedAudio[i];
            double filteredSample = previousSample + alpha * (currentSample - previousSample);
            processedAudio[i] = (short) Math.round(filteredSample);
            previousSample = filteredSample;
        }

        return processedAudio;
    }

}
