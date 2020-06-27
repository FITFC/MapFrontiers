package games.alejandrocoria.mapfrontiers.client.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.ParametersAreNonnullByDefault;

import games.alejandrocoria.mapfrontiers.MapFrontiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ParametersAreNonnullByDefault
@SideOnly(Side.CLIENT)
public class GuiLinkButton extends GuiButton {
    private GuiYesNoCallback screen;
    private GuiSimpleLabel label;
    private String uri;

    public GuiLinkButton(GuiYesNoCallback screen, int componentId, FontRenderer fontRenderer, int x, int y, String label,
            String uri) {
        super(componentId, x, y, fontRenderer.getStringWidth(label) + 8, 16, "");
        this.screen = screen;
        this.x -= width / 2;
        this.label = new GuiSimpleLabel(fontRenderer, x, y + 5, GuiSimpleLabel.Align.Center, TextFormatting.UNDERLINE + label,
                GuiColors.SETTINGS_BUTTON_TEXT);
        this.uri = uri;
    }

    public void openLink() {
        try {
            Desktop.getDesktop().browse(new URI(uri));
        } catch (IOException | URISyntaxException e) {
            MapFrontiers.LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            hovered = (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height);

            if (hovered) {
                label.setColor(GuiColors.SETTINGS_LINK_HIGHLIGHT);
            } else {
                label.setColor(GuiColors.SETTINGS_LINK);
            }

            label.drawLabel(mc, mouseX, mouseY);
        } else {
            hovered = false;
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (enabled && visible && hovered) {
            mc.displayGuiScreen(new GuiConfirmOpenLink(screen, uri, id, false));
            return true;
        }

        return false;
    }
}