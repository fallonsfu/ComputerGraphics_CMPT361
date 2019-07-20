package geometry;

import java.util.Arrays;

public class Transformation {

    private static final int dimension = 4;
    public double[][] matrix;

    public Transformation(double[][] matrix) {
        this.matrix = matrix;
    }

    public static Transformation identity() {
        double[][] identity = new double[dimension][dimension];
        for(int i = 0; i < dimension; i++)
            identity[i][i] = 1;
        return new Transformation(identity);
    }

    public Transformation scale(double sx, double sy, double sz) {
        double[][] scaleMatrix = {{sx, 0, 0, 0}, {0, sy, 0, 0}, {0, 0, sz, 0}, {0, 0, 0, 1}};
        return multiply(scaleMatrix);
    }

    public Transformation translate(double tx, double ty, double tz) {
        double[][] translateMatrix = {{1, 0, 0, tx}, {0, 1, 0, ty}, {0, 0, 1, tz}, {0, 0, 0, 1}};
        return multiply(translateMatrix);
    }

    public Transformation rotate(String axis, double degree) {
        double alpha = Math.toRadians(degree);
        double[][] rotateMatrix = new double[dimension][dimension];
        switch(axis) {
            case "X": rotateMatrix = new double[][] {{1, 0, 0, 0}, {0, Math.cos(alpha), -Math.sin(alpha), 0}, {0, Math.sin(alpha), Math.cos(alpha), 0}, {0, 0, 0, 1}};
                break;
            case "Y": rotateMatrix = new double[][] {{Math.cos(alpha), 0, Math.sin(alpha), 0}, {0, 1, 0, 0}, {-Math.sin(alpha), 0, Math.cos(alpha), 0}, {0, 0, 0, 1}};
                break;
            case "Z": rotateMatrix = new double[][] {{Math.cos(alpha), -Math.sin(alpha), 0, 0}, {Math.sin(alpha), Math.cos(alpha), 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
                break;
        }
        return multiply(rotateMatrix);
    }

    public Transformation multiply(double[][] matrix) {
        double[][] newMatrix = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                for (int k = 0; k < dimension; k++)
                    newMatrix[i][j] += this.matrix[i][k] * matrix[k][j];
            }
        }
        return new Transformation(newMatrix);
    }

    public double[] transform(double[] vector) {
        double[] coords = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++)
                coords[i] += matrix[i][j] * vector[j];
        }
        return coords;
    }

    public double[] rightMultiply(double[] vector) {
        double[] coords = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++)
                coords[i] += vector[j] * matrix[j][i];
        }
        return coords;
    }

    //Methods copied from https://www.sanfoundry.com/java-program-find-inverse-matrix/
    public Transformation inverse() {
        double x[][] = new double[dimension][dimension];
        double b[][] = new double[dimension][dimension];
        int index[] = new int[dimension];
        for (int i = 0; i < dimension; ++i)
            b[i][i] = 1;
        // Transform the matrix into an upper triangle
        double[][] m = new double[dimension][dimension];
        for(int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++)
                m[i][j] = matrix[i][j];
        }
        gaussian(m, index);
        // Update the matrix b[i][j] with the ratios stored
        for (int i = 0; i < dimension - 1; ++i)
            for (int j = i + 1; j < dimension; ++j)
                for (int k = 0; k < dimension; ++k)
                    b[index[j]][k] -= m[index[j]][i] * b[index[i]][k];
        // Perform backward substitutions
        for (int i = 0; i < dimension; ++i) {
            x[dimension - 1][i] = b[index[dimension - 1]][i] / matrix[index[dimension - 1]][dimension - 1];
            for (int j = dimension - 2; j >= 0; --j) {
                x[j][i] = b[index[j]][i];
                for (int k = j + 1; k < dimension; ++k) {
                    x[j][i] -= m[index[j]][k] * x[k][i];
                }
                x[j][i] /= m[index[j]][j];
            }
        }
        return new Transformation(x);
    }

    // Method to carry out the partial-pivoting Gaussian
    // elimination. Here index[] stores pivoting order.
    private void gaussian(double a[][], int index[]) {
        double c[] = new double[dimension];
        // Initialize the index
        for (int i = 0; i < dimension; ++i)
            index[i] = i;
        // Find the rescaling factors, one from each row
        for (int i = 0; i < dimension; ++i) {
            double c1 = 0;
            for (int j = 0; j < dimension; ++j) {
                double c0 = Math.abs(a[i][j]);
                if (c0 > c1)
                    c1 = c0;
            }
            c[i] = c1;
        }
        // Search the pivoting element from each column
        int k = 0;
        for (int j = 0; j < dimension - 1; ++j) {
            double pi1 = 0;
            for (int i = j; i < dimension; ++i) {
                double pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if (pi0 > pi1) {
                    pi1 = pi0;
                    k = i;
                }
            }
            // Interchange rows according to the pivoting order
            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i = j + 1; i < dimension; ++i) {
                double pj = a[index[i]][j] / a[index[j]][j];
                // Record pivoting ratios below the diagonal
                a[index[i]][j] = pj;
                // Modify other elements accordingly
                for (int l = j + 1; l < dimension; ++l)
                    a[index[i]][l] -= pj * a[index[j]][l];
            }
        }
    }
}
