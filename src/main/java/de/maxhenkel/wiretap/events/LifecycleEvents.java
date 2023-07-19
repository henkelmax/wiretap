package de.maxhenkel.wiretap.events;

import de.maxhenkel.wiretap.wiretap.WiretapManager;
import net.minecraft.server.MinecraftServer;

public class LifecycleEvents {

    public static void onServerStopping(MinecraftServer server) {
        WiretapManager.getInstance().clear();
    }

}
