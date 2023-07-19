package de.maxhenkel.wiretap.soundeffects;

import de.maxhenkel.wiretap.Wiretap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SoundEffectManager {

    private static final Supplier<SoundEffect> DEFAULT_SOUND_EFFECT = NoSoundEffect::new;
    private static final Supplier<SoundEffect> WALKIE_TALKIE_SOUND_EFFECT = WalkieTalkieEffect::new;
    private static final Map<String, Supplier<SoundEffect>> SOUND_EFFECTS = new HashMap<>();

    static {
        SOUND_EFFECTS.put("none", DEFAULT_SOUND_EFFECT);
        SOUND_EFFECTS.put("walkie_talkie", WALKIE_TALKIE_SOUND_EFFECT);
    }

    public static SoundEffect getSoundEffect() {
        return SOUND_EFFECTS.getOrDefault(Wiretap.SERVER_CONFIG.speakerSoundEffect.get(), DEFAULT_SOUND_EFFECT).get();
    }

}
