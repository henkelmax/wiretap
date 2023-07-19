package de.maxhenkel.wiretap.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class HeadUtils {

    public static final String MICROPHONE = "microphone-aa41dc91-b8f1-4d4e-8c2d-5d95d541748c";
    public static final String SPEAKER = "speaker-aa41dc91-b8f1-4d4e-8c2d-5d95d541748c";

    // https://minecraft-heads.com/custom-heads/decoration/6360-studio-microphone
    public static ItemStack createMicrophone(UUID id) {
        //TODO Make texture configurable
        return createHead("Microphone", id, MICROPHONE, "http://textures.minecraft.net/texture/ccf0a27d246355e4dcbbd7b369d326cfed7aed1ba04e5dd9ba68cdecc4133d33");
    }

    // https://minecraft-heads.com/custom-heads/decoration/215-radio
    public static ItemStack createSpeaker(UUID id) {
        //TODO Make texture configurable
        return createHead("Speaker", id, SPEAKER, "http://textures.minecraft.net/texture/148a8c55891dec76764449f57ba677be3ee88a06921ca93b6cc7c9611a7af");
    }

    @Nullable
    public static UUID getMicrophone(GameProfile profile) {
        if (!profile.getName().equals(MICROPHONE)) {
            return null;
        }
        return profile.getId();
    }

    @Nullable
    public static UUID getSpeaker(GameProfile profile) {
        if (!profile.getName().equals(SPEAKER)) {
            return null;
        }
        return profile.getId();
    }

    public static ItemStack createHead(String itemName, UUID id, String name, String skinUrl) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        CompoundTag tag = stack.getOrCreateTag();

        MutableComponent loreComponent = Component.literal("ID: %s".formatted(id.toString())).withStyle(style -> style.withItalic(false)).withStyle(ChatFormatting.GRAY);
        MutableComponent nameComponent = Component.literal(itemName).withStyle(style -> style.withItalic(false).withColor(ChatFormatting.WHITE));

        ListTag lore = new ListTag();
        lore.add(0, StringTag.valueOf(Component.Serializer.toJson(loreComponent)));
        CompoundTag display = new CompoundTag();
        display.putString(ItemStack.TAG_DISPLAY_NAME, Component.Serializer.toJson(nameComponent));
        display.put(ItemStack.TAG_LORE, lore);
        tag.put(ItemStack.TAG_DISPLAY, display);
        tag.putInt("HideFlags", ItemStack.TooltipPart.ADDITIONAL.getMask());

        GameProfile gameProfile = getGameProfile(id, name, skinUrl);
        tag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), gameProfile));
        return stack;
    }

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private static GameProfile getGameProfile(UUID uuid, String name, String skinUrl) {
        GameProfile gameProfile = new GameProfile(uuid, name);
        PropertyMap properties = gameProfile.getProperties();

        List<Property> textures = new ArrayList<>();


        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textureMap = new HashMap<>();
        textureMap.put(MinecraftProfileTexture.Type.SKIN, new MinecraftProfileTexture(skinUrl, null));

        String json = gson.toJson(new MinecraftTexturesPayload(textureMap));

        String base64Payload = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

        textures.add(new Property("Value", base64Payload));

        properties.putAll("textures", textures);

        return gameProfile;
    }

    private static class MinecraftTexturesPayload {

        private final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures;

        public MinecraftTexturesPayload(Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures) {
            this.textures = textures;
        }

        public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures() {
            return textures;
        }
    }

}
