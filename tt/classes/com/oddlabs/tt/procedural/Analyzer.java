package com.oddlabs.tt.procedural;

import com.oddlabs.procedural.Channel;

public final strictfp class Analyzer {

	public final static void analyze(Channel height, String name) {
		System.out.println("*** Performing image analysis on \"" + name + "\" ****");
		height.flipV();
		System.out.println("Height map...");
		height.toLayer().saveAsPNG(name);
		statistics(height, "height");
		System.out.println("done");

		System.out.println("Slope map...");
		Channel slope = height.copy().lineart().dynamicRange();
		slope.toLayer().saveAsPNG(name + "_slope");
		statistics(slope, "slope");
		score(slope, "slope");
		System.out.println("done");

		System.out.println("Relative height map...");
		Channel rel1 = height.copy().relativeIntensity(1);
		rel1.toLayer().saveAsPNG(name + "_relative1");
		statistics(rel1, "relative height 1");
		Channel rel2 = height.copy().relativeIntensity(2);
		rel2.toLayer().saveAsPNG(name + "_relative2");
		statistics(rel2, "relative height 2");
		Channel rel4 = height.copy().relativeIntensity(4);
		rel4.toLayer().saveAsPNG(name + "_relative4");
		statistics(rel4, "relative height 4");
		Channel rel8 = height.copy().relativeIntensity(8);
		rel8.toLayer().saveAsPNG(name + "_relative8");
		statistics(rel8, "relative height 8");
		Channel rel16 = height.copy().relativeIntensity(16);
		rel16.toLayer().saveAsPNG(name + "_relative16");
		statistics(rel16, "relative height 16");
		Channel rel32 = height.copy().relativeIntensity(32);
		rel32.toLayer().saveAsPNG(name + "_relative32");
		statistics(rel32, "relative height 32");
		System.out.println("done");

		System.out.println("height histogram...");
		histogram(height, height.width).flipV().toLayer().saveAsPNG(name + "_histogram");
		System.out.println("done");

		System.out.println("Slope histogram...");
		histogram(slope, slope.width).flipV().toLayer().saveAsPNG(name + "_slope_histogram");
		System.out.println("done");

		System.out.println("Relative height histogram...");
		histogram(rel1, height.width).flipV().toLayer().saveAsPNG(name + "_relative1_histogram");
		histogram(rel2, height.width).flipV().toLayer().saveAsPNG(name + "_relative2_histogram");
		histogram(rel4, height.width).flipV().toLayer().saveAsPNG(name + "_relative4_histogram");
		histogram(rel8, height.width).flipV().toLayer().saveAsPNG(name + "_relative8_histogram");
		histogram(rel16, height.width).flipV().toLayer().saveAsPNG(name + "_relative16_histogram");
		histogram(rel32, height.width).flipV().toLayer().saveAsPNG(name + "_relative32_histogram");
		System.out.println("done");

		System.out.println("Accessibility map...");
		Channel access = height.copy().lineart().threshold(0f, 0.025f);
		access.toLayer().saveAsPNG(name + "_access");
		System.out.println("done");
		System.out.println("Accessible area: " + (100f*access.count(1f)/(access.width*access.height)) + "%");
		Channel access_conn = access.copy().largestConnected(1f);
		access_conn.toLayer().saveAsPNG(name + "_access_conn");
		System.out.println("Largest connected accessible area: " + (100f*access_conn.count(1f)/(access_conn.width*access_conn.height)) + "%");
		float access_avrconn = access.averageConnected(1f);
		System.out.println("Average connected accessible area: " + (100f*access_avrconn/(access.width*access.height)) + "%");

		System.out.println("Flatness map...");
		Channel flat = height.copy().lineart().threshold(0f, 0.0125f);
		flat.toLayer().saveAsPNG(name + "_flatness");
		System.out.println("done");
		System.out.println("Flat area: " + (100f*flat.count(1f)/(flat.width*flat.height)) + "%");
		Channel flat_conn = flat.copy().largestConnected(1f);
		flat_conn.toLayer().saveAsPNG(name + "_flatness_conn");
		System.out.println("Largest connected flat area: " + (100f*flat_conn.count(1f)/(flat_conn.width*flat_conn.height)) + "%");
		float flat_avrconn = flat.averageConnected(1f);
		System.out.println("Average connected flat area: " + (100f*flat_avrconn/(flat.width*flat.height)) + "%");
		
		System.out.println("Connectedness map...");
		Channel conn = connectedness(height, 16);
		conn.toLayer().saveAsPNG(name + "_connectedness");
		System.out.println("Overall connectedness: " + conn.sum()/(height.width*height.height));
		System.out.println("done");

		System.out.println("Fourier transform...");
		Channel[] ffts = height.fft();
		ffts[0].copy().dynamicRange().toLayer().saveAsPNG(name + "_fft_magnitude");
		ffts[0].log().dynamicRange().toLayer().saveAsPNG(name + "_fft_magnitude_log");
		histogram(ffts[0], height.width).flipV().toLayer().saveAsPNG(name + "_fft_magnitude_log_histogram");
		ffts[1].dynamicRange().toLayer().saveAsPNG(name + "_fft_phase");
		histogram(ffts[1], height.width).flipV().toLayer().saveAsPNG(name + "_fft_phase_histogram");
		System.out.println("done");

		System.out.println("*** Image analysis complete ****");
	}

	public final static Channel histogram(Channel channel, int size) {
		assert channel.findMin() >= 0 && channel.findMax() <= 1 : "image must be normalized";
		Channel hist = new Channel(size, size).fill(1f);
		int[] histogram = new int[size];
		for (int y = 0; y < channel.width; y++) {
			for (int x = 0; x < channel.height; x++) {
				histogram[(int)(channel.getPixel(x, y)*(size - 1))]++;
			}
		}
		int max = 0;
		for (int i = 0; i < size; i++) {
			if (histogram[i] > max) {
				max = histogram[i];
			}
		}
		float scale = (float)(size)/max;
		for (int x = 0; x < size; x++) {
			int lineheight = (int)(histogram[x]*scale);
			for (int y = 0; y < lineheight; y++) {
				hist.putPixel(x, y, 0f);
			}
		}
		return hist;
	}
	
	public final static void score(Channel channel, String name) {
		float average = average(channel);
		float variance = variance(channel);
		float deviation = standardDeviation(variance);
		System.out.println(name + " erosion score: " + deviation/average);
	}
	
	public final static float score(Channel channel) {
		float average = average(channel);
		float variance = variance(channel);
		float deviation = standardDeviation(variance);
		return deviation/average;
	}

	public final static void statistics(Channel channel, String name) {
		float average = average(channel);
		float variance = variance(channel);
		float deviation = standardDeviation(variance);
		System.out.println(name + " average: " + average);
		System.out.println(name + " standard deviation: " + deviation);
	}

	public final static float average(Channel channel) {
		float sum = 0;
		for (int x = 0; x < channel.width; x++) {
			for (int y = 0; y < channel.height; y++) {
				sum+= channel.getPixel(x, y);
			}
		}
		return sum/(channel.width*channel.height);
	}

	public final static float variance(Channel channel) {
		float average = average(channel);
		float sum = 0;
		for (int x = 0; x < channel.width; x++) {
			for (int y = 0; y < channel.height; y++) {
				float value = channel.getPixel(x, y) - average;
				sum+= value*value;
			}
		}
		return sum/(channel.width*channel.height);
	}
	
	public final static float deviation(Channel channel) {
		return (float)Math.sqrt(variance(channel));
	}

	public final static float standardDeviation(float variance) {
		return (float)Math.sqrt(variance);
	}
	
	public final static Channel connectedness(Channel height, int steps) {
		System.out.print("Analyzing connectedness");
		Channel channel = new Channel(height.width, height.height);
		Channel slope = height.copy().lineart();
		for (int i = 0; i < steps; i++) {
			//channel.channelBrightest(slope.copy().threshold(0f, 16f*i/(height.width*steps)).largestConnected(1f).multiply((float)(steps - i)/steps));
			channel.channelAdd(slope.copy().threshold(0f, 16f*i/(height.width*steps)).largestConnected(1f).multiply(1f/steps));
			System.out.print(".");
		}
		System.out.println("done");
		return channel;
	}
	
	public final static float squareScore(Channel height, float threshold, int square_size) {
		return height.copy().lineart().threshold(0f, threshold).squareFit(1f, square_size).count(1f)/(float)(height.width*height.height);
	}

}
