package com.oddlabs.converter;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.util.*;

public final strictfp class AnimationLoader {
	private AnimationLoader() {
	}

	public final static Map[] loadAnimation(File file) {
		try {
			FileInputStream input_stream = new FileInputStream(file);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new GeometryErrorHandler());
			Document document = builder.parse(input_stream);
			Element root = document.getDocumentElement();
			return parseAnimation(root);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final static Map[] parseAnimation(Node node) {
		NodeList frames = node.getChildNodes();
		Map anim_infos_map = new HashMap();
		for (int i = 0; i < frames.getLength(); i++) {
			Node frame = frames.item(i);
			if (frame.getNodeName().equals("frame")) {
				int frame_index = getAttrInt(frame, "index");
				assert frame_index >= 0;
				anim_infos_map.put(new Integer(frame_index), parseFrame(frame));
			}
		}
		Map[] anim_infos = new Map[anim_infos_map.size()];
		Iterator it = anim_infos_map.keySet().iterator();
		while (it.hasNext()) {
			Integer frame_index_obj = (Integer)it.next();
			Map frame = (Map)anim_infos_map.get(frame_index_obj);
			int index = frame_index_obj.intValue();
			assert anim_infos[index] == null;
			anim_infos[index] = frame;
		}
		return anim_infos;
	}

	public final static Map parseFrame(Node node) {
		NodeList bones = node.getChildNodes();
		Map bone_infos = new HashMap();
		for (int i = 0; i < bones.getLength(); i++) {
			Node bone = bones.item(i);
			if (bone.getNodeName().equals("transform")) {
				String name = bone.getAttributes().getNamedItem("name").getNodeValue();
				float[] matrix = new float[16];
				matrix[0*4 + 0] = getAttrFloat(bone, "m00");
				matrix[0*4 + 1] = getAttrFloat(bone, "m01");
				matrix[0*4 + 2] = getAttrFloat(bone, "m02");
				matrix[0*4 + 3] = getAttrFloat(bone, "m03");
				matrix[1*4 + 0] = getAttrFloat(bone, "m10");
				matrix[1*4 + 1] = getAttrFloat(bone, "m11");
				matrix[1*4 + 2] = getAttrFloat(bone, "m12");
				matrix[1*4 + 3] = getAttrFloat(bone, "m13");
				matrix[2*4 + 0] = getAttrFloat(bone, "m20");
				matrix[2*4 + 1] = getAttrFloat(bone, "m21");
				matrix[2*4 + 2] = getAttrFloat(bone, "m22");
				matrix[2*4 + 3] = getAttrFloat(bone, "m23");
				matrix[3*4 + 0] = getAttrFloat(bone, "m30");
				matrix[3*4 + 1] = getAttrFloat(bone, "m31");
				matrix[3*4 + 2] = getAttrFloat(bone, "m32");
				matrix[3*4 + 3] = getAttrFloat(bone, "m33");
				bone_infos.put(name, matrix);
			}
		}
		return bone_infos;
	}

	private final static int getAttrInt(Node node, String name) {
		return Integer.parseInt(node.getAttributes().getNamedItem(name).getNodeValue());
	}

	private final static float getAttrFloat(Node node, String name) {
		return Float.parseFloat(node.getAttributes().getNamedItem(name).getNodeValue());
	}
}
