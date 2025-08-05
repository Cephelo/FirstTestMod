package dev.cephelo.musicbox.compat;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.ModBlocks;
import dev.cephelo.musicbox.recipe.ModRecipes;
import dev.cephelo.musicbox.recipe.MusicboxRecipe;
import dev.cephelo.musicbox.recipe.PedestalRecipe;
import dev.cephelo.musicbox.screens.custom.MusicboxScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class JEIMusicBoxModPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new MusicboxRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new PedestalRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        //IModPlugin.super.registerCategories(registration);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<MusicboxRecipe> musicboxRecipes =
                recipeManager.getAllRecipesFor(ModRecipes.MUSICBOX_TYPE.get()).stream().map(RecipeHolder::value).toList();

        List<PedestalRecipe> pedestalRecipes =
                recipeManager.getAllRecipesFor(ModRecipes.PEDESTAL_TYPE.get()).stream().map(RecipeHolder::value).toList();

        registration.addRecipes(MusicboxRecipeCategory.MUSICBOX_RECIPE_RECIPE_TYPE, musicboxRecipes);
        registration.addRecipes(PedestalRecipeCategory.PEDESTAL_RECIPE_RECIPE_TYPE, pedestalRecipes);
        //IModPlugin.super.registerRecipes(registration);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // "Click on arrow for all recipes" thing
        registration.addRecipeClickArea(MusicboxScreen.class, 71, 23, 40, 40,
                MusicboxRecipeCategory.MUSICBOX_RECIPE_RECIPE_TYPE);
        //IModPlugin.super.registerGuiHandlers(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // So u can tap "U" on block and have it show its recipes
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MUSICBOX.asItem()),
                MusicboxRecipeCategory.MUSICBOX_RECIPE_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ECHO_PEDESTAL.asItem()),
                PedestalRecipeCategory.PEDESTAL_RECIPE_RECIPE_TYPE);

        //IModPlugin.super.registerRecipeCatalysts(registration);
    }
}
