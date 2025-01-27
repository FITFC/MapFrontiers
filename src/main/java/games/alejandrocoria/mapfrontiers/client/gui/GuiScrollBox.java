package games.alejandrocoria.mapfrontiers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

import static java.lang.Math.max;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class GuiScrollBox extends AbstractWidget {
    private final int elementHeight;
    private int scrollStart = 0;
    private final int scrollHeight;
    private int scrollBarPos = 0;
    private int scrollBarHeight = 0;
    private boolean scrollBarHovered = false;
    private boolean scrollBarGrabbed = false;
    private int scrollBarGrabbedYPos = 0;
    private final List<ScrollElement> elements;
    private int selected;
    private final ScrollBoxResponder responder;

    public GuiScrollBox(int x, int y, int width, int height, int elementHeight, ScrollBoxResponder responder) {
        super(x, y, width, max(height, elementHeight), CommonComponents.EMPTY);
        elements = new ArrayList<>();
        selected = -1;
        this.elementHeight = elementHeight;
        scrollHeight = this.height / elementHeight;
        this.height = scrollHeight * elementHeight;
        this.responder = responder;
    }

    public List<ScrollElement> getElements() {
        return elements;
    }

    public void addElement(ScrollElement element) {
        element.setX(getX());
        element.setY(getY() + elements.size() * elementHeight);
        elements.add(element);
        scrollBarGrabbed = false;
        updateScrollWindow();
        updateScrollBar();
    }

    public void selectElement(ScrollElement element) {
        selected = elements.indexOf(element);
    }

    public ScrollElement getSelectedElement() {
        if (selected >= 0 && selected < elements.size()) {
            return elements.get(selected);
        }

        return null;
    }

    public ScrollElement getElementIf(Predicate<ScrollElement> pred) {
        return elements.stream()
                .filter(pred)
                .findFirst()
                .orElse(null);
    }

    public void selectElementIf(Predicate<ScrollElement> pred) {
        ScrollElement element = elements.stream()
                .filter(pred)
                .findFirst()
                .orElse(null);

        if (element == null) {
            selected = -1;
        } else {
            selectElement(element);
        }
    }

    public void removeElement(ScrollElement element) {
        ListIterator<ScrollElement> it = elements.listIterator();
        while (it.hasNext()) {
            if (it.next() == element) {
                removeElement(element, it);
                return;
            }
        }
    }

    private void removeElement(ScrollElement element, ListIterator<ScrollElement> it) {
        element.delete();
        it.remove();

        if (selected == elements.size()) {
            selected = elements.size() - 1;
        }

        for (int i = 0; i < elements.size(); ++i) {
            elements.get(i).setY(getY() + i * elementHeight);
        }

        scrollBarGrabbed = false;
        updateScrollWindow();
        updateScrollBar();

        if (responder != null) {
            responder.elementDelete(this, element);
        }
    }

    public void removeAll() {
        for (ScrollElement element : elements) {
            element.delete();
        }

        elements.clear();
        selected = -1;
        scrollBarGrabbed = false;
        updateScrollWindow();
        updateScrollBar();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (visible && (isHovered || scrollBarHovered) && !scrollBarGrabbed) {
            int amount = (int) -delta;
            if (amount < 0 && scrollStart == 0) {
                return false;
            } else if (amount > 0 && scrollStart + scrollHeight >= elements.size()) {
                return false;
            }

            scrollStart += amount;
            updateScrollWindow();
            updateScrollBar();
            return true;
        }

        return false;
    }

    public void scrollBottom() {
        scrollStart = elements.size() - scrollHeight;
        scrollBarGrabbed = false;
        updateScrollWindow();
        updateScrollBar();
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < elements.size(); ++i) {
            elements.get(i).render(matrixStack, mouseX, mouseY, partialTicks, selected == i);
        }

        if (scrollBarHeight > 0) {
            if (mouseX >= getX() + width + 5 && mouseY >= getY() && mouseX < getX() + width + 15 && mouseY < getY() + height) {
                scrollBarHovered = true;
            } else {
                scrollBarHovered = false;
            }

            int barColor = GuiColors.SETTINGS_SCROLLBAR;
            if (scrollBarGrabbed) {
                barColor = GuiColors.SETTINGS_SCROLLBAR_GRABBED;
            } else if (scrollBarHovered) {
                barColor = GuiColors.SETTINGS_SCROLLBAR_HOVERED;
            }

            fill(matrixStack, getX() + width + 5, getY(), getX() + width + 15, getY() + height, GuiColors.SETTINGS_SCROLLBAR_BG);
            fill(matrixStack, getX() + width + 5, getY() + scrollBarPos, getX() + width + 15, getY() + scrollBarPos + scrollBarHeight, barColor);
        }
    }

    @Override
    public boolean clicked(double mouseX, double mouseY) {
        if (visible) {
            if (scrollBarHeight > 0 && mouseX >= getX() + width + 5 && mouseY >= getY() && mouseX < getX() + width + 15 && mouseY < getY() + height) {
                if (mouseY < getY() + scrollBarPos) {
                    mouseScrolled(mouseX, mouseY, 1);
                } else if (mouseY > getY() + scrollBarPos + scrollBarHeight) {
                    mouseScrolled(mouseX, mouseY, -1);
                } else {
                    scrollBarGrabbed = true;
                    scrollBarGrabbedYPos = (int) mouseY - getY() - scrollBarPos;
                }

                return true;
            }

            if (isHovered && !scrollBarGrabbed) {
                ListIterator<ScrollElement> it = elements.listIterator();
                while (it.hasNext()) {
                    ScrollElement element = it.next();
                    ScrollElement.Action action = element.mousePressed(mouseX, mouseY);
                    if (action == ScrollElement.Action.Deleted) {
                        removeElement(element, it);
                        return true;
                    } else if (action == ScrollElement.Action.Clicked) {
                        selectElement(element);
                        if (responder != null) {
                            responder.elementClicked(this, element);
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Custom mouseReleased to be called from the Screen.
    public void mouseReleased() {
        if (visible && scrollBarHeight > 0 && scrollBarGrabbed) {
            scrollBarGrabbed = false;
            updateScrollBar();
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (scrollBarHeight > 0 && scrollBarGrabbed) {
            int delta = (int) mouseY - getY() - scrollBarPos - scrollBarGrabbedYPos;

            if (delta == 0) {
                return;
            }

            scrollBarPos += delta;
            if (scrollBarPos < 0) {
                scrollBarPos = 0;
            } else if (scrollBarPos + scrollBarHeight > height) {
                scrollBarPos = height - scrollBarHeight;
            }

            int newScrollStart = Math.round(((float) scrollBarPos) / height * elements.size());

            if (newScrollStart != scrollStart) {
                scrollStart = newScrollStart;
                updateScrollWindow();
            }
        }
    }

    private void updateScrollWindow() {
        if (elements.size() <= scrollHeight) {
            scrollStart = 0;
        } else {
            int bottomExtra = elements.size() - (scrollStart + scrollHeight);
            if (bottomExtra < 0) {
                scrollStart += bottomExtra;
            }

            if (scrollStart < 0) {
                scrollStart = 0;
            }
        }

        for (int i = 0; i < elements.size(); ++i) {
            if (i < scrollStart || i >= scrollStart + scrollHeight) {
                elements.get(i).visible = false;
            } else {
                elements.get(i).visible = true;
                elements.get(i).setY(getY() + (i - scrollStart) * elementHeight);
            }
        }
    }

    private void updateScrollBar() {
        if (elements.size() <= scrollHeight) {
            scrollBarHeight = 0;
            scrollBarHovered = false;
            scrollBarGrabbed = false;
            return;
        }

        scrollBarHeight = Math.round(((float) scrollHeight) / elements.size() * height);
        scrollBarPos = Math.round(((float) scrollStart) / elements.size() * height);
        if (scrollBarPos + scrollBarHeight > height) {
            scrollBarPos = height - scrollBarHeight;
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {

    }

    @OnlyIn(Dist.CLIENT)
    public interface ScrollBoxResponder {
        void elementClicked(GuiScrollBox scrollBox, ScrollElement element);

        void elementDelete(GuiScrollBox scrollBox, ScrollElement element);
    }

    @OnlyIn(Dist.CLIENT)
    public static class ScrollElement extends GuiComponent {
        enum Action {
            None, Clicked, Deleted
        }

        public boolean visible = true;
        protected int x = 0;
        protected int y = 0;
        protected boolean isHovered = false;
        protected final int height;
        protected final int width;

        public ScrollElement(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public void delete() {
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, boolean selected) {
            if (visible) {
                isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
                renderButton(matrixStack, mouseX, mouseY, partialTicks, selected);
            } else {
                isHovered = false;
            }
        }

        public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, boolean selected) {
        }

        public Action mousePressed(double mouseX, double mouseY) {
            return Action.None;
        }
    }
}
