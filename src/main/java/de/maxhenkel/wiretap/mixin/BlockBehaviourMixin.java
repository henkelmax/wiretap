package de.maxhenkel.wiretap.mixin;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.wiretap.utils.HeadUtils;
import de.maxhenkel.wiretap.wiretap.DimensionLocation;
import de.maxhenkel.wiretap.wiretap.WiretapManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide()) {
            return;
        }
        if (!interactionHand.equals(InteractionHand.MAIN_HAND)) {
            return;
        }
        if (!blockState.getBlock().equals(Blocks.PLAYER_HEAD) && !blockState.getBlock().equals(Blocks.PLAYER_WALL_HEAD)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(blockPos);

        if (!(blockEntity instanceof SkullBlockEntity skullBlockEntity)) {
            return;
        }

        GameProfile profile = skullBlockEntity.getOwnerProfile();
        UUID speaker = HeadUtils.getSpeaker(profile);
        if (speaker == null) {
            return;
        }

        DimensionLocation microphoneLocation = WiretapManager.getInstance().getMicrophoneLocation(speaker);

        boolean verified = WiretapManager.getInstance().verifyMicrophoneLocation(speaker, microphoneLocation);

        if (verified) {
            player.sendSystemMessage(Component.literal("Currently connected to %s".formatted(microphoneLocation)));
        } else {
            if (microphoneLocation != null && !microphoneLocation.isLoaded()) {
                player.sendSystemMessage(Component.literal("Microphone is currently not in a loaded chunk"));
            } else {
                player.sendSystemMessage(Component.literal("Microphone is currently not in a loaded chunk or not connected to a microphone"));
            }
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

}
