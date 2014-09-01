package com.oddlabs.converter2;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.util.*;

public final strictfp class SkeletonLoader {
	private SkeletonLoader() {
	}

	public final static Skeleton loadSkeleton(File file) {
		try {
			FileInputStream input_stream = new FileInputStream(file);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new GeometryErrorHandler());
			Document document = builder.parse(input_stream);
			Element root = document.getDocumentElement();
			return parseSkeleton(root);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final static Skeleton parseSkeleton(Node skel_node) {
		Map name_to_bone_map = new HashMap();
		Map initial_pose = AnimationLoader.parseFrame(ConvertToBinary.getNodeByName("init_pose", skel_node));
		NodeList bone_list = ConvertToBinary.getNodeByName("bones", skel_node).getChildNodes();
		Map bone_parent_map = new HashMap();
		for (int i = 0; i < bone_list.getLength(); i++) {
			Node bone_node = bone_list.item(i);
			if (bone_node.getNodeName().equals("bone")) {
				String bone_name = bone_node.getAttributes().getNamedItem("name").getNodeValue();
				String bone_parent_name = bone_node.getAttributes().getNamedItem("parent").getNodeValue();
//System.out.println("bone name = " + bone_name + " parent name = " + bone_parent_name);
				bone_parent_map.put(bone_name, bone_parent_name);
			}
		}
		Map bone_children_map = new HashMap();
		Iterator it = bone_parent_map.keySet().iterator();
		String root = null;
		while (it.hasNext()) {
			String name = (String)it.next();
			String parent = (String)bone_parent_map.get(name);
			if (bone_parent_map.get(parent) == null) {
			   if (root != null) {
				   System.out.println("WARNING: Multiple roots in skeleton, root = " + root + ", additional root = " + name);
				   parent = root;
				   bone_parent_map.put(name, parent);
			   } else
				   root = name;
			}
			List parent_children = (List)bone_children_map.get(parent);
			if (parent_children == null) {
				parent_children = new ArrayList();
				bone_children_map.put(parent, parent_children);
			}
			parent_children.add(name);
		}
		Bone bone_root = buildBone((byte)0, bone_children_map, root, name_to_bone_map);
		return new Skeleton(bone_root, initial_pose, name_to_bone_map);
	}

	private final static Bone buildBone(byte index, Map bone_children_map, String bone_name, Map name_to_bone_map) {
		List children_list = (List)bone_children_map.get(bone_name);
		Bone[] children_array;
		if (children_list != null) {
			children_array = new Bone[children_list.size()];
			for (int i = 0; i < children_array.length; i++) {
				String child_name = (String)children_list.get(i);
				Bone child_bone = buildBone(index, bone_children_map, child_name, name_to_bone_map);
				children_array[i] = child_bone;
				index = (byte)(child_bone.getIndex() + 1);
			}
		} else
			children_array = new Bone[0];
		Bone bone = new Bone(bone_name, index, children_array);
		name_to_bone_map.put(bone_name, bone);
		return bone;
	}
}
