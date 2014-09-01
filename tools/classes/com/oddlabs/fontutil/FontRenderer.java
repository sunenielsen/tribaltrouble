package com.oddlabs.fontutil;

import java.io.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import com.oddlabs.procedural.*;
import com.oddlabs.util.*;

public final strictfp class FontRenderer {
	private final static int GLYPH_X_BORDER = 4;
	private final static int GLYPH_Y_BORDER = 3;
	private final static int GLYPH_X_OVERLAP = 7;
	private final static int GLYPH_Y_OVERLAP = 5;
	private final static float SPACE_SCALE = 0.66666f;

	public final static void main(String[] args) throws Exception {
		/*
		   System.out.println("Available fonts:");
		   String[] fontnames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		   for (int i = 0; i < fontnames.length; i++) {
		   System.out.println(fontnames[i]);
		   }
		 */
		new FontRenderer(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]),
				args[4], args[5], args[6]);
		System.out.println("Conversion complete\n");
	}

	public FontRenderer(String src_font_name, int font_size, int max_image_size, int max_chars, String font_info_dir, String font_tex_dir, String font_tex_classpath) throws Exception {
		System.out.println("Converting first " + max_chars + " chars of " + src_font_name + " size " + font_size);
		String dest_font_name = src_font_name.toLowerCase();
		InputStream font_is = Utils.makeURL("/" + dest_font_name + ".ttf").openStream();
		java.awt.Font src_font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, font_is).deriveFont((float)font_size);

		char[] chars = new char[max_chars];
		for (int i = 0; i < max_chars; i++) {
			chars[i] = (char)i;
		}

		// calculate space width
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = (Graphics2D)image.getGraphics();
		g2d.setFont(src_font);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		FontRenderContext frc = g2d.getFontRenderContext();
		char[] current_char = new char[] {'m', ' ', 'm'};
		GlyphVector gv = src_font.createGlyphVector(frc, current_char);

		Shape glyph_shape0 = gv.getGlyphOutline(0);
		Rectangle2D glyph_bounds0 = glyph_shape0.getBounds2D();

		Shape glyph_shape2 = gv.getGlyphOutline(2);
		Rectangle2D glyph_bounds2 = glyph_shape2.getBounds2D();
		float space_width_f= (int)StrictMath.ceil(glyph_bounds2.getMinX()) - (int)StrictMath.floor(glyph_bounds0.getMaxX());
		int space_width = (int)StrictMath.ceil(space_width_f*SPACE_SCALE) + 2*GLYPH_X_BORDER;
		System.out.println("space_width: " + space_width);

		// calculate optimal image width and height
		int min_area = Integer.MAX_VALUE;
		int best_width = 0;
		int best_height = 0;
		int image_width = max_image_size;
		int image_height = 0;
		int[] heights = null;
		while (image_width > image_height) {
			heights = calculateImageHeight(src_font, image_width, space_width, chars);
			image_height = heights[0];
			int area = image_width*image_height;
			if (area <= min_area) {
				best_width = image_width;
				best_height = image_height;
				min_area = area;
			}
			image_width = image_width>>1;
		}

		// draw font images
		System.out.println("optimal width*height: " + best_width + "*" + best_height);
		int max_glyph_height = heights[1];
		int max_baseline_height = heights[2];
		Channel white_alpha = drawFont(src_font, font_tex_classpath, font_info_dir, dest_font_name, font_size, max_glyph_height, max_baseline_height, best_width, best_height, space_width, chars, true);
		Channel shadow = drawFont(src_font, font_tex_classpath, font_info_dir, dest_font_name, font_size, max_glyph_height, max_baseline_height, best_width, best_height, space_width, chars, false);

		Channel black = new Channel(white_alpha.getWidth(), white_alpha.getHeight()).fill(0f);
		Channel white = new Channel(white_alpha.getWidth(), white_alpha.getHeight()).fill(1f);
		shadow.gamma(0.5f);
		Channel black_alpha = shadow.copy();
		black_alpha.channelBlend(white, shadow.copy().offset(0, -1));
		black_alpha.channelBlend(white, shadow.copy().offset(-1, 0));
		black_alpha.channelBlend(white, shadow.copy().offset(1, 0));
		black_alpha.channelBlend(white, shadow.copy().offset(0, 1));
		black_alpha.smooth(1).smooth(1).offset(1, 1);
		Layer font_image = new Layer(black, black, black, black_alpha);
		Layer highlight = new Layer(white, white, white, white_alpha);
		font_image.layerBlend(highlight);

		font_image.saveAsPNG(font_tex_dir + File.separator + dest_font_name + "_" + font_size);
	}

	private final int[] calculateImageHeight(Font src_font,  int image_width, int space_width, char[] chars) {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = (Graphics2D)image.getGraphics();
		g2d.setFont(src_font);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		FontRenderContext frc = g2d.getFontRenderContext();

		int max_baseline_height = 0;
		int max_under_baseline_height = 0;
		int num_lines = 1;
		int current_x = 0;

		// place chars
		System.out.println("Calculating char placement for width = " + image_width);
		System.out.print("Progress");
		for (int i = 0; i < chars.length; i++) {
			if (i % 1000 == 0) {
				System.out.print(".");
			}
			char ch = (char)i;
			if (src_font.canDisplay(chars[ch])) {
				char[] current_char = new char[] {chars[ch]};
				GlyphVector gv = src_font.createGlyphVector(frc, current_char);
				Shape glyph_shape = gv.getGlyphOutline(0);
				Rectangle2D glyph_bounds = glyph_shape.getBounds2D();
				int min_x = (int)StrictMath.floor(glyph_bounds.getMinX()) - GLYPH_X_BORDER;
				int min_y = (int)StrictMath.floor(glyph_bounds.getMinY()) - GLYPH_Y_BORDER;
				int max_x = (int)StrictMath.ceil(glyph_bounds.getMaxX()) + GLYPH_X_BORDER;
				int max_y = (int)StrictMath.ceil(glyph_bounds.getMaxY()) + GLYPH_Y_BORDER;
				int baseline_height = -min_y;
				if (baseline_height > max_baseline_height)
					max_baseline_height = baseline_height;
				int under_baseline_height = max_y;
				if (under_baseline_height > max_under_baseline_height)
					max_under_baseline_height = under_baseline_height;
				int glyph_width;
				if (i == ' ')
					glyph_width = space_width;
				else
					glyph_width = max_x - min_x;
				assert glyph_width <= image_width : "character too wide to fit in image";
				if (current_x + glyph_width > image_width) {
					current_x = 0;
					num_lines++;
				}
				current_x += glyph_width;
			}
		}
		System.out.println("done");
		int max_glyph_height = max_under_baseline_height + max_baseline_height;
		int image_height = Utils.nextPowerOf2(max_glyph_height*num_lines);
		return new int[]{image_height, max_glyph_height, max_baseline_height};
	}

	private final Channel drawFont(Font src_font, String font_tex_classpath, String font_info_dir, String dest_font_name, int font_size, int max_glyph_height, int max_baseline_height, int image_width, int image_height, int space_width, char[] chars, boolean create_xml) {
		BufferedImage image = new BufferedImage(image_width, image_height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = (Graphics2D)image.getGraphics();
		g2d.setFont(src_font);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		FontRenderContext frc = g2d.getFontRenderContext();

		g2d.translate(0, max_baseline_height);
		int current_x = 0;
		int current_y = 0;
		int valid_chars = 0;
		Quad[] key_map = null;
		if (create_xml) {
			key_map = new Quad[Character.MAX_VALUE];
		}

		System.out.println("Drawing chars for width*height = " + image_width + "*" + image_height);
		System.out.print("Progress");
		for (int i = 0; i < chars.length; i++) {
			if (i % 1000 == 0) {
				System.out.print(".");
			}
			char ch = (char)i;
			if (src_font.canDisplay(chars[ch])) {
				valid_chars++;
				char[] current_char = new char[] {chars[ch]};
				GlyphVector gv = src_font.createGlyphVector(frc, current_char);
				Shape glyph_shape = gv.getGlyphOutline(0);
				Rectangle2D glyph_bounds = glyph_shape.getBounds2D();
				int min_x = (int)StrictMath.floor(glyph_bounds.getMinX()) - GLYPH_X_BORDER;
				//int min_y = (int)StrictMath.floor(glyph_bounds.getMinY()) - GLYPH_Y_BORDER;
				int max_x = (int)StrictMath.ceil(glyph_bounds.getMaxX()) + GLYPH_X_BORDER;
				//int max_y = (int)StrictMath.ceil(glyph_bounds.getMaxY()) + GLYPH_Y_BORDER;
				int glyph_width;
				if (i == 32)
					glyph_width = space_width;
				else
					glyph_width = max_x - min_x;
				if (current_x + glyph_width > image_width) {
					g2d.translate(-current_x, max_glyph_height);
					current_x = 0;
					current_y += max_glyph_height;
				}
				if (create_xml) {
					float left = (float)current_x/image_width;
					float bottom = 1f - (float)(current_y + max_glyph_height)/image_height;
					float top = 1f - (float)current_y/image_height;
					float right = (float)(current_x + glyph_width)/image_width;
					key_map[i] = new Quad(left, bottom, right, top, glyph_width, max_glyph_height);
				}
				g2d.translate(-min_x, 0);
				g2d.translate(0, -1);
				g2d.fill(glyph_shape);
				g2d.translate(0, 1);
				g2d.translate(min_x + glyph_width, 0);
				current_x += glyph_width;
			}
		}

		System.out.println("done");
		if (create_xml) {
			String tex_name = font_tex_classpath + "/" + dest_font_name + "_" + font_size;
			FontInfo font_info = new FontInfo(tex_name, key_map, GLYPH_X_OVERLAP, GLYPH_Y_OVERLAP, max_glyph_height);
			String font_file_name = font_info_dir + File.separator + dest_font_name + "_" + font_size + ".font";
			font_info.saveToFile(font_file_name);
			System.out.println("Number of valid chars found: " + valid_chars);
		}

		Channel channel = new Channel(image_width, image_height);
		byte[] image_pixels = (byte[])image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int pos = y * image.getWidth() * 4 + x * 4;
				byte alpha = image_pixels[pos + 3];
				//byte img = image_pixels[pos + 1];
				int pixel = alpha & 0xff;
				float channel_pixel = pixel/255f;
				channel.putPixel(x, y, channel_pixel);
			}
		}
		return channel;
	}
}
