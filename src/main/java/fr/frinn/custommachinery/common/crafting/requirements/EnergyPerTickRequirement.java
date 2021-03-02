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

public class EnergyPerTickRequirement extends AbstractTickableRequirement<EnergyMachineComponent> {

    public static final Codec<EnergyPerTickRequirement> CODEC = RecordCodecBuilder.create(energyPerTickRequirementInstance ->
            energyPerTickRequirementInstance.group(
                    Codec.STRING.fieldOf("mode").forGetter(requirement -> requirement.getMode().toString()),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount)
            ).apply(energyPerTickRequirementInstance, (mode, amount) -> new EnergyPerTickRequirement(MODE.value(mode), amount))
    );

    private int amount;

    public EnergyPerTickRequirement(MODE mode, int amount) {
        super(mode);
        this.amount = amount;
    }

    @Override
    public RequirementType getType() {
        return Registration.ENERGY_PER_TICK_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getComponentType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(EnergyMachineComponent energy) {
        return true;
    }

    @Override
    public CraftingResult processStart(EnergyMachineComponent energy) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(EnergyMachineComponent energy) {
        if(getMode() == MODE.INPUT) {
            int canExtract = energy.extractEnergy(this.amount, true);
            if(canExtract == this.amount) {
                energy.extractEnergy(this.amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new StringTextComponent("Not enough energy, " + this.amount + "FE needed but only " + canExtract + "FE found !"));
        }
        else {
            int canReceive = energy.receiveEnergy(this.amount, true);
            if(canReceive == this.amount) {
                energy.receiveEnergy(this.amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(new StringTextComponent("Not enough space for storing " + this.amount + "FE !"));
        }
    }

    @Override
    public CraftingResult processEnd(EnergyMachineComponent energy) {
        return CraftingResult.pass();
    }
}