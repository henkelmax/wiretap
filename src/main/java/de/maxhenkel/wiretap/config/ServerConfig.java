package de.maxhenkel.wiretap.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;

public class ServerConfig {

    public final ConfigEntry<Double> microphonePickupRange;
    public final ConfigEntry<Integer> commandPermissionLevel;
    public final ConfigEntry<Integer> packetBufferSize;
    public final ConfigEntry<Boolean> anvilCrafting;
    public final ConfigEntry<String> speakerSoundEffect;

    public ServerConfig(ConfigBuilder builder) {
        microphonePickupRange = builder.doubleEntry("microphone_pickup_range", 32D, 1D, 512D, "The range in which microphones can pick up sounds");
        commandPermissionLevel = builder.integerEntry("command_permission_level", 2, 0, Integer.MAX_VALUE, "The permission level required to use the commands");
        packetBufferSize = builder.integerEntry("packet_buffer_size", 6, 1, Integer.MAX_VALUE, "The amount of packets to buffer before playing");
        anvilCrafting = builder.booleanEntry("anvil_crafting", true, "Whether the items can be crafted in the anvil");
        speakerSoundEffect = builder.stringEntry(
                "speaker_sound_effect",
                "old_speaker",
                "The sound effect to apply to the speaker",
                "Valid values are: none, old_speaker"
        );
    }

}
