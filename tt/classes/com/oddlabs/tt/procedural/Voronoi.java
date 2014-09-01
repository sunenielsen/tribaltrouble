/*
TODO

omskriv if-check ved modulo til (x+n)%n
*/

package com.oddlabs.tt.procedural;

import java.util.Random;

import com.oddlabs.procedural.Channel;

public final strictfp class Voronoi {
	// coordinate indexing
	public static final int X = 0;
	public static final int Y = 1;
	public static final int SEED = 2;

	private final int size;
	private Random random;
	private Channel dist1;
	private Channel dist2;
	private Channel dist3;
	private Channel hit;

	public Voronoi(int size, int x_domains, int y_domains, int checkradius, float randomness, long seed) {
		this(size, x_domains, y_domains, checkradius, randomness, seed, false);
	}
	
	public Voronoi(int size, int x_domains, int y_domains, int checkradius, float randomness, long seed, boolean border) {
		this.size = size;
		x_domains = StrictMath.max(1, x_domains);
		y_domains = StrictMath.max(1, y_domains);
		checkradius = StrictMath.min(StrictMath.max(1, checkradius), StrictMath.max(x_domains, y_domains));
		random = new Random(seed);
		dist1 = new Channel(size, size);
		dist2 = new Channel(size, size);
		dist3 = new Channel(size, size);
		hit = new Channel(size, size);
		
		// fill in hitpoints according to distribution
		float[][][] domains = new float[x_domains][y_domains][3];
		for (int j = 0; j < y_domains; j++) {
			for (int i = 0; i < x_domains; i++) {
				domains[i][j][X] = (1 - randomness)*((i + .5f)/x_domains) + randomness*((i + random.nextFloat())/x_domains);
				domains[i][j][Y] = (1 - randomness)*((j + .5f)/y_domains) + randomness*((j + random.nextFloat())/y_domains);
				if (border && (j == 0 || j == y_domains-1 || i == 0 || i == x_domains-1)) {
					domains[i][j][SEED] = 0f;
				} else if (border && j != 0 && j != y_domains-1 && i != 0 && i != x_domains-1) {
					domains[i][j][SEED] = 1f;
				} else {
					domains[i][j][SEED] = random.nextFloat();
				}
			}
		}

		// fill in pixelvalues
		for (int y = 0; y < size; y++) {
			float y_coord = (float)y/size;
			int j = (int)(y_coord * y_domains);
			for (int x = 0; x < size; x++) {
				float x_coord = (float)x/size;
				float d1 = Float.MAX_VALUE;
				float d2 = Float.MAX_VALUE;
				float d3 = Float.MAX_VALUE;
				float dist = 0;
				float hitpoint = 0;
				int i = (int)(x_coord * x_domains);

				// traverse neighboring domains in wrap-around style (for seamless textures)
				for (int l = -checkradius; l <= checkradius; l++) {
					int l_wrap = j + l;
					if (l_wrap < 0 || l_wrap >= y_domains) {
						l_wrap = l_wrap % y_domains;
						if (l_wrap < 0) l_wrap += y_domains;
					}
					for (int k = -checkradius; k <= checkradius; k++) {

						// calculate wrapped domain coords
						int k_wrap = i + k;
						if (k_wrap < 0 || k_wrap >= x_domains) {
							k_wrap = k_wrap % x_domains;
							if (k_wrap < 0) k_wrap += x_domains;
						}
						float dx = 0;
						float dy = 0;

						// calculate distance to current hit point taking wrap-around into consideration
						if (i + k >= 0 && i + k < x_domains) {
							dx = StrictMath.abs(domains[k_wrap][l_wrap][X] - x_coord);
						} else if (i + k < 0) {
							dx = StrictMath.abs(1 - domains[k_wrap][l_wrap][X] + x_coord);
						} else if (i + k >= x_domains) {
							dx = StrictMath.abs(1 - x_coord + domains[k_wrap][l_wrap][X]);
						}
						if (j + l >= 0 && j + l < y_domains) {
							dy = StrictMath.abs(domains[k_wrap][l_wrap][Y] - y_coord);
						} else if (j + l < 0) {
							dy = StrictMath.abs(1 - domains[k_wrap][l_wrap][Y] + y_coord);
						} else if (j + l >= y_domains) {
							dy = StrictMath.abs(1 - y_coord + domains[k_wrap][l_wrap][Y]);
						}
						dx*=x_domains;
						dy*=y_domains;

						// maintain F1, F2, F3 and nearest hitpoint values
						dist = dx*dx + dy*dy;
						if (dist <= d1) {
							d3 = d2;
							d2 = d1;
							d1 = dist;
							hitpoint = domains[k_wrap][l_wrap][SEED];
						} else if (dist <= d2 && dist > d1) {
							d3 = d2;
							d2 = dist;
						} else if (dist <= d3 && dist > d2) {
							d3 = dist;
						}
					}
				}
				dist1.putPixel(x, y, d1);
				dist2.putPixel(x, y, d2);
				dist3.putPixel(x, y, d3);
				hit.putPixel(x, y, hitpoint);
			}
		}
	}

	public final Channel getDistance(float c1, float c2, float c3) {
		Channel channel = new Channel(size, size);
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				channel.putPixel(x, y, c1*dist1.getPixel(x, y) + c2*dist2.getPixel(x, y) + c3*dist3.getPixel(x, y));
			}
		}
		return channel.dynamicRange();
	}
	
	public final Channel getHitpoint() {
		return hit;
	}
	
}
