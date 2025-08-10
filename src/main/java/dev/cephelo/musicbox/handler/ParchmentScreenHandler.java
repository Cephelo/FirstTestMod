package dev.cephelo.musicbox.handler;

import dev.cephelo.musicbox.screens.custom.ParchmentScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ParchmentScreenHandler {
    public static void openScreen(Player player) {
        Minecraft.getInstance().setScreen(new ParchmentScreen(Component.empty(), player));
    }
}
