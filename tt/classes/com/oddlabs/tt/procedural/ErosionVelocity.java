package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Channel;
import com.oddlabs.tt.resource.GLIntImage;

public final strictfp class ErosionVelocity {

	public final Channel velocityFieldErosion1(Channel channel, int iterations) {
		float water_particle = 1f;
		float erosion_factor = 0.1f;

		// step 0 - prepare arrays
		Channel w = new Channel(channel.width, channel.height); // water quantity
		Channel v = new Channel(channel.width, channel.height); // velocity

		for (int iter = 1; iter <= iterations; iter++) {
			// step 2a - obtain w and v from terrain

			// trace water particles
			for (int y = 0; y < channel.height; y++) {
				for (int x = 0; x < channel.width; x++) {
					int i = x;
					int j = y;
					while (i >= 0 && i < channel.width && j >= 0 && j < channel.height) {
						int k = 0;
						int l = 0;
						float max = Float.MIN_VALUE;
						float h = channel.getPixel(i, j);

						float h1 = channel.getPixelSafe(i, j + 1);
						float h2 = channel.getPixelSafe(i + 1, j);
						float h3 = channel.getPixelSafe(i, j - 1);
						float h4 = channel.getPixelSafe(i - 1, j);
						float h5 = channel.getPixelSafe(i + 1, j + 1);
						float h6 = channel.getPixelSafe(i + 1, j - 1);
						float h7 = channel.getPixelSafe(i - 1, j - 1);
						float h8 = channel.getPixelSafe(i - 1, j + 1);

						// break if local depression
						if (h1 >= h && h2 >= h && h3 >= h && h4 >= h && h5 >= h && h6 >= h && h7 >= h && h8 >= h) {
							w.putPixelSafe(i, j, w.getPixelSafe(i, j) + water_particle); // add water to flowmap
							break;
						}

						// determine flow (i,j) -> (k,l)
						float d1 = h - h1;
						float d2 = h - h2;
						float d3 = h - h3;
						float d4 = h - h4;
						float d5 = h - h5;
						float d6 = h - h6;
						float d7 = h - h7;
						float d8 = h - h8;

						if (d1 > max) { max = d1; k = i; l = j + 1; }
						if (d2 > max) { max = d2; k = i + 1; l = j; }
						if (d3 > max) { max = d3; k = i; l = j - 1; }
						if (d4 > max) { max = d4; k = i - 1; l = j; }
						if (d5 > max) { max = d5; k = i + 1; l = j + 1; }
						if (d6 > max) { max = d6; k = i + 1; l = j - 1; }
						if (d7 > max) { max = d7; k = i - 1; l = j - 1; }
						if (d8 > max) { max = d8; k = i - 1; l = j + 1; }

						w.putPixelSafe(i, j, w.getPixelSafe(i, j) + water_particle); // add water to water map
						v.putPixelSafe(i, j, v.getPixelSafe(i, j) + max); // add velocity to velocity map

						i = k;
						j = l;
					}
				}
			}

			// step 2b - perform erosion on terrain based on w and v

			// normalize velocity map
			for (int y = 0; y < channel.height; y++) {
				for (int x = 0; x < channel.width; x++) {
					if (w.getPixel(x, y) > 1) {
						v.putPixel(x, y, v.getPixel(x, y) / w.getPixel(x, y));
					}
					channel.putPixel(x, y, channel.getPixel(x, y) - erosion_factor*v.getPixel(x, y));
				}
			}

			// save height- and flowmap
			v.dynamicRange();
			if (iter < 10) {
				new GLIntImage(channel.toLayer()).saveAsBMP("height00" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow00" + iter);
			} else if (iter < 100) {
				new GLIntImage(channel.toLayer()).saveAsBMP("height0" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow0" + iter);
			} else {
				new GLIntImage(channel.toLayer()).saveAsBMP("height" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow" + iter);
			}

			// clear flowmap for next iteration
			v.fill(0f);
			w.fill(0f);

		}
		return channel;
	}

	public final Channel velocityFieldErosion2(Channel channel, int iterations) {
		float erosion_factor = 0.001f;

		// step 0 - prepare arrays
		int[][] w = new int[channel.width][channel.height]; // water quantity
		Channel v = new Channel(channel.width, channel.height); // velocity
		int[][][] dir = new int[channel.width][channel.height][2]; // velocity direction (end coords)
		Channel s = new Channel(channel.width, channel.height); // transported sediment
		Channel s_max; // max sediment capacity

		// init dir map
		for (int y = 0; y < channel.height; y++) {
			for (int x = 0; x < channel.width; x++) {
				dir[x][y][0] = -2;
			}
		}

		for (int iter = 1; iter <= iterations; iter++) {
			// step 2a - obtain w, v, s from terrain

			// trace water particles
			for (int y = 0; y < channel.height; y++) {
				for (int x = 0; x < channel.width; x++) {
					int i = x;
					int j = y;
					while (i >= 0 && i < channel.width && j >= 0 && j < channel.height) {
						w[i][j]++; // add water
						int k = 0;
						int l = 0;
						float max = Float.MIN_VALUE;
						float h = channel.getPixel(i, j);

						float h1 = channel.getPixelSafe(i, j + 1);
						float h2 = channel.getPixelSafe(i + 1, j);
						float h3 = channel.getPixelSafe(i, j - 1);
						float h4 = channel.getPixelSafe(i - 1, j);
						float h5 = channel.getPixelSafe(i + 1, j + 1);
						float h6 = channel.getPixelSafe(i + 1, j - 1);
						float h7 = channel.getPixelSafe(i - 1, j - 1);
						float h8 = channel.getPixelSafe(i - 1, j + 1);

						// break if local depression
						if (h1 >= h && h2 >= h && h3 >= h && h4 >= h && h5 >= h && h6 >= h && h7 >= h && h8 >= h) {
							break;
						}

						// determine flow velocity and direction
						if (dir[i][j][0] == -2) {
							float d1 = h - h1;
							float d2 = h - h2;
							float d3 = h - h3;
							float d4 = h - h4;
							float d5 = h - h5;
							float d6 = h - h6;
							float d7 = h - h7;
							float d8 = h - h8;

							if (d1 > max) { max = d1; k = i; l = j + 1; }
							if (d2 > max) { max = d2; k = i + 1; l = j; }
							if (d3 > max) { max = d3; k = i; l = j - 1; }
							if (d4 > max) { max = d4; k = i - 1; l = j; }
							if (d5 > max) { max = d5; k = i + 1; l = j + 1; }
							if (d6 > max) { max = d6; k = i + 1; l = j - 1; }
							if (d7 > max) { max = d7; k = i - 1; l = j - 1; }
							if (d8 > max) { max = d8; k = i - 1; l = j + 1; }

							dir[i][j][0] = k;
							dir[i][j][1] = l;
						} else {
							max = channel.getPixel(i, j) - channel.getPixelSafe(dir[i][j][0], dir[i][j][1]);
							k = dir[i][j][0];
							l = dir[i][j][1];
						}
						v.putPixel(i, j, v.getPixel(i, j) + max); // add velocity
						i = k;
						j = l;

					}
				}
			}

			// step 2b - perform erosion on terrain based on w, v and dir

			// normalize velocity map
			for (int y = 0; y < channel.height; y++) {
				for (int x = 0; x < channel.width; x++) {
					if (w[x][y] > 1) {
						v.putPixel(x, y, v.getPixel(x, y) / (float)w[x][y]);
					}
				}
			}

			// move sediment
			s_max = v.copy().power2();
			for (int s_iter = 0; s_iter < 16; s_iter++) {
				for (int y = 0; y < channel.height; y++) {
					for (int x = 0; x < channel.width; x++) {
						// calculate new terrain height
						float s_q = v.getPixel(x, y);
						float s_t = s.getPixel(x, y) + s_q;
						float dh = -erosion_factor*s_q;
						float ds = s_t;
						if (s_t > s_max.getPixel(x, y)) {
							dh = dh + erosion_factor*(s_t - s_max.getPixel(x, y));
							ds = s_max.getPixel(x, y);
						}
						channel.putPixel(x, y, dh + channel.getPixel(x, y));

						// transport excess sediment
						s.putPixel(x, y, s.getPixel(x, y) - ds);
						s.putPixelSafe(dir[x][y][0], dir[x][y][1], s.getPixelSafe(dir[x][y][0], dir[x][y][1]) + ds);
					}
				}
			}

			// save height- and flowmap
			v.dynamicRange();
			if (iter < 10) {
				new GLIntImage(channel.toLayer()).saveAsBMP("height00" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow00" + iter);
			} else if (iter < 100) {
				new GLIntImage(channel.toLayer()).saveAsBMP("height0" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow0" + iter);
			} else {
				new GLIntImage(channel.toLayer()).saveAsBMP("height" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow" + iter);
			}

			// clear v, w and dir for next iteration
			v.fill(0f);
			s.fill(0f);
			for (int y = 0; y < channel.height; y++) {
				for (int x = 0; x < channel.width; x++) {
					w[x][y] = 0;
					dir[x][y][0] = -2;
				}
			}

		}
		return channel;
	}

	public final Channel velocityFieldErosion3(Channel channel, int iterations) {
		int s_iters = 1;
		float erosion_factor = 0.01f;

		// step 0 - prepare arrays
		int[][] w = new int[channel.width][channel.height]; // water quantity
		Channel v = new Channel(channel.width, channel.height); // velocity
		int[][][] dir = new int[channel.width][channel.height][2]; // velocity direction (end coords)
		Channel s = new Channel(channel.width, channel.height); // transported sediment
		Channel s_max; // max sediment capacity

		// init dir map
		for (int y = 0; y < channel.height; y++) {
			for (int x = 0; x < channel.width; x++) {
				dir[x][y][0] = -2;
			}
		}

		for (int iter = 1; iter <= iterations; iter++) {
			// step 2a - obtain w, v, s from terrain

			// trace water particles
			for (int y = 0; y < channel.height; y++) {
				for (int x = 0; x < channel.width; x++) {
					int i = x;
					int j = y;
					while (true/*i >= 0 && i < channel.width && j >= 0 && j < channel.height*/) {
						i = (i + channel.width) % channel.width;
						j = (j + channel.height) % channel.height;
						w[i][j]++; // add water
						int k = 0;
						int l = 0;
						float max = Float.MIN_VALUE;
						float h = channel.getPixelWrap(i, j);

						float h1 = channel.getPixelWrap(i, j + 1);
						float h2 = channel.getPixelWrap(i + 1, j);
						float h3 = channel.getPixelWrap(i, j - 1);
						float h4 = channel.getPixelWrap(i - 1, j);
						float h5 = channel.getPixelWrap(i + 1, j + 1);
						float h6 = channel.getPixelWrap(i + 1, j - 1);
						float h7 = channel.getPixelWrap(i - 1, j - 1);
						float h8 = channel.getPixelWrap(i - 1, j + 1);

						// break if local depression
						if (h1 >= h && h2 >= h && h3 >= h && h4 >= h && h5 >= h && h6 >= h && h7 >= h && h8 >= h) {
							break;
						}

						// determine flow velocity and direction
						if (dir[i][j][0] == -2) {
							float d1 = h - h1;
							float d2 = h - h2;
							float d3 = h - h3;
							float d4 = h - h4;
							float d5 = h - h5;
							float d6 = h - h6;
							float d7 = h - h7;
							float d8 = h - h8;

							if (d1 > max) { max = d1; k = i; l = j + 1; }
							if (d2 > max) { max = d2; k = i + 1; l = j; }
							if (d3 > max) { max = d3; k = i; l = j - 1; }
							if (d4 > max) { max = d4; k = i - 1; l = j; }
							if (d5 > max) { max = d5; k = i + 1; l = j + 1; }
							if (d6 > max) { max = d6; k = i + 1; l = j - 1; }
							if (d7 > max) { max = d7; k = i - 1; l = j - 1; }
							if (d8 > max) { max = d8; k = i - 1; l = j + 1; }

							dir[i][j][0] = k;
							dir[i][j][1] = l;
						} else {
							max = channel.getPixelWrap(i, j) - channel.getPixelWrap(dir[i][j][0], dir[i][j][1]);
							k = dir[i][j][0];
							l = dir[i][j][1];
						}
						v.putPixelWrap(i, j, v.getPixelWrap(i, j) + max); // add velocity
						i = k;
						j = l;

					}
				}
			}

			// step 2b - perform erosion on terrain based on w, v and dir
			v.add(1f).log().dynamicRange().smoothWrap(1).smoothWrap(1).smoothWrap(1);
			s_max = v.copy().add(1f).power2().add(-1f);
			for (int s_iter = 0; s_iter < s_iters; s_iter++) {
				for (int y = 0; y < channel.height; y++) {
					for (int x = 0; x < channel.width; x++) {
						// calculate new terrain height
						float s_q = v.getPixel(x, y);
						float s_t = s.getPixel(x, y) + s_q;
						float dh = -erosion_factor*s_q;
						float ds = s_t;
						if (s_t > s_max.getPixel(x, y)) {
							dh = dh + erosion_factor*(s_t - s_max.getPixel(x, y));
							ds = s_max.getPixel(x, y);
						}
						channel.putPixel(x, y, dh + channel.getPixel(x, y));

						// transport excess sediment
						s.putPixel(x, y, s.getPixel(x, y) - ds);
						s.putPixelWrap(dir[x][y][0], dir[x][y][1], s.getPixelWrap(dir[x][y][0], dir[x][y][1]) + ds);
					}
				}
			}

			// save height- and flowmap
			v.dynamicRange();
			if (iter < 10) {
				new GLIntImage(channel.toLayer()).saveAsBMP("height00" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow00" + iter);
			} else if (iter < 100) {
				new GLIntImage(channel.toLayer()).saveAsBMP("height0" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow0" + iter);
			} else {
				new GLIntImage(channel.toLayer()).saveAsBMP("height" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow" + iter);
			}

			// clear v, w and dir for next iteration
			v.fill(0f);
			//s.fill(0f);
			for (int y = 0; y < channel.height; y++) {
				for (int x = 0; x < channel.width; x++) {
					w[x][y] = 0;
					dir[x][y][0] = -2;
				}
			}

		}
		return channel;
	}

	public final Channel velocityFieldErosion4(Channel channel, int iterations) {
		int s_iters = 10;
		float erosion_factor = 0.01f;

		// step 0 - prepare arrays
		int[][] w = new int[channel.width][channel.height]; // water quantity
		Channel v = new Channel(channel.width, channel.height); // velocity
		int[][][] dir = new int[channel.width][channel.height][2]; // velocity direction (end coords)
		Channel s = new Channel(channel.width, channel.height); // transported sediment
		Channel s_max; // max sediment capacity

		// init dir map
		for (int y = 0; y < channel.height; y++) {
			for (int x = 0; x < channel.width; x++) {
				dir[x][y][0] = -2;
			}
		}

		for (int iter = 1; iter <= iterations; iter++) {
			// step 2a - obtain w, v, s from terrain

			// trace water particles
			for (int y = 0; y < channel.height; y++) {
				for (int x = 0; x < channel.width; x++) {
					int i = x;
					int j = y;
					while (i >= 0 && i < channel.width && j >= 0 && j < channel.height) {
						//i = (i + channel.width) % channel.width;
						//j = (j + channel.height) % channel.height;
						int k = 0;
						int l = 0;
						float max = Float.MIN_VALUE;
						float h = channel.getPixelSafe(i, j);
						float h1 = channel.getPixelSafe(i, j + 1);
						float h2 = channel.getPixelSafe(i + 1, j);
						float h3 = channel.getPixelSafe(i, j - 1);
						float h4 = channel.getPixelSafe(i - 1, j);
						float h5 = channel.getPixelSafe(i + 1, j + 1);
						float h6 = channel.getPixelSafe(i + 1, j - 1);
						float h7 = channel.getPixelSafe(i - 1, j - 1);
						float h8 = channel.getPixelSafe(i - 1, j + 1);

						// break if local depression
						if (h1 >= h && h2 >= h && h3 >= h && h4 >= h && h5 >= h && h6 >= h && h7 >= h && h8 >= h) {
							break;
						}

						// determine flow velocity and direction
						if (dir[i][j][0] == -2) {
							float d1 = h - h1;
							float d2 = h - h2;
							float d3 = h - h3;
							float d4 = h - h4;
							float d5 = h - h5;
							float d6 = h - h6;
							float d7 = h - h7;
							float d8 = h - h8;

							if (d1 > max) { max = d1; k = i; l = j + 1; }
							if (d2 > max) { max = d2; k = i + 1; l = j; }
							if (d3 > max) { max = d3; k = i; l = j - 1; }
							if (d4 > max) { max = d4; k = i - 1; l = j; }
							if (d5 > max) { max = d5; k = i + 1; l = j + 1; }
							if (d6 > max) { max = d6; k = i + 1; l = j - 1; }
							if (d7 > max) { max = d7; k = i - 1; l = j - 1; }
							if (d8 > max) { max = d8; k = i - 1; l = j + 1; }

							dir[i][j][0] = k;
							dir[i][j][1] = l;
						} else {
							max = channel.getPixelSafe(i, j) - channel.getPixelSafe(dir[i][j][0], dir[i][j][1]);
							k = dir[i][j][0];
							l = dir[i][j][1];
						}
						v.putPixelSafe(k, l, v.getPixelSafe(k, l) + max); // add velocity
						if (k >= 0 && k < channel.width && l >= 0 && l < channel.height) {
							w[k][l]++; // add water
						}
						i = k;
						j = l;

					}
				}
			}

			// step 2b - perform erosion on terrain based on w, v and dir
			System.out.println("v.min: " + v.findMin() + " v.max: " + v.findMax());
			v.add(1f).log().dynamicRange().smooth(1).smooth(1);
			s_max = v.copy().add(1f).power2().add(-1f);
			for (int s_iter = 0; s_iter < s_iters; s_iter++) {
				for (int y = 0; y < channel.height; y++) {
					for (int x = 0; x < channel.width; x++) {
						// calculate new terrain height
						float s_q = v.getPixel(x, y);
						float s_t = s.getPixel(x, y) + s_q;
						float dh = -erosion_factor*s_q;
						float ds = s_t;
						if (s_t > s_max.getPixel(x, y)) {
							dh = dh + erosion_factor*(s_t - s_max.getPixel(x, y));
							ds = s_max.getPixel(x, y);
						}
						channel.putPixel(x, y, dh + channel.getPixel(x, y));

						// transport excess sediment
						s.putPixel(x, y, 0f);
						s.putPixelSafe(dir[x][y][0], dir[x][y][1], s.getPixelSafe(dir[x][y][0], dir[x][y][1]) + ds);
					}
				}
			}

			// save height- and flowmap
			v.dynamicRange();
			if (iter < 10) {
				new GLIntImage(channel.toLayer()).saveAsBMP("height00" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow00" + iter);
			} else if (iter < 100) {
				new GLIntImage(channel.toLayer()).saveAsBMP("height0" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow0" + iter);
			} else {
				new GLIntImage(channel.toLayer()).saveAsBMP("height" + iter);
				new GLIntImage(v.toLayer()).saveAsBMP("flow" + iter);
			}

			// clear v, w and dir for next iteration
			v.fill(0f);
			//s.fill(0f);
			for (int y = 0; y < channel.height; y++) {
				for (int x = 0; x < channel.width; x++) {
					w[x][y] = 0;
					dir[x][y][0] = -2;
				}
			}

		}
		return channel;
	}

}
