package dev.cephelo.musicbox.screens.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.screens.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MusicboxScreen extends AbstractContainerScreen<MusicboxMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/musicbox_gui.png");
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/arrow_progress.png");
    private static final ResourceLocation ICON_PLAY =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/icon_play.png");
    private static final ResourceLocation ICON_PAUSE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/icon_pause.png");
    private static final ResourceLocation ICON_CRAFT =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/icon_craft.png");

    private final IconButton previewButton = new IconButton(this.getGuiLeft() + 16, this.getGuiTop() + 16, 16, 16, Component.empty(),
            menu.isPlayingPreviewSound() && !menu.isCrafting() ? ICON_PAUSE : ICON_PLAY, m -> this.menu.pressPreviewButton());
    private final IconButton craftButton = new IconButton(this.getGuiLeft() + 16, this.getGuiTop() + 48, 16, 16, Component.empty(),
            ICON_CRAFT, b -> this.menu.pressCraftButton());

    public MusicboxScreen(MusicboxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(previewButton);
        this.addRenderableWidget(craftButton);
        super.init();
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

    // Progress Arrow/Texture
    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(ARROW_TEXTURE,x + 83, y + 35, 0, 0, menu.getScaledArrowProgress(), 16, 24, 16);
        }
    }

    public void toggleButtons(boolean enablePreviewButton, boolean enableCraftButton) {
        this.previewButton.active = enablePreviewButton;
        this.craftButton.active = enableCraftButton;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        //MusicBoxMod.LOGGER.info("ui: " + this.previewButton.active + " " + this.craftButton.active);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
