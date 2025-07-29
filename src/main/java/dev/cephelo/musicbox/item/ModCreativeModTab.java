package dev.cephelo.musicbox.item;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MusicBoxMod.MODID);

    public static final Supplier<CreativeModeTab> MUSICBOX_TAB = CREATIVE_MODE_TAB.register("musicbox_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.MUSICBOX.get()))
                    .title(Component.translatable("creativetab.musicbox.musicbox_tab"))
                    .displayItems((itemDisplayParameters, event) -> {
                        event.accept(ModBlocks.MUSICBOX);
                        event.accept(ModBlocks.ECHO_PEDESTAL);
                        event.accept(ModBlocks.CHORUS_ORE);
                        event.accept(ModBlocks.RAW_CHORUS_BLOCK);
                        event.accept(ModBlocks.CHORUS_BLOCK);
                        event.accept(ModBlocks.CHISELED_CHORUS_BLOCK);
                        event.accept(ModBlocks.CUT_CHORUS_BLOCK);
                        event.accept(ModBlocks.CUT_CHORUS_STAIRS);
                        event.accept(ModBlocks.CUT_CHORUS_SLAB);
                        event.accept(ModBlocks.CHORUS_DOOR);
                        event.accept(ModBlocks.CHORUS_TRAPDOOR);
                        event.accept(ModBlocks.CHORUS_LAMP);
                        event.accept(ModItems.RAW_CHORUS);
                        event.accept(ModItems.CHORUS_INGOT);
                        event.accept(ModItems.CHORUS_NUGGET);
                        event.accept(ModItems.OLD_PARCHMENT);
                        event.accept(ModItems.DISCLET);
                        event.accept(Items.ECHO_SHARD);
                    }).build());

    public static void register (IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
