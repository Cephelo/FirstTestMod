package dev.cephelo.musicbox.block.entity;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MusicBoxMod.MODID);

    public  static final Supplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL_BE =
            BLOCK_ENTITIES.register("pedestal_be", () -> BlockEntityType.Builder.of(
                    PedestalBlockEntity::new, ModBlocks.ECHO_PEDESTAL.get()).build(null));

    public  static final Supplier<BlockEntityType<MusicboxBlockEntity>> MUSICBOX_BE =
            BLOCK_ENTITIES.register("musicbox_be", () -> BlockEntityType.Builder.of(
                    MusicboxBlockEntity::new, ModBlocks.MUSICBOX.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
