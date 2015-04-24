package com.geag.rmi;

public interface GeagRmiInterface {
	public String getResponse(String data);
	
	public String getResponseOfScientificOperation(String data);
	
	public String getResponseOfJScienceOperation(double[][] a, double[][] b);

}
