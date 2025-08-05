package dev.cephelo.musicbox.block.entity;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.custom.MusicboxStatus;
import dev.cephelo.musicbox.handler.MBToggleButtonPacket;
import dev.cephelo.musicbox.recipe.ModRecipes;
import dev.cephelo.musicbox.recipe.MusicboxRecipe;
import dev.cephelo.musicbox.recipe.MusicboxRecipeInput;
import dev.cephelo.musicbox.screens.custom.MusicboxMenu;
import dev.cephelo.musicbox.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static dev.cephelo.musicbox.block.custom.MusicboxBlock.BEACON;
import static dev.cephelo.musicbox.block.custom.MusicboxBlock.STATUS;

public class MusicboxBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int INPUT1_SLOT = 0;
    private static final int INPUT2_SLOT = 1;
    private static final int INPUT3_SLOT = 2;
    private static final int INPUT4_SLOT = 3;
    private static final int OUTPUT_SLOT = 4;

    protected final ContainerData data;

    private int progress = 0;
    private int maxProgress = 200;
    private ItemStack itemBeingCrafted = ItemStack.EMPTY;

    private int previewProgress = 0;
    private int maxPreviewProgress = 200;
    private boolean isPlayingPreviewSound = false;

    private final SoundManager manager = Minecraft.getInstance().getSoundManager();
    private SimpleSoundInstance previewSound;
    private final SimpleSoundInstance shudderSound = new SimpleSoundInstance(ModSounds.CRAFTING_SHUDDER.get(), SoundSource.RECORDS, 1, 1, SoundInstance.createUnseededRandom(), this.getBlockPos());

    public MusicboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.MUSICBOX_BE.get(), pos, blockState);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> MusicboxBlockEntity.this.progress;
                    case 1 -> MusicboxBlockEntity.this.maxProgress;
                    case 2 -> MusicboxBlockEntity.this.previewProgress;
                    case 3 -> MusicboxBlockEntity.this.maxPreviewProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: MusicboxBlockEntity.this.progress = value;
                    case 1: MusicboxBlockEntity.this.maxProgress = value;
                    case 2: MusicboxBlockEntity.this.previewProgress = value;
                    case 3: MusicboxBlockEntity.this.maxPreviewProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.musicbox.musicbox");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MusicboxMenu(containerId, inventory, this, this.data);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inv.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.put("inventory", itemHandler.serializeNBT(pRegistries));
        pTag.putInt("musicbox.progress", progress);
        pTag.putInt("musicbox.max_progress", maxProgress);
        pTag.putInt("musicbox.preview_progress", previewProgress);
        pTag.putInt("musicbox.max_preview_progress", maxPreviewProgress);
        pTag.put("musicbox.item_being_crafted", ItemStack.OPTIONAL_CODEC.encodeStart(pRegistries.createSerializationContext(NbtOps.INSTANCE), itemBeingCrafted).result().get());

        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        itemHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        progress = pTag.getInt("musicbox.progress");
        maxProgress = pTag.getInt("musicbox.max_progress");
        previewProgress = pTag.getInt("musicbox.preview_progress");
        maxPreviewProgress = pTag.getInt("musicbox.max_preview_progress");
        itemBeingCrafted = ItemStack.OPTIONAL_CODEC.decode(pRegistries.createSerializationContext(NbtOps.INSTANCE),
                        pTag.get("musicbox.item_being_crafted")).result().get().getFirst();
    }

    public void tick(Level level, BlockPos pos, BlockState state) {

        // Display music note particles like jukebox
        if (isPlayingPreviewSound && previewProgress % (itemBeingCrafted == ItemStack.EMPTY ? 20 : 10) == 0) {
            if (level instanceof ServerLevel serverlevel) {
                Vec3 vec3 = Vec3.atBottomCenterOf(pos).add(0.0, 1.2F, 0.0);
                float f = level.getRandom().nextInt(4) / 24.0F + (this.getBlockState().getValue(BEACON) ? 0.65F : 0.35F);
                serverlevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, f, 0.0, 0.0, 1.0);
            }
        }

        // Beacon check
        boolean beacon = level.getBlockEntity(this.getBlockPos().below()) instanceof BeaconBlockEntity bea && bea.levels >= 4 && !bea.getBeamSections().isEmpty();
        if (this.getBlockState().getValue(BEACON) != beacon)
            level.setBlock(pos, state.setValue(BEACON, beacon), 3);

        if (itemBeingCrafted != ItemStack.EMPTY || progress > 0) {
            progress++;
            if (progress >= maxProgress) {
                // Reset progress
                progress = 0;

                // Put item in output slot
                itemHandler.setStackInSlot(OUTPUT_SLOT, itemBeingCrafted);

                itemBeingCrafted = ItemStack.EMPTY;
                // play finish sound
                manager.stop(shudderSound);
                if (this.level != null) this.level.playSound(null, this.getBlockPos(), ModSounds.CRAFTING_DONE.get(), SoundSource.RECORDS);

                level.setBlock(pos, state.setValue(STATUS, MusicboxStatus.IDLE), 3);
            }

            if (progress == maxProgress - 50) {
                manager.play(shudderSound); // play 11 sound
            }
        }

        if (isPlayingPreviewSound) {
            previewProgress++;
            // Loop preview sound if it's too short
            if (!manager.isActive(previewSound)) playPreviewSound(progress >= 2, false);
        }

        if (progress == 0 && itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            MusicboxRecipe recipe = checkRecipe();
            enableButtons(recipe != null && recipe.preview(), recipe != null);

            if (recipe != null) {
                // show ghost item in output
            } else {
                // hide ghost item in output
            }
        }

        if (previewProgress >= maxPreviewProgress) stopPreviewSound(true, false);

        setChanged(level, pos, state);

    }

    public void handleButtonPress(int id) {
        if (this.level.isClientSide()) return;

        switch (id) {
            case 0: { // previewButton
                this.previewButton();
                break;
            }
            case 1: { // craftButton
                this.attemptDiscWrite();
                break;
            }
        }
    }

    private void previewButton() {
        if (previewProgress < 2) this.playPreviewSound(false, true);
        else this.stopPreviewSound(true, false);
    }

    // When Craft button is clicked
    private void attemptDiscWrite() {
        if (itemBeingCrafted != ItemStack.EMPTY) return;

        MusicboxRecipe recipe = checkRecipe();

        if (recipe != null && itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {

            if (!beaconCheck(recipe.beacon())) return;

            enableButtons(false, false);
            // hide ghost item in output

            // Remove 1 item from each input slot
            itemHandler.extractItem(INPUT1_SLOT, 1, false);
            itemHandler.extractItem(INPUT2_SLOT, 1, false);
            itemHandler.extractItem(INPUT3_SLOT, 1, false);
            itemHandler.extractItem(INPUT4_SLOT, 1, false);

            itemBeingCrafted = recipe.output().copy();
            playPreviewSound(true, true, recipe);

            if (level != null) level.setBlock(this.getBlockPos(), this.getBlockState().setValue(STATUS, MusicboxStatus.CRAFTING), 3);
        } else {
            // play error sound
            if (level != null) this.level.playSound(null, this.getBlockPos(), ModSounds.ERROR.get(), SoundSource.RECORDS);
            MusicBoxMod.LOGGER.info("null recipe");
        }
    }

    private MusicboxRecipe checkRecipe() {
        NonNullList<ItemStack> itemList = NonNullList.create();
        for (int i = 0; i < 4; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                itemList.add(itemHandler.getStackInSlot(i));
            }
        }

        Optional<RecipeHolder<MusicboxRecipe>> recipe =
                this.level.getRecipeManager().getRecipeFor(ModRecipes.MUSICBOX_TYPE.get(),
                        new MusicboxRecipeInput(itemList), level);

        return recipe.map(RecipeHolder::value).orElse(null);//recipe.isEmpty() ? null : recipe.get().value();
    }

    private void playPreviewSound(boolean spedUp, boolean isFirstLoop) {
        playPreviewSound(spedUp, isFirstLoop, null);
    }

        private void playPreviewSound(boolean spedUp, boolean isFirstLoop, MusicboxRecipe recipe) {
        /*MusicboxRecipe*/if (recipe == null) recipe = checkRecipe();
        if (recipe != null && itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            if (isFirstLoop) stopPreviewSound(false, spedUp);

            if (!beaconCheck(recipe.beacon())) return;

            if (this.level != null && isFirstLoop)
                this.level.playSound(null, this.getBlockPos(), ModSounds.PREVIEW_START.get(), SoundSource.RECORDS);

            // play recipe.sound on SoundSource, pitch spedUp+1
            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.tryParse(recipe.sound()));
            if (sound != null) {
                previewSound = new SimpleSoundInstance(sound, SoundSource.RECORDS, 1, (spedUp ? 2 : 1), SoundInstance.createUnseededRandom(), this.getBlockPos());
                manager.play(previewSound);
                isPlayingPreviewSound = true;

                if (!spedUp && level != null) level.setBlock(this.getBlockPos(), this.getBlockState().setValue(STATUS, MusicboxStatus.PREVIEW), 3);
            }
        } else {
            // play error sound
            if (level != null) this.level.playSound(null, this.getBlockPos(), ModSounds.ERROR.get(), SoundSource.RECORDS);
            if (recipe != null) MusicBoxMod.LOGGER.info("could not play sound {} | spedUp: {}", recipe.sound(), spedUp);
            else MusicBoxMod.LOGGER.info("could not play sound [null] | spedUp: {}", spedUp);
        }
    }

    private void stopPreviewSound(boolean playStopSound, boolean craftStop) {
        manager.stop(previewSound);

        // Record Scratch sound plays is crafting is started while preview is playing
        if (previewProgress < maxPreviewProgress && previewProgress > 0 && craftStop && this.level != null)
            this.level.playSound(null, this.getBlockPos(), ModSounds.RECORD_SCRATCH.get(), SoundSource.RECORDS);
        // Otherwise stop sound is played
        else if (this.level != null && playStopSound)
            this.level.playSound(null, this.getBlockPos(), ModSounds.PREVIEW_STOP.get(), SoundSource.RECORDS);

        if (level != null) level.setBlock(this.getBlockPos(), this.getBlockState().setValue(STATUS, MusicboxStatus.IDLE), 3);

        previewProgress = 0;
        isPlayingPreviewSound = false;

    }

    private boolean beaconCheck(boolean needsBeacon) {
        if (!needsBeacon) return true;

//        if (level != null
//                && level.getBlockEntity(this.getBlockPos().below()) instanceof BeaconBlockEntity bea) {
//            return bea.levels >= 4 && !bea.getBeamSections().isEmpty();
//        }

        // Play beacon deactivate sound if recipe needs Lvl4 beacon but does not have one
        if (!this.getBlockState().getValue(BEACON) && level != null) this.level.playSound(null, this.getBlockPos(), ModSounds.BEACON_FAIL.get(), SoundSource.RECORDS);
        return this.getBlockState().getValue(BEACON);
    }

    private void enableButtons(boolean enablePreviewButton, boolean enableCraftButton) {
        // enable/disable buttons
        PacketDistributor.sendToServer(new MBToggleButtonPacket(this.getBlockPos(), enablePreviewButton ? 1 : 0, enableCraftButton ? 1 : 0, isPlayingPreviewSound ? 1 : 0, this.getBlockState().getValue(BEACON) ? 1 : 0));
    }

    // Client-Server Sync
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // Client-Server Sync
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}