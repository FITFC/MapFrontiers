package games.alejandrocoria.mapfrontiers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import games.alejandrocoria.mapfrontiers.common.ConfigData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class GuiSimpleLabel extends AbstractWidget {
    public enum Align {
        Left, Center, Right
    }

    private final Font font;
    private int scale = 1;
    private int color;
    private final Align align;
    private List<String> texts;
    private List<Integer> widths;
    private ConfigData.Point topLeft;
    private ConfigData.Point bottomRight;

    public GuiSimpleLabel(Font font, int x, int y, Align align, Component text, int color) {
        super(x, y, 0, 0, text);
        this.font = font;
        this.color = color;
        this.align = align;

        setText(text);
    }

    public void setText(Component text) {
        texts = new ArrayList<>();
        widths = new ArrayList<>();

        for (String t : text.getString().split("\\R")) {
            texts.add(t);
            widths.add(font.width(t));
        }

        topLeft = new ConfigData.Point();
        bottomRight = new ConfigData.Point();
        bottomRight.y = texts.size() * 12;

        if (align == Align.Left) {
            for (int i = 0; i < texts.size(); ++i) {
                int width = widths.get(i);
                if (width > bottomRight.x) {
                    bottomRight.x = width;
                }
            }
        } else if (align == Align.Center) {
            for (int i = 0; i < texts.size(); ++i) {
                int halfWidth = widths.get(i) / 2;
                if (-halfWidth < topLeft.x) {
                    topLeft.x = -halfWidth;
                }
                if (halfWidth > bottomRight.x) {
                    bottomRight.x = halfWidth;
                }
            }
        } else {
            for (int i = 0; i < texts.size(); ++i) {
                int width = widths.get(i);
                if (-width < topLeft.x) {
                    topLeft.x = -width;
                }
            }
        }
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getText(int line) {
        return texts.get(line);
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack.scale(scale, scale, 1.0F);

        if (align == Align.Left) {
            for (int i = 0; i < texts.size(); ++i) {
                font.draw(matrixStack, texts.get(i), getX() / scale, (getY() + i * 12) / scale, color);
            }
        } else if (align == Align.Center) {
            for (int i = 0; i < texts.size(); ++i) {
                font.draw(matrixStack, texts.get(i), getX() / scale - (widths.get(i) - 1) / 2, (getY() + i * 12) / scale,
                        color);
            }
        } else {
            for (int i = 0; i < texts.size(); ++i) {
                font.draw(matrixStack, texts.get(i), getX() / scale - widths.get(i), (getY() + i * 12) / scale, color);
            }
        }

        isHovered = (mouseX >= topLeft.x * scale + getX() && mouseY >= topLeft.y * scale + getY() && mouseX < bottomRight.x * scale + getX()
                && mouseY < bottomRight.y * scale + getY());

        matrixStack.scale(1.0F / scale, 1.0F / scale, 1.0F);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
