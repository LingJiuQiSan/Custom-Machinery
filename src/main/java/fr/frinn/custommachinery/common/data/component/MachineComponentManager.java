package fr.frinn.custommachinery.common.data.component;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.component.*;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.theoneprobe.IProbeInfoComponent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MachineComponentManager implements IMachineComponentManager, INBTSerializable<CompoundNBT> {

    private final List<IMachineComponent> components;
    private final CustomMachineTile tile;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public MachineComponentManager(List<IMachineComponentTemplate<? extends IMachineComponent>> templates, CustomMachineTile tile) {
        this.tile = tile;
        this.components = new ArrayList<>();
        templates.forEach(template -> {
            IMachineComponent component = template.build(this);
            if(component.getType().isSingle())
                this.components.add(component);
            else {
                IComponentHandler handler = this.components.stream().filter(c -> c instanceof IComponentHandler && c.getType() == component.getType()).map(c -> (IComponentHandler)c).findFirst().orElse(null);
                if(handler != null) {
                    handler.putComponent(component);
                } else {
                    handler = component.getType().getHandler(this);
                    handler.putComponent(component);
                    this.components.add(handler);
                }
            }
        });
        Registration.MACHINE_COMPONENT_TYPE_REGISTRY.get().getValues().stream().filter(type -> type.isDefaultComponent() && !this.getComponent(type).isPresent()).forEach(type -> this.components.add(type.getDefaultComponentBuilder().apply(this)));
    }

    @Override
    public List<IMachineComponent> getComponents() {
        return Lists.newArrayList(this.components);
    }

    @Override
    public List<ICapabilityComponent> getCapabilityComponents() {
        return this.components.stream().filter(component -> component instanceof ICapabilityComponent).map(component -> (ICapabilityComponent)component).collect(Collectors.toList());
    }

    @Override
    public List<ISerializableComponent> getSerializableComponents() {
        return this.components.stream().filter(component -> component instanceof ISerializableComponent).map(component -> (ISerializableComponent)component).collect(Collectors.toList());
    }

    @Override
    public List<ITickableComponent> getTickableComponents() {
        return this.components.stream().filter(component -> component instanceof ITickableComponent).map(component -> (ITickableComponent)component).collect(Collectors.toList());
    }

    public List<IProbeInfoComponent> getProbeInfoComponents() {
        return this.components.stream().filter(component -> component instanceof IProbeInfoComponent).map(component -> (IProbeInfoComponent)component).collect(Collectors.toList());
    }

    @Override
    public List<ISyncableStuff> getSyncableComponents() {
        return this.components.stream().filter(component -> component instanceof ISyncableStuff).map(component -> (ISyncableStuff)component).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IMachineComponent> Optional<T> getComponent(MachineComponentType<T> type) {
        return this.components.stream().filter(component -> component.getType() == type).map(component -> (T)component).findFirst();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IMachineComponent> Optional<IComponentHandler<T>> getComponentHandler(MachineComponentType<T> type) {
        return getComponent(type).filter(component -> component instanceof IComponentHandler).map(component -> (IComponentHandler<T>)component);
    }

    @Override
    public List<IComparatorInputComponent> getComparatorInputComponents() {
        return this.components.stream().filter(component -> component instanceof IComparatorInputComponent).map(component -> (IComparatorInputComponent)component).collect(Collectors.toList());
    }

    @Override
    public boolean hasComponent(MachineComponentType<?> type) {
        return this.components.stream().anyMatch(component -> component.getType() == type);
    }

    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        getSyncableComponents().forEach(syncableComponent -> syncableComponent.getStuffToSync(container));
    }

    @Override
    public CustomMachineTile getTile() {
        return this.tile;
    }

    @Override
    public World getWorld() {
        return getTile().getWorld();
    }

    @Override
    public MinecraftServer getServer() {
        return getWorld().getServer();
    }

    public void serverTick() {
        getTickableComponents().forEach(ITickableComponent::serverTick);
    }

    public void clientTick() {
        getTickableComponents().forEach(ITickableComponent::clientTick);
    }

    public void markDirty() {
        this.getTile().markDirty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        getSerializableComponents().forEach(component -> component.serialize(nbt));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        getSerializableComponents().forEach(component -> component.deserialize(nbt));
    }
}
