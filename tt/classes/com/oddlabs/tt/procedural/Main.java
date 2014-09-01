package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Channel;

public final strictfp class Main {

	public final static void main(String[] args) {
		long time = System.currentTimeMillis();
		
		/*
		int seed = 42;
		int TEXTURE_SIZE = 256;
		*/
		
		/*
		Channel rock_bump = new Voronoi(TEXTURE_SIZE, 8, 8, 1, 1f, seed).getDistance(-1f, 0f, 0f).multiply(.49f);
		rock_bump.channelAdd(new Voronoi(TEXTURE_SIZE, 8, 8, 1, 1f, seed+1).getDistance(-1f, 0f, 0f).multiply(.49f));
		Channel noise = new Midpoint(TEXTURE_SIZE, 7, .4f, seed).toChannel();
		rock_bump.channelAdd(noise.copy().multiply(.02f));
		Channel rock = noise.copy().dynamicRange(.3f, .4f);
		rock.bump(rock_bump, TEXTURE_SIZE/256f, 0f, 1f, 1f, 0f).gamma(.5f).dynamicRange(0f, .25f);
		rock.bumpSpecular(rock_bump, 1f, 0f, 1f, 0f, .1f, 1);
		*/
		
		/*
		Channel rock_bump = new Voronoi(TEXTURE_SIZE, 8, 8, 1, 1f, seed).getDistance(-1f, 0f, 0f).multiply(.49f);
		rock_bump.channelAdd(new Voronoi(TEXTURE_SIZE, 8, 8, 1, 1f, seed+1).getDistance(-1f, 0f, 0f).multiply(.49f));
		Channel noise = new Midpoint(TEXTURE_SIZE, 7, .4f, seed).toChannel();
		Channel noise2 = new Midpoint(TEXTURE_SIZE, 3, .45f, seed).toChannel();
		rock_bump.channelAdd(noise.copy().multiply(.02f));
		Layer rock = noise.copy().dynamicRange(.5f, 0.75f).toLayer();
		Layer rust = new Layer(new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(.4f), new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(.15f), new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(0f), noise2.copy().gamma2().invert());
		rock.layerBlend(rust);
		rock.bump(rock_bump, TEXTURE_SIZE/256f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f).gamma(.5f).dynamicRange(0f, .75f);
		rock.bumpSpecular(rock_bump, 1f, 0f, 1f, 0f, .1f, .1f, .1f, 1);
		Layer stain = new Layer(noise2.copy().multiply(.4f), noise2.copy().multiply(.15f), noise2.copy().multiply(0f), noise2.copy().gamma(.75f).rotate(90));
		stain.bump(noise2.copy(), 8f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f);
		rock.layerBlend(stain);
		rock.toGLIntImage().saveAsPNG("generator_iron");
		*/
		
		/*
		int TEXTURE_SIZE = 128;
		Channel sonic_alpha = new Channel(TEXTURE_SIZE>>1, TEXTURE_SIZE>>1);
		
		float x_coord;
		float y_coord;
		float radius;
		
		for (int x = 0; x < TEXTURE_SIZE>>1; x++) {
			for (int y = 0; y < TEXTURE_SIZE>>1; y++) {
				x_coord = 0.5f - (x + 0.5f)/TEXTURE_SIZE;
				y_coord = 0.5f - (y + 0.5f)/TEXTURE_SIZE;
				radius = 4f*(x_coord*x_coord*x_coord + y_coord*y_coord*y_coord);
				if (radius < .5f) {
					sonic_alpha.putPixel(x, y, Tools.interpolateLinear(1f, 0f, (float)Math.sqrt(2f*radius)));
				}
				
			}
		}
		Channel sonic_alpha_final = new Channel(TEXTURE_SIZE, TEXTURE_SIZE);
		sonic_alpha_final.quadJoin(sonic_alpha, sonic_alpha.copy().rotate(270), sonic_alpha.copy().rotate(90), sonic_alpha.copy().rotate(180));
		
		
		Channel sonic_color = new Channel(TEXTURE_SIZE, TEXTURE_SIZE).fill(1f);
		Layer sonic = new Layer(sonic_color, sonic_color, sonic_color, sonic_alpha_final);
		sonic.toGLIntImage().saveAsPNG("generator_sonic");
		*/
		
		// thunder & lightning!
		/*
		Channel greyscale = new Gradient(128, 128, new float[][]{{0f, 0f}, {.47f, .25f}, {.5f, 1f}, {.53f, .25f}, {1f, 0f}}, Gradient.HORIZONTAL, Gradient.SMOOTH).toChannel();
		Layer color = new Layer(greyscale.copy(), greyscale.copy(), new Channel(128, 128).fill(1f), greyscale.copy());
		color.toGLIntImage().saveAsPNG("lightning");
		*/
		
		Voronoi voronoi = new Voronoi(512, 8, 8, 1, 1f, 42, true);
		Channel hitpoint = voronoi.getHitpoint();
		//Channel distance = voronoi.getDistance(-1f, 1f, 0f);
		//Channel height = distance.channelMultiply(hitpoint);
		//height.erode(16f/512f, 64);
		Channel hitpoint2 = hitpoint.copy().erodeThermal(16f/512f, 64);
		Channel noise = new Midpoint(512, 3, 0.25f, 42).toChannel().contrast(4f);
		
		Channel height = hitpoint.channelMultiply(noise.copy()).channelAdd(hitpoint2.channelMultiply(noise.invert()));
		
		height.toLayer().saveAsPNG("test_voronoi");
		
		
		System.out.println("******** Time taken: " + ((System.currentTimeMillis() - time)/1000f) + " seconds");
	}
}
