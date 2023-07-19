package de.maxhenkel.wiretap.events;

import de.maxhenkel.wiretap.wiretap.WiretapManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class LifecycleEvents {

    public static void onServerStopping(MinecraftServer server) {
        WiretapManager.getInstance().clear();
    }

    public static void onDisconnect(ServerGamePacketListenerImpl serverGamePacketListener, MinecraftServer server) {
        WiretapManager.getInstance().onPlayerDisconnect(serverGamePacketListener.getPlayer());
    }
}
