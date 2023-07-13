package fr.frinn.custommachinery.client.render.element;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElementRenderer;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.data.gui.DumpGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class DumpGuiElementRenderer implements IGuiElementRenderer<DumpGuiElement> {

    private static final List<ITextComponent> TOOLTIPS = Lists.newArrayList(
            new TranslationTextComponent("custommachinery.gui.element.dump.name"),
            new TranslationTextComponent("custommachinery.gui.element.dump.tooltip").mergeStyle(TextFormatting.DARK_RED)
    );

    @Override
    public void renderElement(MatrixStack matrix, DumpGuiElement element, IMachineScreen screen) {
        int posX = element.getX();
        int posY = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();

        Minecraft.getInstance().getTextureManager().bindTexture(element.getTexture());
        AbstractGui.blit(matrix, posX, posY, 0, 0, width, height, width, height);
    }

    @Override
    public void renderTooltip(MatrixStack matrix, DumpGuiElement element, IMachineScreen screen, int mouseX, int mouseY) {
        screen.getScreen().func_243308_b(matrix, TOOLTIPS, mouseX, mouseY);
    }
}