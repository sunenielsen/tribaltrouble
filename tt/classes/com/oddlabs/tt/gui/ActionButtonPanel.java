package com.oddlabs.tt.gui;

import java.util.ResourceBundle;

import org.lwjgl.input.Keyboard;

import com.oddlabs.tt.animation.Animated;
import com.oddlabs.tt.camera.GameCamera;
import com.oddlabs.tt.form.DemoForm;
import com.oddlabs.tt.form.InGameDemoForm;
import com.oddlabs.tt.guievent.MouseClickListener;
import com.oddlabs.tt.landscape.TreeSupply;
import com.oddlabs.tt.landscape.World;
import com.oddlabs.tt.model.Abilities;
import com.oddlabs.tt.model.Building;
import com.oddlabs.tt.model.IronSupply;
import com.oddlabs.tt.model.MountUnitContainer;
import com.oddlabs.tt.model.Race;
import com.oddlabs.tt.model.RockSupply;
import com.oddlabs.tt.model.RubberSupply;
import com.oddlabs.tt.model.SupplyCounter;
import com.oddlabs.tt.model.Unit;
import com.oddlabs.tt.model.behaviour.StunController;
import com.oddlabs.tt.model.weapon.IronAxeWeapon;
import com.oddlabs.tt.model.weapon.RockAxeWeapon;
import com.oddlabs.tt.model.weapon.RubberAxeWeapon;
import com.oddlabs.tt.player.PlayerInterface;
import com.oddlabs.tt.player.Player;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.util.StateChecksum;
import com.oddlabs.tt.util.Target;
import com.oddlabs.tt.util.Utils;
import com.oddlabs.tt.delegate.*;
import com.oddlabs.util.Quad;
import com.oddlabs.tt.viewer.WorldViewer;

