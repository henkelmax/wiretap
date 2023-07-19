package de.maxhenkel.wiretap.soundeffects;

public class WalkieTalkieEffect implements SoundEffect {

    private static final double SAMPLE_RATE = 48000D;
    private static final double MAX_SHORT = Short.MAX_VALUE;

    private final double normalizedCenterFrequency;
    private final double normalizedBandwidth;

    // State variables for the filter
    private double lastInputSample1;
    private double lastInputSample2;
    private double lastOutputSample1;
    private double lastOutputSample2;

    /**
     * @param centerFrequency center frequency in Hz
     * @param bandwidth       bandwidth in Hz
     */
    public WalkieTalkieEffect(double centerFrequency, double bandwidth) {
        this.normalizedCenterFrequency = 2D * centerFrequency / SAMPLE_RATE;
        this.normalizedBandwidth = 2D * bandwidth / SAMPLE_RATE;
    }

    public WalkieTalkieEffect() {
        this(750, 2000);
    }

    /**
     * Function to apply the walkie-talkie effect on the input audio
     *
     * @param audioData input audio data
     * @return output audio data
     */
    @Override
    public short[] applyEffect(short[] audioData) {
        double[] doubleSamples = new double[audioData.length];
        for (int i = 0; i < audioData.length; i++) {
            doubleSamples[i] = audioData[i] / MAX_SHORT;
        }

        // Apply the bandpass filter
        double maxValue = MAX_SHORT;
        for (int i = 0; i < doubleSamples.length; i++) {
            doubleSamples[i] = bandpassFilter(doubleSamples[i]) * MAX_SHORT;
            if (Math.abs(doubleSamples[i]) > maxValue) {
                maxValue = Math.abs(doubleSamples[i]);
            }
        }

        short[] outputData = new short[audioData.length];
        double factor = MAX_SHORT / maxValue;
        for (int i = 0; i < doubleSamples.length; i++) {
            outputData[i] = (short) (Math.floor(doubleSamples[i] * factor));
        }

        return outputData;
    }

    /**
     * Bandpass filter implementation (basic second-order IIR filter)
     *
     * @param inputSample input sample
     * @return filtered sample
     */
    private double bandpassFilter(double inputSample) {
        double w0 = 2D * Math.PI * normalizedCenterFrequency;
        double alpha = Math.sin(w0) * Math.sinh(Math.log(2D) / 2D * normalizedBandwidth * w0 / Math.sin(w0));
        double a0 = 1D + alpha;

        double b0 = (1D - Math.cos(w0)) / 2D;
        double b1 = 1D - Math.cos(w0);
        double b2 = b0;
        double a1 = -2D * Math.cos(w0);
        double a2 = 1D - alpha;

        // Apply the bandpass filter
        double filteredSample = (b0 * inputSample + b1 * lastInputSample1 + b2 * lastInputSample2 - a1 * lastOutputSample1 - a2 * lastOutputSample2) / a0;

        // Update the state variables for the next iteration
        lastInputSample2 = lastInputSample1;
        lastInputSample1 = inputSample;
        lastOutputSample2 = lastOutputSample1;
        lastOutputSample1 = filteredSample;

        return filteredSample;
    }
}
