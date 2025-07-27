package dev.cephelo.musicbox.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.cephelo.musicbox.MusicBoxMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.function.Function;

public record PedestalRecipe (NonNullList<Ingredient> inputs, ItemStack output, boolean consume) implements Recipe<PedestalRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    @Override
    public boolean matches(PedestalRecipeInput recipeInput, Level level) {
        if (level.isClientSide()) return false;

        if (recipeInput.size() != this.inputs.size()) return false;
        else {
            for (int i = 0; i < this.inputs.size(); i++)
                if (!this.inputs.get(i).test(recipeInput.getItem(i))) return false;

            return true;
        }
    }

    @Override
    public ItemStack assemble(PedestalRecipeInput recipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PEDESTAL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PEDESTAL_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<PedestalRecipe> {
        // Read from json file
        public static final MapCodec<PedestalRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(
                        inputList -> {
                            Ingredient[] aingredient = inputList.toArray(Ingredient[]::new); // Neo skip the empty check and immediately create the array.
                            if (aingredient.length == 0) {
                                return DataResult.error(() -> "No ingredients for pedestal recipe");
                            } else {
                                return aingredient.length > 2
                                        ? DataResult.error(() -> "Too many ingredients for pedestal recipe. The maximum is 2")
                                        : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                            }
                        },
                        DataResult::success
                ).forGetter(PedestalRecipe::inputs),
                ItemStack.CODEC.fieldOf("result").forGetter(PedestalRecipe::output),
                Codec.BOOL.optionalFieldOf("consumeHeldItem", false).forGetter(PedestalRecipe::consume)
        ).apply(inst, PedestalRecipe::new));

        // Sent from network to ensure client and server know the same things
        public static final StreamCodec<RegistryFriendlyByteBuf, PedestalRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).map(NonNullList::copyOf, Function.identity()), PedestalRecipe::inputs,
                        ItemStack.STREAM_CODEC, PedestalRecipe::output,
                        ByteBufCodecs.BOOL, PedestalRecipe::consume,
                        PedestalRecipe::new
                );

        @Override
        public MapCodec<PedestalRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PedestalRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
