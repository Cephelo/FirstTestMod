package dev.cephelo.musicbox.sound;

import dev.cephelo.musicbox.MusicBoxMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MusicBoxMod.MODID);

    public static final Supplier<SoundEvent> PREVIEW_START = registerSoundEvent("preview_start");
    public static final Supplier<SoundEvent> PREVIEW_STOP = registerSoundEvent("preview_stop");
    public static final Supplier<SoundEvent> RECORD_SCRATCH = registerSoundEvent("record_scratch");
    public static final Supplier<SoundEvent> CRAFTING_SHUDDER = registerSoundEvent("crafting_shudder");
    public static final Supplier<SoundEvent> CRAFTING_DONE = registerSoundEvent("crafting_done");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
