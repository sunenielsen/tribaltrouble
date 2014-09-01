package com.oddlabs.converter;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.util.*;

public final strictfp class MeshLoader {
	private MeshLoader() {
	}

	public final static ModelInfo loadMesh(File file, Map name_to_bone_map, float scale) {
		try {
			FileInputStream input_stream = new FileInputStream(file);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new GeometryErrorHandler());
			Document document = builder.parse(input_stream);
			Element root = document.getDocumentElement();
			return createModelInfo(root, name_to_bone_map, scale);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final static ModelInfo createModelInfo(Node node, Map name_to_bone_map, float scale) {
//		String texture_name = cutTextureName(node.getAttributes().getNamedItem("texture").getNodeValue());

		NodeList polygon_list = ConvertToBinary.getNodeByName("polygons", node).getChildNodes();
		int num_polygons = countPolys(polygon_list);
		int num_vertices = num_polygons*3;
		float[] vertices = new float[num_vertices*3];
		float[] colors = new float[num_vertices*3];
		float[] normals = new float[num_vertices*3];
		float[] uvs = new float[num_vertices*2];
		byte[][] skin_names = new byte[num_vertices][];
		float[][] skin_weights = new float[num_vertices][];

		int polygon_index = 0;
		for (int i = 0; i < polygon_list.getLength(); i++) {
			Node polygon = polygon_list.item(i);
			if (polygon.getNodeType() == Node.ELEMENT_NODE && polygon.getNodeName().equals("polygon")) {
				NodeList vertex_list = polygon.getChildNodes();
				int vertex_index = 0;
				for (int j = 0; j < vertex_list.getLength(); j++) {
					Node vertex = vertex_list.item(j);
					if (!vertex.getNodeName().equals("vertex"))
						continue;
					// vertex data
					vertices[polygon_index*9 + vertex_index*3 + 0] = getAttrFloat(vertex, "x")*scale;
					vertices[polygon_index*9 + vertex_index*3 + 1] = getAttrFloat(vertex, "y")*scale;
					vertices[polygon_index*9 + vertex_index*3 + 2] = getAttrFloat(vertex, "z")*scale;
					colors[polygon_index*9 + vertex_index*3 + 0] = getAttrFloat(vertex, "r");
					colors[polygon_index*9 + vertex_index*3 + 1] = getAttrFloat(vertex, "g");
					colors[polygon_index*9 + vertex_index*3 + 2] = getAttrFloat(vertex, "b");
					float nx = getAttrFloat(vertex, "nx");
					float ny = getAttrFloat(vertex, "ny");
					float nz = getAttrFloat(vertex, "nz");
					float vec_len_inv = 1f/(float)StrictMath.sqrt(nx*nx + ny*ny + nz*nz);
					normals[polygon_index*9 + vertex_index*3 + 0] = nx*vec_len_inv;
					normals[polygon_index*9 + vertex_index*3 + 1] = ny*vec_len_inv;
					normals[polygon_index*9 + vertex_index*3 + 2] = nz*vec_len_inv;
					uvs[polygon_index*6 + vertex_index*2 + 0] = getAttrFloat(vertex, "u");
					uvs[polygon_index*6 + vertex_index*2 + 1] = getAttrFloat(vertex, "v");

					// skin data
					NodeList skins = vertex.getChildNodes();
					int skin_count = 0;
					for (int skin_index = 0; skin_index < skins.getLength(); skin_index++)
						if (skins.item(skin_index).getNodeName().equals("skin"))
							skin_count++;
					byte[] vertex_skin_names = new byte[skin_count];
					float[] vertex_skin_weights = new float[skin_count];
					skin_names[polygon_index*3 + vertex_index] = vertex_skin_names;
					skin_weights[polygon_index*3 + vertex_index] = vertex_skin_weights;
					int skin_index = 0;
					for (int k = 0; k < skins.getLength(); k++) {
						Node skin = skins.item(k);
						if (!skin.getNodeName().equals("skin"))
							continue;
						String skin_name = skin.getAttributes().getNamedItem("bone").getNodeValue();
						float skin_weight = getAttrFloat(skin, "weight");
						byte bone_index;
						if (name_to_bone_map != null) {
							Bone bone = (Bone)name_to_bone_map.get(skin_name);
							bone_index = bone.getIndex();
						} else
							bone_index = 0;
						vertex_skin_names[skin_index] = bone_index;
						vertex_skin_weights[skin_index] = skin_weight;
						skin_index++;
					}
					vertex_index++;
				}
				assert vertex_index == 3 : "Mesh not triangulated.";
				polygon_index++;
			}
		}
		assert polygon_index == num_polygons;
		return Optimizer.optimize(/*texture_name, */num_vertices, vertices, normals, colors, uvs, skin_names, skin_weights);
	}

	private final static String cutTextureName(String name) {
		if (name.equals(""))
			return name;
		String result = name.replaceAll("\\\\", "/");
		int last_slash = result.lastIndexOf("/");
		int dot_index = result.lastIndexOf(".");
		if (dot_index != -1)
			result = result.substring(last_slash + 1, dot_index);
		return result;
	}

	private final static int countPolys(NodeList polygon_list) {
		int counter = 0;
		for (int i = 0; i < polygon_list.getLength(); i++) {
			Node polygon = polygon_list.item(i);
			if (polygon.getNodeType() == Node.ELEMENT_NODE && polygon.getNodeName().equals("polygon"))
				counter++;
		}
		return counter;
	}

	private final static float getAttrFloat(Node node, String name) {
		return Float.parseFloat(node.getAttributes().getNamedItem(name).getNodeValue());
	}
}
