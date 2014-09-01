/* 
 * Copyright (c) 2002-2004 LWJGL Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are 
 * met:
 * 
 * * Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of 
 *   its contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oddlabs.tt.util;

import java.nio.FloatBuffer;

/**
 * $Id: StrictVector3f.java,v 1.3 2004/06/12 20:28:29 matzon Exp $
 *
 * Holds a 3-tuple vector.
 * 
 * @author
 * @version $Revision: 1.3 $
 */

public strictfp class StrictVector3f {

	public float x, y, z;

	/**
	 * Constructor for StrictVector3f.
	 */
	public StrictVector3f() {
		super();
	}

	/**
	 * Constructor
	 */
	public StrictVector3f(float x, float y, float z) {
		set(x, y, z);
	}

	/* (non-Javadoc)
	 * @see org.lwjgl.util.vector.WritableStrictVector2f#set(float, float)
	 */
	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void set(StrictVector3f v) {
		set(v.x, v.y, v.z);
	}

	/* (non-Javadoc)
	 * @see org.lwjgl.util.vector.WritableStrictVector3f#set(float, float, float)
	 */
	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * @return the length of the vector
	 */
	public final float length() {
		return (float) StrictMath.sqrt(lengthSquared());
	}


	/**
	 * @return the length squared of the vector
	 */
	public float lengthSquared() {
		return x * x + y * y + z * z;
	}

	/**
	 * Translate a vector
	 * @param x The translation in x
	 * @param y the translation in y
	 * @return this
	 */
	public StrictVector3f translate(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	/**
	 * Add a vector to another vector and place the result in a destination
	 * vector.
	 * @param left The LHS vector
	 * @param right The RHS vector
	 * @param dest The destination vector, or null if a new vector is to be created
	 * @return the sum of left and right in dest
	 */
	public static StrictVector3f add(StrictVector3f left, StrictVector3f right, StrictVector3f dest) {
		if (dest == null)
			return new StrictVector3f(left.x + right.x, left.y + right.y, left.z + right.z);
		else {
			dest.set(left.x + right.x, left.y + right.y, left.z + right.z);
			return dest;
		}
	}

	/**
	 * Subtract a vector from another vector and place the result in a destination
	 * vector.
	 * @param left The LHS vector
	 * @param right The RHS vector
	 * @param dest The destination vector, or null if a new vector is to be created
	 * @return left minus right in dest
	 */
	public static StrictVector3f sub(StrictVector3f left, StrictVector3f right, StrictVector3f dest) {
		if (dest == null)
			return new StrictVector3f(left.x - right.x, left.y - right.y, left.z - right.z);
		else {
			dest.set(left.x - right.x, left.y - right.y, left.z - right.z);
			return dest;
		}
	}

	/**
	 * The cross product of two vectors.
	 * 
	 * @param left The LHS vector
	 * @param right The RHS vector
	 * @param dest The destination result, or null if a new vector is to be created
	 * @return left cross right
	 */
	public static StrictVector3f cross(
			StrictVector3f left,
			StrictVector3f right,
			StrictVector3f dest)
	{

		if (dest == null)
			dest = new StrictVector3f();

		dest.set(
				left.y * right.z - left.z * right.y,
				right.x * left.z - right.z * left.x,
				left.x * right.y - left.y * right.x
				);

		return dest;
	}    



	/**
	 * Negate a vector
	 * @return this
	 */
	public StrictVector3f negate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	/**
	 * Negate a vector and place the result in a destination vector.
	 * @param dest The destination vector or null if a new vector is to be created
	 * @return the negated vector
	 */
	public StrictVector3f negate(StrictVector3f dest) {
		if (dest == null)
			dest = new StrictVector3f();
		dest.x = -x;
		dest.y = -y;
		dest.z = -z;
		return dest;
	}


	/**
	 * Normalise this vector
	 * @return this
	 */
	public final StrictVector3f normalise() {
		float len = length();
		if (len != 0.0f) {
			float l = 1.0f / len;
			return scale(l);
		} else
			throw new IllegalStateException("Zero length vector");
	}


	/**
	 * Normalise this vector and place the result in another vector.
	 * @param dest The destination vector, or null if a new vector is to be created
	 * @return the normalised vector
	 */
	public StrictVector3f normalise(StrictVector3f dest) {
		float l = length();

		if (dest == null)
			dest = new StrictVector3f(x / l, y / l, z / l);
		else
			dest.set(x / l, y / l, z / l);

		return dest;
	}

	/**
	 * The dot product of two vectors is calculated as
	 * v1.x * v2.x + v1.y * v2.y + v1.z * v2.z
	 * @param left The LHS vector
	 * @param right The RHS vector
	 * @return left dot right
	 */
	public static float dot(StrictVector3f left, StrictVector3f right) {
		return left.x * right.x + left.y * right.y + left.z * right.z;
	}

	/**
	 * Calculate the angle between two vectors, in degrees
	 * @param a A vector
	 * @param b The other vector
	 * @return the angle between the two vectors, in degrees
	 */
	public static float angle(StrictVector3f a, StrictVector3f b) {
		float dls = dot(a, b) / (a.length() * b.length());
		if (dls < -1f)
			dls = -1f;
		else if (dls > 1.0f)
			dls = 1.0f;
		return (float) StrictMath.toDegrees(StrictMath.acos(dls));
	}

	/* (non-Javadoc)
	 * @see org.lwjgl.vector.StrictVector#load(FloatBuffer)
	 */
	public StrictVector3f load(FloatBuffer buf) {
		x = buf.get();
		y = buf.get();
		z = buf.get();
		return this;
	}

	/* (non-Javadoc)
	 * @see org.lwjgl.vector.StrictVector#scale(float)
	 */
	public StrictVector3f scale(float scale) {

		x *= scale;
		y *= scale;
		z *= scale;

		return this;

	}

	/* (non-Javadoc)
	 * @see org.lwjgl.vector.StrictVector#store(FloatBuffer)
	 */
	public StrictVector3f store(FloatBuffer buf) {

		buf.put(x);
		buf.put(y);
		buf.put(z);

		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(64);

		sb.append("StrictVector3f[");
		sb.append(x);
		sb.append(", ");
		sb.append(y);
		sb.append(", ");
		sb.append(z);
		sb.append(']');
		return sb.toString();
	}

	/**
	 * @return x
	 */
	public final float getX() {
		return x;
	}

	/**
	 * @return y
	 */
	public final float getY() {
		return y;
	}

	/**
	 * Set X
	 * @param x
	 */
	public final void setX(float x) {
		this.x = x;
	}

	/**
	 * Set Y
	 * @param y
	 */
	public final void setY(float y) {
		this.y = y;
	}

	/**
	 * Set Z
	 * @param z
	 */
	public void setZ(float z) {
		this.z = z;
	}

	/* (Overrides)
	 * @see org.lwjgl.vector.ReadableStrictVector3f#getZ()
	 */
	public float getZ() {
		return z;
	}
}
