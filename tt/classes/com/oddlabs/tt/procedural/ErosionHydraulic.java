package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Channel;
import com.oddlabs.tt.resource.GLIntImage;

public final strictfp class ErosionHydraulic {

	private final static boolean save_frames = false;
	private final static boolean show_score = false;
	private final static boolean show_time = false;
	private final static boolean show_diff = false;

	private final static int rain_freq = 1;
	private final static float rain = 0.01f;
	private final static float solulibility = 0.01f;
	private final static float vaporization = 0.5f;
	
	private static Channel height;
	private static Channel height_d;
	private static Channel water;
	private static Channel water_d;
	private static Channel sediment;
	private static Channel sediment_d;
	private static Channel deposition;
	private static Channel old;

	public final static Channel erodeReference(Channel channel, int iterations) {
		height = channel;
		height_d = new Channel(height.width, height.height);
		water = new Channel(height.width, height.height);
		water_d = new Channel(height.width, height.height);
		sediment = new Channel(height.width, height.height);
		sediment_d = new Channel(height.width, height.height);
		deposition = new Channel(height.width, height.height);
		long start = System.currentTimeMillis();
		old = height.copy();
		for (int i = 0; i < iterations; i++) {
			water(i);
			dissolve();
			transport();
			sedimentation();
			if (save_frames && i%10 == 0) save(i + 1);
			if (show_score && i%10 == 0) score(i + 1);
			if (show_time && i%10 == 0) timer(i + 1, start);
			if (show_diff && i%10 == 0) diff(i + 1);
		}
		return height;
	}
	
	private final static void water(int i) {
		if (i%rain_freq == 0) water.add(rain);
	}
	
	private final static void dissolve() {
		sediment.channelAdd(water.copy().multiply(solulibility));
		height.channelSubtract(water.copy().multiply(solulibility));
	}
	
	private final static void transport() {
		float h, h1, h2, h3, h4, h5, h6, h7, h8, d1, d2, d3, d4, d5, d6, d7, d8, total_height, total_height_d, min_d, avr_height, water_amount, h_new, sum_d, factor, water_transport, sediment_amount, sediment_per_height, sediment_transport;
		int cells;
		for (int y = 1; y < height.height - 1; y++) {
			for (int x = 1; x < height.width - 1; x++) {
				h = height.getPixel(x, y) + water.getPixel(x, y);
				h1 = height.getPixel(x, y + 1) + water.getPixel(x, y + 1);
				h2 = height.getPixel(x - 1, y) + water.getPixel(x - 1, y);
				h3 = height.getPixel(x + 1, y) + water.getPixel(x + 1, y);
				h4 = height.getPixel(x, y - 1) + water.getPixel(x, y - 1);
				h5 = height.getPixel(x - 1, y + 1) + water.getPixel(x - 1, y + 1);
				h6 = height.getPixel(x + 1, y + 1) + water.getPixel(x + 1, y + 1);
				h7 = height.getPixel(x - 1, y - 1) + water.getPixel(x - 1, y - 1);
				h8 = height.getPixel(x + 1, y - 1) + water.getPixel(x + 1, y - 1);
				d1 = h - h1;
				d2 = h - h2;
				d3 = h - h3;
				d4 = h - h4;
				d5 = h - h5;
				d6 = h - h6;
				d7 = h - h7;
				d8 = h - h8;
				total_height = 0f;
				total_height_d = 0f;
				min_d = Float.MAX_VALUE;
				cells = 1;
				if (d1 > 0) {
					total_height_d+= d1;
					total_height+= h1;
					cells++;
					if (d1 < min_d) min_d = d1;
				}
				if (d2 > 0) {
					total_height_d+= d2;
					total_height+= h2;
					cells++;
					if (d2 < min_d) min_d = d2;
				}
				if (d3 > 0) {
					total_height_d+= d3;
					total_height+= h3;
					cells++;
					if (d3 < min_d) min_d = d3;
				}
				if (d4 > 0) {
					total_height_d+= d4;
					total_height+= h4;
					cells++;
					if (d4 < min_d) min_d = d4;
				}
				if (d5 > 0) {
					total_height_d+= d5;
					total_height+= h5;
					cells++;
					if (d5 < min_d) min_d = d5;
				}
				if (d6 > 0) {
					total_height_d+= d6;
					total_height+= h6;
					cells++;
					if (d6 < min_d) min_d = d6;
				}
				if (d7 > 0) {
					total_height_d+= d7;
					total_height+= h7;
					cells++;
					if (d7 < min_d) min_d = d7;
				}
				if (d8 > 0) {
					total_height_d+= d8;
					total_height+= h8;
					cells++;
					if (d8 < min_d) min_d = d8;
				}
				if (cells == 1) {
					continue;
				}
				avr_height = total_height/cells;
				water_amount = StrictMath.min(StrictMath.min(water.getPixel(x, y), h - avr_height), min_d);
				h_new = h - water_amount;
				sum_d = 0f;
				if (d1 > 0) sum_d+= (h_new - h1);
				if (d2 > 0) sum_d+= (h_new - h2);
				if (d3 > 0) sum_d+= (h_new - h3);
				if (d4 > 0) sum_d+= (h_new - h4);
				if (d5 > 0) sum_d+= (h_new - h5);
				if (d6 > 0) sum_d+= (h_new - h6);
				if (d7 > 0) sum_d+= (h_new - h7);
				if (d8 > 0) sum_d+= (h_new - h8);
				factor = 0f;
				if (sum_d > 0) factor = water_amount/sum_d;
				water_d.putPixel(x, y, water_d.getPixel(x, y) - water_amount);
				sediment_amount = 0f;
				if (water.getPixel(x, y) > 0) sediment_amount = sediment.getPixel(x, y)*water_amount/water.getPixel(x, y);
				sediment_per_height = sediment_amount/total_height_d;
				sediment_d.putPixel(x, y, sediment_d.getPixel(x, y) - sediment_amount);
				if (d1 > 0) {
					water_transport = factor*(h_new - h1);
					water_d.putPixel(x, y + 1, water_d.getPixel(x, y + 1) + water_transport);
					sediment_transport = d1*sediment_per_height;
					sediment_d.putPixel(x, y + 1, sediment_d.getPixel(x, y + 1) + sediment_transport);
				}
				if (d2 > 0) {
					water_transport = factor*(h_new - h2);
					water_d.putPixel(x - 1, y, water_d.getPixel(x - 1, y) + water_transport);
					sediment_transport = d2*sediment_per_height;
					sediment_d.putPixel(x - 1, y, sediment_d.getPixel(x - 1, y) + sediment_transport);
				}
				if (d3 > 0) {
					water_transport = factor*(h_new - h3);
					water_d.putPixel(x + 1, y, water_d.getPixel(x + 1, y) + water_transport);
					sediment_transport = d3*sediment_per_height;
					sediment_d.putPixel(x + 1, y, sediment_d.getPixel(x + 1, y) + sediment_transport);
				}
				if (d4 > 0) {
					water_transport = factor*(h_new - h4);
					water_d.putPixel(x, y - 1, water_d.getPixel(x, y - 1) + water_transport);
					sediment_transport = d4*sediment_per_height;
					sediment_d.putPixel(x, y - 1, sediment_d.getPixel(x, y - 1) + sediment_transport);
				}
				if (d5 > 0) {
					water_transport = factor*(h_new - h5);
					water_d.putPixel(x - 1, y + 1, water_d.getPixel(x - 1, y + 1) + water_transport);
					sediment_transport = d5*sediment_per_height;
					sediment_d.putPixel(x - 1, y + 1, sediment_d.getPixel(x - 1, y + 1) + sediment_transport);
				}
				if (d6 > 0) {
					water_transport = factor*(h_new - h6);
					water_d.putPixel(x + 1, y + 1, water_d.getPixel(x + 1, y + 1) + water_transport);
					sediment_transport = d6*sediment_per_height;
					sediment_d.putPixel(x + 1, y + 1, sediment_d.getPixel(x + 1, y + 1) + sediment_transport);
				}
				if (d7 > 0) {
					water_transport = factor*(h_new - h7);
					water_d.putPixel(x - 1, y - 1, water_d.getPixel(x - 1, y - 1) + water_transport);
					sediment_transport = d7*sediment_per_height;
					sediment_d.putPixel(x - 1, y - 1, sediment_d.getPixel(x - 1, y - 1) + sediment_transport);
				}
				if (d8 > 0) {
					water_transport = factor*(h_new - h8);
					water_d.putPixel(x + 1, y - 1, water_d.getPixel(x + 1, y - 1) + water_transport);
					sediment_transport = d8*sediment_per_height;
					sediment_d.putPixel(x + 1, y - 1, sediment_d.getPixel(x + 1, y - 1) + sediment_transport);
				}
			}
		}
		height.channelAddNoClip(height_d);
		height_d.fill(0f);
		water.channelAddNoClip(water_d);
		water_d.fill(0f);
		sediment.channelAddNoClip(sediment_d);
		sediment_d.fill(0f);
	}
	
	private final static void sedimentation() {
		water.multiply(1f - vaporization);
		deposition = sediment.copy().channelSubtract(water);
		sediment.channelSubtract(deposition);
		height.channelAdd(deposition);
	}
	
	private final static void save(int i) {
		if (i%10 == 0) {
			System.out.println("height   " + i + " checksum: " + height.sum());
			System.out.println("water    " + i + " checksum: " + water.sum());
			if (sediment != null) System.out.println("sediment " + i + " checksum: " + sediment.sum());
			//System.out.println("material " + i + " checksum: " + (height.sum() + sediment.sum()));
			if (i < 10) {
				new GLIntImage(height.toLayer()).saveAsBMP("height00" + i);
				//water.copy().dynamicRange().toLayer().toGLIntImage().saveAsBMP("water00" + i);
				//if (sediment != null) sediment.toLayer().toGLIntImage().saveAsBMP("sediment00" + i);
			} else if (i < 100) {
				new GLIntImage(height.toLayer()).saveAsBMP("height0" + i);
				//water.copy().dynamicRange().toLayer().toGLIntImage().saveAsBMP("water0" + i);
				//if (sediment != null) sediment.copy().dynamicRange().toLayer().toGLIntImage().saveAsBMP("sediment0" + i);
			} else {
				new GLIntImage(height.toLayer()).saveAsBMP("height" + i);
				//water.copy().dynamicRange().toLayer().toGLIntImage().saveAsBMP("water0" + i);
				//if (sediment != null) sediment.copy().dynamicRange().toLayer().toGLIntImage().saveAsBMP("sediment0" + i);
			}
		}
	}
	
	private final static void score(int i) {
		Channel slope = height.copy().lineart();
		float average = Analyzer.average(slope);
		float deviation = Analyzer.deviation(slope);
		float score = deviation/average;
		System.out.println(score);
	}
	
	private final static void timer(int i, long start) {
		long stop = System.currentTimeMillis();
		System.out.println((stop - start)/1000f);
	}
	
	private final static void diff(int i) {
		float diff = old.channelDifference(height).sum();
		System.out.println(diff/(old.width*old.height));
		old = height.copy();
	}

	public final static Channel erodeFast(Channel channel, int iterations) {
		height = channel;
		water = new Channel(channel.width, channel.height);
		deposition = new Channel(channel.width, channel.height);
		//float solulibility2 = solulibility*3.5f;
		float h, h1, h2, h3, h4, d1, d2, d3, d4, max_d, transport, sedimentation;
		int i, j;
		old = height.copy();
		long start = System.currentTimeMillis();
		for (int iter = 0; iter < iterations; iter++) {
			if (iter%rain_freq == 0) {
				//water.add(rain*10f);
				//height.add(-solulibility2*rain*10f);
				water.add(rain*0.03f);
				height.add(-rain*0.03f);
			}
			for (int y = 1; y < channel.height - 1; y++) {
				for (int x = 1; x < channel.width - 1; x++) {
					h = height.getPixel(x, y) + water.getPixel(x, y);
					h1 = height.getPixel(x, y + 1) + water.getPixel(x, y + 1);
					h2 = height.getPixel(x - 1, y) + water.getPixel(x - 1, y);
					h3 = height.getPixel(x + 1, y) + water.getPixel(x + 1, y);
					h4 = height.getPixel(x, y - 1) + water.getPixel(x, y - 1);
					d1 = h - h1;
					d2 = h - h2;
					d3 = h - h3;
					d4 = h - h4;
					i = 0;
					j = 0;
					max_d = 0f;
					if (d1 > max_d) {
						max_d = d1;
						j = 1;
					}
					if (d2 > max_d) {
						max_d = d2;
						i = -1;
						j = 0;
					}
					if (d3 > max_d) {
						max_d = d3;
						i = 1;
						j = 0;
					}
					if (d4 > max_d) {
						max_d = d4;
						i = 0;
						j = -1;
					}
					transport = Math.min(0.5f*max_d, water.getPixel(x, y));
					sedimentation = water.getPixel(x, y)*vaporization;
					water.putPixel(x, y, water.getPixel(x, y) - transport - sedimentation);
					water.putPixel(x + i, y + j, water.getPixel(x + i, y + j) + transport);
					//height.putPixel(x, y, height.getPixel(x, y) + sedimentation*solulibility2);
					height.putPixel(x, y, height.getPixel(x, y) + sedimentation);
				}
			}
			if (save_frames && iter%10 == 0) save(iter + 1);
			if (show_score && iter%10 == 0) score(iter + 1);
			if (show_time && iter%10 == 0) timer(iter + 1, start);
			if (show_diff && iter%10 == 0) diff(iter + 1);
		}
		return channel;
	}

}
