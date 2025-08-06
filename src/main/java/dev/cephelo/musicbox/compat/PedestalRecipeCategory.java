package dev.cephelo.musicbox.compat;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.ModBlocks;
import dev.cephelo.musicbox.recipe.PedestalRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PedestalRecipeCategory implements IRecipeCategory<PedestalRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"pedestal");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/jei/pedestal_gui_jei.png");
    public static final ResourceLocation X_TEXTURE = ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID,"textures/gui/jei/pedestal_x_icon.png");

    public static final RecipeType<PedestalRecipe> PEDESTAL_RECIPE_RECIPE_TYPE = new RecipeType<>(UID, PedestalRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final RecipeConditionIcon consumeX;

    public PedestalRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 106, 40);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ECHO_PEDESTAL));
        this.consumeX = new RecipeConditionIcon(helper.createDrawable(X_TEXTURE, 0, 0, 7, 7),
                17, 29, "jeitooltip.musicbox.consumes");
    }

    @Override
    public RecipeType<PedestalRecipe> getRecipeType() {
        return PEDESTAL_RECIPE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.musicbox.echo_pedestal");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PedestalRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 45, 12).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 12).addIngredients(recipe.getIngredients().get(1));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 78, 12).addItemStack(recipe.getResultItem(null));
        builder.setShapeless();
    }

    @Override
    public void draw(PedestalRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        if (recipe.consume()) {
            consumeX.draw(guiGraphics);
            consumeX.tryRenderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}
