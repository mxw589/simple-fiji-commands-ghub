import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class Watershed {
	
	/**
	 * static method to apply the watershedding algorithm to a given image
	 * @param chosenImg the given image
	 * @param hMin the maximum value of the coloration in a pixel
	 * @param hMax the minimum value of the coloration in a pixel
	 * @param threshVal the threshold value to be applied
	 * @return the altered image
	 */
	public static ImagePlus computeWatershed(ImagePlus chosenImg, double hMin, double hMax, double threshVal) {

		ImageProcessor ip = apply(chosenImg.getProcessor(), hMin, hMax, threshVal);

		String title = chosenImg.getTitle();
		String ext = "";
		int index = title.lastIndexOf( "." );
		if( index != -1 )
		{
			ext = title.substring(index);
			title = title.substring(0, index);				
		}

		ImagePlus imageWSApplied = new ImagePlus(title + "-watershed" + ext, ip);
		imageWSApplied.setCalibration( chosenImg.getCalibration() );

		return imageWSApplied;

	}

	/**
	 * method to build the image processor for the image to be watershedded.
	 * handles the construction of an arraylist of the pixels and their
	 * coloration values, to be threshholded in order to create a new binary array,
	 * which can then be eroded and dilated as required.
	 * 
	 * @param input the given image's processor
	 * @param hMin the maximum value of the coloration in a pixel
	 * @param hMax the minimum value of the coloration in a pixel
	 * @param threshVal the threshold value to be applied
	 * @return
	 */
	public static ImageProcessor apply(ImageProcessor input, double hMin, double hMax, double threshVal){
		final int width = input.getWidth();
		IJ.log("Width: " + width);
		final int height = input.getHeight();
		IJ.log("Height: " + height);

		// output labels
		final int[][] tabLabels = new int[width][height];
		
		for( int i=0; i<width; i++ ){
			Arrays.fill(tabLabels[i], 0);
		}
		
		//label to be applied to points under the threshold
		int label = 1;
		
		IJ.showStatus( "Extracting coloration values");
		IJ.log("Extracting coloration values");
		
		ArrayList<PixelsValues> pixelList = extractPixelValues(input, hMin, hMax );

		IJ.showStatus("Sorting pixels by coloration value");
		IJ.log("Sorting pixels by coloration value");
		
		Collections.sort(pixelList);
		
		boolean stopCheck = false;
		Iterator<PixelsValues> pixelIterator = pixelList.iterator();
		PixelsValues currentPixel = null;
		
		IJ.showStatus("Thresholding");
		IJ.log("Thresholding");
		long start = System.currentTimeMillis();
		
		while(pixelIterator.hasNext() && stopCheck == false){
			currentPixel = pixelIterator.next();
			if(currentPixel.getValue() < threshVal){
				tabLabels[currentPixel.getPixelPos().getX()][currentPixel.getPixelPos().getY()] = label;
			} else {
				stopCheck = true;
			}
		}
	
		long end = System.currentTimeMillis();
		IJ.log("Thresholding took " + (end-start) + " ms.");
		
		String currLine = "";
		
		/*
		 * DEBUG log the thresholding result
		 */
		for(int heightPr = 0; heightPr < height; heightPr++){
			for(int widthPr = 0; widthPr < width; widthPr++){
				currLine += " " + tabLabels[widthPr][heightPr];
			}
			IJ.log(currLine);
			currLine = "";
		}
		
		return input;
		
	}

	/**
	 * static method that returns an ArrayList of PixelsValues (which includes
	 * their position and coloration value and overall position within the image)
	 * from a given image.
	 * @param input ImageProcessor of image that is being watershedded
	 * @param hMin the maximum value of the coloration in a pixel
	 * @param hMax the minimum value of the coloration in a pixel
	 * @return an ArrayList of PixelsValues (which includes
	 * their position and coloration value and overall position within the image)
	 */
	private static ArrayList<PixelsValues> extractPixelValues(ImageProcessor input, double hMin, double hMax) {
		final int width = input.getWidth();
		final int height = input.getHeight();
		
		ArrayList<PixelsValues> list = new ArrayList<PixelsValues>();
		/*
		 * variable used to keep track of overall position (useful for sorting)
		 */
		int pixelNo = 0;
		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				/*
				 * get the coloration value of the given pixel
				 */
				final double h = input.getf(x, y);
				if( h >= hMin && h <= hMax ){
					PixelPos currPos = new PixelPos(x, y);
					list.add(new PixelsValues(currPos, h, pixelNo));
				}
				pixelNo++;
			}
		}

		return list;
	}

}
