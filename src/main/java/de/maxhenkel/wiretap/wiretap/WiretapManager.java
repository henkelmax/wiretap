package de.maxhenkel.wiretap.wiretap;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.wiretap.Wiretap;
import de.maxhenkel.wiretap.utils.HeadUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WiretapManager {

    //TODO Regularly check for dead channels
    private final Map<UUID, DimensionLocation> microphones;
    private final Map<UUID, SpeakerChannel> speakers;

    public WiretapManager() {
        microphones = new HashMap<>();
        speakers = new HashMap<>();
    }

    public void onLoadHead(SkullBlockEntity skullBlockEntity) {
        if (!(skullBlockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        GameProfile ownerProfile = skullBlockEntity.getOwnerProfile();
        if (ownerProfile == null) {
            return;
        }

        UUID microphone = HeadUtils.getMicrophone(ownerProfile);
        if (microphone != null) {
            microphones.put(microphone, new DimensionLocation(serverLevel, skullBlockEntity.getBlockPos()));
            return;
        }

        UUID speaker = HeadUtils.getSpeaker(ownerProfile);
        if (speaker != null) {
            speakers.put(speaker, new SpeakerChannel(speaker, new DimensionLocation(serverLevel, skullBlockEntity.getBlockPos())));
            return;
        }
    }

    public List<UUID> getNearbyMicrophones(ServerLevel level, Vec3 pos) {
        double range = Wiretap.SERVER_CONFIG.microphonePickupRange.get();
        return microphones.entrySet().stream().filter(l -> l.getValue().isDimension(level)).filter(l -> l.getValue().getDistance(pos) <= range).map(Map.Entry::getKey).toList();
    }

    public void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) {
            return;
        }
        ServerPlayer player = (ServerPlayer) senderConnection.getPlayer().getPlayer();
        ServerLevel serverLevel = player.serverLevel();

        List<UUID> nearbyMicrophones = getNearbyMicrophones(serverLevel, player.position());

        for (UUID id : nearbyMicrophones) {
            verifyChannel(serverLevel, id);
            if (!microphones.containsKey(id)) {
                continue;
            }
            SpeakerChannel channel = speakers.get(id);
            if (channel == null) {
                continue;
            }
            channel.addPacket(player.getUUID(), event.getPacket());
        }
    }

    private long lastCheck = 0L;

    private void verifyChannel(ServerLevel serverLevel, UUID id) {
        long time = System.currentTimeMillis();
        if (time - lastCheck < 1000L) {
            return;
        }
        lastCheck = time;

        serverLevel.getServer().execute(() -> {
            DimensionLocation dimensionLocation = microphones.get(id);
            if (dimensionLocation == null) {
                return;
            }
            boolean valid = verifyMicrophoneLocation(id, dimensionLocation);
            if (!valid) {
                microphones.remove(id);
            }

            SpeakerChannel channel = speakers.get(id);
            if (channel == null) {
                return;
            }
            valid = verifySpeakerLocation(id, channel);
            if (!valid) {
                channel.close();
                speakers.remove(id);
            }
        });
    }

    private boolean verifyMicrophoneLocation(UUID microphoneId, DimensionLocation location) {
        ServerLevel level = location.getLevel();
        BlockPos pos = location.getPos();
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SkullBlockEntity skullBlockEntity)) {
            return false;
        }
        GameProfile ownerProfile = skullBlockEntity.getOwnerProfile();
        if (ownerProfile == null) {
            return false;
        }
        UUID realMicrophoneId = HeadUtils.getMicrophone(ownerProfile);
        if (realMicrophoneId == null) {
            return false;
        }
        return realMicrophoneId.equals(microphoneId);
    }

    private boolean verifySpeakerLocation(UUID speakerId, SpeakerChannel channel) {
        ServerLevel level = channel.getDimensionLocation().getLevel();
        BlockPos pos = channel.getDimensionLocation().getPos();
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SkullBlockEntity skullBlockEntity)) {
            return false;
        }
        GameProfile ownerProfile = skullBlockEntity.getOwnerProfile();
        if (ownerProfile == null) {
            return false;
        }
        UUID realSpeakerId = HeadUtils.getSpeaker(ownerProfile);
        if (realSpeakerId == null) {
            return false;
        }
        return realSpeakerId.equals(speakerId);
    }

    public void clear() {
        speakers.values().forEach(SpeakerChannel::close);
        speakers.clear();
        microphones.clear();
    }

    private static WiretapManager instance;

    public static WiretapManager getInstance() {
        if (instance == null) {
            instance = new WiretapManager();
        }
        return instance;
    }

    public void removeMicrophone(UUID microphone) {
        microphones.remove(microphone);
    }

    public void removeSpeaker(UUID speaker) {
        SpeakerChannel speakerChannel = speakers.remove(speaker);
        if (speakerChannel != null) {
            speakerChannel.close();
        }
    }
}
