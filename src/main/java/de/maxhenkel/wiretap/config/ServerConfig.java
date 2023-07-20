package de.maxhenkel.wiretap.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;

public class ServerConfig {

    public final ConfigEntry<Double> microphonePickupRange;
    public final ConfigEntry<Integer> commandPermissionLevel;
    public final ConfigEntry<Integer> packetBufferSize;
    public final ConfigEntry<Boolean> anvilCrafting;
    public final ConfigEntry<String> speakerSoundEffect;
    public final ConfigEntry<String> microphoneSkinUrl;
    public final ConfigEntry<String> speakerSkinUrl;

    public ServerConfig(ConfigBuilder builder) {
        microphonePickupRange = builder.doubleEntry(
                "microphone_pickup_range",
                32D,
                1D,
                512D,
                "The range in which microphones can pick up sounds"
        );
        commandPermissionLevel = builder.integerEntry(
                "command_permission_level",
                2,
                0,
                Integer.MAX_VALUE,
                "The permission level required to use the commands"
        );
        packetBufferSize = builder.integerEntry(
                "packet_buffer_size",
                6,
                1,
                Integer.MAX_VALUE,
                "The amount of packets to buffer before playing"
        );
        anvilCrafting = builder.booleanEntry(
                "anvil_crafting",
                true,
                "Whether the items can be crafted in the anvil"
        );
        speakerSoundEffect = builder.stringEntry(
                "speaker_sound_effect",
                "old_speaker",
                "The sound effect to apply to the speaker",
                "Valid values are: 'none', 'old_speaker', 'low_quality'"
        );
        microphoneSkinUrl = builder.stringEntry(
                "microphone_skin_url",
                "http://textures.minecraft.net/texture/ccf0a27d246355e4dcbbd7b369d326cfed7aed1ba04e5dd9ba68cdecc4133d33",
                "The skin url for the microphone block"
        );
        speakerSkinUrl = builder.stringEntry(
                "speaker_skin_url",
                "http://textures.minecraft.net/texture/148a8c55891dec76764449f57ba677be3ee88a06921ca93b6cc7c9611a7af",
                "The skin url for the speaker block"
        );
    }

}
