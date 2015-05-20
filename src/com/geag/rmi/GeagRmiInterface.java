package com.geag.rmi;

public interface GeagRmiInterface {
	public String getResponseOfFaceDetection(String data);

	public String getResponseOfMatriceMultiplicationWithScilab(String data); 

	public String getResponseOfPolynomialMultiplicationWithJScience(String coeffs);

	public String getResponseOfMatriceMultiplicationWithJScience(double[][] a);

	public String getResponseOfOcrDetection(String fileUrl);

}
