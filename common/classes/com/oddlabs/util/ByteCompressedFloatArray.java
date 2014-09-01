package com.oddlabs.util;

import java.io.*;

public final strictfp class ByteCompressedFloatArray implements Serializable { 
	private final static long serialVersionUID = 1;

	private final float[] scale;
	private final float[] offset;
	private final byte[][] data;

	public ByteCompressedFloatArray(float[] array, int channels) {
		int channel_length = array.length/channels;
		scale = new float[channels];
		offset = new float[channels];
		data = new byte[channels][channel_length];
		float[][] split_data = new float[channels][channel_length];
		for (int i = 0; i < channel_length; i++) {
			for (int j = 0; j < channels; j++) {
				split_data[j][i] = array[i*channels + j];
			}
		}
		for (int i = 0; i < channels; i++)
			compress(split_data[i], i);
	}

	private final void compress(float[] array, int channel) {
		float min = array[0];
		float max = array[0];

		for (int i = 0; i < array.length; i++) {
			float current = array[i];
			if (current < min)
				min = current;
			else if (current > max)
				max = current;
		}

		float mid = (max + min)/2;
		offset[channel] = mid;
		scale[channel] = (max - mid)/Byte.MAX_VALUE;

		for (int i = 0; i < array.length; i++)
			data[channel][i] = (byte)((array[i] - offset[channel])/scale[channel]);
	}

	public final float[] getFloatArray() {
		int channels = data.length;
		int channel_length = data[0].length;
		float[] result = new float[channels*channel_length];

		for (int i = 0; i < channel_length; i++) {
			for (int j = 0; j < channels; j++) {
				result[i*channels + j] = data[j][i]*scale[j] + offset[j];
			}
		}
		return result;
	}

	public final String toString() {
		float[] array = getFloatArray();
		String result = "";
		for (int i = 0; i < array.length; i++)
			result += array[i] + ", ";
		return result;
	}
/*
	public final static void main(String[] args) {
		float[] array = {200f, 30f, 23.7765f};
		ByteCompressedFloatArray scfa = new ByteCompressedFloatArray(array, 1);
System.out.println("array = " + scfa);
	}
*/
}
