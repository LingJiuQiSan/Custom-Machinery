package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class EnergyRequirement extends AbstractRequirement<EnergyMachineComponent> {

    public static final Codec<EnergyRequirement> CODEC = RecordCodecBuilder.create(energyRequirementInstance ->
            energyRequirementInstance.group(
                    Codec.STRING.fieldOf("mode").forGetter(requirement -> requirement.getMode().toString()),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount)
            ).apply(energyRequirementInstance, (mode, amount) -> new EnergyRequirement(MODE.value(mode), amount))
    );

    private int amount;

    public EnergyRequirement(MODE mode, int amount) {
        super(mode);
        this.amount = amount;
    }

    @Override
    public RequirementType getType() {
        return Registration.ENERGY_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getComponentType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(EnergyMachineComponent energy) {

        if(getMode() == MODE.INPUT)
            return energy.getEnergyStored() > this.amount;
        else
            return true;
    }

    @Override
    public CraftingResult processStart(EnergyMachineComponent energy) {
        if(getMode() == MODE.INPUT) {
            int canExtract = energy.extractEnergy(this.amount, true);
            if(canExtract == this.amount) {
                energy.extractEnergy(this.amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new StringTextComponent("Not enough energy, " + this.amount + "FE needed but only " + canExtract + "FE found !"));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(EnergyMachineComponent energy) {
        if (getMode() == MODE.OUTPUT) {
            int canReceive = energy.receiveEnergy(this.amount, true);
            if(canReceive == this.amount) {
                energy.receiveEnergy(this.amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new StringTextComponent("Not enough space for storing " + this.amount + "FE !"));
        }
        return CraftingResult.pass();
    }
}