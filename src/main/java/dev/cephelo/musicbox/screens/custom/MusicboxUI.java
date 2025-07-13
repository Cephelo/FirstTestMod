package dev.cephelo.musicbox.screens.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cephelo.musicbox.MusicBoxMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MusicboxUI extends AbstractContainerScreen<MusicboxMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/musicbox_gui.png");
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/arrow_progress.png");

    public MusicboxUI(MusicboxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    // GUI Background
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y);
    }

    private void renderButtons(GuiGraphics guiGraphics) {
        //guiGraphics.
    }

    // Progress Arrow/Texture
    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(ARROW_TEXTURE,x + 83, y + 35, 0, 0, menu.getScaledArrowProgress(), 16, 24, 16);
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
