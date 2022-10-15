package fr.frinn.custommachinery.common.crafting;

import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.upgrade.RecipeModifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class CraftingContext implements ICraftingContext {

    private static final Random RAND = new Random();

    private final CraftingManager manager;
    private final UpgradeManager upgrades;
    private final CustomMachineRecipe recipe;

    public CraftingContext(CraftingManager manager, UpgradeManager upgrades, CustomMachineRecipe recipe) {
        this.manager = manager;
        this.upgrades = upgrades;
        this.recipe = recipe;
    }

    @Override
    public CustomMachineTile getMachineTile() {
        return this.manager.getTile();
    }

    @Override
    public CustomMachineRecipe getRecipe() {
        return this.recipe;
    }

    @Override
    public double getRemainingTime() {
        if(getRecipe() == null)
            return 0;
        return getRecipe().getRecipeTime() - this.manager.getRecipeProgressTime();
    }

    @Override
    public double getModifiedSpeed() {
        if(getRecipe() == null)
            return 1;
        int baseTime = getRecipe().getRecipeTime();
        double modifiedTime = getModifiedValue(baseTime, Registration.SPEED_REQUIREMENT.get(), null, null);
        double speed = baseTime / modifiedTime;
        return Math.max(0.01, speed);
    }

    @Override
    public double getModifiedValue(double value, IRequirement<?> requirement, @Nullable String target) {
        return getModifiedValue(value, requirement.getType(), target, requirement.getMode());
    }

    @Override
    public double getPerTickModifiedValue(double value, IRequirement<?> requirement, @Nullable String target) {
        if(this.getRemainingTime() > 0)
            return getModifiedValue(value, requirement, target) * Math.min(this.getModifiedSpeed(), this.getRemainingTime());
        return getModifiedValue(value, requirement, target) * this.getModifiedSpeed();
    }

    private double getModifiedValue(double value, RequirementType<?> type, @Nullable String target, @Nullable RequirementIOMode mode) {
        List<RecipeModifier> toApply = new ArrayList<>();
        this.upgrades.getModifiers(type, target, mode)
                .stream()
                .filter(entry -> entry.getFirst().getChance() > RAND.nextDouble())
                .forEach(entry -> IntStream.range(0, entry.getSecond()).forEach(index -> toApply.add(entry.getFirst())));

        double toAdd = 0.0D;
        double toMult = 1.0D;
        for(RecipeModifier modifier : toApply) {
            switch (modifier.getOperation()) {
                case ADDITION -> toAdd += modifier.getModifier();
                case MULTIPLICATION -> toMult *= modifier.getModifier();
            }
        }
        return (value + toAdd) * toMult;
    }

    public static class Mutable extends CraftingContext {

        private CustomMachineRecipe recipe;

        public Mutable(CraftingManager manager, UpgradeManager upgrades) {
            super(manager, upgrades, null);
        }

        public Mutable setRecipe(CustomMachineRecipe recipe) {
            this.recipe = recipe;
            return this;
        }

        @Override
        public CustomMachineRecipe getRecipe() {
            return this.recipe;
        }
    }
}