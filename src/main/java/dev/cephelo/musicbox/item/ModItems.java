package dev.cephelo.musicbox.item;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.item.custom.ParchmentItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MusicBoxMod.MODID);

    public static final DeferredItem<Item> CHORUS_INGOT = ITEMS.register("chorus_ingot",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> CHORUS_NUGGET = ITEMS.register("chorus_nugget",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> RAW_CHORUS = ITEMS.register("raw_chorus",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> OLD_PARCHMENT = ITEMS.register("old_parchment",
            () -> new ParchmentItem(new Item.Properties()));

    public static final DeferredItem<Item> CRYSTAL_SHARD = ITEMS.register("crystal_shard",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
