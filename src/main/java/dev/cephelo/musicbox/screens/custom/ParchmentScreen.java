package dev.cephelo.musicbox.screens.custom;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class ParchmentScreen extends Screen {

    private final Player player;

    public ParchmentScreen(Component title, Player player) {
        super(title);
        this.player = player;
    }
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/parchment_gui.png");

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        //super.renderBlurredBackground(partialTick);
    }

    protected void renderBg(GuiGraphics guiGraphics) {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - 256) / 2;
        int y = (height - 256) / 2;

        guiGraphics.blit(TEXTURE, x, y, 256, 256, 0, 0, 256, 256, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // Close when Inventory / ESC key pressed
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null &&
                this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))
        || keyCode == 256) // ESC key
            this.onClose();
        return true;
    }

    // Close when either mouse button clicked
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.onClose();
        return true;
    }

    // Play paper flip sound then close
    @Override
    public void onClose() {
        Minecraft.getInstance().getSoundManager().play(
                new SimpleSoundInstance(ModSounds.PAPER_FLIP.get(), SoundSource.PLAYERS, 1, 1,
                        SoundInstance.createUnseededRandom(), player.blockPosition()));
        super.onClose();
    }
}
