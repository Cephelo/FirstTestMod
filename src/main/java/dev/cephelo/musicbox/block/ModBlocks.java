package dev.cephelo.musicbox.block;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.custom.ChorusLampBlock;
import dev.cephelo.musicbox.block.custom.ChorusOreBlock;
import dev.cephelo.musicbox.block.custom.MusicboxBlock;
import dev.cephelo.musicbox.block.custom.PedestalBlock;
import dev.cephelo.musicbox.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MusicBoxMod.MODID);

    public static final DeferredBlock<Block> CHORUS_BLOCK = registerBlock("chorus_block", () -> new Block(BlockBehaviour.Properties.of()
            .sound(SoundType.AMETHYST_CLUSTER)
            .strength(3f, 4f)
            .requiresCorrectToolForDrops()
            .instrument(NoteBlockInstrument.CHIME)
    ));

    public static final DeferredBlock<Block> RAW_CHORUS_BLOCK = registerBlock("raw_chorus_block", () -> new Block(BlockBehaviour.Properties.of()
            .sound(SoundType.AMETHYST_CLUSTER)
            .strength(3f, 4f)
            .requiresCorrectToolForDrops()
            .instrument(NoteBlockInstrument.FLUTE)
    ));

    public static final DeferredBlock<Block> CHORUS_ORE = registerBlock("chorus_ore",
            () -> new ChorusOreBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.STONE)
                    .strength(3f, 4f)
                    .requiresCorrectToolForDrops()
                    .randomTicks()
                    .instrument(NoteBlockInstrument.BASEDRUM)
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

    public static final DeferredBlock<Block> CHISELED_CHORUS_BLOCK = registerBlock("chiseled_chorus_block", () -> new Block(BlockBehaviour.Properties.of()
            .sound(SoundType.AMETHYST_CLUSTER)
            .strength(3f, 4f)
            .requiresCorrectToolForDrops()
            .instrument(NoteBlockInstrument.CHIME)
    ));

    public static final DeferredBlock<Block> CUT_CHORUS_BLOCK = registerBlock("cut_chorus_block", () -> new Block(BlockBehaviour.Properties.of()
            .sound(SoundType.AMETHYST_CLUSTER)
            .strength(3f, 4f)
            .requiresCorrectToolForDrops()
            .instrument(NoteBlockInstrument.CHIME)
    ));

    public static final DeferredBlock<StairBlock> CUT_CHORUS_STAIRS = registerBlock("cut_chorus_stairs",
            () -> new StairBlock(ModBlocks.CUT_CHORUS_BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .strength(3f, 4f)
                    .requiresCorrectToolForDrops()
            ));

    public static final DeferredBlock<SlabBlock> CUT_CHORUS_SLAB = registerBlock("cut_chorus_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .strength(3f, 4f)
                    .requiresCorrectToolForDrops()
            ));

    public static final DeferredBlock<DoorBlock> CHORUS_DOOR = registerBlock("chorus_door",
            () -> new DoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(3f, 4f)
                    .requiresCorrectToolForDrops()
            ));

    public static final DeferredBlock<TrapDoorBlock> CHORUS_TRAPDOOR = registerBlock("chorus_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(3f, 4f)
                    .requiresCorrectToolForDrops()
            ));

    public static final DeferredBlock<Block> CHORUS_LAMP = registerBlock("chorus_lamp",
            () -> new ChorusLampBlock(BlockBehaviour.Properties.of()
                    .lightLevel(litBlockEmission(15))
                    .sound(SoundType.GLASS)
                    .strength(3f, 4f)
                    .requiresCorrectToolForDrops()
                    .randomTicks()
    ));

    public static final DeferredBlock<Block> CRYSTAL_BLOCK = registerBlock("crystal_block", () -> new AmethystBlock(BlockBehaviour.Properties.of()
            .sound(SoundType.AMETHYST)
            .strength(3f, 4f)
            .requiresCorrectToolForDrops()
            .instrument(NoteBlockInstrument.CHIME)
    ));

    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return p_50763_ -> p_50763_.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

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
