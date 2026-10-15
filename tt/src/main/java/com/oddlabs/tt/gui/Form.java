package com.oddlabs.tt.gui;

import com.oddlabs.tt.font.Font;
import com.oddlabs.tt.guievent.CloseListener;
import com.oddlabs.tt.guievent.MouseMotionListener;
import com.oddlabs.tt.input.GameAction;
import com.oddlabs.tt.input.InputEvent;
import com.oddlabs.tt.input.InputPhase;
import com.oddlabs.tt.render.GUIRenderer;
import com.oddlabs.tt.render.Renderer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Form extends Group {
    private final Set<@NonNull CloseListener> close_listeners = new CopyOnWriteArraySet<>();

    private final @Nullable String caption;

    private boolean drag = false;

    public Form(@Nullable String caption) {
        this.caption = caption;
        setFocusCycle(true);
    }

    public Form() {
        this(null);
    }

    @Override
    public final void compileCanvas() {
        int spacing = Skin.getSkin().getFormData().objectSpacing();
        Box form;

        if (caption != null) {
            form = Skin.getSkin().getFormData().form();
            super.compileCanvas(form.getLeftOffset() + spacing,
                    form.getBottomOffset() + spacing,
                    form.getRightOffset() + spacing,
                    form.getTopOffset() + spacing);

            FormData form_data = Skin.getSkin().getFormData();
            Font font = form_data.captionFont();

            GUIObject label = new Label(caption, font);
            label.setPos(form_data.captionLeft(), getHeight() - form_data.captionY() - font.getHeight() / 2);
            label.addMouseMotionListener(new DragListener(this));
            addChild(label);

            GUIObject close_button = new IconButton(Skin.getSkin().getFormData().formClose(), null);
            close_button.setPos(getWidth() - close_button.getWidth() - form_data.closeRight(),
                    getHeight() - close_button.getHeight() - form_data.closeTop());
            close_button.addMouseClickListener((_, _, _, _) -> this.cancel());
            addChild(close_button);
        } else {
            form = Skin.getSkin().getFormData().slimForm();
            super.compileCanvas(form.getLeftOffset() + spacing,
                    form.getBottomOffset() + spacing,
                    form.getRightOffset() + spacing,
                    form.getTopOffset() + spacing);
        }
    }

    public final void centerPos() {
        GUIRoot root = getParentGUIRoot();
        int w, h;
        if (root != null) {
            w = root.getWidth();
            h = root.getHeight();
        } else {
            var renderer = Renderer.getRenderer();
            int physW = renderer.getDisplayWidth();
            int physH = renderer.getDisplayHeight();
            float scale = GUIRoot.calculateEffectiveScale(physW, physH);
            w = (int) (physW / scale);
            h = (int) (physH / scale);
        }
        setPos((w - getWidth()) / 2, (h - getHeight()) / 2);
    }

    @Override
    protected void renderGeometry(@NonNull GUIRenderer renderer) {
        var data = Skin.getSkin().getFormData();
        var form = caption != null ? data.form() : data.slimForm();
        var skinMode = isDisabled() ? ModeIconQuads.Mode.DISABLED : isActive() ? ModeIconQuads.Mode.ACTIVE : ModeIconQuads.Mode.NORMAL;
        form.render(renderer, 0f, 0f, getWidth(), getHeight(), skinMode);
    }

    @Override
    protected final void mousePressed(@NonNull MouseButton button, int x, int y) {
        if (caption != null && y >= getHeight() - Skin.getSkin().getFormData().form().getTopOffset())
            drag = true;
    }

    @Override
    protected final void mouseReleased(@NonNull MouseButton button, int x, int y) {
        drag = false;
    }

    @Override
    public final void mouseDragged(@NonNull MouseButton button, int x, int y, int rel_x, int rel_y, int abs_x,
            int abs_y) {
        if (drag)
            setPos(getX() + rel_x, getY() + rel_y);
    }

    @Override
    protected final void mouseScrolled(int amount) {
    }

    @Override
    protected void mouseMoved(int x, int y) {
    }

    @Override
    protected final void mouseExited() {
    }

    @Override
    protected final void mouseEntered() {
    }

    @Override
    protected final void mouseClicked(@NonNull MouseButton button, int x, int y, int clicks) {
    }

    @Override
    protected void handleInput(@NonNull InputEvent event) {
        if (event.getPhase() == InputPhase.PRESSED || event.getPhase() == InputPhase.REPEAT) {
            if (event.consumeAction(GameAction.UI_NEXT_PANEL)) {
                cyclePanelGroup(this, FocusDirection.FORWARD);
                event.consume();
                return;
            }
            if (event.consumeAction(GameAction.UI_PREV_PANEL)) {
                cyclePanelGroup(this, FocusDirection.BACKWARD);
                event.consume();
                return;
            }
            if (event.consumeAction(GameAction.UI_CANCEL)) {
                cancel();
                event.consume();
                return;
            }
        }
        super.handleInput(event);
    }

    private boolean cyclePanelGroup(GUIObject root, @NonNull FocusDirection dir) {
        if (root instanceof PanelGroup pg) {
            pg.cyclePanel(dir);
            return true;
        }
        GUIObject child = root.getFirstChild();
        while (child != null) {
            if (cyclePanelGroup(child, dir)) return true;
            child = child.getNext();
        }
        return false;
    }

    @Override
    protected final void mouseHeld(@NonNull MouseButton button, int x, int y) {
    }

    public final void closedAll() {
        closed();
        close_listeners.forEach(CloseListener::closed);
    }

    protected void closed() {
    }

    public final void addCloseListener(@NonNull CloseListener listener) {
        close_listeners.add(listener);
    }

    public final void removeCloseListener(@NonNull CloseListener listener) {
        close_listeners.remove(listener);
    }

    public void cancel() {
        doCancel();
        remove();
    }

    protected void doCancel() {
    }

    @Override
    public final void remove() {
        if (getParent() != null)
            closedAll();
        super.remove();
    }

    private static final class DragListener implements MouseMotionListener {
        private final Form owner;

        public DragListener(Form owner) {
            this.owner = owner;
        }

        @Override
        public void mouseDragged(@NonNull MouseButton button, int x, int y, int rel_x, int rel_y, int abs_x,
                int abs_y) {
            owner.mouseDragged(button, x, y, rel_x, rel_y, abs_x, abs_y);
        }

        @Override
        public void mouseMoved(int x, int y) {
        }

        @Override
        public void mouseEntered() {
        }

        @Override
        public void mouseExited() {
        }
    }
}
