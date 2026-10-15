package com.oddlabs.tt.gui;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.camera.CameraState;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.render.GUIRenderer;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.render.UIRenderer;
import com.oddlabs.tt.render.state.RenderContext;
import com.oddlabs.tt.viewer.AmbientAudio;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class GUI implements Animated {
    private final @NonNull GUIRenderer guiRenderer;
    private @NonNull GUIRoot current_root;
    private @Nullable Fade fade;
    private @Nullable UIRenderer renderer;
    private final CameraState frustum_state = new CameraState();

    public GUI() {
        this.guiRenderer = new GUIRenderer();
        this.current_root = createRoot();
    }

    public @NonNull GUIRoot newFade() {
        return newFade(null, null);
    }

    public @NonNull GUIRoot newFade(@Nullable Fadable fadable, @Nullable UIRenderer renderer) {
        GUIRoot gui_root = createRoot();
        newFade(fadable, gui_root, renderer);
        return gui_root;
    }

    public void newFade(@Nullable Fadable fadable, @NonNull GUIRoot gui_root, @Nullable UIRenderer renderer) {
        fade = new Fade(fadable, gui_root, renderer);
        LocalEventQueue.getQueue().getManager().registerAnimation(this);
    }

    public @NonNull GUIRoot createRoot() {
        GUIRoot gui_root = new GUIRoot(this);
        var renderer = Renderer.getRenderer();
        gui_root.displayChanged(renderer.getDisplayWidth(), renderer.getDisplayHeight());
        return gui_root;
    }

    @Override
    public void animate(float t) {
        if (fade != null) {
            fade.animate(this, t);
        }
    }

    void stopFade() {
        LocalEventQueue.getQueue().getManager().removeAnimation(this);
        fade = null;
    }

    void switchRoot(@NonNull GUIRoot gui_root, @Nullable UIRenderer renderer) {
        current_root.removeTree();
        current_root = gui_root;
        this.renderer = renderer;
    }

    public @NonNull GUIRoot getGUIRoot() {
        return current_root;
    }

    @Nullable
    Fade getFade() {
        return fade;
    }

    public void render(@NonNull AmbientAudio ambient) {
        Matrix4f proj = new Matrix4f();
        Matrix4f modelView = new Matrix4f();
        var guiRoot = getGUIRoot();
        CameraState camera = guiRoot.getDelegate().getCamera().getState();

        var renderer_instance = Renderer.getRenderer();
        RenderContext context = renderer_instance.getRenderContext();

        camera.setView(guiRoot.multProjection(proj.identity()), renderer_instance.getDisplayWidth(),
                renderer_instance.getDisplayHeight());
        modelView.set(camera.getModelView());

        if (!Globals.frustum_freeze) {
            frustum_state.set(camera);
        }

        if (renderer != null) {
            renderer.startFrame(context);
        } else {
            context.clear(true, true);
        }

        if (renderer != null)
            renderer.render(context, ambient, frustum_state, current_root);

        if (renderer != null) {
            renderer.endFrame(context, this::renderGUI);
        } else {
            renderGUI(context);
        }
    }

    public void pickHover() {
        var guiRoot = getGUIRoot();
        CameraState camera = guiRoot.getDelegate().getCamera().getState();
        GUIObject gui_hit = guiRoot.getCurrentGUIObject();
        if (renderer != null) {
            var localInput = Renderer.getLocalInput();
            renderer.pickHover(gui_hit.canHoverBehind(), camera, localInput.getMouseX(), localInput.getMouseY());
        }
    }

    private void renderGUI(@NonNull RenderContext context) {
        GUIRoot guiRoot = getGUIRoot();
        guiRenderer.renderFrame(context, guiRoot.getWidth(), guiRoot.getHeight(), () -> {
            guiRoot.render(guiRenderer);
            guiRoot.renderTopmost(guiRenderer, renderer != null ? renderer.getToolTip() : null, renderer != null
                    && renderer.isCheater());
        });
    }
}
