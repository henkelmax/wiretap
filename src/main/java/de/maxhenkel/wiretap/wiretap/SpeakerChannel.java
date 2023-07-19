package de.maxhenkel.wiretap.wiretap;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.wiretap.Wiretap;
import de.maxhenkel.wiretap.WiretapVoicechatPlugin;
import de.maxhenkel.wiretap.utils.AudioUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class SpeakerChannel implements Supplier<short[]> {

    private final UUID id;
    private final Map<UUID, List<short[]>> packetBuffer;
    private final DimensionLocation dimensionLocation;
    private final Map<UUID, OpusDecoder> decoder;
    @Nullable
    private AudioPlayer audioPlayer;

    public SpeakerChannel(UUID id, DimensionLocation dimensionLocation) {
        this.id = id;
        this.dimensionLocation = dimensionLocation;
        packetBuffer = new HashMap<>();
        decoder = new HashMap<>();
    }

    public void addPacket(UUID sender, MicrophonePacket packet) {
        List<short[]> microphonePackets = packetBuffer.computeIfAbsent(sender, k -> new ArrayList<>());

        if (microphonePackets.isEmpty()) {
            for (int i = 0; i < Wiretap.SERVER_CONFIG.packetBufferSize.get(); i++) {
                microphonePackets.add(null);
            }
        }

        OpusDecoder decoder = getDecoder(sender);
        byte[] opusEncodedData = packet.getOpusEncodedData();
        if (opusEncodedData == null || opusEncodedData.length <= 0) {
            decoder.resetState();
            return;
        }
        microphonePackets.add(decoder.decode(opusEncodedData));

        if (audioPlayer == null) {
            getAudioPlayer().startPlaying();
        }
    }

    private OpusDecoder getDecoder(UUID sender) {
        return decoder.computeIfAbsent(sender, k -> WiretapVoicechatPlugin.voicechatServerApi.createDecoder());
    }

    private AudioPlayer getAudioPlayer() {
        if (audioPlayer == null) {
            de.maxhenkel.voicechat.api.ServerLevel serverLevel = WiretapVoicechatPlugin.voicechatServerApi.fromServerLevel(dimensionLocation.getLevel());
            Position position = WiretapVoicechatPlugin.voicechatServerApi.createPosition(dimensionLocation.getX() + 0.5D, dimensionLocation.getY() + 0.5D, dimensionLocation.getZ() + 0.5D);
            LocationalAudioChannel channel = WiretapVoicechatPlugin.voicechatServerApi.createLocationalAudioChannel(id, serverLevel, position);
            channel.setCategory(WiretapVoicechatPlugin.WIRETAP_CATEGORY);
            audioPlayer = WiretapVoicechatPlugin.voicechatServerApi.createAudioPlayer(channel, WiretapVoicechatPlugin.voicechatServerApi.createEncoder(), this);
        }
        return audioPlayer;
    }

    @Nullable
    public short[] generatePacket() {
        List<short[]> packetsToCombine = new ArrayList<>();
        for (Map.Entry<UUID, List<short[]>> packets : packetBuffer.entrySet()) {
            if (packets.getValue().isEmpty()) {
                continue;
            }
            short[] audio = packets.getValue().remove(0);
            if (audio == null) {
                continue;
            }
            packetsToCombine.add(audio);
        }
        packetBuffer.values().removeIf(List::isEmpty);

        if (packetsToCombine.isEmpty()) {
            return null;
        }

        short[] combinedAudio = AudioUtils.combineAudio(packetsToCombine);

        spawnParticle();
        // TODO Add noise effects
        // return AudioUtils.applyRadioEffect(combinedAudio);
        return combinedAudio;
    }

    public void close() {
        decoder.values().forEach(OpusDecoder::close);
    }

    @Override
    public short[] get() {
        short[] audio = generatePacket();
        if (audio == null) {
            audioPlayer = null;
            return null;
        }
        return audio;
    }

    public DimensionLocation getDimensionLocation() {
        return dimensionLocation;
    }

    private long lastParticle = 0L;

    public void spawnParticle() {
        long time = System.currentTimeMillis();
        if (time - lastParticle < 1000L) {
            return;
        }
        lastParticle = time;

        ServerLevel level = dimensionLocation.getLevel();
        Vec3 vec3 = Vec3.atBottomCenterOf(dimensionLocation.getPos()).add(0D, 0.8D, 0D);
        float random = (float) level.getRandom().nextInt(4) / 24F;
        level.players().stream().filter(player -> dimensionLocation.getDistance(player.position()) <= 32D).forEach(player -> {
            level.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, random, 0D, 0D, 1D);
        });
    }

    public void onPlayerDisconnect(ServerPlayer serverPlayer) {
        OpusDecoder remove = decoder.remove(serverPlayer.getUUID());
        if (remove != null) {
            remove.close();
        }
    }
}
