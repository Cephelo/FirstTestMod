package dev.cephelo.musicbox.handler;

import dev.cephelo.musicbox.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.HashMap;

public class MusicboxBESoundHandler {
    private static final SoundManager manager = Minecraft.getInstance().getSoundManager();

    private static HashMap<Long, SimpleSoundInstance> previews = new HashMap<Long, SimpleSoundInstance>(){};
    private static HashMap<Long, SimpleSoundInstance> shudders = new HashMap<Long, SimpleSoundInstance>(){};

    private static void setPreviewSound(String sound, BlockPos pos, boolean spedUp) {
        ResourceLocation rl = ResourceLocation.tryParse(sound);
        if (rl == null) return;

        previews.put(pos.asLong(), new SimpleSoundInstance(rl, SoundSource.RECORDS, 1, (spedUp ? 2 : 1), RandomSource.create(), true, 0, SoundInstance.Attenuation.LINEAR, pos.getX(), pos.getY(), pos.getZ(), false));
        playPreviewSound(pos);
    }

    private static void playPreviewSound(BlockPos pos) {
        manager.play(previews.get(pos.asLong()));
    }

    private static void playShudderSound(BlockPos pos) {
        shudders.put(pos.asLong(), new SimpleSoundInstance(ModSounds.CRAFTING_SHUDDER.get(), SoundSource.RECORDS, 1, 1, RandomSource.create(), pos));
        manager.play(shudders.get(pos.asLong()));
    }

    private static void stopPreviewSound(BlockPos pos) {
        manager.stop(previews.get(pos.asLong()));
    }

    private static void stopShudderSound(BlockPos pos) {
        manager.stop(shudders.get(pos.asLong()));
    }

    public static void handleMethodCall(int id, BlockPos pos, String sound, boolean spedUp) {
        switch (id) {
            case 0: {
                setPreviewSound(sound, pos, spedUp);
                break;
            }
            case 1: {
                playPreviewSound(pos);
                break;
            }
            case 2: {
                playShudderSound(pos);
                break;
            }
            case 3: {
                stopPreviewSound(pos);
                break;
            }
            case 4: {
                stopShudderSound(pos);
                break;
            }
            case 5: {
                stopPreviewSound(pos);
                stopShudderSound(pos);
                break;
            }
        }
    }
}
