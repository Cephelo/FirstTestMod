package dev.cephelo.musicbox.item.custom;

import dev.cephelo.musicbox.screens.custom.ParchmentScreen;
import dev.cephelo.musicbox.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ParchmentItem extends Item {
    public ParchmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide()) openScreen(player);
        level.playSound(player, player.blockPosition(), ModSounds.PAPER_FLIP.get(), SoundSource.PLAYERS, 1, 1);
        return InteractionResultHolder.consume(player.getItemInHand(usedHand)); // Generic item use animation
    }

    private static void openScreen(Player player) {
        Minecraft.getInstance().setScreen(new ParchmentScreen(Component.empty(), player));
    }
}
