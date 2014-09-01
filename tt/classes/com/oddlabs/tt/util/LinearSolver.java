package com.oddlabs.tt.util;

public final strictfp class LinearSolver {
	private final static float THRESHOLD = 0.001f;

	public final static void solve(float[][] eq_system_orig, float[] solution) {
		float[][] eq_system = copyEquation(eq_system_orig);
//System.out.println("orig system:");
//dumpEquation(eq_system);
		int pivot_row = 0;
		for (int column = 0; column < eq_system[0].length - 1; column++)
			if (solve(column, pivot_row, eq_system))
				pivot_row++;
//dumpEquation(eq_system);
		reduce(eq_system);
/*System.out.println("reduced system:");
dumpEquation(eq_system);*/
		assert checkEquation(eq_system);
		assignSolutions(eq_system, solution);
//dumpSolution(solution);
		assert checkSolution(eq_system_orig, solution);
	}

	private final static float[][] copyEquation(float[][] orig_eq) {
		float[][] result = new float[orig_eq.length][orig_eq[0].length];
		for (int row = 0; row < result.length; row++)
			for (int column = 0; column < result[0].length; column++)
				result[row][column] = orig_eq[row][column];
		return result;
	}

	private final static boolean checkSolution(float[][] eq_system, float[] solution) {
		for (int row = 0; row < eq_system.length; row++) {
			float right_side = 0f;
			for (int column = 0; column < solution.length; column++)
				right_side += eq_system[row][column]*solution[column];
			float left_side = eq_system[row][eq_system[0].length -1];
			if (!isEqual(right_side, left_side)) {
				System.out.println("mismatch " + right_side + " != " + left_side);
				return false;
			}
		}
		return true;
	}

	private final static void assignSolutions(float[][] eq_system, float[] solution) {
		int row = 0;
		for (int column = 0; column < eq_system[0].length - 1; column++) {
			float pivot = eq_system[row][column];
			if (!isZero(pivot)) {
				solution[column] = eq_system[row][eq_system[0].length - 1];
				row++;
				if (row == eq_system.length)
					break;
			}
		}
	}

	private final static void reduce(float[][] eq_system) {
		for (int row = 1; row < eq_system.length; row++) {
			int column;
			for (column = 0; column < eq_system[0].length; column++) {
				float pivot = eq_system[row][column];
				if (!isZero(pivot))
					break;
			}
			if (column == eq_system[0].length)
				continue;
			for (int inner_row = 0; inner_row < row; inner_row++) {
				float row_pivot = eq_system[inner_row][column];
				subtractRow(row_pivot, eq_system[inner_row], eq_system[row]);
			}
		}
	}

	private final static boolean checkEquation(float[][] eq_system) {
		for (int row = 0; row < eq_system.length; row++) {
			int num_non_zero = 0;
			for (int column = 0; column < eq_system[0].length; column++)
				if (!isZero(eq_system[row][column]))
					num_non_zero++;
			if (num_non_zero == 1 && !isZero(eq_system[row][eq_system[0].length - 1]))
				return false;
		}
		return true;
	}

	public final static boolean isEqual(float val1, float val2) {
		return StrictMath.abs(val1 - val2) <= THRESHOLD;
	}

	public final static boolean isZero(float val) {
		return isEqual(val, 0f);
	}

	private final static void swapRows(int row_index1, int row_index2, float[][] eq_system) {
		float[] row = eq_system[row_index1];
		eq_system[row_index1] = eq_system[row_index2];
		eq_system[row_index2] = row;
//System.out.println("swapping rows " + row_index1 + " " + row_index2);
	}

	private final static void subtractRow(float scale, float[] row1, float[] row2) {
		for (int i = 0; i < row1.length; i++)
			row1[i] -= scale*row2[i];
	}

	private final static void scaleRow(float scale, float[] row) {
		for (int column = 0; column < row.length; column++)
			row[column] *= scale;
	}

	private final static boolean solve(int column, int pivot_row, float[][] eq_system) {
		for (int row = pivot_row; row < eq_system.length; row++) {
			float pivot = eq_system[row][column];
			if (!isZero(pivot)) {
				swapRows(pivot_row, row, eq_system);
				scaleRow(1f/pivot, eq_system[pivot_row]);
//System.out.println("pivot " + pivot);
				for (int inner_row = pivot_row + 1; inner_row < eq_system.length; inner_row++) {
					float row_pivot = eq_system[inner_row][column];
//System.out.println("row pivot " + row_pivot);
					subtractRow(row_pivot, eq_system[inner_row], eq_system[pivot_row]);
				}
				return true;
			}
		}
		return false;
	}

/*	private final static void dumpEquation(float[][] eq_system) {
		for (int row = 0; row < eq_system.length; row++)
			dumpSolution(eq_system[row]);
		System.out.println();
	}
*/
	public final static void dumpSolution(float[] solution) {
		for (int column = 0; column < solution.length; column++) {
			System.out.print(solution[column] + " ");
		}
		System.out.println();
	}

/*	public final static void main(String[] args) {
		float[][] system0 = new float[][]{{1, 2, 1, 3}, {3, -1, -3, -1}, {2, 3, 1, 4}};
		float[][] system1 = new float[][]{{0, -1, -1, 1, 0}, {1, 1, 1, 1, 6}, {2, 4, 1, -2, -1}, {3, 1, -2, 2, 3}};
		float[][] system2 = new float[][]{{1, 1, 1, 1, 1, 1}, {-1, -1, 0, 0, 1, -1}, {-2, -2, 0, 0, 3, 1}, {0, 0, 1, 1, 3, -1}, {1, 1, 2, 2, 4, 1}};
		float[][] system3 = new float[][]{{1, 1, 1, 1, 1, 1}, {-1, -1, 0, 0, 1, -1}, {-2, -2, 0, 0, 3, 1}, {0, 0, 1, 1, 3, 3}, {1, 1, 2, 2, 4, 4}};
		float[] solution0 = new float[system0[0].length - 1];
		float[] solution1 = new float[system1[0].length - 1];
		float[] solution2 = new float[system2[0].length - 1];
		float[] solution3 = new float[system3[0].length - 1];
		solve(system0, solution0);
		solve(system1, solution1);
//		solve(system2, solution2);
		solve(system3, solution3);
	}*/
}
