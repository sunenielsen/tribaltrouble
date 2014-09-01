package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Channel;
import com.oddlabs.tt.resource.GLIntImage;

public final strictfp class ErosionThermal {

	private final static boolean save_frames = false;
	private final static boolean show_score = false;
	private final static boolean show_time = false;
	private final static boolean show_diff = false;
	
	private static Channel height;
	private static Channel height_d;
	private static Channel old;

	public final static Channel erodeReference(Channel channel, int iterations) {
		height = channel;
		height_d = new Channel(channel.width, channel.height);
		float talus = 4f/channel.width;
		long start = System.currentTimeMillis();
		old = height.copy();
		for (int iter = 0; iter < iterations; iter++) {
			for (int y = 1; y < channel.height - 1; y++) {
				for (int x = 1; x < channel.width - 1; x++) {
					float h = channel.getPixel(x, y);
					float h1 = channel.getPixel(x, y + 1);
					float h2 = channel.getPixel(x - 1, y);
					float h3 = channel.getPixel(x + 1, y);
					float h4 = channel.getPixel(x, y - 1);
					float h5 = channel.getPixel(x - 1, y + 1);
					float h6 = channel.getPixel(x + 1, y + 1);
					float h7 = channel.getPixel(x - 1, y - 1);
					float h8 = channel.getPixel(x + 1, y - 1);
					float d1 = h - h1;
					float d2 = h - h2;
					float d3 = h - h3;
					float d4 = h - h4;
					float d5 = h - h5;
					float d6 = h - h6;
					float d7 = h - h7;
					float d8 = h - h8;
					float max_d = Float.MIN_VALUE;
					float total_d = 0;
					if (d1 > talus) {
						total_d+= d1;
						if (d1 > max_d) {
							max_d = d1;
						}
					}
					if (d2 > talus) {
						total_d+= d2;
						if (d2 > max_d) {
							max_d = d2;
						}
					}
					if (d3 > talus) {
						total_d+= d3;
						if (d3 > max_d) {
							max_d = d3;
						}
					}
					if (d4 > talus) {
						total_d+= d4;
						if (d4 > max_d) {
							max_d = d4;
						}
					}
					if (d5 > talus) {
						total_d+= d5;
						if (d5 > max_d) {
							max_d = d5;
						}
					}
					if (d6 > talus) {
						total_d+= d6;
						if (d6 > max_d) {
							max_d = d6;
						}
					}
					if (d7 > talus) {
						total_d+= d7;
						if (d7 > max_d) {
							max_d = d7;
						}
					}
					if (d8 > talus) {
						total_d+= d8;
						if (d8 > max_d) {
							max_d = d8;
						}
					}
					if (max_d <= talus) {
						continue;
					}
					float amount = 0.5f*(max_d - talus);
					float factor = amount/total_d;
					height_d.putPixel(x, y, height_d.getPixel(x, y) - amount);
					if (d1 > 0) height_d.putPixel(x, y + 1, height_d.getPixel(x, y + 1) + factor*d1);
					if (d2 > 0) height_d.putPixel(x - 1, y, height_d.getPixel(x - 1, y) + factor*d2);
					if (d3 > 0) height_d.putPixel(x + 1, y, height_d.getPixel(x + 1, y) + factor*d3);
					if (d4 > 0) height_d.putPixel(x, y - 1, height_d.getPixel(x, y - 1) + factor*d4);
					if (d5 > 0) height_d.putPixel(x - 1, y + 1, height_d.getPixel(x - 1, y + 1) + factor*d5);
					if (d6 > 0) height_d.putPixel(x + 1, y + 1, height_d.getPixel(x + 1, y + 1) + factor*d6);
					if (d7 > 0) height_d.putPixel(x - 1, y - 1, height_d.getPixel(x - 1, y - 1) + factor*d7);
					if (d8 > 0) height_d.putPixel(x + 1, y - 1, height_d.getPixel(x + 1, y - 1) + factor*d8);
				}
			}
			height.channelAddNoClip(height_d);
			height_d.fill(0f);
			if (save_frames && iter%10 == 0) save(iter + 1);
			if (show_score && iter%10 == 0) score(iter + 1);
			if (show_time && iter%10 == 0) timer(iter + 1, start);
			if (show_diff && iter%10 == 0) diff(iter + 1);
		}
		return channel;
	}

	public final static Channel erodeFast(Channel channel, int iterations) {
		height = channel;
		float talus = 3.75f/channel.width;
		long start = System.currentTimeMillis();
		old = height.copy();
		for (int iter = 0; iter < iterations; iter++) {
			for (int y = 1; y < channel.height - 1; y++) {
				for (int x = 1; x < channel.width - 1; x++) {
					float h = channel.getPixel(x, y);
					/*
					float h1 = channel.getPixel(x, y + 1);
					float h2 = channel.getPixel(x - 1, y);
					float h3 = channel.getPixel(x + 1, y);
					float h4 = channel.getPixel(x, y - 1);
					*/
					float h1 = channel.getPixel(x - 1, y + 1);
					float h2 = channel.getPixel(x + 1, y + 1);
					float h3 = channel.getPixel(x - 1, y - 1);
					float h4 = channel.getPixel(x + 1, y - 1);
					
					float d1 = h - h1;
					float d2 = h - h2;
					float d3 = h - h3;
					float d4 = h - h4;
					int i = 0, j = 0;
					float max_d = 0f;
					/*
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
					*/
					if (d1 > max_d) {
						max_d = d1;
						i = -1;
						j = 1;
					}
					if (d2 > max_d) {
						max_d = d2;
						i = 1;
						j = 1;
					}
					if (d3 > max_d) {
						max_d = d3;
						i = -1;
						j = -1;
					}
					if (d4 > max_d) {
						max_d = d4;
						i = 1;
						j = -1;
					}
					
					if (max_d <= talus) {
						continue;
					}
					max_d *= 0.25f;
					height.putPixel(x, y, height.getPixel(x, y) - max_d);
					height.putPixel(x + i, y + j, height.getPixel(x + i, y + j) + max_d);
				}
			}
			if (save_frames && iter%10 == 0) save(iter + 1);
			if (show_score && iter%10 == 0) score(iter + 1);
			if (show_time && iter%10 == 0) timer(iter + 1, start);
			if (show_diff && iter%10 == 0) diff(iter + 1);
		}
		return channel;
	}
	
	public final static Channel erodeFast2(Channel channel, int iterations) {
		height = channel;
		float talus = 12f/channel.width;
		long start = System.currentTimeMillis();
		old = height.copy();
		for (int iter = 0; iter < iterations; iter++) {
			for (int y = 1; y < channel.height - 1; y++) {
				for (int x = 1; x < channel.width - 1; x++) {
					float h = channel.getPixel(x, y);
					float h1 = channel.getPixel(x, y + 1);
					float h2 = channel.getPixel(x - 1, y);
					float h3 = channel.getPixel(x + 1, y);
					float h4 = channel.getPixel(x, y - 1);
					float d1 = h - h1;
					float d2 = h - h2;
					float d3 = h - h3;
					float d4 = h - h4;
					int i = 0, j = 0;
					float max_d = 0f;
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
					if (max_d > talus) {
						continue;
					}
					max_d *= 0.5f;
					height.putPixel(x, y, height.getPixel(x, y) - max_d);
					height.putPixel(x + i, y + j, height.getPixel(x + i, y + j) + max_d);
				}
			}
			if (save_frames && iter%10 == 0) save(iter + 1);
			if (show_score && iter%10 == 0) score(iter + 1);
			if (show_time && iter%10 == 0) timer(iter + 1, start);
			if (show_diff && iter%10 == 0) diff(iter + 1);
		}
		return channel;
	}
	
	public final static Channel erodeFast3(Channel channel, int iterations) {
		height = channel;
		float talus = 16f/channel.width;
		long start = System.currentTimeMillis();
		old = height.copy();
		for (int iter = 0; iter < iterations; iter++) {
			for (int y = 1; y < channel.height - 1; y++) {
				for (int x = 1; x < channel.width - 1; x++) {
					float h = channel.getPixel(x, y);
					float h1 = channel.getPixel(x, y + 1);
					float h2 = channel.getPixel(x - 1, y);
					float h3 = channel.getPixel(x + 1, y);
					float h4 = channel.getPixel(x, y - 1);
					float d1 = h - h1;
					float d2 = h - h2;
					float d3 = h - h3;
					float d4 = h - h4;
					int i = 0, j = 0;
					float max_d = 0f;
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
					if (max_d > talus) {
						continue;
					}
					max_d *= 0.5f;
					height.putPixel(x, y, height.getPixel(x, y) - max_d);
					height.putPixel(x + i, y + j, height.getPixel(x + i, y + j) + max_d);
				}
			}
			if (save_frames && iter%10 == 0) save(iter + 1);
			if (show_score && iter%10 == 0) score(iter + 1);
			if (show_time && iter%10 == 0) timer(iter + 1, start);
			if (show_diff && iter%10 == 0) diff(iter + 1);
		}
		return channel;
	}
	
	private final static void save(int i) {
		System.out.println("height " + i + " checksum: " + height.sum());
		if (i < 10) {
			new GLIntImage(height.toLayer()).saveAsBMP("height00" + i);
		} else if (i < 100) {
			new GLIntImage(height.toLayer()).saveAsBMP("height0" + i);
		} else {
			new GLIntImage(height.toLayer()).saveAsBMP("height" + i);
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

}
