package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.apiimpl.network.syncable.IntegerSyncable;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SyncableContainer extends Container {

    private final List<ServerPlayerEntity> players = new ArrayList<>();
    private final List<ISyncable<?, ?>> stuffToSync = new ArrayList<>();

    public SyncableContainer(@Nullable ContainerType<?> type, int id, ISyncableStuff syncableStuff) {
        super(type, id);
        syncableStuff.getStuffToSync(this.stuffToSync::add);
        this.detectAndSendChanges();
    }

    public abstract boolean needFullSync();

    @Override
    public void detectAndSendChanges() {
        if(!this.players.isEmpty()) {
            if(this.needFullSync()) {
                List<IData<?>> toSync = new ArrayList<>();
                for(short id = 0; id < this.stuffToSync.size(); id++)
                    toSync.add(this.stuffToSync.get(id).getData(id));
                this.players.forEach(player -> NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateContainerPacket(this.windowId, toSync)));
                return;
            }
            List<IData<?>> toSync = new ArrayList<>();
            for(short id = 0; id < this.stuffToSync.size(); id++) {
                if(this.stuffToSync.get(id).needSync())
                    toSync.add(this.stuffToSync.get(id).getData(id));
            }
            if(!toSync.isEmpty())
                this.players.forEach(player -> NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateContainerPacket(this.windowId, toSync)));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void handleData(IData<?> data) {
        short id = data.getID();
        ISyncable syncable = this.stuffToSync.get(id);
        if(syncable != null)
            syncable.set(data.getValue());
    }

    @Override
    protected IntReferenceHolder trackInt(IntReferenceHolder intReferenceHolder) {
        this.stuffToSync.add(IntegerSyncable.create(intReferenceHolder::get, intReferenceHolder::set));
        return intReferenceHolder;
    }

    @Override
    protected void trackIntArray(IIntArray array) {
        for(int i = 0; i < array.size(); i++) {
            int index = i;
            this.stuffToSync.add(IntegerSyncable.create(() -> array.get(index), integer -> array.set(index, integer)));
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        if(listener instanceof ServerPlayerEntity)
            this.players.add((ServerPlayerEntity)listener);
    }

    @Override
    public void removeListener(IContainerListener listener) {
        super.removeListener(listener);
        if(listener instanceof ServerPlayerEntity)
            this.players.remove(listener);
    }
}