package de.maxhenkel.wiretap;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.wiretap.wiretap.WiretapManager;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WiretapVoicechatPlugin implements VoicechatPlugin {

    public static String WIRETAP_CATEGORY = "wiretap";

    @Nullable
    public static VoicechatServerApi voicechatServerApi;
    @Nullable
    public static VolumeCategory wiretaps;

    private ExecutorService executorService;

    public WiretapVoicechatPlugin() {
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("WiretapMicrophoneProcessThread");
            thread.setUncaughtExceptionHandler((t, e) -> {
                Wiretap.LOGGER.error("Error in wiretap microphone process thread", e);
            });
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public String getPluginId() {
        return Wiretap.MODID;
    }

    @Override
    public void initialize(VoicechatApi api) {

    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, microphonePacketEvent -> {
            executorService.submit(() -> {
                WiretapManager.getInstance().onMicPacket(microphonePacketEvent);
            });
        });
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        voicechatServerApi = event.getVoicechat();
        wiretaps = voicechatServerApi.volumeCategoryBuilder()
                .setId(WIRETAP_CATEGORY)
                .setName("Wiretaps")
                .setDescription("The volume of wiretap speakers")
                .setIcon(getIcon("category_wiretaps.png"))
                .build();

        voicechatServerApi.registerVolumeCategory(wiretaps);
    }

    @Nullable
    private int[][] getIcon(String path) {
        try {
            Enumeration<URL> resources = WiretapVoicechatPlugin.class.getClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                BufferedImage bufferedImage = ImageIO.read(resources.nextElement().openStream());
                if (bufferedImage.getWidth() != 16) {
                    continue;
                }
                if (bufferedImage.getHeight() != 16) {
                    continue;
                }
                int[][] image = new int[16][16];
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        image[x][y] = bufferedImage.getRGB(x, y);
                    }
                }
                return image;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
