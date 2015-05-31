package com.geag.ocr;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;


public class Tess {
	public static final String TAG = "TESS";
	
	public String tess2Operation(String dataPath, String lang, Bitmap bitmap ){


			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		

		// _image.setImageBitmap( bitmap );
		
		Log.v(TAG, "Before baseApi");

		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(dataPath, lang);
		baseApi.setImage(bitmap);
		
		String recognizedText = baseApi.getUTF8Text();
		
		baseApi.end();

		// You now have the text in recognizedText var, you can do anything with it.
		// We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
		// so that garbage doesn't make it to the display.

		Log.v(TAG, "OCRED TEXT: " + recognizedText);
		
		return recognizedText;
	}
	
	public static void main(String[] args) {
		Tess tess = new Tess();
		//tess.performOcrOperation("");
		
	}


}
