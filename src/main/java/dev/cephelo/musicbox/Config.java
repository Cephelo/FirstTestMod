package dev.cephelo.musicbox;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue ORE_SING_CHANCE = BUILDER
            .comment(" Chance for Murichorum Ore to sing on random tick")
            .defineInRange("oreSingChance", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue LAMP_SING_CHANCE = BUILDER
            .comment("\n Chance for Murichorum Lamp to sing on random tick while lit")
            .defineInRange("lampSingChance", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.IntValue LAMP_SING_STRENGTH = BUILDER
            .comment("\n Minimum redstone signal strength for Murichorum Lamp to sing on random tick while lit")
            .defineInRange("lampSingStrength", 15, 1, 15);

    public static final ModConfigSpec.BooleanValue MUSICBOX_PREVIEW_SPEEDUP = BUILDER
            .comment("\n Whether the Music Box should speed up the sound it plays while playing its Preview")
            .define("musicboxPreviewSoundSpeedup", false);

    public static final ModConfigSpec.BooleanValue MUSICBOX_CRAFT_SPEEDUP = BUILDER
            .comment("\n Whether the Music Box should speed up the sound it plays while Crafting")
            .define("musicboxCraftSoundSpeedup", true);

    public static final ModConfigSpec.IntValue MUSICBOX_DEFAULT_PREVIEW_TIME = BUILDER
            .comment("\n Default amount of time a musicbox recipe can play its preview sound for (custom recipes can override this)")
            .defineInRange("musicboxDefaultPreviewTime", 200, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MUSICBOX_DEFAULT_CRAFT_TIME = BUILDER
            .comment("\n Default amount of time a musicbox recipe takes to finish -(custom recipes can override this)")
            .defineInRange("musicboxDefaultCraftTime", 200, 1, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

}
