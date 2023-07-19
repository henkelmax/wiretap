package de.maxhenkel.wiretap.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.wiretap.Wiretap;
import de.maxhenkel.wiretap.utils.HeadUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class WiretapCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection environment) {
        LiteralArgumentBuilder<CommandSourceStack> literalBuilder = Commands.literal("wiretap")
                .requires((commandSource) -> commandSource.hasPermission(Wiretap.SERVER_CONFIG.commandPermissionLevel.get()));

        literalBuilder.then(Commands.literal("items").executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            UUID id = UUID.randomUUID();
            ItemStack microphone = HeadUtils.createMicrophone(id);
            ItemStack speaker = HeadUtils.createSpeaker(id);
            player.getInventory().add(microphone);
            player.getInventory().add(speaker);
            return 1;
        }));

        dispatcher.register(literalBuilder);
    }

}
