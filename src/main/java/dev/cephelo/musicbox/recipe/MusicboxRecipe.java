package dev.cephelo.musicbox.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

public record MusicboxRecipe (NonNullList<Ingredient> inputs, ItemStack output, boolean preview, boolean beacon, String sound) implements Recipe<MusicboxRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        //NonNullList<Ingredient> itemList = NonNullList.create();
        //for (Ingredient i : inputs) itemList.add(i);
        return inputs;//itemList;
    }
    
    @Override
    public boolean matches(MusicboxRecipeInput musicboxRecipeInput, Level level) {
        if (level.isClientSide()) return false;

        if (musicboxRecipeInput.size() != this.inputs.size()) return false;
        else {
            var nonEmptyItems = new ArrayList<ItemStack>(musicboxRecipeInput.size());
            for (ItemStack item : musicboxRecipeInput.getInputs())
                if (!item.isEmpty()) nonEmptyItems.add(item);
            return RecipeMatcher.findMatches(nonEmptyItems, this.inputs) != null;
        }
//        for (int i = 0; i < inputs.size(); i++) {
//            if (!inputs.get(i).test(musicboxRecipeInput.getItem(i))) return false;
//        }
//
//        return true;
    }


    @Override
    public ItemStack assemble(MusicboxRecipeInput musicboxRecipeInput, HolderLookup.Provider provider) {
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
        return ModRecipes.MUSICBOX_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MUSICBOX_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<MusicboxRecipe> {
        // Read from json file
        public static final MapCodec<MusicboxRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(
                        inputList -> {
                            Ingredient[] aingredient = inputList.toArray(Ingredient[]::new); // Neo skip the empty check and immediately create the array.
                            if (aingredient.length == 0) {
                                return DataResult.error(() -> "No ingredients for musicbox recipe");
                            } else {
                                return aingredient.length > 4
                                        ? DataResult.error(() -> "Too many ingredients for musicbox recipe. The maximum is 4")
                                        : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                            }
                        },
                        DataResult::success
                ).forGetter(MusicboxRecipe::inputs),
                ItemStack.CODEC.fieldOf("result").forGetter(MusicboxRecipe::output),
                Codec.BOOL.optionalFieldOf("enablePreview", true).forGetter(MusicboxRecipe::preview),
                Codec.BOOL.optionalFieldOf("needsBeacon", true).forGetter(MusicboxRecipe::beacon),
                Codec.STRING.fieldOf("soundEvent").forGetter(MusicboxRecipe::sound)
        ).apply(inst, MusicboxRecipe::new));

        // Sent from network to ensure client and server know the same things
        public static final StreamCodec<RegistryFriendlyByteBuf, MusicboxRecipe> STREAM_CODEC =
                //StreamCodec.of(MusicboxRecipe.Serializer::toNetwork, MusicboxRecipe.Serializer::fromNetwork);
                StreamCodec.composite(
                        //Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), MusicboxRecipe::inputs,
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).map(NonNullList::copyOf, Function.identity()), MusicboxRecipe::inputs,
                        ItemStack.STREAM_CODEC, MusicboxRecipe::output,
                        ByteBufCodecs.BOOL, MusicboxRecipe::preview,
                        ByteBufCodecs.BOOL, MusicboxRecipe::beacon,
                        ByteBufCodecs.STRING_UTF8, MusicboxRecipe::sound,
                        MusicboxRecipe::new
                );


        @Override
        public MapCodec<MusicboxRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MusicboxRecipe> streamCodec() {
            return STREAM_CODEC;
        }

//        private static MusicboxRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
//            int i = buffer.readVarInt();
//            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);
//            nonnulllist.replaceAll(p_319735_ -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
//            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
//
//            Boolean p = buffer.readBoolean();
//            Boolean aer = buffer.readBoolean();
//            String s = buffer.readUtf();
//
//            return new MusicboxRecipe(nonnulllist, itemstack, p, aer, s);
//        }
//
//        private static void toNetwork(RegistryFriendlyByteBuf buffer, MusicboxRecipe recipe) {
//            buffer.writeUtf(recipe.sound);
//            buffer.writeBoolean(recipe.preview);
//            buffer.writeBoolean(recipe.beacon);
//
//            for (Ingredient ingredient : recipe.inputs) {
//                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
//            }
//
//            ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
//        }
    }
}
