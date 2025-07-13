package dev.cephelo.musicbox.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record MusicboxRecipeInput(NonNullList<ItemStack> inputs) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index < inputs.size()) {
            return inputs.get(index);
        } else throw new IllegalArgumentException("Recipe does not contain slot " + index);
    }

    public NonNullList<ItemStack> getInputs() {
        return inputs;
    }

    @Override
    public int size() {
        return inputs.size();//4;
    }

    public boolean isEmpty() {
        return this.inputs.isEmpty();
    }

}
