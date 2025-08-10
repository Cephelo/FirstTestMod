package dev.cephelo.musicbox.handler;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.entity.MusicboxBlockEntity;
import dev.cephelo.musicbox.screens.custom.MusicboxMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MBClickButtonPacket(BlockPos pos, int id) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MBClickButtonPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID, "mb_clickbutton_data"));

    public static final StreamCodec<ByteBuf, MBClickButtonPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MBClickButtonPacket::pos,
            ByteBufCodecs.VAR_INT,
            MBClickButtonPacket::id,
            MBClickButtonPacket::new
    );

    public void handle(IPayloadContext context)
    {
        context.enqueueWork(() -> this.handleMainThread(context));
    }

    private void handleMainThread(IPayloadContext context)
    {
        if (!(context.player() instanceof ServerPlayer player) || !(player.containerMenu instanceof MusicboxMenu))
            return;

        ServerLevel level = player.serverLevel();
        try {
            ((MusicboxBlockEntity)level.getBlockEntity(pos)).handleButtonPress(id());
        } catch (Exception e) {
            MusicBoxMod.LOGGER.info("naw shit broke");
        }

    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}