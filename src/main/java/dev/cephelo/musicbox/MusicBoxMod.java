package dev.cephelo.musicbox;

import dev.cephelo.musicbox.block.ModBlocks;
import dev.cephelo.musicbox.block.entity.ModBlockEntities;
import dev.cephelo.musicbox.block.entity.renderer.PedestalBlockEntityRenderer;
import dev.cephelo.musicbox.handler.MBClickButtonPacket;
import dev.cephelo.musicbox.handler.MBToggleButtonPacket;
import dev.cephelo.musicbox.item.ModCreativeModTab;
import dev.cephelo.musicbox.item.ModItems;
import dev.cephelo.musicbox.recipe.ModRecipes;
import dev.cephelo.musicbox.screens.ModMenuTypes;
import dev.cephelo.musicbox.screens.custom.MusicboxScreen;
import dev.cephelo.musicbox.sound.ModSounds;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MusicBoxMod.MODID)
public class MusicBoxMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "musicbox";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MusicBoxMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register Items, Blocks and Menus
        ModCreativeModTab.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModSounds.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
//        // Some common setup code
//        LOGGER.info("HELLO FROM COMMON SETUP");
//
//        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
//            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
//        }
//
//        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());
//
//        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.RAW_CHORUS);
            event.accept(ModItems.CHORUS_INGOT);
            event.accept(ModItems.CHORUS_NUGGET);
            event.accept(ModItems.CRYSTAL_SHARD);
            event.accept(ModItems.OLD_PARCHMENT);
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.CHORUS_BLOCK);
            event.accept(ModBlocks.CHISELED_CHORUS_BLOCK);
            event.accept(ModBlocks.CUT_CHORUS_BLOCK);
            event.accept(ModBlocks.CUT_CHORUS_STAIRS);
            event.accept(ModBlocks.CUT_CHORUS_SLAB);
            event.accept(ModBlocks.CHORUS_DOOR);
            event.accept(ModBlocks.CHORUS_TRAPDOOR);
            event.accept(ModBlocks.CRYSTAL_BLOCK);
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModBlocks.CHORUS_ORE);
            event.accept(ModBlocks.RAW_CHORUS_BLOCK);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.CHORUS_LAMP);
            event.accept(ModBlocks.MUSICBOX);
            event.accept(ModBlocks.ECHO_PEDESTAL);
        }
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModBlocks.CHORUS_LAMP);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
//        // Do something when the server starts
//        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MusicBoxMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
//            // Some client setup code
//            LOGGER.info("HELLO FROM CLIENT SETUP");
//            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        // Combines Pedestal Block Entity with Pedestal Block Entity Renderer
        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL_BE.get(), PedestalBlockEntityRenderer::new);
        }

        // Combines Musicbox Block Entity with Musicbox Screen
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.MUSICBOX_MENU.get(), MusicboxScreen::new);
        }

        @SubscribeEvent // on the mod event bus
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToServer(
                    MBClickButtonPacket.TYPE,
                    MBClickButtonPacket.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            MBClickButtonPacket::handle,
                            MBClickButtonPacket::handle
                    )
            );

            final PayloadRegistrar registrar2 = event.registrar("2");
            registrar2.playToServer(
                    MBToggleButtonPacket.TYPE,
                    MBToggleButtonPacket.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            MBToggleButtonPacket::handle,
                            MBToggleButtonPacket::handle
                    )
            );
        }
    }


}
