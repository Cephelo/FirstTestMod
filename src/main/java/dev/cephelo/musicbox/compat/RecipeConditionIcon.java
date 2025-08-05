package dev.cephelo.musicbox.compat;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class RecipeConditionIcon {
    private final IDrawable icon;
    private final int x;
    private final int y;
    private final String tooltipKey;

    public RecipeConditionIcon(IDrawable icon, int x, int y, String tooltipKey) {
        this.icon = icon;
        this.x = x;
        this.y = y;
        this.tooltipKey = tooltipKey;
    }

    public void draw(GuiGraphics guiGraphics) {
        icon.draw(guiGraphics, x, y);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x &&
                mouseY >= this.y &&
                mouseX < this.x + icon.getWidth() &&
                mouseY < this.y + icon.getHeight();
    }

    public void tryRenderTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (this.isMouseOver(mouseX, mouseY)) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font,
                    Component.translatable(tooltipKey), (int)mouseX, (int)mouseY);
        }
    }
}