public final strictfp class ActionButtonPanel extends GUIObject implements Animated {
	private final static int GROUP_LEFT_OFFSET = 10;
	private final static int GROUP_BOTTOM_OFFSET = 10;
	private final static int GROUP_RIGHT_OFFSET = 10;
	private final static int GROUP_TOP_OFFSET = 20;
	private final Group unit_group;
	private final Group peon_group;
	private final Group chieftain_group;
	private final Group tower_group;
	private final Group quarters_status_group;
	private final Group quarters_group;
	private final Group status_group;
	private final Group armory_group;
	private final Group harvest_group;
	private final Group build_group;
	private final Group army_group;
	private final Group transport_group;
	
	private final NonFocusIconButton tower_attack_button;
	private final NonFocusIconButton tower_exit_button;
//	private boolean tower_exit_button_disabled;
	private final NonFocusIconButton move_button;
	private final NonFocusIconButton attack_button;
	private final NonFocusIconButton gather_repair_button;
	private final NonFocusIconButton quarters_button;
//	private boolean quarters_button_disabled;
	private final RechargeButton magic1_button;
	private final RechargeButton magic2_button;
	private final NonFocusIconButton armory_button;
//	private boolean armory_button_disabled;
	private final NonFocusIconButton tower_button;
//	private boolean tower_button_disabled;
	private final NonFocusIconButton harvest_button;
	private final NonFocusIconButton build_button;
	private final NonFocusIconButton army_button;
	private final NonFocusIconButton transport_button;
	private final NonFocusIconButton rally_point_button;

	private final StatusIcon unit_status;
	private final StatusIcon weapon_rock_status;
	private final StatusIcon weapon_iron_status;
	private final StatusIcon weapon_rubber_status;
	private final StatusIcon tree_status;
	private final StatusIcon rock_status;
	private final StatusIcon iron_status;
	private final StatusIcon rubber_status;

	private final WatchStatusIcon quarters_unit_status;
	private final DeploySpinner quarters_peon_button;
	private final ChieftainButton quarters_chieftain_button;
	private final NonFocusIconButton quarters_rally_point_button;

	private final DeploySpinner harvest_tree_button;
	private final DeploySpinner harvest_rock_button;
	private final DeploySpinner harvest_iron_button;
	private final DeploySpinner harvest_rubber_button;
	private final NonFocusIconButton harvest_back_button;

	private final BuildSpinner build_weapon_rock_button;
	private final BuildSpinner build_weapon_iron_button;
	private final BuildSpinner build_weapon_rubber_button;
	private final NonFocusIconButton build_back_button;

	private final DeploySpinner army_peon_button;
	private final DeploySpinner army_warrior_rock_button;
	private final DeploySpinner army_warrior_iron_button;
	private final DeploySpinner army_warrior_rubber_button;
	private final NonFocusIconButton army_back_button;

	private final DeploySpinner transport_tree_button;
	private final DeploySpinner transport_rock_button;
	private final DeploySpinner transport_iron_button;
	private final DeploySpinner transport_rubber_button;
	private final NonFocusIconButton transport_back_button;
	private final ResourceBundle bundle = ResourceBundle.getBundle(ActionButtonPanel.class.getName());

	private final GameCamera camera;
	private final WorldViewer viewer;

	private Group current_submenu = null;
	private boolean update = false;
	private boolean current_quarters = false;
	private boolean current_armory = false;
	private Building current_building;
	private boolean current_unit = false;
	private boolean current_peon = false;
	private Unit current_chieftain;
	private boolean current_tower = false;
//	private boolean[] magic_disabled = new boolean[2];

	private final String formatTip(String tip_key, String shortcut_key) {
		return Utils.getBundleString(bundle, tip_key, new Object[]{shortcut_key});
	}
	
	public ActionButtonPanel(WorldViewer viewer, GameCamera camera) {
		this(viewer, camera, viewer.getGUIRoot().getWidth(), viewer.getGUIRoot().getHeight());
	}

	public ActionButtonPanel(final WorldViewer viewer, GameCamera camera, int width, int height) {
		this.viewer = viewer;
		this.camera = camera;
		RaceIcons race_icons = viewer.getLocalPlayer().getRace().getIcons();
		Skin skin = Skin.getSkin();
		Icons icons = Icons.getIcons();
		String widest_char = "" + skin.getEditFont().getWidestChar("0123456789");
		int label_width = skin.getEditFont().getWidth(widest_char + widest_char + widest_char);

		unit_group = new NonFocusGroup();
		peon_group = new NonFocusGroup();
		chieftain_group = new NonFocusGroup();
		tower_group = new NonFocusGroup();
		quarters_status_group = new NonFocusGroup();
		quarters_group = new NonFocusGroup();
		status_group = new NonFocusGroup();
		armory_group = new NonFocusGroup();
		harvest_group = new NonFocusGroup();
		build_group = new NonFocusGroup();
		army_group = new NonFocusGroup();
		transport_group = new NonFocusGroup();

		move_button = new NonFocusIconButton(race_icons.getMoveIcon(), formatTip("move_tip", "M"));
		move_button.setIconDisabler(new IconDisabler() {
			public final boolean isDisabled() {
				return !viewer.getLocalPlayer().canMove();
			}
		});
		unit_group.addChild(move_button);
		move_button.addMouseClickListener(new TargetListener(Target.ACTION_MOVE));
		attack_button = new NonFocusIconButton(race_icons.getAttackIcon(), formatTip("attack_tip", "A"));
		attack_button.setIconDisabler(new IconDisabler() {
			public final boolean isDisabled() {
				return !viewer.getLocalPlayer().canAttack();
			}
		});
		unit_group.addChild(attack_button);
		attack_button.addMouseClickListener(new TargetListener(Target.ACTION_ATTACK));
		move_button.place();
		attack_button.place(move_button, BOTTOM_MID);
		unit_group.compileCanvas(GROUP_LEFT_OFFSET, 0, GROUP_RIGHT_OFFSET, GROUP_BOTTOM_OFFSET);

		gather_repair_button = new NonFocusIconButton(race_icons.getGatherRepairIcon(), formatTip("gather_repair_tip", "G"));
		peon_group.addChild(gather_repair_button);
		gather_repair_button.addMouseClickListener(new TargetListener(Target.ACTION_GATHER_REPAIR));
		gather_repair_button.setIconDisabler(new IconDisabler() {
			public final boolean isDisabled() {
				return !viewer.getLocalPlayer().canRepair();
			}
		});
		quarters_button = new NonFocusIconButton(race_icons.getQuartersIcon(), formatTip("quarters_tip", "Q"));
		peon_group.addChild(quarters_button);
		quarters_button.addMouseClickListener(new PlaceListener(Race.BUILDING_QUARTERS));
		quarters_button.setIconDisabler(new BuildingDisabler(Race.BUILDING_QUARTERS));
		armory_button = new NonFocusIconButton(race_icons.getArmoryIcon(), formatTip("armory_tip", "R"));
		peon_group.addChild(armory_button);
		armory_button.addMouseClickListener(new PlaceListener(Race.BUILDING_ARMORY));
		armory_button.setIconDisabler(new BuildingDisabler(Race.BUILDING_ARMORY));
		tower_button = new NonFocusIconButton(race_icons.getTowerIcon(), formatTip("tower_tip", "T"));
		peon_group.addChild(tower_button);
		tower_button.addMouseClickListener(new TowerPlaceListener());
		tower_button.setIconDisabler(new BuildingDisabler(Race.BUILDING_TOWER));
		gather_repair_button.place();
		quarters_button.place(gather_repair_button, BOTTOM_MID);
		armory_button.place(quarters_button, BOTTOM_MID);
		tower_button.place(armory_button, BOTTOM_MID);
		peon_group.compileCanvas(GROUP_LEFT_OFFSET, GROUP_BOTTOM_OFFSET, GROUP_RIGHT_OFFSET, 0);

		PlayerInterface player_interface = viewer.getPeerHub().getPlayerInterface();
		magic1_button = new RechargeButton(player_interface, race_icons.getMagic1Icon(), race_icons.getMagic1Desc(), 0);
		chieftain_group.addChild(magic1_button);
//		magic1_button.addMouseClickListener(new MagicListener(0));
		magic2_button = new RechargeButton(player_interface, race_icons.getMagic2Icon(), race_icons.getMagic2Desc(), 1);
		chieftain_group.addChild(magic2_button);
//		magic2_button.addMouseClickListener(new MagicListener(1));
		magic1_button.place();
		magic2_button.place(magic1_button, BOTTOM_MID);
		chieftain_group.compileCanvas(GROUP_LEFT_OFFSET, GROUP_BOTTOM_OFFSET, GROUP_RIGHT_OFFSET, 0);

		tower_attack_button = new NonFocusIconButton(race_icons.getAttackIcon(), formatTip("attack_tip", "A"));
		tower_group.addChild(tower_attack_button);
		tower_attack_button.addMouseClickListener(new TargetListener(Target.ACTION_ATTACK));
		tower_exit_button = new NonFocusIconButton(race_icons.getTowerExitIcon(), formatTip("exit_tip", "X"));
		tower_group.addChild(tower_exit_button);
		tower_exit_button.addMouseClickListener(new TowerExitListener());
		tower_attack_button.place();
		tower_exit_button.place(tower_attack_button, BOTTOM_MID);
		tower_group.compileCanvas();

		unit_status = new StatusIcon(label_width, race_icons.getUnitStatusIcon(), Utils.getBundleString(bundle, "units_tip"));
		status_group.addChild(unit_status);
		weapon_rock_status = new StatusIcon(label_width, race_icons.getWeaponRockStatusIcon(), Utils.getBundleString(bundle, "rock_weapons_tip"));
		status_group.addChild(weapon_rock_status);
		weapon_iron_status = new StatusIcon(label_width, race_icons.getWeaponIronStatusIcon(), Utils.getBundleString(bundle, "iron_weapons_tip"));
		status_group.addChild(weapon_iron_status);
		weapon_rubber_status = new StatusIcon(label_width, race_icons.getWeaponRubberStatusIcon(), Utils.getBundleString(bundle, "chicken_weapons_tip"));
		status_group.addChild(weapon_rubber_status);
		tree_status = new StatusIcon(label_width, icons.getTreeStatusIcon(), Utils.getBundleString(bundle, "tree_resources_tip"));
		status_group.addChild(tree_status);
		rock_status = new StatusIcon(label_width, icons.getRockStatusIcon(), Utils.getBundleString(bundle, "rock_resources_tip"));
		status_group.addChild(rock_status);
		iron_status = new StatusIcon(label_width, icons.getIronStatusIcon(), Utils.getBundleString(bundle, "iron_resources_tip"));
		status_group.addChild(iron_status);
		rubber_status = new StatusIcon(label_width, icons.getRubberStatusIcon(), Utils.getBundleString(bundle, "chicken_resources_tip"));
		status_group.addChild(rubber_status);
		unit_status.place();
		weapon_rock_status.place(unit_status, BOTTOM_MID);
		weapon_iron_status.place(weapon_rock_status, BOTTOM_MID);
		weapon_rubber_status.place(weapon_iron_status, BOTTOM_MID);
		tree_status.place(unit_status, LEFT_MID, 5);
		rock_status.place(tree_status, BOTTOM_MID);
		iron_status.place(rock_status, BOTTOM_MID);
		rubber_status.place(iron_status, BOTTOM_MID);
		status_group.compileCanvas(5, 5, 5, 5);

		quarters_unit_status = new WatchStatusIcon(label_width, race_icons.getUnitStatusIcon(), Utils.getBundleString(bundle, "units_tip"));
		quarters_status_group.addChild(quarters_unit_status);
		quarters_unit_status.place();
		quarters_status_group.compileCanvas(5, 5, 5, 5);

		quarters_peon_button = new DeploySpinner(viewer, player_interface, race_icons.getPeonIcon(), Utils.getBundleString(bundle, "deploy_peon_tip"),
				new Quad[]{race_icons.getUnitStatusIcon()}, "P");
		quarters_group.addChild(quarters_peon_button);
		quarters_chieftain_button = new ChieftainButton(viewer, player_interface, race_icons.getChieftainIcon(), formatTip("train_chieftain_tip", "C"));
//		if (Settings.getSettings().developer_mode) {
			quarters_group.addChild(quarters_chieftain_button);
//		}
		quarters_rally_point_button = new NonFocusIconButton(race_icons.getRallyPointIcon(), formatTip("rally_point_tip", "R"));
		quarters_group.addChild(quarters_rally_point_button);
		quarters_rally_point_button.addMouseClickListener(new RallyPointListener());
		quarters_peon_button.place();
//		if (Settings.getSettings().developer_mode) {
			quarters_chieftain_button.place(quarters_peon_button, BOTTOM_MID);
			quarters_rally_point_button.place(quarters_chieftain_button, BOTTOM_MID);
//		} else {
//			quarters_rally_point_button.place(quarters_peon_button, BOTTOM_MID);
//		}
		quarters_group.compileCanvas(GROUP_LEFT_OFFSET, GROUP_BOTTOM_OFFSET, GROUP_RIGHT_OFFSET, GROUP_TOP_OFFSET);

		harvest_button = new NonFocusIconButton(icons.getHarvestIcon(), formatTip("gather_resources_tip", "G"));
		harvest_button.setIconDisabler(new IconDisabler() {
			public final boolean isDisabled() {
				return !viewer.getLocalPlayer().canHarvest();
			}
		});
		armory_group.addChild(harvest_button);
		harvest_button.addMouseClickListener(new GroupListener(armory_group, harvest_group));
		build_button = new NonFocusIconButton(race_icons.getBuildWeaponsIcon(), formatTip("produce_weapons_tip", "W"));
		build_button.setIconDisabler(new IconDisabler() {
			public final boolean isDisabled() {
				return !viewer.getLocalPlayer().canBuildWeapons();
			}
		});
		armory_group.addChild(build_button);
		build_button.addMouseClickListener(new GroupListener(armory_group, build_group));
		army_button = new NonFocusIconButton(race_icons.getArmyIcon(), formatTip("deploy_army_tip", "A"));
		army_button.setIconDisabler(new IconDisabler() {
			public final boolean isDisabled() {
				return !viewer.getLocalPlayer().canBuildArmies();
			}
		});
		armory_group.addChild(army_button);
		army_button.addMouseClickListener(new GroupListener(armory_group, army_group));
		transport_button = new NonFocusIconButton(race_icons.getTransportIcon(), formatTip("transport_resources_tip", "T"));
		armory_group.addChild(transport_button);
		transport_button.addMouseClickListener(new GroupListener(armory_group, transport_group));
		rally_point_button = new NonFocusIconButton(race_icons.getRallyPointIcon(), formatTip("rally_point_tip", "R"));
		rally_point_button.setIconDisabler(new IconDisabler() {
			public final boolean isDisabled() {
				return !viewer.getLocalPlayer().canSetRallyPoints();
			}
		});
		armory_group.addChild(rally_point_button);
		rally_point_button.addMouseClickListener(new RallyPointListener());
		harvest_button.place();
		build_button.place(harvest_button, BOTTOM_MID);
		army_button.place(build_button, BOTTOM_MID);
		transport_button.place(army_button, BOTTOM_MID);
		rally_point_button.place(transport_button, BOTTOM_MID);
		armory_group.compileCanvas(GROUP_LEFT_OFFSET, GROUP_BOTTOM_OFFSET, GROUP_RIGHT_OFFSET, GROUP_TOP_OFFSET);

		harvest_tree_button = new DeploySpinner(viewer, player_interface, icons.getTreeIcon(), Utils.getBundleString(bundle, "harvest_tree_tip"), new Quad[]{race_icons.getUnitStatusIcon()}, "W");
		harvest_group.addChild(harvest_tree_button);
		harvest_rock_button = new DeploySpinner(viewer, player_interface, icons.getRockIcon(), Utils.getBundleString(bundle, "harvest_rock_tip"), new Quad[]{race_icons.getUnitStatusIcon()}, "R");
		harvest_group.addChild(harvest_rock_button);
		harvest_iron_button = new DeploySpinner(viewer, player_interface, icons.getIronIcon(), Utils.getBundleString(bundle, "harvest_iron_tip"), new Quad[]{race_icons.getUnitStatusIcon()}, "I");
		harvest_group.addChild(harvest_iron_button);
		harvest_rubber_button = new DeploySpinner(viewer, player_interface, icons.getRubberIcon(), Utils.getBundleString(bundle, "harvest_chicken_tip"), new Quad[]{race_icons.getUnitStatusIcon()}, "C");
		harvest_group.addChild(harvest_rubber_button);
		harvest_back_button = new NonFocusIconButton(skin.getBackButton(), formatTip("back_tip", "Esc"));
		harvest_back_button.addMouseClickListener(new CancelListener());
		harvest_group.addChild(harvest_back_button);
		harvest_tree_button.place();
		harvest_rock_button.place(harvest_tree_button, BOTTOM_MID);
		harvest_iron_button.place(harvest_rock_button, BOTTOM_MID);
		harvest_rubber_button.place(harvest_iron_button, BOTTOM_MID);
		harvest_back_button.place(harvest_rubber_button, BOTTOM_MID);
		harvest_group.compileCanvas(GROUP_LEFT_OFFSET, GROUP_BOTTOM_OFFSET, GROUP_RIGHT_OFFSET, GROUP_TOP_OFFSET);

		build_weapon_rock_button = new BuildSpinner(viewer, player_interface, race_icons.getBuildWeaponRockIcon(), Utils.getBundleString(bundle, "build_rock_tip"), Building.COST_ROCK_WEAPON.toIconArray(), "R");
		build_group.addChild(build_weapon_rock_button);
		build_weapon_iron_button = new BuildSpinner(viewer, player_interface, race_icons.getBuildWeaponIronIcon(), Utils.getBundleString(bundle, "build_iron_tip"), Building.COST_IRON_WEAPON.toIconArray(), "I");
		build_group.addChild(build_weapon_iron_button);
		build_weapon_rubber_button = new BuildSpinner(viewer, player_interface, race_icons.getBuildWeaponRubberIcon(), Utils.getBundleString(bundle, "build_chicken_tip"), Building.COST_RUBBER_WEAPON.toIconArray(), "C");
		build_group.addChild(build_weapon_rubber_button);
		build_back_button = new NonFocusIconButton(skin.getBackButton(), formatTip("back_tip", "Esc"));
		build_back_button.addMouseClickListener(new CancelListener());
		build_group.addChild(build_back_button);
		build_weapon_rock_button.place();
		build_weapon_iron_button.place(build_weapon_rock_button, BOTTOM_MID);
		build_weapon_rubber_button.place(build_weapon_iron_button, BOTTOM_MID);
		build_back_button.place(build_weapon_rubber_button, BOTTOM_MID);
		build_group.compileCanvas(GROUP_LEFT_OFFSET, GROUP_BOTTOM_OFFSET, GROUP_RIGHT_OFFSET, GROUP_TOP_OFFSET);

		army_peon_button = new DeploySpinner(viewer, player_interface, race_icons.getPeonIcon(), Utils.getBundleString(bundle, "deploy_peon_tip"),
				new Quad[]{race_icons.getUnitStatusIcon()}, "P");
		army_group.addChild(army_peon_button);
		army_warrior_rock_button = new DeploySpinner(viewer, player_interface, race_icons.getWarriorRockIcon(), Utils.getBundleString(bundle, "deploy_rock_tip"),
				new Quad[]{race_icons.getUnitStatusIcon(), race_icons.getWeaponRockStatusIcon()}, "R");
		army_group.addChild(army_warrior_rock_button);
		
		army_warrior_iron_button = new DeploySpinner(viewer, player_interface, race_icons.getWarriorIronIcon(), Utils.getBundleString(bundle, "deploy_iron_tip"),
				new Quad[]{race_icons.getUnitStatusIcon(), race_icons.getWeaponIronStatusIcon()}, "I");
		army_group.addChild(army_warrior_iron_button);
		army_warrior_iron_button.setNag("Iron warriors are unavailable in this demo version of Tribal Trouble.");
		
		army_warrior_rubber_button = new DeploySpinner(viewer, player_interface, race_icons.getWarriorRubberIcon(), Utils.getBundleString(bundle, "deploy_chicken_tip"),
				new Quad[]{race_icons.getUnitStatusIcon(), race_icons.getWeaponRubberStatusIcon()}, "C");
		army_group.addChild(army_warrior_rubber_button);
		army_warrior_rubber_button.setNag(Utils.getBundleString(bundle, "chicken_unavailable"));
		
		army_back_button = new NonFocusIconButton(skin.getBackButton(), formatTip("back_tip", "Esc"));
		army_back_button.addMouseClickListener(new CancelListener());
		army_group.addChild(army_back_button);
		army_peon_button.place();
		army_warrior_rock_button.place(army_peon_button, BOTTOM_MID);
		army_warrior_iron_button.place(army_warrior_rock_button, BOTTOM_MID);
		army_warrior_rubber_button.place(army_warrior_iron_button, BOTTOM_MID);
		army_back_button.place(army_warrior_rubber_button, BOTTOM_MID);
		army_group.compileCanvas(GROUP_LEFT_OFFSET, GROUP_BOTTOM_OFFSET, GROUP_RIGHT_OFFSET, GROUP_TOP_OFFSET);

		transport_tree_button = new DeploySpinner(viewer, player_interface, icons.getTreeIcon(), Utils.getBundleString(bundle, "transport_tree_tip"),
				new Quad[]{race_icons.getUnitStatusIcon(), icons.getTreeStatusIcon()}, "W");
		transport_group.addChild(transport_tree_button);
		transport_rock_button = new DeploySpinner(viewer, player_interface, icons.getRockIcon(), Utils.getBundleString(bundle, "transport_rock_tip"),
				new Quad[]{race_icons.getUnitStatusIcon(), icons.getRockStatusIcon()}, "R");
		transport_group.addChild(transport_rock_button);
		transport_iron_button = new DeploySpinner(viewer, player_interface, icons.getIronIcon(), Utils.getBundleString(bundle, "transport_iron_tip"),
				new Quad[]{race_icons.getUnitStatusIcon(), icons.getIronStatusIcon()}, "I");
		transport_group.addChild(transport_iron_button);
		transport_rubber_button = new DeploySpinner(viewer, player_interface, icons.getRubberIcon(), Utils.getBundleString(bundle, "transport_chicken_tip"),
				new Quad[]{race_icons.getUnitStatusIcon(), icons.getRubberStatusIcon()}, "C");
		transport_group.addChild(transport_rubber_button);
		transport_back_button = new NonFocusIconButton(skin.getBackButton(), formatTip("back_tip", "Esc"));
		transport_back_button.addMouseClickListener(new CancelListener());
		transport_group.addChild(transport_back_button);
		transport_tree_button.place();
		transport_rock_button.place(transport_tree_button, BOTTOM_MID);
		transport_iron_button.place(transport_rock_button, BOTTOM_MID);
		transport_rubber_button.place(transport_iron_button, BOTTOM_MID);
		transport_back_button.place(transport_rubber_button, BOTTOM_MID);
		transport_group.compileCanvas(GROUP_LEFT_OFFSET, GROUP_BOTTOM_OFFSET, GROUP_RIGHT_OFFSET, GROUP_TOP_OFFSET);

		setCanFocus(true);
		displayChangedNotify(width, height);
	}

	public final void doAdd() {
		super.doAdd();
		viewer.getAnimationManagerLocal().registerAnimation(this);
	}
	
	protected final void doRemove() {
		super.doRemove();
		viewer.getAnimationManagerLocal().removeAnimation(this);
	}

	public final void updateChecksum(StateChecksum sum) {
	}

	public final void animate(float t) {
		Building new_building = viewer.getSelection().getCurrentSelection().getBuilding();
		boolean different_building = new_building != current_building;
		current_building = new_building;
		viewer.getRenderer().setSelectedBuilding(new_building);
		
		Unit new_chieftain = viewer.getSelection().getCurrentSelection().getChieftain();
		boolean different_chieftain = new_chieftain != current_chieftain;
		current_chieftain = new_chieftain;

		int current_num_units = viewer.getSelection().getCurrentSelection().getNumUnits();
		int current_num_peons = viewer.getSelection().getCurrentSelection().getNumBuilders();
		
		boolean new_quarters = current_building != null && current_building.getAbilities().hasAbilities(Abilities.REPRODUCE);
		boolean new_armory = current_building != null && current_building.getAbilities().hasAbilities(Abilities.BUILD_ARMIES);
		boolean new_unit = current_num_units > 0;
		boolean new_peon = current_num_peons > 0;
		boolean new_tower = current_building != null && current_building.getAbilities().hasAbilities(Abilities.ATTACK);
		update = update || different_building || different_chieftain || new_quarters != current_quarters || new_armory != current_armory || new_unit != current_unit || new_peon != current_peon || new_tower != current_tower;
		if (update) {
			current_quarters = new_quarters;
			current_armory = new_armory;
			current_tower = new_tower;
			current_unit = new_unit;
			current_peon = new_peon;
			update = false;

			removeGroups();

			if (current_unit) {
				addChild(unit_group);
			}
			if (current_peon) {
				addChild(peon_group);
			}
			if (current_chieftain != null) {
				addChild(chieftain_group);
				updateGroups();
				Player player = viewer.getLocalPlayer();
				if (player.canDoMagic(0)) {
					magic1_button.setUnit(current_chieftain);
					magic1_button.setIconDisabler(new MagicDisabler(current_chieftain, 0));
					chieftain_group.addChild(magic1_button);
				} else
					magic1_button.remove();
				if (player.canDoMagic(1)) {
					magic2_button.setUnit(current_chieftain);
					magic2_button.setIconDisabler(new MagicDisabler(current_chieftain, 1));
					chieftain_group.addChild(magic2_button);
				} else
					magic2_button.remove();
			}
			if (current_tower) {
				addChild(tower_group);
				tower_attack_button.setIconDisabler(new TowerActionDisabler(current_building, false));
				tower_exit_button.setIconDisabler(new TowerActionDisabler(current_building, true));
			}
			if (current_quarters) {
				addChild(quarters_status_group);
				addChild(quarters_group);
				SupplyCounter unit_counter = new SupplyCounter(current_building, Unit.class);
				quarters_unit_status.setCounter(unit_counter);
				quarters_unit_status.setUnitContainerBuilding(current_building);
				quarters_peon_button.setContainers(current_building, Building.KEY_DEPLOY_PEON, null);
				quarters_peon_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter}));
				quarters_chieftain_button.setIconDisabler(new ChieftainDisabler(current_building));
				quarters_chieftain_button.setBuilding(current_building);
			}
			if (current_armory) {
				addChild(status_group);
				addChild(armory_group);
				if (viewer.getLocalPlayer().canUseRubber()) {
					build_group.addChild(build_weapon_rubber_button);
					army_group.addChild(army_warrior_rubber_button);
				} else {
					build_weapon_rubber_button.remove();
					army_warrior_rubber_button.remove();
				}
				updateCounters();
			}
		}
		updateButtons();
	}

	private final void updateButtons() {
		if (current_building != null && current_building.getAbilities().hasAbilities(Abilities.ATTACK)) {
			tower_attack_button.doUpdate();
			tower_exit_button.doUpdate();
		} else if (current_building != null && current_building.getAbilities().hasAbilities(Abilities.BUILD_ARMIES)) {
			unit_status.doUpdate();
			weapon_rock_status.doUpdate();
			weapon_iron_status.doUpdate();
			weapon_rubber_status.doUpdate();
			tree_status.doUpdate();
			rock_status.doUpdate();
			iron_status.doUpdate();
			rubber_status.doUpdate();

			harvest_button.doUpdate();
			build_button.doUpdate();
			army_button.doUpdate();
			
			harvest_tree_button.doUpdate();
			harvest_rock_button.doUpdate();
			harvest_iron_button.doUpdate();
			harvest_rubber_button.doUpdate();
			build_weapon_rock_button.doUpdate();
			build_weapon_iron_button.doUpdate();
			build_weapon_rubber_button.doUpdate();
			army_warrior_rubber_button.doUpdate();
			army_warrior_iron_button.doUpdate();
			army_warrior_rock_button.doUpdate();
			army_peon_button.doUpdate();
			transport_tree_button.doUpdate();
			transport_rock_button.doUpdate();
			transport_iron_button.doUpdate();
			transport_rubber_button.doUpdate();
		} else if (current_building != null && current_building.getAbilities().hasAbilities(Abilities.REPRODUCE)) {
			quarters_unit_status.doUpdate();
			
			quarters_peon_button.doUpdate();
			quarters_chieftain_button.doUpdate();
		} else if (current_peon) {
			quarters_button.doUpdate();
			armory_button.doUpdate();
			tower_button.doUpdate();
		}
		if (current_unit) {
			move_button.doUpdate();
			attack_button.doUpdate();
			gather_repair_button.doUpdate();
		}
		if (current_chieftain != null) {
			magic1_button.doUpdate();
			magic2_button.doUpdate();
		}
	}

	private final void removeGroups() {
		unit_group.remove();
		peon_group.remove();
		chieftain_group.remove();
		tower_group.remove();
		quarters_status_group.remove();
		quarters_group.remove();
		status_group.remove();
		armory_group.remove();
		harvest_group.remove();
		build_group.remove();
		army_group.remove();
		transport_group.remove();
		current_submenu = null;
	}

	private final void updateCounters() {
		assert current_building != null: "Building is null";
		SupplyCounter unit_counter = new SupplyCounter(current_building, Unit.class);
		unit_status.setCounter(unit_counter);
		SupplyCounter weapon_rock_counter = new SupplyCounter(current_building, RockAxeWeapon.class);
		weapon_rock_status.setCounter(weapon_rock_counter);
		SupplyCounter weapon_iron_counter = new SupplyCounter(current_building, IronAxeWeapon.class);
		weapon_iron_status.setCounter(weapon_iron_counter);
		SupplyCounter weapon_rubber_counter = new SupplyCounter(current_building, RubberAxeWeapon.class);
		weapon_rubber_status.setCounter(weapon_rubber_counter);
		SupplyCounter tree_counter = new SupplyCounter(current_building, TreeSupply.class);
		tree_status.setCounter(tree_counter);
		SupplyCounter rock_counter = new SupplyCounter(current_building, RockSupply.class);
		rock_status.setCounter(rock_counter);
		SupplyCounter iron_counter = new SupplyCounter(current_building, IronSupply.class);
		iron_status.setCounter(iron_counter);
		SupplyCounter rubber_counter = new SupplyCounter(current_building, RubberSupply.class);
		rubber_status.setCounter(rubber_counter);

		harvest_tree_button.setContainers(current_building, Building.KEY_DEPLOY_PEON_HARVEST_TREE, null);
		harvest_tree_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter}));
		harvest_rock_button.setContainers(current_building, Building.KEY_DEPLOY_PEON_HARVEST_ROCK, null);
		harvest_rock_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter}));
		harvest_iron_button.setContainers(current_building, Building.KEY_DEPLOY_PEON_HARVEST_IRON, null);
		harvest_iron_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter}));
		harvest_rubber_button.setContainers(current_building, Building.KEY_DEPLOY_PEON_HARVEST_RUBBER, null);
		harvest_rubber_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter}));

		build_weapon_rock_button.setBuildSupplyContainer(current_building, RockAxeWeapon.class);
		build_weapon_iron_button.setBuildSupplyContainer(current_building, IronAxeWeapon.class);
		build_weapon_rubber_button.setBuildSupplyContainer(current_building, RubberAxeWeapon.class);

		army_peon_button.setContainers(current_building, Building.KEY_DEPLOY_PEON, null);
		army_peon_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter}));
		army_warrior_rock_button.setContainers(current_building, Building.KEY_DEPLOY_ROCK_WARRIOR, RockAxeWeapon.class);
		army_warrior_rock_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter, weapon_rock_counter}));
		army_warrior_iron_button.setContainers(current_building, Building.KEY_DEPLOY_IRON_WARRIOR, IronAxeWeapon.class);
		army_warrior_iron_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter, weapon_iron_counter}));
		army_warrior_rubber_button.setContainers(current_building, Building.KEY_DEPLOY_RUBBER_WARRIOR, RubberAxeWeapon.class);
		army_warrior_rubber_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter, weapon_rubber_counter}));

		transport_tree_button.setContainers(current_building, Building.KEY_DEPLOY_PEON_TRANSPORT_TREE, TreeSupply.class);
		transport_tree_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter, tree_counter}));
		transport_rock_button.setContainers(current_building, Building.KEY_DEPLOY_PEON_TRANSPORT_ROCK, RockSupply.class);
		transport_rock_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter, rock_counter}));
		transport_iron_button.setContainers(current_building, Building.KEY_DEPLOY_PEON_TRANSPORT_IRON, IronSupply.class);
		transport_iron_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter, iron_counter}));
		transport_rubber_button.setContainers(current_building, Building.KEY_DEPLOY_PEON_TRANSPORT_RUBBER, RubberSupply.class);
		transport_rubber_button.setIconDisabler(new EmptySupplyDisabler(new SupplyCounter[]{unit_counter, rubber_counter}));
	}

	public final void displayChangedNotify(int width, int height) {
		setDim(width, height);
		updateGroups();
	}

	private void updateGroups() {
		int width = getWidth();
		int height = getHeight();
		unit_group.setPos(width - unit_group.getWidth(), height - unit_group.getHeight());
		peon_group.setPos(width - peon_group.getWidth(), unit_group.getY() - peon_group.getHeight());
		if (current_peon)
			chieftain_group.setPos(width - chieftain_group.getWidth(), peon_group.getY() - chieftain_group.getHeight());
		else
			chieftain_group.setPos(width - chieftain_group.getWidth(), unit_group.getY() - chieftain_group.getHeight());
		tower_group.setPos(width - tower_group.getWidth(), height - tower_group.getHeight());
		quarters_status_group.setPos(width - quarters_status_group.getWidth(), height - quarters_status_group.getHeight());
		quarters_group.setPos(width - quarters_group.getWidth(), quarters_status_group.getY() - quarters_group.getHeight());
		status_group.setPos(width - status_group.getWidth(), height - status_group.getHeight());
		armory_group.setPos(width - armory_group.getWidth(), status_group.getY() - armory_group.getHeight());
		harvest_group.setPos(width - harvest_group.getWidth(), status_group.getY() - harvest_group.getHeight());
		build_group.setPos(width - build_group.getWidth(), status_group.getY() - build_group.getHeight());
		army_group.setPos(width - army_group.getWidth(), status_group.getY() - army_group.getHeight());
		transport_group.setPos(width - transport_group.getWidth(), status_group.getY() - transport_group.getHeight());
	}

	protected final void keyReleased(KeyboardEvent event) {
		((GUIObject)getParent()).keyReleased(event);
	}

	protected final void keyPressed(KeyboardEvent event) {
		((GUIObject)getParent()).keyPressed(event);
	}

	public final boolean doKeyPressed(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_M:
			case Keyboard.KEY_Q:
				if (current_unit)
					return true;
				break;
			case Keyboard.KEY_A:
				if ((current_unit || current_armory || current_tower) && !event.isControlDown())
					return true;
				break;
			case Keyboard.KEY_P:
				if (current_quarters)
					return true;
				if (current_unit)
					break;
			case Keyboard.KEY_G:
			case Keyboard.KEY_T:
				if (current_unit || current_armory)
					return true;
			case Keyboard.KEY_C:
			case Keyboard.KEY_I:
			case Keyboard.KEY_W:
			case Keyboard.KEY_ESCAPE:
				if (current_armory)
					if (current_submenu == harvest_group ||
							current_submenu == build_group ||
							current_submenu == army_group ||
							current_submenu == transport_group)
						return true;
				break;
			case Keyboard.KEY_R:
				if (current_armory || current_quarters)
					return true;
				break;
			case Keyboard.KEY_X:
				if (current_tower)
					return true;
				break;
			default:
				break;
		}
		return false;
	}

	public final boolean doKeyRepeat(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_M:
				if (current_unit) {
					move_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					return true;
				}
				break;
			case Keyboard.KEY_A:
				if (!event.isControlDown()) {
					if (current_unit) {
						attack_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
						return true;
					} else if (current_armory) {
						if (current_submenu == null) {
							army_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
						} else {
							break;
						}
						return true;
					} else if (current_tower) {
						tower_attack_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
						return true;
					}
				}
				break;
			case Keyboard.KEY_G:
				if (current_unit) {
					gather_repair_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					return true;
				} else if (current_armory) {
					harvest_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					return true;
				}
				break;
			case Keyboard.KEY_C:
				if (current_quarters) {
//					if (Settings.getSettings().developer_mode) {
						quarters_chieftain_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
//					}
				} else if (current_chieftain != null) {
					Player player = viewer.getLocalPlayer();
					if (player.canDoMagic(1)) {
						magic2_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					}
				} else if (current_armory) {
					if (current_submenu == harvest_group) {
						harvest_rubber_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == build_group) {
						build_weapon_rubber_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == army_group) {
						army_warrior_rubber_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == transport_group) {
						transport_rubber_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else {
						break;
					}
					return true;
				}
				break;
			case Keyboard.KEY_Q:
				if (current_peon) {
					quarters_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					return true;
				}
				break;
			case Keyboard.KEY_P:
				if (current_quarters) {
					quarters_peon_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					return true;
				}
				if (current_armory) {
					if (current_submenu == army_group) {
						army_peon_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else {
						break;
					}
					return true;
				}
				break;
			case Keyboard.KEY_I:
				if (current_armory) {
					if (current_submenu == harvest_group) {
						harvest_iron_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == build_group) {
						build_weapon_iron_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == army_group) {
						army_warrior_iron_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == transport_group) {
						transport_iron_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else {
						break;
					}
					return true;
				}
				break;
			case Keyboard.KEY_R:
				if (current_peon) {
					armory_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					return true;
				} else if (current_armory) {
					if (current_submenu == harvest_group) {
						harvest_rock_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == build_group) {
						build_weapon_rock_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == army_group) {
						army_warrior_rock_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == transport_group) {
						transport_rock_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == null) {
						rally_point_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					} else {
						break;
					}
					return true;
				} else if (current_quarters) {
					quarters_rally_point_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					return true;
				}
				break;
			case Keyboard.KEY_S:
				if (current_chieftain != null) {
					Player player = viewer.getLocalPlayer();
					if (player.canDoMagic(0)) {
						magic1_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					}
				}
				break;
			case Keyboard.KEY_T:
				if (current_peon) {
					tower_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					return true;
				} else if (current_armory) {
					if (current_submenu == null) {
						transport_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					} else {
						break;
					}
					return true;
				}
				break;
			case Keyboard.KEY_X:
				if (current_tower) {
					tower_exit_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					return true;
				}
				break;
			case Keyboard.KEY_W:
				if (current_armory) {
					if (current_submenu == null) {
						build_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					} else if (current_submenu == harvest_group) {
						harvest_tree_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == transport_group) {
						transport_tree_button.shortcutPressed(event.isShiftDown(), event.isControlDown());
					} else {
						break;
					}

					return true;
				}
				break;
			case Keyboard.KEY_ESCAPE:
				if (current_armory) {
					if (current_submenu == harvest_group) {
						harvest_back_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					} else if (current_submenu == build_group) {
						build_back_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					} else if (current_submenu == army_group) {
						army_back_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					} else if (current_submenu == transport_group) {
						transport_back_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					} else {
						break;
					}
					return true;
				}
				break;
			default:
				break;
		}
		return false;
	}

	public final boolean doKeyReleased(KeyboardEvent event) {
		switch (event.getKeyCode()) {
			case Keyboard.KEY_C:
				if (current_armory) {
					if (current_submenu == harvest_group) {
						harvest_rubber_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == build_group) {
						build_weapon_rubber_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == army_group) {
						army_warrior_rubber_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == transport_group) {
						transport_rubber_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else {
						break;
					}
					return true;
				}
				break;
			case Keyboard.KEY_P:
				if (current_quarters) {
					quarters_peon_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					return true;
				}
				if (current_armory) {
					if (current_submenu == army_group) {
						army_peon_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else {
						break;
					}
					return true;
				}
				break;
			case Keyboard.KEY_I:
				if (current_armory) {
					if (current_submenu == harvest_group) {
						harvest_iron_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == build_group) {
						build_weapon_iron_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == army_group) {
						army_warrior_iron_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == transport_group) {
						transport_iron_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else {
						break;
					}
					return true;
				}
				break;
			case Keyboard.KEY_R:
				if (current_armory) {
					if (current_submenu == harvest_group) {
						harvest_rock_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == build_group) {
						build_weapon_rock_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == army_group) {
						army_warrior_rock_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == transport_group) {
						transport_rock_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else {
						break;
					}
					return true;
				}
				break;
			case Keyboard.KEY_T:
				if (current_armory) {
					if (current_submenu == null) {
						transport_button.mouseClickedAll(LocalInput.LEFT_BUTTON, 0, 0, 1);
					} else {
						break;
					}
					return true;
				}
				break;
			case Keyboard.KEY_W:
				if (current_armory) {
					if (current_submenu == harvest_group) {
						harvest_tree_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else if (current_submenu == transport_group) {
						transport_tree_button.shortcutReleased(event.isShiftDown(), event.isControlDown());
					} else {
						break;
					}
				return true;
				}
				break;
			default:
				break;
		}
		return false;
	}

	public boolean canHoverBehind() {
		return true;
	}

	protected final void renderGeometry() {
	}

	public final boolean inHarvestMenu() {
		return current_submenu == harvest_group;
	}

	public final boolean inBuildMenu() {
		return current_submenu == build_group;
	}

	public final boolean inArmyMenu() {
		return current_submenu == army_group;
	}

	public final boolean inTransportMenu() {
		return current_submenu == transport_group;
	}

					
	public final void mouseDragged(int button, int x, int y, int relative_x, int relative_y, int absolute_x, int absolute_y) {
		if (getParent() != null)
			((GUIObject)getParent()).mouseDragged(button, x, y, relative_x, relative_y, absolute_x, absolute_y);
	}

	private final strictfp class TargetListener implements MouseClickListener {
		private final int action;
		
		public TargetListener(int action) {
			this.action = action;
		}
		
		public final void mouseClicked(int button, int x, int y, int clicks) {
			viewer.getGUIRoot().pushDelegate(new TargetDelegate(viewer, camera, action));
		}
	}

	private final strictfp class GroupListener implements MouseClickListener {
		private final Group remove_group;
		private final Group add_group;

		public GroupListener(Group remove_group, Group add_group) {
			this.remove_group = remove_group;
			this.add_group = add_group;
		}

		public final void mouseClicked(int button, int x, int y, int clicks) {
			remove_group.remove();
			addChild(add_group);
			current_submenu = add_group;
			if (add_group == build_group)
				updateCounters();
		}
	}

	private final strictfp class CancelListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			removeGroups();
			update = true;
		}
	}

	private final strictfp class TowerExitListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			if (!current_building.isDead())
				viewer.getPeerHub().getPlayerInterface().exitTower(current_building);
			removeGroups();
			update = true;
		}
	}

	private final strictfp class RallyPointListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			if (!current_building.isDead())
				viewer.getGUIRoot().pushDelegate(new RallyPointDelegate(viewer,  camera, current_building));
			removeGroups();
			update = true;
		}
	}

	private final strictfp class ChieftainDisabler implements IconDisabler {
		private final Building building;

		public ChieftainDisabler(Building building) {
			this.building = building;
		}

		public final boolean isDisabled() {
			return !building.canBuildChieftain() && !building.canStopChieftain();
		}
	}

	private final strictfp class MagicDisabler implements IconDisabler {
		private final Unit unit;
		private final int magic_index;

		public MagicDisabler(Unit unit, int magic_index) {
			this.unit = unit;
			this.magic_index = magic_index;
		}

		public final boolean isDisabled() {
			return !unit.canDoMagic(magic_index);
		}
	}

	private final strictfp class EmptySupplyDisabler implements IconDisabler {
		private final SupplyCounter[] counters;

		public EmptySupplyDisabler(SupplyCounter[] counters) {
			this.counters = counters;
		}

		public final boolean isDisabled() {
			for (int i = 0; i < counters.length; i++) {
				if (counters[i].getNumSupplies() == 0)
					return true;
			}
			return false;
		}
	}

	private final strictfp class TowerActionDisabler implements IconDisabler {
		private final Building building;
		private final boolean exit;

		public TowerActionDisabler(Building building, boolean exit) {
			this.building = building;
			this.exit = exit;
		}

		public final boolean isDisabled() {
			if (exit)
				return !building.canExitTower();
			else
				return !building.getAbilities().hasAbilities(Abilities.ATTACK);
		}
	}

	private final strictfp class BuildingDisabler implements IconDisabler {
		private final int building;

		public BuildingDisabler(int building) {
			this.building = building;
		}

		public final boolean isDisabled() {
			return !viewer.getLocalPlayer().canBuild(building);
		}
	}

	private final strictfp class TowerPlaceListener implements MouseClickListener {
		public final void mouseClicked(int button, int x, int y, int clicks) {
			if (!Renderer.isRegistered()) {
				ResourceBundle db = ResourceBundle.getBundle(DemoForm.class.getName());
				Form demo_form = new InGameDemoForm(viewer, Utils.getBundleString(db, "tower_unavailable_header"), new GUIImage(512, 256, 0f, 0f, 1f, 1f, "/textures/gui/demo_towers"), Utils.getBundleString(db, "tower_unavailable"));
				viewer.getGUIRoot().addModalForm(demo_form);
			} else {
				viewer.getGUIRoot().pushDelegate(new PlacingDelegate(viewer, camera.getState(), Race.BUILDING_TOWER));
			}
		}
	}

	private final strictfp class PlaceListener implements MouseClickListener {
		private final int building_index;

		public PlaceListener(int building_index) {
			this.building_index = building_index;
		}

		public final void mouseClicked(int button, int x, int y, int clicks) {
			viewer.getGUIRoot().pushDelegate(new PlacingDelegate(viewer, camera.getState(), building_index));
		}
	}
}
