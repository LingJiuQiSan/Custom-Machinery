package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import io.netty.handler.codec.EncoderException;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class SUpdateUpgradesPacket extends BaseS2CMessage {

    private final List<MachineUpgrade> upgrades;

    public SUpdateUpgradesPacket(List<MachineUpgrade> upgrades) {
        this.upgrades = upgrades;
    }

    @Override
    public MessageType getType() {
        return PacketManager.UPDATE_UPGRADES;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.upgrades.size());
        this.upgrades.forEach(upgrade -> {
            try {
                buf.writeWithCodec(MachineUpgrade.CODEC, upgrade);
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        });
    }

    public static SUpdateUpgradesPacket decode(FriendlyByteBuf buf) {
        List<MachineUpgrade> upgrades = new ArrayList<>();
        int size = buf.readVarInt();
        for(int i = 0; i < size; i++) {
            try {
                MachineUpgrade upgrade = buf.readWithCodec(MachineUpgrade.CODEC);
                upgrades.add(upgrade);
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        }
        return new SUpdateUpgradesPacket(upgrades);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnv() == EnvType.CLIENT)
            context.queue(() -> {
                CustomMachinery.UPGRADES.refresh(this.upgrades);
            });
    }
}
