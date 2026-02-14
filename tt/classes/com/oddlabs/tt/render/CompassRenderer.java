package com.oddlabs.tt.render;

import com.oddlabs.tt.font.Font;
import com.oddlabs.tt.gui.Skin;
import com.oddlabs.util.Quad;

import org.lwjgl.opengl.GL11;

/**
 * Renders a compass indicator in the bottom-right corner of the screen. North is defined as the
 * positive Y axis in world space. The compass rotates based on the camera's horizontal angle so
 * that cardinal directions always reflect their true world-space positions, giving players a shared
 * frame of reference regardless of camera orientation.
 */
public final strictfp class CompassRenderer {
    private static final int RADIUS = 30;
    private static final int TICK_LEN = 6;
    private static final int LABEL_OFFSET = 14;
    private static final int MARGIN = 50;
    private static final int SEGMENTS = 32;
    private static final int NEEDLE_WIDTH = 4;
    private static final int NEEDLE_LEN = 10;

    // World-space angles for cardinal directions (North = +Y axis)
    private static final float NORTH_ANGLE = (float) (StrictMath.PI / 2.0);
    private static final float EAST_ANGLE = 0f;
    private static final float SOUTH_ANGLE = -(float) (StrictMath.PI / 2.0);
    private static final float WEST_ANGLE = (float) StrictMath.PI;

    /**
     * Renders the compass indicator. Must be called during GUI rendering while a GL_QUADS batch is
     * active (the standard state inside renderGUI). Restores the QUADS batch before returning.
     *
     * @param horiz_angle the camera's horizontal angle in radians
     * @param screen_width the screen width in pixels
     * @param screen_height the screen height in pixels
     */
    public static void render(float horiz_angle, int screen_width, int screen_height) {
        int cx = screen_width - RADIUS - MARGIN;
        int cy = RADIUS + MARGIN;

        // End the active QUADS batch from GUI rendering
        GL11.glEnd();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);

        // Semi-transparent background circle
        GL11.glColor4f(0f, 0f, 0f, 0.4f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= SEGMENTS; i++) {
            float a = (float) (2.0 * StrictMath.PI * i / SEGMENTS);
            GL11.glVertex2f(
                    cx + (float) StrictMath.cos(a) * (RADIUS + 2),
                    cy + (float) StrictMath.sin(a) * (RADIUS + 2));
        }
        GL11.glEnd();

        // Circle outline
        GL11.glColor4f(0.7f, 0.7f, 0.7f, 0.6f);
        GL11.glLineWidth(1.5f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < SEGMENTS; i++) {
            float a = (float) (2.0 * StrictMath.PI * i / SEGMENTS);
            GL11.glVertex2f(
                    cx + (float) StrictMath.cos(a) * RADIUS,
                    cy + (float) StrictMath.sin(a) * RADIUS);
        }
        GL11.glEnd();

        // Compute screen-space deltas for each cardinal direction
        float north_delta = horiz_angle - NORTH_ANGLE;
        float east_delta = horiz_angle - EAST_ANGLE;
        float south_delta = horiz_angle - SOUTH_ANGLE;
        float west_delta = horiz_angle - WEST_ANGLE;

        // North needle (red triangle pointing toward North)
        drawNeedle(cx, cy, north_delta);

        // Cardinal tick marks
        drawTick(cx, cy, north_delta, 0.9f, 0.2f, 0.2f, 2f);
        drawTick(cx, cy, east_delta, 0.8f, 0.8f, 0.8f, 1.5f);
        drawTick(cx, cy, south_delta, 0.8f, 0.8f, 0.8f, 1.5f);
        drawTick(cx, cy, west_delta, 0.8f, 0.8f, 0.8f, 1.5f);

        // Center dot
        GL11.glColor4f(0.7f, 0.7f, 0.7f, 0.5f);
        GL11.glPointSize(3f);
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glVertex2f(cx, cy);
        GL11.glEnd();

        // Restore GL state for font rendering
        GL11.glPointSize(7f);
        GL11.glLineWidth(1f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glColor3f(1f, 1f, 1f);

        // Render cardinal direction labels
        renderLabels(cx, cy, horiz_angle);

        // Restart the QUADS batch for remaining GUI rendering
        Skin.getSkin().bindTexture();
        GL11.glBegin(GL11.GL_QUADS);
    }

    private static void drawNeedle(int cx, int cy, float delta) {
        float sx = (float) StrictMath.sin(delta);
        float sy = (float) StrictMath.cos(delta);

        float tip_x = cx + sx * (RADIUS - 2);
        float tip_y = cy + sy * (RADIUS - 2);
        float base_x = cx + sx * (RADIUS - 2 - NEEDLE_LEN);
        float base_y = cy + sy * (RADIUS - 2 - NEEDLE_LEN);
        float perp_x = -sy * NEEDLE_WIDTH;
        float perp_y = sx * NEEDLE_WIDTH;

        GL11.glColor4f(0.9f, 0.2f, 0.2f, 0.85f);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(tip_x, tip_y);
        GL11.glVertex2f(base_x - perp_x, base_y - perp_y);
        GL11.glVertex2f(base_x + perp_x, base_y + perp_y);
        GL11.glEnd();
    }

    private static void drawTick(
            int cx, int cy, float delta, float r, float g, float b, float line_width) {
        float sx = (float) StrictMath.sin(delta);
        float sy = (float) StrictMath.cos(delta);

        GL11.glColor4f(r, g, b, 0.8f);
        GL11.glLineWidth(line_width);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(cx + sx * (RADIUS - TICK_LEN), cy + sy * (RADIUS - TICK_LEN));
        GL11.glVertex2f(cx + sx * RADIUS, cy + sy * RADIUS);
        GL11.glEnd();
    }

    private static void renderLabels(int cx, int cy, float horiz_angle) {
        Font font = Skin.getSkin().getEditFont();

        renderLabel(font, "N", cx, cy, horiz_angle - NORTH_ANGLE, 1f, 0.3f, 0.3f);
        renderLabel(font, "E", cx, cy, horiz_angle - EAST_ANGLE, 0.9f, 0.9f, 0.9f);
        renderLabel(font, "S", cx, cy, horiz_angle - SOUTH_ANGLE, 0.9f, 0.9f, 0.9f);
        renderLabel(font, "W", cx, cy, horiz_angle - WEST_ANGLE, 0.9f, 0.9f, 0.9f);
    }

    private static void renderLabel(
            Font font, String text, int cx, int cy, float delta, float r, float g, float b) {
        float sx = (float) StrictMath.sin(delta);
        float sy = (float) StrictMath.cos(delta);

        int tw = font.getWidth(text);
        int th = font.getHeight();
        int lx = cx + (int) (sx * (RADIUS + LABEL_OFFSET)) - tw / 2;
        int ly = cy + (int) (sy * (RADIUS + LABEL_OFFSET)) - th / 2;

        GL11.glColor4f(r, g, b, 0.9f);
        font.setupQuads();
        for (int i = 0; i < text.length(); i++) {
            Quad quad = font.getQuad(text.charAt(i));
            if (quad != null) {
                quad.render(lx, ly);
                lx += quad.getWidth() - font.getXBorder();
            }
        }
        font.resetQuads();
        GL11.glColor3f(1f, 1f, 1f);
    }
}
