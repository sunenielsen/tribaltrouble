package com.oddlabs.tt.gui;

import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.player.PlayerInterface;
import com.oddlabs.tt.util.ToolTip;

public strictfp class RechargeButton extends NonFocusIconButton implements ToolTip {
	private final PlayerInterface player_interface;
	private final int magic_index;
	private Unit unit;

	public RechargeButton(PlayerInterface player_interface, IconQuad[] icon_quad, String tool_tip, int magic_index) {
		super(icon_quad, tool_tip);
		this.player_interface = player_interface;
		this.magic_index = magic_index;
		setCanFocus(true);
		setDim(icon_quad[0].getWidth(), icon_quad[0].getHeight());
	}

	public final void setUnit(Unit unit) {
		this.unit = unit;
	}

	public final void mouseClicked(int button, int x, int y, int clicks) {
		if (unit.canDoMagic(magic_index))
			player_interface.doMagic(unit, magic_index);
	}

	protected final void postRender() {
		IconQuad[] watch = Icons.getIcons().getWatch();
		float progress = unit.getMagicProgress(magic_index);
		int index = (int)(progress*(watch.length - 1));
		if (!unit.isDead() && progress < 1f)
			watch[index].render(getWidth() - watch[index].getWidth(),  getHeight() - watch[index].getHeight());
	}
}
