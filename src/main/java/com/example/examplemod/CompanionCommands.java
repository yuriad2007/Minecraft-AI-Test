package com.example.examplemod;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CompanionCommands {
    private CompanionCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("companion")
                        .then(Commands.literal("spawn").executes(context -> spawn(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("follow").executes(context -> setFollow(context.getSource().getPlayerOrException(), true)))
                        .then(Commands.literal("stop").executes(context -> setFollow(context.getSource().getPlayerOrException(), false)))
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ask(player, StringArgumentType.getString(context, "message"));
                                    return 1;
                                }))
        );
    }

    private static int spawn(ServerPlayer player) {
        CompanionEntity companion = ExampleMod.COMPANION.get().create(player.serverLevel());
        if (companion == null) {
            player.sendSystemMessage(Component.literal("Could not create the companion."));
            return 0;
        }

        companion.setOwner(player);
        companion.moveTo(player.getX() + 1.5D, player.getY(), player.getZ() + 1.5D, player.getYRot(), 0.0F);
        player.serverLevel().addFreshEntity(companion);
        player.sendSystemMessage(Component.literal(Config.companionName + " has joined you."));
        return 1;
    }

    private static int setFollow(ServerPlayer player, boolean follow) {
        CompanionEntity companion = findCompanion(player);
        if (companion == null) {
            player.sendSystemMessage(Component.literal("You don't have a companion nearby."));
            return 0;
        }
        companion.setFollowing(follow);
        player.sendSystemMessage(Component.literal(follow
                ? Config.companionName + " is following you."
                : Config.companionName + " will stay here."));
        return 1;
    }

    private static void ask(ServerPlayer player, String message) {
        CompanionEntity companion = findCompanion(player);
        if (companion == null) {
            player.sendSystemMessage(Component.literal("Spawn your companion first with /companion spawn."));
            return;
        }

        CompanionMemory.add(player.getUUID(), player.getName().getString(), message);

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return OllamaClient.chat(
                                player.getName().getString(),
                                message,
                                CompanionMemory.getHistory(player.getUUID())
                        );
                    } catch (Exception e) {
                        return new OllamaClient.AiResponse(
                                "I couldn't reach my local AI brain. Make sure Ollama is running.",
                                "NONE"
                        );
                    }
                })
                .thenAccept(response -> player.server.execute(() -> {
                    if (!player.isAlive()) {
                        return;
                    }

                    CompanionMemory.add(player.getUUID(), Config.companionName, response.message());
                    applyAction(companion, response.action());

                    player.sendSystemMessage(Component.literal(
                            "<" + Config.companionName + "> " + response.message()
                    ));
                }));
    }

    private static void applyAction(CompanionEntity companion, String action) {
        switch (action) {
            case "FOLLOW" -> companion.setFollowing(true);
            case "STOP" -> companion.setFollowing(false);
            case "COME" -> companion.comeToOwner();
            default -> {
            }
        }
    }

    private static CompanionEntity findCompanion(ServerPlayer player) {
        UUID owner = player.getUUID();
        return player.serverLevel().getEntitiesOfClass(
                CompanionEntity.class,
                player.getBoundingBox().inflate(64.0D),
                entity -> owner.equals(entity.getOwnerId())
        ).stream().findFirst().orElse(null);
    }
}
