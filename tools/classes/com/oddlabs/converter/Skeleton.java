package com.oddlabs.converter;

import java.util.Map;

public final strictfp class Skeleton {
	private final Bone bone_root;
	private final Map initial_pose;
	private final Map name_to_bone_map;

	public Skeleton(Bone bone_root, Map initial_pose, Map name_to_bone_map) {
		this.bone_root = bone_root;
		this.initial_pose = initial_pose;
		this.name_to_bone_map = name_to_bone_map;
	}

	public final Map getNameToBoneMap() {
		return name_to_bone_map;
	}

	public final Bone getBoneRoot() {
		return bone_root;
	}

	public final Map getInitialPose() {
		return initial_pose;
	}
}
