package dev.cephelo.musicbox.handler;

import dev.cephelo.musicbox.MusicBoxMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MBSoundHandlerPacket(BlockPos pos, int id, String sound, boolean spedUp) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MBSoundHandlerPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID, "mb_soundhandler_data"));

    public static final StreamCodec<ByteBuf, MBSoundHandlerPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MBSoundHandlerPacket::pos,
            ByteBufCodecs.VAR_INT,
            MBSoundHandlerPacket::id,
            ByteBufCodecs.STRING_UTF8,
            MBSoundHandlerPacket::sound,
            ByteBufCodecs.BOOL,
            MBSoundHandlerPacket::spedUp,
            MBSoundHandlerPacket::new
    );

    public void handle(IPayloadContext context)
    {
        context.enqueueWork(() -> this.handleMainThread(context));
    }

    private void handleMainThread(IPayloadContext context)
    {
        try {
            MusicboxBESoundHandler.handleMethodCall(id(), pos(), sound(), spedUp());
        } catch (Exception e) {
            MusicBoxMod.LOGGER.info("naw shit broke SH");
        }

    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}