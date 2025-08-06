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

import java.util.ArrayList;

public class MusicboxScreen extends AbstractContainerScreen<MusicboxMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/musicbox_gui.png");
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/arrow_progress.png");
    private static final ResourceLocation TRANSPARENT_ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/arrow_transparent.png");
    private static final ResourceLocation BEACON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/musicbox_beacon_gui.png");
    private static final ResourceLocation ICON_PLAY =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/icon_play.png");
    private static final ResourceLocation ICON_PAUSE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/icon_pause.png");
    private static final ResourceLocation ICON_CRAFT =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/icon_craft.png");

    private final ArrayList<IconButton> buttons = new ArrayList<>();
    private boolean hasBeacon;

    public MusicboxScreen(MusicboxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        IconButton previewButton = new IconButton(leftPos + 116, topPos + 59, 16, 16, Component.empty(),
                this.menu.isPlayingPreviewSound() && !menu.isCrafting() ? ICON_PAUSE : ICON_PLAY, m -> this.menu.pressPreviewButton(),
                this.menu.isPlayingPreviewSound() && !menu.isCrafting() ? "tooltip.musicbox.stop_preview" : "tooltip.musicbox.play_preview");
        IconButton craftButton = new IconButton(leftPos + 140, topPos + 35, 16, 16, Component.empty(),
                ICON_CRAFT, b -> this.menu.pressCraftButton(), "tooltip.musicbox.start_craft");

        this.addRenderableWidget(previewButton);
        this.addRenderableWidget(craftButton);

        this.buttons.add(previewButton);
        this.buttons.add(craftButton);

        toggleButtons(false, false, false, false);
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

        renderTransparentArrow(guiGraphics, x, y);
        renderProgressArrow(guiGraphics, x, y);
        renderBeaconSprite(guiGraphics, x, y);
    }

    private void renderTransparentArrow(GuiGraphics guiGraphics, int x, int y) {
        if (this.buttons.get(1).active || this.menu.isCrafting()) {
            guiGraphics.blit(TRANSPARENT_ARROW_TEXTURE,x + 71, y + 23, 0, 0, 40, 40, 40, 40);
        }
    }

    // Progress Arrow/Texture
    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (this.menu.isCrafting()) {
            guiGraphics.blit(ARROW_TEXTURE,x + 71, y + 23, 0, 0, this.menu.getScaledArrowProgress(), 41, 40, 41);
        }
    }

    // If block has active lvl4 beacon under it
    private void renderBeaconSprite(GuiGraphics guiGraphics, int x, int y) {
        if (this.hasBeacon) {
            guiGraphics.blit(BEACON_TEXTURE,x + 45, y + 37, 0, 0, 12, 12, 256, 256);
        }
    }

    public void toggleButtons(boolean enablePreviewButton, boolean enableCraftButton, boolean isPlaying, boolean hasBeacon) {
        this.buttons.get(0).active = enablePreviewButton;
        this.buttons.get(1).active = enableCraftButton;
        this.buttons.get(0).setSprite(isPlaying && enablePreviewButton ? ICON_PAUSE : ICON_PLAY);
        this.buttons.get(0).setTooltipText(isPlaying && enablePreviewButton ? "tooltip.musicbox.stop_preview" : "tooltip.musicbox.play_preview");
        this.hasBeacon = hasBeacon;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        this.buttons.get(0).tryRenderTooltip(pGuiGraphics, pMouseX, pMouseY);
        this.buttons.get(1).tryRenderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
