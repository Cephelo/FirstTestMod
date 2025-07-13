package dev.cephelo.musicbox.recipe;

import dev.cephelo.musicbox.MusicBoxMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MusicBoxMod.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MusicBoxMod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MusicboxRecipe>> MUSICBOX_SERIALIZER =
            SERIALIZERS.register("musicbox", MusicboxRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<MusicboxRecipe>> MUSICBOX_TYPE =
            TYPES.register("musicbox", () -> new RecipeType<MusicboxRecipe>() {
                @Override
                public String toString() {
                    return "musicbox";
                }
            });


    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
