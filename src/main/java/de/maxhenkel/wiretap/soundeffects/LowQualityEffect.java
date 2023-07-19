package de.maxhenkel.wiretap.soundeffects;

public class LowQualityEffect implements SoundEffect {

    @Override
    public short[] applyEffect(short[] audio) {
        int reduction = 16;

        short[] result = new short[audio.length];
        for (int i = 0; i < audio.length; i += reduction) {
            int sum = 0;
            for (int j = 0; j < reduction; j++) {
                sum += audio[i + j];
            }
            int avg = sum / reduction;
            for (int j = 0; j < reduction; j++) {
                result[i + j] = (short) avg;
            }
        }

        return result;
    }

}
