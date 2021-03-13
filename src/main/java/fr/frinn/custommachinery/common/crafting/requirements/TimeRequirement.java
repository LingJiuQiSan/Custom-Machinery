package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.TimeMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.TimeComparator;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class TimeRequirement extends AbstractRequirement<TimeMachineComponent> {

    public static final Codec<TimeRequirement> CODEC = RecordCodecBuilder.create(timeRequirementInstance ->
            timeRequirementInstance.group(
                   TimeComparator.CODEC.listOf().fieldOf("times").forGetter(requirement -> requirement.times)
            ).apply(timeRequirementInstance, TimeRequirement::new)
    );

    private List<TimeComparator> times;

    public TimeRequirement(List<TimeComparator> times) {
        super(MODE.INPUT);
        this.times = times;
    }

    @Override
    public RequirementType getType() {
        return Registration.TIME_REQUIREMENT.get();
    }

    @Override
    public boolean test(TimeMachineComponent component) {
        return this.times.stream().allMatch(comparator -> comparator.compare((int)component.getTime()));
    }

    @Override
    public CraftingResult processStart(TimeMachineComponent component) {
        if(test(component))
            return CraftingResult.success();
        else
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.time.error"));
    }

    @Override
    public CraftingResult processEnd(TimeMachineComponent component) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<TimeMachineComponent> getComponentType() {
        return Registration.TIME_MACHINE_COMPONENT.get();
    }

    @Override
    public IIngredientType<?> getJEIIngredientType() {
        return null;
    }

    @Override
    public Object asJEIIngredient() {
        return null;
    }

    @Override
    public void addJeiIngredients(IIngredients ingredients) {

    }
}