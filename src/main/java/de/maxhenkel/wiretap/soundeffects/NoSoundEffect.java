package de.maxhenkel.wiretap.soundeffects;

public class NoSoundEffect implements SoundEffect {

    @Override
    public short[] applyEffect(short[] audioData) {
        return audioData;
    }

}
