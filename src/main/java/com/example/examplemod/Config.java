package com.example.examplemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER.define("logDirtBlock", true);
    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER.defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION =
            BUILDER.define("magicNumberIntroduction", "The magic number is... ");
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS =
            BUILDER.defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);
    public static final ForgeConfigSpec.ConfigValue<String> ollamaUrl =
            BUILDER.comment("Local Ollama address.").define("ollamaUrl", "http://127.0.0.1:11434");
    public static final ForgeConfigSpec.ConfigValue<String> ollamaModel =
            BUILDER.comment("Ollama model name, for example qwen3:8b.").define("ollamaModel", "qwen3:8b");
    public static final ForgeConfigSpec.ConfigValue<String> companionName =
            BUILDER.comment("Name displayed above the companion.").define("companionName", "Eliza");
    static final ForgeConfigSpec SPEC = BUILDER.build();
    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;
    public static String ollamaUrlValue;
    public static String ollamaModelValue;
    public static String companionNameValue;
    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName &&
                ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }
    @SubscribeEvent static void onLoad(final ModConfigEvent event) {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
        ollamaUrlValue = ollamaUrl.get();
        ollamaModelValue = ollamaModel.get();
        companionNameValue = companionName.get();
    }
}