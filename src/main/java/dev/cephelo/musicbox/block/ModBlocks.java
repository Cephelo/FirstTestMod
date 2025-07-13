package dev.cephelo.musicbox.block;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.custom.MusicboxBlock;
import dev.cephelo.musicbox.block.custom.PedestalBlock;
import dev.cephelo.musicbox.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MusicBoxMod.MODID);

    public static final DeferredBlock<Block> CHORUS_BLOCK = registerBlock("chorus_block", () -> new Block(BlockBehaviour.Properties.of()
            .sound(SoundType.AMETHYST_CLUSTER)
            .strength(3f, 4f)
            .requiresCorrectToolForDrops()
    ));

    public static final DeferredBlock<Block> RAW_CHORUS_BLOCK = registerBlock("raw_chorus_block", () -> new Block(BlockBehaviour.Properties.of()
            .sound(SoundType.AMETHYST_CLUSTER)
            .strength(3f, 4f)
            .requiresCorrectToolForDrops()
    ));

    public static final DeferredBlock<Block> CHORUS_ORE = registerBlock("chorus_ore", () -> new Block(BlockBehaviour.Properties.of()
            .sound(SoundType.STONE)
            .strength(3f, 4f)
            .requiresCorrectToolForDrops()
    ));

    public static final DeferredBlock<Block> ECHO_PEDESTAL = registerBlock("echo_pedestal",
            () -> new PedestalBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .sound(SoundType.STONE)
                    .strength(3f, 4f)
                    .requiresCorrectToolForDrops()
    ));

    public static final DeferredBlock<Block> MUSICBOX = registerBlock("musicbox",
            () -> new MusicboxBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .sound(SoundType.BAMBOO_WOOD)
                    .strength(3f, 4f)
            ));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
