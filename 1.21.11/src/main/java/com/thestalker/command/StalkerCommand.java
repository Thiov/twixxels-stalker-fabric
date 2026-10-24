package com.thestalker.command;

import com.mojang.brigadier.CommandDispatcher;
import com.thestalker.events.StalkerScheduler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class StalkerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, StalkerScheduler scheduler) {
        dispatcher.register(Commands.literal("stalker")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("trigger")
                        .then(Commands.literal("close_stalk").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            scheduler.triggerCloseStalk(player, ctx.getSource().getLevel());
                            return success(ctx.getSource(), "close_stalk");
                        }))
                        .then(Commands.literal("distant_stalk").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            scheduler.triggerDistantStalk(player, ctx.getSource().getLevel());
                            return success(ctx.getSource(), "distant_stalk");
                        }))
                        .then(Commands.literal("behind_scare").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            scheduler.triggerBehindScare(player, ctx.getSource().getLevel());
                            return success(ctx.getSource(), "behind_scare");
                        }))
                        .then(Commands.literal("chase").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            scheduler.triggerChase(player, ctx.getSource().getLevel());
                            return success(ctx.getSource(), "chase");
                        }))
                        .then(Commands.literal("break_event").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            scheduler.triggerBreakEvent(player, ctx.getSource().getLevel());
                            return success(ctx.getSource(), "break_event");
                        }))));
    }

    private static int success(CommandSourceStack src, String name) {
        src.sendSuccess(() -> Component.literal("[Stalker] Triggered: " + name), true);
        return 1;
    }
}
