package dev.cephelo.musicbox.compat;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.ModBlocks;
import dev.cephelo.musicbox.recipe.MusicboxRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MusicboxRecipeCategory implements IRecipeCategory<MusicboxRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"musicbox");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/jei/musicbox_gui_jei.png");
    public static final ResourceLocation BEACON_TEXTURE = ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/musicbox_beacon_gui.png");

    public static final RecipeType<MusicboxRecipe> MUSICBOX_RECIPE_RECIPE_TYPE = new RecipeType<>(UID, MusicboxRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final RecipeConditionIcon needsBeacon;

    public MusicboxRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 106, 40);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.MUSICBOX));
        this.needsBeacon = new RecipeConditionIcon(helper.createDrawable(BEACON_TEXTURE, 0, 0, 12, 12),
                14, 14, "jeitooltip.musicbox.beacon");
    }

    @Override
    public RecipeType<MusicboxRecipe> getRecipeType() {
        return MUSICBOX_RECIPE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.musicbox.musicbox");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public @Nullable IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MusicboxRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 1).addIngredients(recipe.getIngredients().get(1));
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 23).addIngredients(recipe.getIngredients().get(2));
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 23).addIngredients(recipe.getIngredients().get(3));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 12).addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(MusicboxRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        if (recipe.beacon()) {
            needsBeacon.draw(guiGraphics);
            needsBeacon.tryRenderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}
