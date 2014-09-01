package com.oddlabs.converter;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import com.oddlabs.geometry.*;

public final strictfp class ConvertToBinary {
	public final static void main(String[] args) {
		if (args.length != 3)
			throw new RuntimeException("Invalid number of argument");
		String xml_file = args[0];
		String src_dir = args[1];
		String build_dir = args[2];

		try {
			FileInputStream input_stream = new FileInputStream(src_dir + File.separatorChar + xml_file);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new GeometryErrorHandler());
			Document document = builder.parse(input_stream);
			org.w3c.dom.Element root = document.getDocumentElement();
			parseGeometry(root, src_dir, build_dir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final static void parseGeometry(Node n, String src_dir, String build_dir) {
		if (n.hasChildNodes()) {
			NodeList nl = n.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++)
				if (nl.item(i).getNodeType() == Node.ELEMENT_NODE)
					parseGroup(nl.item(i), src_dir, build_dir);
		}
	}

	private final static void parseGroup(Node n, String src_dir, String build_dir) {
		if (n.hasChildNodes()) {
			String new_build_dir = build_dir + File.separatorChar + getName(n);
			NodeList nl = n.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node child = nl.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					String name = child.getNodeName();
					if (name.equals("sprite"))
						parseSprite(child, src_dir, new_build_dir);
					else if (name.equals("lowdetail"))
						parseLowDetail(child, src_dir, new_build_dir);
				}
			}
		}
	}

	private final static boolean isModified(File src, File dest) {
		return !dest.exists() || dest.lastModified() < src.lastModified();
	}

	private final static ModelObjectInfo[] getModelObjectInfos(Node n, String src_dir) {
		NodeList nl = n.getChildNodes();
		List object_infos = new ArrayList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item.getNodeName().equals("model")) {
				float r = getInt(item, "r")/255f;
				float g = getInt(item, "g")/255f;
				float b = getInt(item, "b")/255f;
				String[][] textures = getTextureInfos(item, src_dir);
				object_infos.add(new ModelObjectInfo(new File(src_dir, getText(item)), textures, new float[]{r, g, b}));
			}
		}
		ModelObjectInfo[] infos = new ModelObjectInfo[object_infos.size()];
		object_infos.toArray(infos);
		return infos;
	}

	private final static AnimObjectInfo[] getAnimObjectInfos(Node n, String src_dir) {
		NodeList nl = n.getChildNodes();
		List object_infos = new ArrayList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item.getNodeName().equals("animation")) {
				float wpc = Float.parseFloat(item.getAttributes().getNamedItem("wpc").getNodeValue());
				assert wpc != 0f;
				String type_str = item.getAttributes().getNamedItem("type").getNodeValue();
				int type = getTypeFromString(type_str);
				object_infos.add(new AnimObjectInfo(new File(src_dir, getText(item)), wpc, type));
			}
		}
		AnimObjectInfo[] infos = new AnimObjectInfo[object_infos.size()];
		object_infos.toArray(infos);
		return infos;
	}

	private final static String[][] getTextureInfos(Node n, String src_dir) {
		NodeList nl = n.getChildNodes();
		List object_infos = new ArrayList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item.getNodeName().equals("texture")) {
				String name = item.getAttributes().getNamedItem("name").getNodeValue();
				Node team_node = item.getAttributes().getNamedItem("team");
				String team_name;
				if (team_node != null)
					team_name = team_node.getNodeValue();
				else
					team_name = null;
				object_infos.add(new String[]{name, team_name});
			}
		}
		String[][] infos = new String[object_infos.size()][];
		object_infos.toArray(infos);
		return infos;
	}

	private final static void parseSprite(Node n, String src_dir, String build_dir) {
		String name = getName(n);
		AnimObjectInfo[] anim_object_infos = getAnimObjectInfos(n, src_dir);
		ModelObjectInfo[] model_object_infos = getModelObjectInfos(n, src_dir);
		File build_file = new File(build_dir + File.separatorChar + name + ".binsprite");

		boolean modified = false;
		for (int i = 0; i < anim_object_infos.length; i++) {
			if (isModified(anim_object_infos[i].getFile(), build_file)) {
				modified = true;
				break;
			}
		}
		for (int i = 0; i < model_object_infos.length; i++) {
			if (isModified(model_object_infos[i].getFile(), build_file)) {
				modified = true;
				break;
			}
		}
		if (modified) {
			float scale;
			Node scale_node = n.getAttributes().getNamedItem("scale");
			if (scale_node != null)
				scale = Float.parseFloat(scale_node.getNodeValue());
			else
				scale = 1f;
			ObjectInfo skeleton_info = getSkeletonObjectInfo(n, src_dir);
			AnimationInfo[] animations;
			Map name_to_bone_map;
			if (skeleton_info != null) {
				Skeleton skeleton = SkeletonLoader.loadSkeleton(getSkeletonObjectInfo(n, src_dir).getFile());
				name_to_bone_map = skeleton.getNameToBoneMap();
				animations = new AnimationInfo[anim_object_infos.length];
				for (int i = 0; i < anim_object_infos.length; i++) {
					AnimObjectInfo current = anim_object_infos[i];
					Map[] animation_map = AnimationLoader.loadAnimation(current.getFile());
					assert animations[i] == null;
					animations[i] = Optimizer.convertToAnimation(skeleton.getBoneRoot(), skeleton.getInitialPose(), animation_map, current.getType(), current.getWPC());
				}
			} else {
				float[][] identity_frame = {{1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0}};
				animations = new AnimationInfo[]{new AnimationInfo(identity_frame, AnimationInfo.ANIM_LOOP, 1f)};
				name_to_bone_map = null;
			}
			SpriteInfo[] sprite_models = new SpriteInfo[model_object_infos.length];
			for (int i = 0; i < model_object_infos.length; i++) {
				ModelObjectInfo current = model_object_infos[i];
				ModelInfo model_info = MeshLoader.loadMesh(current.getFile(), name_to_bone_map, scale);
				assert sprite_models[i] == null;
				sprite_models[i] = Optimizer.convertToSprite(current.getTextures(), model_info, current.getClearColor());
			}
			write(new Object[]{sprite_models, animations}, build_file);
		}
	}

	private final static ObjectInfo getSkeletonObjectInfo(Node n, String src_dir) {
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item.getNodeName().equals("skeleton")) {
				return new ObjectInfo(new File(src_dir, getText(item)));
			}
		}
		return null;
	}

	private final static void parseLowDetail(Node n, String src_dir, String build_dir) {
		String name = getName(n);
		ObjectInfo object_info = getModelObjectInfos(n, src_dir)[0];
		File build_file = new File(build_dir + File.separatorChar + name + ".binlowdetail");

		if (isModified(object_info.getFile(), build_file)) {
			ModelInfo model_info = MeshLoader.loadMesh(object_info.getFile(), null, 1f);
			LowDetailModel lowdetailmodel = new LowDetailModel(model_info.indices, model_info.vertices, model_info.texcoords);
			write(lowdetailmodel, build_file);
		}
	}

	public final static Node getNodeByName(String name, Node n) {
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().equals(name))
				return nl.item(i);
		}
		throw new RuntimeException("Missing node: " + name);
	}

	private final static String getName(Node n) {
		return n.getAttributes().getNamedItem("name").getNodeValue();
	}

	private final static int getInt(Node n, String key) {
		String string = n.getAttributes().getNamedItem(key).getNodeValue();
		return Integer.parseInt(string);
	}

	private final static int getTypeFromString(String str) {
		if (str.equals("loop"))
			return AnimationInfo.ANIM_LOOP;
		else if (str.equals("plain"))
			return AnimationInfo.ANIM_PLAIN;
		else
			throw new RuntimeException("Unknown animation type: " + str);
	}

	private final static String getText(Node n) {
		return n.getFirstChild().getNodeValue().trim();
	}

	private final static void write(Object output, File file) {
		System.err.println("Saving to " + file);
		FileOutputStream file_stream;
		ObjectOutputStream obj_stream;

		try {
			file.getParentFile().mkdirs();
			file_stream = new FileOutputStream(file);
			obj_stream = new ObjectOutputStream(new BufferedOutputStream(file_stream));
			obj_stream.writeObject(output);
			obj_stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
