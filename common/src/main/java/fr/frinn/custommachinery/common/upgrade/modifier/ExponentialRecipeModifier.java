package fr.frinn.custommachinery.common.upgrade.modifier;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ExponentialRecipeModifier extends MultiplicationRecipeModifier {

    public ExponentialRecipeModifier(RequirementType<?> requirementType, RequirementIOMode mode, double modifier, String target, double chance, double max, double min, @Nullable Component tooltip) {
        super(requirementType, mode, modifier, target, chance, max, min, tooltip);
    }

    @Override
    public double apply(double original, int upgradeAmount) {
        return Mth.clamp(original * Math.pow(this.modifier, upgradeAmount), this.min, this.max);
    }
}