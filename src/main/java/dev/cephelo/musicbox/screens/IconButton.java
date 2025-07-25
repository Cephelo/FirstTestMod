package dev.cephelo.musicbox.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public class IconButton extends ExtendedButton {
    private ResourceLocation sprite;
    private final OnPress onPress;

    public IconButton(int x, int y, int width, int height, Component displayString,
                      ResourceLocation sprite, OnPress handler) {
        super(x, y, width, height, displayString, handler);
        this.sprite = sprite;
        this.onPress = handler;

        if (displayString != null) {
            setTooltip(Tooltip.create(displayString));
        }
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    public void setSprite(ResourceLocation sprite) {
        this.sprite = sprite;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(sprite, getX(), getY(), 0, this.active ? 0 : 16, 0, width, height, 32, 16);
    }
}
