package dev.cephelo.musicbox.block.custom;

import net.minecraft.util.StringRepresentable;

public enum MusicboxStatus implements StringRepresentable {
    IDLE("idle"),
    PREVIEW("preview"),
    CRAFTING("crafting");

    private final String name;

    MusicboxStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}