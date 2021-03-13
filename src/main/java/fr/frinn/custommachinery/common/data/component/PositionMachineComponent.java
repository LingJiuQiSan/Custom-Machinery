package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.common.init.Registration;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class PositionMachineComponent extends AbstractMachineComponent {

    public PositionMachineComponent(MachineComponentManager manager) {
        super(manager, Mode.NONE);
    }

    @Override
    public MachineComponentType<PositionMachineComponent> getType() {
        return Registration.POSITION_MACHINE_COMPONENT.get();
    }

    public BlockPos getPosition() {
        return this.getManager().getTile().getPos();
    }

    public Biome getBiome() {
        return this.getManager().getTile().getWorld().getBiome(getPosition());
    }

    public RegistryKey<World> getDimension() {
        return this.getManager().getTile().getWorld().getDimensionKey();
    }

    @Override
    public void serialize(CompoundNBT nbt) {

    }

    @Override
    public void deserialize(CompoundNBT nbt) {

    }

    @Override
    public void addProbeInfo(IProbeInfo info) {

    }
}