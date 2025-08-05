package dev.cephelo.musicbox.worldgen;

import dev.cephelo.musicbox.MusicBoxMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModConfiguredFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, MusicBoxMod.MODID);

    public static final DeferredHolder<Feature<?>, OreChorusPlantFeature> ORE_CHORUS_PLANT_FEATURE = FEATURES.register("ore_chorus_plant",
            () -> new OreChorusPlantFeature(OreConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
