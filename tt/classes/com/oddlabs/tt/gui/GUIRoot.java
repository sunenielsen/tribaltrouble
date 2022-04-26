package com.oddlabs.tt.gui;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.oddlabs.tt.animation.TimerAnimation;
import com.oddlabs.tt.animation.Updatable;
import com.oddlabs.tt.audio.Audio;
import com.oddlabs.tt.audio.AudioFile;
import com.oddlabs.tt.audio.AbstractAudioPlayer;
import com.oddlabs.tt.camera.StaticCamera;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.delegate.MainMenu;
import com.oddlabs.tt.delegate.InGameMainMenu;
import com.oddlabs.tt.form.MessageForm;
import com.oddlabs.tt.form.Status;
import com.oddlabs.tt.form.TerrainMenu;
import com.oddlabs.tt.global.Globals;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.tt.guievent.CloseListener;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.net.PeerHub;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.render.Picker;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.render.Texture;
import com.oddlabs.tt.resource.Resources;
import com.oddlabs.tt.util.ToolTip;
import com.oddlabs.tt.util.GLUtils;
import com.oddlabs.tt.input.PointerInput;
import com.oddlabs.util.Utils;
import com.oddlabs.tt.delegate.*;

public final strictfp class GUIRoot extends GUIObject implements Updatable {
	public final static int CURSOR_NORMAL = 0;
	public final static int CURSOR_TARGET = 1;
	public final static int CURSOR_TEXT = 2;
	public final static int CURSOR_NULL = 3;
	
//	private static int inc_seed = 2;
	private final ResourceBundle bundle = ResourceBundle.getBundle(GUIRoot.class.getName());

	private final static int CURSOR_OFFSET_Y = 27;
	private final com.oddlabs.tt.resource.Cursor[] cursors = new com.oddlabs.tt.resource.Cursor[]{
		new com.oddlabs.tt.resource.Cursor(Utils.makeURL("/textures/gui/pointer_16_1.image"), 1, 15,
										   Utils.makeURL("/textures/gui/pointer_32_1.image"), 2, 29,
										   Utils.makeURL("/textures/gui/pointer_32_8.image"), 2, 29),
		
		new com.oddlabs.tt.resource.Cursor(Utils.makeURL("/textures/gui/pointer_target_16_1.image"), 7, 8,
										   Utils.makeURL("/textures/gui/pointer_target_32_1.image"), 14, 17,
										   Utils.makeURL("/textures/gui/pointer_target_32_8.image"), 14, 17),

		new com.oddlabs.tt.resource.Cursor(Utils.makeURL("/textures/gui/pointer_text_16_1.image"), 4, 8,
										   Utils.makeURL("/textures/gui/pointer_text_32_1.image"), 6, 20,
										   Utils.makeURL("/textures/gui/pointer_text_32_8.image"), 6, 20)};
	private final FloatBuffer matrix_buf = BufferUtils.createFloatBuffer(16);

	private final List delegate_stack = new ArrayList();
	private final List modal_delegate_stack = new ArrayList();
	private final List focus_backup_stack = new ArrayList();

	private final ToolTipBox tool_tip;
	private final TimerAnimation tool_tip_timer = new TimerAnimation(this, 0);
	private final InfoPrinter info_printer;
	private final Status status = new Status(this);
	private final InputState input_state = new InputState(this);
	private final GUI gui;
	private boolean render_tool_tip = false;

	private GUIObject current_gui_object = this;
	private GUIObject global_focus = this;

	private GUIObject cursor_object = this;

	GUIRoot(GUI gui) {
		this.gui = gui;
		tool_tip = new ToolTipBox();
		setPos(0, 0);
		setCanFocus(true);

		setToolTipTimer();
		this.info_printer = new InfoPrinter(this, 4, Skin.getSkin().getEditFont());
		addChild(info_printer);
		info_printer.setPos(0, 0);
	}

	public final GUI getGUI() {
		return gui;
	}

	public final InputState getInputState() {
		return input_state;
	}

	public final GUIObject getGlobalFocus() {
		return global_focus;
	}

	public final void setGlobalFocus(GUIObject object) {
		global_focus = object;
	}

	public final void setToolTipTimer() {
		tool_tip_timer.setTimerInterval(Settings.getSettings().tooltip_delay*ToolTipBox.MAX_DELAY_SECONDS);
	}

	public void update(Object anim) {
		render_tool_tip = true;
		tool_tip_timer.stop();
	}

	public final InfoPrinter getInfoPrinter() {
		return info_printer;
	}

	public final void pushDelegate(CameraDelegate delegate) {
		if (delegate_stack.size() > 0) {
			getDelegate().remove();
		}
		assert !delegate_stack.contains(delegate);
		delegate_stack.add(delegate);
		addChild(delegate);
		mousePick();
	}

	public final void removeDelegate(CameraDelegate delegate) {
		boolean top_most = getDelegate() == delegate;
		delegate.remove();

		delegate_stack.remove(delegate);

		if (top_most && delegate_stack.size() > 0) {
			addChild(getDelegate());
		}
		mousePick();
	}

	public final CameraDelegate getDelegate() {
		if (delegate_stack.size() == 0)
			return null;
		else
			return (CameraDelegate)delegate_stack.get(delegate_stack.size() - 1);
	}

	private final void pushModalDelegate(ModalDelegate delegate) {
		if (modal_delegate_stack.size() > 0) {
			getModalDelegate().remove();
		}
		modal_delegate_stack.add(delegate);
		super.addChild(delegate);
		mousePick();
	}

	private final void popModalDelegate(ModalDelegate delegate) {
		int index = modal_delegate_stack.indexOf(delegate);
		if (index == -1)
			return;
		boolean top_most = getModalDelegate() == delegate;
		modal_delegate_stack.remove(index);
		delegate.remove();

		delegate = getModalDelegate();
		if (top_most && delegate != null)
			super.addChild(delegate);

		GUIObject object = (GUIObject)focus_backup_stack.remove(index);
		if (delegate_stack.size() > 0)
			getDelegate().setFocus();
		if (top_most && object != null)
			object.setFocus();
		mousePick();
	}

	public final ModalDelegate getModalDelegate() {
		if (modal_delegate_stack.size() > 0)
			return (ModalDelegate)modal_delegate_stack.get(modal_delegate_stack.size() - 1);
		else
			return null;
	}

	public final void addModalForm(Form form) {
		focus_backup_stack.add(global_focus);
		ModalDelegate delegate = new ModalDelegate();
		delegate.addChild(form);
		form.addCloseListener(new ModalFormCloseListener(delegate));
		pushModalDelegate(delegate);
		form.setFocus();
	}

	private final strictfp class ModalFormCloseListener implements CloseListener {
		private final ModalDelegate delegate;

		public ModalFormCloseListener(ModalDelegate delegate) {
			this.delegate = delegate;
		}
		
		public final void closed() {
			popModalDelegate(delegate);
		}
	}

	public final void swapFocusBackup(GUIObject o) {
		focus_backup_stack.remove(focus_backup_stack.size() - 1);
		focus_backup_stack.add(o);
	}

	public final static float getUnitsPerPixel(float z) {
		return (float)(z*StrictMath.tan(Globals.FOV*(StrictMath.PI/180.0f)*0.5f)/(LocalInput.getViewHeight()*0.5d));
	}

	public final void displayChanged() {
		displayChanged(Settings.getSettings().view_width, Settings.getSettings().view_height);
	}

	protected final void displayChangedNotify(int width, int height) {
		//Reset The Current Viewport And Perspective Transformation
		setDim(width, height);
		if (width != 0) {
			float scale = getUnitsPerPixel(Globals.GUI_Z);
			Matrix4f m1 = new Matrix4f();
			m1.setIdentity();
			Matrix4f m2 = new Matrix4f();
			m2.setIdentity();
			Matrix4f m3 = new Matrix4f();
			m1.scale(new Vector3f(scale, scale, scale));
			m2.translate(new Vector3f(0f, 0f, -Globals.GUI_Z));
			Matrix4f.mul(m2, m1, m3);
			m2.load(m3);
			m3.setIdentity();
			m3.translate(new Vector3f(-width/2f, -height/2f, 0f));
			Matrix4f.mul(m2, m3, m1);
			m1.store(matrix_buf);
			matrix_buf.rewind();
		}
		for (int i = 0; i < delegate_stack.size(); i++) {
			((CameraDelegate)delegate_stack.get(i)).displayChanged(width, height);
		}
	}

	protected final void keyPressed(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_S:
				if (event.isControlDown()) {
					String filename = GLUtils.takeScreenshot("");
					info_printer.print(com.oddlabs.tt.util.Utils.getBundleString(bundle, "screenshot_message", new Object[]{filename}));
				}
				break;
				
			case Keyboard.KEY_H:
				if (event.isControlDown() && (LocalInput.getNativeCursorCaps() & org.lwjgl.input.Cursor.CURSOR_ONE_BIT_TRANSPARENCY) != 0) {
					Settings.getSettings().use_native_cursor = !Settings.getSettings().use_native_cursor;
					if (Settings.getSettings().use_native_cursor)
						info_printer.print(com.oddlabs.tt.util.Utils.getBundleString(bundle, "hardware_cursor_on"));
					else
						info_printer.print(com.oddlabs.tt.util.Utils.getBundleString(bundle, "hardware_cursor_off"));
				}
				break;
				
			case Keyboard.KEY_A:
				if (event.isControlDown()) {
					Settings.getSettings().aggressive_units = !Settings.getSettings().aggressive_units;
					if (Settings.getSettings().aggressive_units)
						info_printer.print(com.oddlabs.tt.util.Utils.getBundleString(bundle, "aggressive_unites_on"));
					else
						info_printer.print(com.oddlabs.tt.util.Utils.getBundleString(bundle, "aggressive_unites_off"));
				}
				break;
				
			 case Keyboard.KEY_I:
				if (event.isControlDown()) {
					Globals.draw_status = !Globals.draw_status;
				}
				break;
			default:
				break;
		}

/*		if (Settings.getSettings().inBetaMode()) {
			switch (event.getKeyCode()) {
				case Keyboard.KEY_K:
					if (event.isControlDown()) {
						if (event.isShiftDown()) {
							Player[] players = World.getPlayers();
							for (int i = 0; i < players.length; i++) {
								players[i].debugKillSelection(players[i].getUnits().filter(Abilities.NONE));
							}
						} else {
							Globals.process_shadows = !Globals.process_shadows;
						}
					}
					break;
			}
		}
*/
		if (!Settings.getSettings().inDeveloperMode())
			return;

		switch (event.getKeyCode()) {
			case Keyboard.KEY_U:
//				new AudioPlayer(0f, 0f, 0f, (Audio)Resources.findResource(new AudioFile("/sfx/hit1.ogg")), AudioPlayer.AUDIO_RANK_NOTIFICATION, AudioPlayer.AUDIO_DISTANCE_NOTIFICATION, 1f, 1f, 2f, false, true);
				Renderer.getRenderer().startMovieRecording();
				break;
			case Keyboard.KEY_W:
				if (event.isControlDown())
					Globals.draw_water = !Globals.draw_water;
				break;
			case Keyboard.KEY_R:
				if (event.isControlDown()) {
					Globals.run_ai = !Globals.run_ai;
System.out.println("Globals.run_ai = " + Globals.run_ai);
				} else {
//System.out.println("R pressed !!!!");
//					SupplyManager.debugSpawn();
				}
				break;

			case Keyboard.KEY_O:
				Globals.draw_light = !Globals.draw_light;
				break;
			case Keyboard.KEY_P:
				if (event.isControlDown())
					GLUtils.takeScreenshot("");
				else
					Globals.draw_plants = !Globals.draw_plants;
				break;
//			 case Keyboard.KEY_H:
//				Globals.draw_sky = !Globals.draw_sky;
//				break;
			case Keyboard.KEY_E:
				Globals.draw_particles = !Globals.draw_particles;
				break;
			case Keyboard.KEY_A:
				if (!event.isControlDown()) {
					Globals.draw_axes = !Globals.draw_axes;
				}
				break;
			case Keyboard.KEY_M:
				if (event.isControlDown())
					Globals.draw_misc = !Globals.draw_misc;
				else {
					System.out.println("WARNING: KEY_M pressed!");
					Globals.process_misc = !Globals.process_misc;
				}
				break;
/*			case Keyboard.KEY_N:
				if (event.isControlDown() && event.isShiftDown()) {
					TerrainMenu menu = new TerrainMenu(null, false, null);
					menu.parseMapcode(World.getParameters().getMapcode());
					menu.setSeed(inc_seed++);
					menu.startGame();
				}
				break;*/
			case Keyboard.KEY_J:
				org.lwjgl.input.Mouse.setCursorPosition(10, 10);
				break;
			case Keyboard.KEY_S:
				if (!event.isControlDown()) {
					Globals.draw_detail = !Globals.draw_detail;
				}
				break;
			case Keyboard.KEY_C:
				if (event.isControlDown()) {
					System.out.println("crash!");
					throw new RuntimeException("Ctrl+C pressed -> throwing a runtime exception.");
				} else {
					Globals.clear_frame_buffer = !Globals.clear_frame_buffer;
				}
				break;
			case Keyboard.KEY_D:
				Globals.switchBoundingMode();
				break;
			case Keyboard.KEY_V:
				Globals.frustum_freeze = !Globals.frustum_freeze;
				System.out.println("Globals.frustum_freeze = " + Globals.frustum_freeze);
				break;
//			case Keyboard.KEY_Q:
//				Renderer.getRenderer().shutdown();
//				break;
//			case Keyboard.KEY_F5:
		//		float x = getLandscapeLocationX();
		//		float y = getLandscapeLocationY();
		//		float z = World.getHeightMap().getNearestHeight(x, y) + 15f;
/*
public ParametricEmitter(ParametricFunction function, Vector3f position,
		float area_xy, float area_z, float velocity_u, float velocity_v, float velocity_random_margin,
		int num_particles, float particles_per_second,
		Vector4f color, Vector4f delta_color,
		Vector3f particle_radius, Vector3f growth_rate, float energy,
		int src_blend_func, int dst_blend_func, Texture[] textures,
		AnimationManager manager) {
*/
		//		new ParametricEmitter(new CloudFunction(2.5f, .7f), new Vector3f(x, y, z),
		//				0f, 0f, .5f, .5f, .2f,
		//				25, 100f,
		//				new Vector4f(.4f, .4f, .4f, .6f), new Vector4f(0f, 0f, 0f, 0f),
		//				new Vector3f(3f, 3f, 1f), new Vector3f(0f, 0f, 0f), 100,
		//				GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, RacesResources.getSmokeTextures(),
		//				World.getAnimationManagerGameTime());


		//		float src_x = getLandscapeLocationX();
		//		float src_y = getLandscapeLocationY();
		//		float src_z = World.getHeightMap().getNearestHeight(src_x, src_y) + 15f;
		//		float dst_x = getLandscapeLocationX();
		//		float dst_y = getLandscapeLocationY();
		//		float dst_z = World.getHeightMap().getNearestHeight(dst_x, dst_y);
/*
public Lightning(Vector3f src, Vector3f dst, float width,
		int num_particles, Vector4f color, Vector4f delta_color,
		int src_blend_func, int dst_blend_func, Texture texture, int energy,
		AnimationManager manager) {
*/
		//		new Lightning(new Vector3f(src_x, src_y, src_z), new Vector3f(dst_x, dst_y, dst_z), 1f,
		//				15, new Vector4f(1f, 1f, 1f, 1f), new Vector4f(0f, 0f, 0f, 0f),
		//				GL11.GL_SRC_ALPHA, GL11.GL_ONE, RacesResources.getLightningTexture(), 100,
		//				World.getAnimationManagerGameTime());
		//		break;
		//	case Keyboard.KEY_F6:
		//		addModalForm(new CreditsForm());
/*
public RandomVelocityEmitter(Vector3f position,
		float emitter_radius, float emitter_height, float angle_bound, float angle_max_jump,
		int num_particles, float particles_per_second,
		Vector3f velocity, Vector3f acceleration,
		Vector4f color, Vector4f delta_color,
		Vector3f particle_radius, Vector3f growth_rate, float energy, float friction,
		int src_blend_func, int dst_blend_func,
		Texture[] textures, AnimationManager manager) {
*/
//				float x1 = getLandscapeLocationX();
//				float y1 = getLandscapeLocationY();
//				float z1 = World.getHeightMap().getNearestHeight(x1, y1);
//				new Lightning(new Vector3f(x1, y1, z1), new Vector3f(x1, y1, z1 + 15f), .5f,
//						15, new Vector4f(1f, 1f, 1f, 1f), new Vector4f(0f, 0f, 0f, -1f/10f),
//						GL11.GL_SRC_ALPHA, GL11.GL_ONE, RacesResources.getLightningTexture(), 10f,
//						World.getAnimationManagerGameTime());

/*
				float alpha = 12f;
				float energy = 4f;
				new RandomVelocityEmitter(new Vector3f(x1, y1, z1),
						.001f, .001f, .5f, (float)StrictMath.PI,
						50, 25f,
						new Vector3f(0f, 0f, 4f), new Vector3f(0f, 0f, -2f),
						new Vector4f(1f, 1f, 1f, alpha), new Vector4f(0f, 0f, 0f, -alpha/energy),
						new Vector3f(.2f, .2f, .2f), new Vector3f(.0005f, .0005f, .0005f), energy, 1f,
						GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
						RacesResources.getNoteTextures(),
						World.getAnimationManagerGameTime());
*/
		//		break;
			case Keyboard.KEY_F1:
System.out.println("*********************************************************");
				LocalEventQueue.getQueue().debugPrintAnimations();
System.out.println("Texture.global_size = " + Texture.global_size);
				break;
			case Keyboard.KEY_F11:
				LocalInput.toggleFullscreen();
				break;
			case Keyboard.KEY_F12:
System.out.println("GC Forced");
				System.gc();
				Runtime.getRuntime().runFinalization();
				System.gc();
				break;
			default:
				break;
		}
	}
	
	public final void mousePick() {
		mousePick(LocalInput.getMouseX(), LocalInput.getMouseY());
	}

	private void mousePick(int x, int y) {
		GUIObject target = (GUIObject)pick(x ,y);
		if (target != null && target != current_gui_object) {
			current_gui_object.mouseExitedAll();
			tool_tip_timer.resetTime();
			boolean old_tip = current_gui_object instanceof ToolTip;
			boolean new_tip = target instanceof ToolTip;
			if (!old_tip && new_tip) {
				tool_tip_timer.start();
				render_tool_tip = false;
			}
			if (old_tip && !new_tip) {
				if (!render_tool_tip)
					tool_tip_timer.stop();
				else
					render_tool_tip = false;
			}
			current_gui_object = target;
			current_gui_object.mouseEnteredAll();
			if (!current_gui_object.isDisabled())
				cursor_object = current_gui_object;
		}
	}

	public final GUIObject getCurrentGUIObject() {
		return current_gui_object;
	}

	public final void setupGUIView() {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0f);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(Globals.FOV, LocalInput.getViewAspect(), Globals.VIEW_MIN, Globals.VIEW_MAX);
		GL11.glMultMatrix(matrix_buf);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glEnable(GL11.GL_BLEND);
	}

	public final void addChild(Renderable child) {
		super.addChild(child);
		ModalDelegate modal_delegate = getModalDelegate();
		putFirst(info_printer);
		if (modal_delegate != null) {
			super.addChild(modal_delegate); // move to front
		}
	}

	protected final void renderGeometry() {
		getDelegate().render2D();

		Skin.getSkin().bindTexture();
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		GL11.glColor3f(1f, 1f, 1f);
		GL11.glBegin(GL11.GL_QUADS);
		// MUST END IN POSTRENDER!
		

		// render forced delegates
		for (int i = 0; i < delegate_stack.size() - 1; i++) {
			CameraDelegate delegate = (CameraDelegate)delegate_stack.get(i);
			if (delegate.forceRender()) {
				delegate.render();
			}
		}
	}

	final boolean showToolTip() {
		return getModalDelegate() != null || getDelegate().renderCursor();
	}

	public final void renderTopmost() {
		if (Globals.draw_status)
			status.render(0, getWidth(), 0, getHeight());
		GL11.glEnd(); // Started in renderGeometry()
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);

		if (cursor_object.getCursorIndex() != CURSOR_NULL) {
			cursors[cursor_object.getCursorIndex()].setActive();
			if (getModalDelegate() != null || getDelegate().renderCursor()) {
				cursors[cursor_object.getCursorIndex()].render(LocalInput.getMouseX(), LocalInput.getMouseY());
			}
		} else
			PointerInput.setActiveCursor(null);
	}

	public final static void resetGUIView() {
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	final ToolTip getToolTip() {
		if (getCurrentGUIObject() instanceof ToolTip && render_tool_tip)
			return (ToolTip)getCurrentGUIObject();
		else
			return null;
	}
		
	final void renderToolTip(ToolTip hovered) {
		if (hovered != null) {
			tool_tip.clear();
			hovered.appendToolTip(tool_tip);
			tool_tip.render(LocalInput.getMouseX(), LocalInput.getMouseY() - CURSOR_OFFSET_Y, 0, getWidth(), 0, getHeight());
		}
	}
}
