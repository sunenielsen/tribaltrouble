package com.oddlabs.tt.model;

import com.oddlabs.tt.gui.RaceIcons;
import com.oddlabs.tt.render.SpriteKey;
import com.oddlabs.tt.audio.Audio;
import com.oddlabs.tt.model.weapon.MagicFactory;
import com.oddlabs.tt.player.ChieftainAI;

public final strictfp class Race {
	public final static int BUILDING_QUARTERS = 0;
	public final static int BUILDING_ARMORY = 1;
	public final static int BUILDING_TOWER = 2;

	public final static int NUM_BUILDINGS = 3;

	public final static int UNIT_WARRIOR_ROCK = 0;
	public final static int UNIT_WARRIOR_IRON = 1;
	public final static int UNIT_WARRIOR_RUBBER = 2;
	public final static int UNIT_PEON = 3;
	public final static int UNIT_CHIEFTAIN = 4;

	private final BuildingTemplate[] buildings = new BuildingTemplate[NUM_BUILDINGS];
	private final UnitTemplate[] units = new UnitTemplate[5];
	private final SpriteKey rally_point;
	private final RaceIcons icons;
	private final Audio attack_notification;
	private final Audio building_notification;
	private final MagicFactory[] magic_factory;
	private final ChieftainAI chieftain_ai;
	private final String music_path;

	public Race(BuildingTemplate quarters,
			BuildingTemplate armory,
			BuildingTemplate tower,
			UnitTemplate warrior_rock,
			UnitTemplate warrior_iron,
			UnitTemplate warrior_rubber,
			UnitTemplate peon,
			UnitTemplate chieftain,
			SpriteKey rally_point,
			RaceIcons icons,
			Audio attack_notification,
			Audio building_notification,
			MagicFactory[] magic_factory,
			ChieftainAI chieftain_ai,
			String music_path) {
		buildings[BUILDING_QUARTERS] = quarters;
		buildings[BUILDING_ARMORY] = armory;
		buildings[BUILDING_TOWER] = tower;
		for (int i = 0; i < buildings.length; i++)
			assert buildings[i].getTemplateID() == i;
		units[UNIT_WARRIOR_ROCK] = warrior_rock;
		units[UNIT_WARRIOR_IRON] = warrior_iron;
		units[UNIT_WARRIOR_RUBBER] = warrior_rubber;
		units[UNIT_PEON] = peon;
		units[UNIT_CHIEFTAIN] = chieftain;
		this.rally_point = rally_point;
		this.icons = icons;
		this.attack_notification = attack_notification;
		this.building_notification = building_notification;
		this.magic_factory = magic_factory;
		this.chieftain_ai = chieftain_ai;
		this.music_path = music_path;
	}

	public final BuildingTemplate getBuildingTemplate(int index) {
		return buildings[index];
	}

	public final UnitTemplate getUnitTemplate(int index) {
		return units[index];
	}

	public final SpriteKey getRallyPoint() {
		return rally_point;
	}

	public final RaceIcons getIcons() {
		return icons;
	}

	public final Audio getAttackNotificationAudio() {
		return attack_notification;
	}

	public final Audio getBuildingNotificationAudio() {
		return building_notification;
	}

	public final MagicFactory getMagicFactory(int i) {
		return magic_factory[i];
	}

	public final ChieftainAI getChieftainAI() {
		return chieftain_ai;
	}

	public final String getMusicPath() {
		return music_path;
	}
}
