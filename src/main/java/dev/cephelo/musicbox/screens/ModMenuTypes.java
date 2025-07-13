package dev.cephelo.musicbox.screens;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.screens.custom.MusicboxMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MusicBoxMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MusicboxMenu>> MUSICBOX_MENU =
            registerMenuTypes("musicbox_menu", MusicboxMenu::new);

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuTypes(String name,
                                                                                                                IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
