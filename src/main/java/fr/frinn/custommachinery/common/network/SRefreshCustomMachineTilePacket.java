package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SRefreshCustomMachineTilePacket {

    private final BlockPos pos;
    private final ResourceLocation machine;

    public SRefreshCustomMachineTilePacket(BlockPos pos, ResourceLocation machine) {
        this.pos = pos;
        this.machine = machine;
    }

    public static void encode(SRefreshCustomMachineTilePacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeResourceLocation(pkt.machine);
    }

    public static SRefreshCustomMachineTilePacket decode(PacketBuffer buf) {
        BlockPos pos = buf.readBlockPos();
        ResourceLocation machine = buf.readResourceLocation();
        return new SRefreshCustomMachineTilePacket(pos, machine);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
            context.get().enqueueWork(() -> ClientPacketHandler.handleRefreshCustomMachineTilePacket(this.pos, this.machine));
        context.get().setPacketHandled(true);
    }
}