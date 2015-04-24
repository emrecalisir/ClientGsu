package com.clientgsu.jscience;


import org.jscience.mathematics.vector.Float64Matrix;

public class JScienceCalculation {
    
	public String calculateWithJScience(double[][] a, double[][] b) {

	    Float64Matrix A = Float64Matrix.valueOf(a);
	    Float64Matrix B = Float64Matrix.valueOf(b);
	    Float64Matrix C = A.times(B);
	    Float64Matrix D = C.transpose();
	    
        return D.determinant().toString();
	}
	


}