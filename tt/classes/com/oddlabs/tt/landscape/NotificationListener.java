package com.oddlabs.tt.landscape;

import com.oddlabs.tt.model.Selectable;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.util.StrictMatrix4f;

public strictfp interface NotificationListener {
	void newAttackNotification(Selectable target);
	void newSelectableNotification(Selectable target);
	void registerTarget(Target target);
	void unregisterTarget(Target target);
	void updateTreeLowDetail(StrictMatrix4f matrix, TreeSupply tree);
	void patchesEdited(int patch_x0, int patch_y0, int patch_x1, int patch_y1);
	void gamespeedChanged(int speed);
	void playerGamespeedChanged();
}
