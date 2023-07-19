package de.maxhenkel.wiretap.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;

public class ServerConfig {

    public final ConfigEntry<Double> microphonePickupRange;
    public final ConfigEntry<Integer> commandPermissionLevel;
    public final ConfigEntry<Boolean> anvilCrafting;

    public ServerConfig(ConfigBuilder builder) {
        microphonePickupRange = builder.doubleEntry("microphone_pickup_range", 16D, 1D, 512D, "The range in which microphones can pick up sounds");
        commandPermissionLevel = builder.integerEntry("command_permission_level", 2, 0, Integer.MAX_VALUE, "The permission level required to use the commands");
        anvilCrafting = builder.booleanEntry("anvil_crafting", true, "Whether the items can be crafted in the anvil");
    }

}
