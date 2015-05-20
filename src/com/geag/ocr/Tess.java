package com.geag.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Tess {
	public String performOcrOperation(String imageUrl) {

		File imageFile = null;
		String result = "";
		try {

			URL url = new URL(imageUrl);
			BufferedImage img = ImageIO.read(url);
			imageFile = new File("downloaded.png");
			ImageIO.write(img, "png", imageFile);

			ITesseract instance = new Tesseract(); // JNA Interface Mapping
			// ITesseract instance = new Tesseract1(); // JNA Direct Mapping

			result = instance.doOCR(imageFile);
			System.out.println(result);
		} catch (TesseractException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return result;
	}


}
