package com.oddlabs.tt.gui;

import com.oddlabs.tt.guievent.MouseButtonListener;

public final strictfp class PanelGroup extends GUIObject {
	private final Group focus_group;
	private final PanelBox box;
	private final Panel[] panels;

	private int selected;

	public PanelGroup(Panel[] panels, int selected) {
		assert selected < panels.length && panels.length > 0: "Invalid index selected.";
		this.panels = panels;

		int tab_height = panels[0].getTab().getHeight();
		int width = 0;
		int height = 0;
		for (int i = 0; i < panels.length; i++) {
			if (width < panels[i].getWidth())
				width = panels[i].getWidth();
			if (height < panels[i].getHeight())
				height = panels[i].getHeight();
		}
		int total_height = height + tab_height;
		setDim(width, total_height);
		int x = Skin.getSkin().getPanelData().getLeftTabOffset();
		int y = height;
		for (int i = 0; i < panels.length; i++) {
			panels[i].setPos((width - panels[i].getWidth())/2, Skin.getSkin().getPanelData().getBottomTabOffset() + (height - panels[i].getHeight())/2);
			panels[i].getTab().setPos(x, y);
			x += panels[i].getTab().getWidth();
			panels[i].getTab().addMouseButtonListener(new TabListener(i));
		}
		box = new PanelBox(width, total_height - panels[0].getTab().getHeight() + Skin.getSkin().getPanelData().getBottomTabOffset());

		focus_group = new Group();
		focus_group.setDim(width, total_height);
		focus_group.setPos(0, 0);
		addChild(focus_group);
		setCanFocus(true);
		selectPanel(selected);
	}

	private final void selectPanel(int index) {
		focus_group.clearChildren();
		for (int i = 0; i < panels.length; i++) {
			if (i != index) {
				focus_group.addChild(panels[i].getTab());
				panels[i].getTab().select(false);
			}
		}
		focus_group.addChild(box);
		focus_group.addChild(panels[index].getTab());
		panels[index].getTab().select(true);
		focus_group.addChild(panels[index]);
		selected = index;
		panels[index].setFocus();
	}

	public final void setFocus() {
		focus_group.setGroupFocus(LocalInput.isShiftDownCurrently() ? -1 : 1);
	}

	protected final void renderGeometry() {}

	private final strictfp class PanelBox extends GUIObject {
		public PanelBox(int width, int height) {
			setDim(width, height);
			setPos(0, 0);
		}

		protected final void renderGeometry() {
			Skin.getSkin().getPanelData().getBox().render(0, 0, getWidth(), getHeight(), panels[selected].getTab().getRenderState());
		}
	}

	private final strictfp class TabListener implements MouseButtonListener {
		private final int index;

		public TabListener(int index) {
			this.index = index;
		}

		public void mousePressed(int button, int x, int y) {
			selectPanel(index);
		}

		public void mouseReleased(int button, int x, int y) {}
		public void mouseHeld(int button, int x, int y) {}
		public void mouseClicked(int button, int x, int y, int clicks) {}
	}
}
