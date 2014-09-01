package com.oddlabs.tt.procedural;

import java.util.Random;

import com.oddlabs.procedural.Channel;
import com.oddlabs.procedural.Layer;
import com.oddlabs.procedural.Tools;

public final strictfp class Grass {
	private Random random;
	public Channel r;
	public Channel g;
	public Channel b;
	public Channel a;

	public Grass(int width, int height, int leaves, int seed, int clusters, int smooth) {
		r = new Channel(width<<1, height<<1);
		g = new Channel(width<<1, height<<1);
		b = new Channel(width<<1, height<<1);
		a = new Channel(width<<1, height<<1);
		r.fill(0f);
		g.fill(0f);
		b.fill(0f);
		a.fill(0f);
		random = new Random(seed);
		float aspect = (float)height/width;
		
		// leaf loop
		for (int leaf = 0; leaf < leaves; leaf++) {
			
			float twist = random.nextFloat();
			float radius = 2f*Tools.gaussify(random.nextFloat());
			float fadein = 0.5f*random.nextFloat();
			
			float color0_g = 0.25f*Tools.gaussify(random.nextFloat());
			float color0_r = color0_g*(0.5f + 0.5f*Tools.gaussify(random.nextFloat()));
			float color0_b = color0_r*(0.4f + 0.6f*Tools.gaussify(random.nextFloat()));
			
			float color1_g = 0.3f*Tools.gaussify(random.nextFloat());
			float color1_r = color1_g*(0.4f + 0.3f*Tools.gaussify(random.nextFloat()));
			float color1_b = color1_r*(0.3f + 0.6f*Tools.gaussify(random.nextFloat()));
			
			float color2_g = 0.35f*Tools.gaussify(random.nextFloat());
			float color2_r = color2_g*(0.6f + 0.5f*Tools.gaussify(random.nextFloat()));
			float color2_b = color2_r*(0.4f + 0.5f*Tools.gaussify(random.nextFloat()));

			float color3_g = 0.35f*Tools.gaussify(random.nextFloat());
			float color3_r = color3_g*(0.5f + 0.5f*Tools.gaussify(random.nextFloat()));
			float color3_b = color3_r*(0.4f + 0.6f*Tools.gaussify(random.nextFloat()));
			
			float color4_g = 0.3f*Tools.gaussify(random.nextFloat());
			float color4_r = color4_g*(0.5f + 0.5f*Tools.gaussify(random.nextFloat()));
			float color4_b = color4_r*(0.4f + 0.6f*Tools.gaussify(random.nextFloat()));
			
			int cluster = (int)(clusters*random.nextFloat());
			
			float p1_x = (float)cluster/clusters + Tools.gaussify(Tools.gaussify(random.nextFloat()))/clusters;
			float p1_y = 0.25f*(1f - ((float)leaf/leaves));
			float p2_x = StrictMath.max(0.025f, StrictMath.min(p1_x + aspect*Tools.gaussify(random.nextFloat()) - 0.5f*aspect, 0.975f));
			float p2_y = StrictMath.max(0.25f, StrictMath.min(0.7f*random.nextFloat(), 0.975f));
			
			p2_x = (1 - p2_y)*p1_x + p2_y*p2_x;
			
			float p3_x = StrictMath.max(0.025f, StrictMath.min(p2_x + p2_x - p1_x, 0.975f));
			float p3_y;
			
			if (p2_y < 0.2 && random.nextFloat() < 0.5) {
				p3_y = StrictMath.max(0.025f, StrictMath.min(p2_y*(random.nextFloat() + 1f), 0.975f));
			} else {
				p3_y = StrictMath.max(0.025f, StrictMath.min(p2_y + 1.25f*p2_y*Tools.gaussify(random.nextFloat()) - 0.5f*p2_y, 0.975f));
			}
			p2_x = (p1_x + p3_x)/2f;
			if (p2_y < 0.25) {
				radius/=2f;
			}
			
			float t1_x = 0.5f*(p2_x - p1_x);
			float t1_y = (p2_y - p1_y);
			float t2_x;
			if (p3_y > p2_y) {
				t2_x = Tools.gaussify(Tools.gaussify(random.nextFloat()))*(p3_x - p1_x);
			} else {
				t2_x = Tools.gaussify(Tools.gaussify(random.nextFloat()))*4f*(p3_x - p2_x);
			}
			float t2_y = 0.5f*(p3_y - p1_y);
			float t3_x = 0;
			float t3_y = 0;
			
			float l1 = (float)StrictMath.sqrt((p2_x - p1_x)*(p2_x - p1_x) + (p2_y - p1_y)*(p2_y - p1_y));
			float l2 = (float)StrictMath.sqrt((p3_x - p2_x)*(p3_x - p2_x) + (p3_y - p2_y)*(p3_y - p2_y));
			
			// parameter loop
			for (int t_int = 0; t_int < (height<<1); t_int++) {
				float t = (float)t_int/(height<<1);
				
				float s;
				
				if (t < l1/(l1 + l2)) {
					s = (t*(l1 + l2)/l1);
				} else {
					s = (t - (l1/(l1 + l2)))/(l2/(l1 + l2));
				}
				
				float h1 = (float)(2*StrictMath.pow(s, 3) - 3*StrictMath.pow(s, 2) + 1);
				float h2 = (float)(-2*StrictMath.pow(s, 3) + 3*StrictMath.pow(s, 2));
				float h3 = (float)(StrictMath.pow(s, 3) - 2*StrictMath.pow(s, 2) + s);
				float h4 = (float)(StrictMath.pow(s, 3) -  StrictMath.pow(s, 2));
				
				float x;
				float y;

				if (t < l1/(l1 + l2)) {
					x = (width<<1)*(h1*p1_x + h2*p2_x + h3*t1_x + h4*t2_x);
					y = (height<<1)*(h1*p1_y + h2*p2_y + h3*t1_y + h4*t2_y);
				} else {
					x = (width<<1)*(h1*p2_x + h2*p3_x + h3*t2_x + h4*t3_x);
					y = (height<<1)*(h1*p2_y + h2*p3_y + h3*t2_y + h4*t3_y);
				}

				float rd = 0;
				if (twist < 0.5) {
					rd = (width>>8)*radius*(1f - (0.5f*(float)StrictMath.pow(4, t) - 1f));
				} else {
					rd = (width>>8)*radius*((float)(StrictMath.cos(3*StrictMath.PI*t) + 1.5f + 0.5*(1f - t)))*0.5f;
				}
				
				float color_r = 0;
				float color_g = 0;
				float color_b = 0;
				
				switch ((int)(t*4)) {
					case 0:
						color_r = Tools.interpolateLinear(color0_r, color1_r, t/0.25f);
						color_g = Tools.interpolateLinear(color0_g, color1_g, t/0.25f);
						color_b = Tools.interpolateLinear(color0_b, color1_b, t/0.25f);
						break;
					case 1:
						color_r = Tools.interpolateLinear(color1_r, color2_r, (t - 0.25f)/0.25f);
						color_g = Tools.interpolateLinear(color1_g, color2_g, (t - 0.25f)/0.25f);
						color_b = Tools.interpolateLinear(color1_b, color2_b, (t - 0.25f)/0.25f);
						break;
					case 2:
						color_r = Tools.interpolateLinear(color2_r, color3_r, (t - 0.50f)/0.25f);
						color_g = Tools.interpolateLinear(color2_g, color3_g, (t - 0.50f)/0.25f);
						color_b = Tools.interpolateLinear(color2_b, color3_b, (t - 0.50f)/0.25f);
						break;
					case 3:
						color_r = Tools.interpolateLinear(color3_r, color4_r, (t - 0.75f)/0.25f);
						color_g = Tools.interpolateLinear(color3_g, color4_g, (t - 0.75f)/0.25f);
						color_b = Tools.interpolateLinear(color3_b, color4_b, (t - 0.75f)/0.25f);
						break;
					default:
						assert false: "incorrect color point";
				}
				
				int x_int = (int)x;
				int y_int = (int)y;
				int r_int = (int)rd;
				
				// draw circle
				for (int u = x_int - r_int - 8; u < x_int + r_int + 10; u++) {
					for (int v = y_int - r_int - 8; v < y_int + r_int + 10; v++) {
						float dx = StrictMath.abs(u - x);
						float dy = StrictMath.abs(v - y);
						float dist = (float)StrictMath.sqrt(dx*dx + dy*dy);
						if (u >= 0 && u < (width<<1) && v >= 0 && v < (height<<1)) {
							float alpha = 0;
							if (smooth == 1) {
								if (dist < rd - 1) {
									alpha = 1f;
								}
								if (dist >= rd - 1 && dist < rd + 1) {
									alpha = StrictMath.max(Tools.interpolateLinear(1f, 0f, (dist - rd)/2f), a.getPixel(u, v));
								}
								if (t < fadein) {
									alpha = StrictMath.min(alpha, Tools.interpolateLinear(0f, 1f, t/fadein));
								}
								a.putPixel(u, v, StrictMath.max(alpha, a.getPixel(u, v)));
								
								if (dist < rd + 1) {
									r.putPixel(u, v, color_r);
									g.putPixel(u, v, color_g);
									b.putPixel(u, v, color_b);
								}
							} else {
								if (dist < rd + 1) {
									alpha = 1f;
								}
								a.putPixel(u, v, StrictMath.max(alpha, a.getPixel(u, v)));
								if (dist < rd + 3) {
									r.putPixel(u, v, color_r);
									g.putPixel(u, v, color_g);
									b.putPixel(u, v, color_b);
								}
							}
							if (r.getPixel(u, v) == 0f && g.getPixel(u, v) == 0f && b.getPixel(u, v) == 0f) {
								r.putPixel(u, v, color_r);
								g.putPixel(u, v, color_g);
								b.putPixel(u, v, color_b);
							}
						}
					}
				}
			}
		}

		// scale to achieve antialiasing
		if (smooth == 1) {
			r.scale(width, height);
			g.scale(width, height);
			b.scale(width, height);
			a.scale(width, height);
		} else {
			r.scale(width, height);
			g.scale(width, height);
			b.scale(width, height);
			a.scaleFast(width, height);
		}
	}
	
	public final Layer toLayer() {
		return new Layer(r, g, b);
	}

}
