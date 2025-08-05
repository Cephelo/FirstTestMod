package dev.cephelo.musicbox.screens.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.screens.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;

import static dev.cephelo.musicbox.block.custom.MusicboxBlock.BEACON;

public class MusicboxScreen extends AbstractContainerScreen<MusicboxMenu> /*implements RecipeUpdateListener*/ {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/musicbox_gui.png");
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/arrow_progress.png");
    private static final ResourceLocation BEACON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/musicbox_beacon_gui.png");
    private static final ResourceLocation ICON_PLAY =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/icon_play.png");
    private static final ResourceLocation ICON_PAUSE =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/icon_pause.png");
    private static final ResourceLocation ICON_CRAFT =
            ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/icon_craft.png");

    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    //private boolean widthTooNarrow;
    //private boolean buttonClicked;

    private final ArrayList<IconButton> buttons = new ArrayList<>();
    private boolean hasBeacon;

    public MusicboxScreen(MusicboxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

//        this.widthTooNarrow = this.width < 379;
//        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
//        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
//        this.addRenderableWidget(
//                new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, p_313434_ -> {
//                    this.recipeBookComponent.toggleVisibility();
//                    this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
//                    p_313434_.setPosition(this.leftPos + 104, this.height / 2 - 22);
//                    this.buttonClicked = true;
//                })
//        );
//        this.addWidget(this.recipeBookComponent);

        IconButton previewButton = new IconButton(leftPos + 116, topPos + 59, 16, 16, Component.empty(),
                this.menu.isPlayingPreviewSound() && !menu.isCrafting() ? ICON_PAUSE : ICON_PLAY, m -> this.menu.pressPreviewButton());
        IconButton craftButton = new IconButton(leftPos + 140, topPos + 35, 16, 16, Component.empty(),
                ICON_CRAFT, b -> this.menu.pressCraftButton());

        this.addRenderableWidget(previewButton);
        this.addRenderableWidget(craftButton);

        this.buttons.add(previewButton);
        this.buttons.add(craftButton);

        toggleButtons(false, false, false, false);
    }

//    @Override
//    public void containerTick() {
//        this.recipeBookComponent.tick();
//    }

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
        renderBeaconSprite(guiGraphics, x, y);
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
        this.hasBeacon = hasBeacon;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

//        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
//            this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
//            this.recipeBookComponent.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
//        } else {
//            super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
//            this.recipeBookComponent.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
//            this.recipeBookComponent.renderGhostRecipe(pGuiGraphics, this.leftPos, this.topPos, false, pPartialTick);
//        }

        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        //this.recipeBookComponent.renderTooltip(pGuiGraphics, this.leftPos, this.topPos, pMouseX, pMouseY);
    }

    // RECIPE BOOK STUFF (RecipeBookComponent)

//    /**
//     * Called when a keyboard key is pressed within the GUI element.
//     * <p>
//     * @return {@code true} if the event is consumed, {@code false} otherwise.
//     *
//     * @param keyCode   the key code of the pressed key.
//     * @param scanCode  the scan code of the pressed key.
//     * @param modifiers the keyboard modifiers.
//     */
//    @Override
//    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        return this.recipeBookComponent.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
//    }
//
//    /**
//     * Called when a character is typed within the GUI element.
//     * <p>
//     * @return {@code true} if the event is consumed, {@code false} otherwise.
//     *
//     * @param codePoint the code point of the typed character.
//     * @param modifiers the keyboard modifiers.
//     */
//    @Override
//    public boolean charTyped(char codePoint, int modifiers) {
//        return this.recipeBookComponent.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
//    }
//
//    @Override
//    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
//        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
//    }
//
//    /**
//     * Called when a mouse button is clicked within the GUI element.
//     * <p>
//     * @return {@code true} if the event is consumed, {@code false} otherwise.
//     *
//     * @param mouseX the X coordinate of the mouse.
//     * @param mouseY the Y coordinate of the mouse.
//     * @param button the button that was clicked.
//     */
//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
//            this.setFocused(this.recipeBookComponent);
//            return true;
//        } else {
//            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(mouseX, mouseY, button);
//        }
//    }
//
//    /**
//     * Called when a mouse button is released within the GUI element.
//     * <p>
//     * @return {@code true} if the event is consumed, {@code false} otherwise.
//     *
//     * @param mouseX the X coordinate of the mouse.
//     * @param mouseY the Y coordinate of the mouse.
//     * @param button the button that was released.
//     */
//    @Override
//    public boolean mouseReleased(double mouseX, double mouseY, int button) {
//        if (this.buttonClicked) {
//            this.buttonClicked = false;
//            return true;
//        } else {
//            return super.mouseReleased(mouseX, mouseY, button);
//        }
//    }
//
//    @Override
//    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
//        boolean flag = mouseX < (double)guiLeft
//                || mouseY < (double)guiTop
//                || mouseX >= (double)(guiLeft + this.imageWidth)
//                || mouseY >= (double)(guiTop + this.imageHeight);
//        return this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) && flag;
//    }
//
//    /**
//     * Called when the mouse is clicked over a slot or outside the gui.
//     */
//    @Override
//    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
//        super.slotClicked(slot, slotId, mouseButton, type);
//        this.recipeBookComponent.slotClicked(slot);
//    }
//
//    @Override
//    public void recipesUpdated() {
//        this.recipeBookComponent.recipesUpdated();
//    }
//
//    @Override
//    public RecipeBookComponent getRecipeBookComponent() {
//        return this.recipeBookComponent;
//    }
}
