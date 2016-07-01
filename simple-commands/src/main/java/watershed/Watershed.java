package watershed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import dataTypes.PixelPos;
import dataTypes.PixelsValues;
import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
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
		final int[][] labelled = new int[width][height];
		
		for( int i=0; i<width; i++ ){
			Arrays.fill(labelled[i], 1);
		}
		
		//label to be applied to points under the threshold
		int label = 0;
		
		IJ.showStatus( "Extracting coloration values");
		IJ.log("Extracting coloration values");
		
		ArrayList<PixelsValues> pixelList = extractPixelValues(input, hMin, hMax );

		IJ.showStatus("Sorting pixels by coloration value");
		IJ.log("Sorting pixels by coloration value");
		
		Collections.sort(pixelList);
		
		/*
		 * thresholding
		 */
		Threshold.threshold(pixelList, labelled, threshVal, label);
		
		/*
		 * DEBUG printing the labels after thresholding
		 */
//		String currLine = "";
//		
//		for(int heightPr = 0; heightPr < labelled[0].length; heightPr++){
//			for(int widthPr = 0; widthPr < labelled.length; widthPr++){
//				currLine += " " + labelled[widthPr][heightPr];
//			}
//			IJ.log(currLine);
//			currLine = "";
//		}
		
		/*
		 * eroding
		 */
		Erode.erode(labelled, 1, 0, width, height);
		
		/*
		 * dilating
		 */
		Dilate.dilate(labelled, 1, 0, width, height);
		
		/*
		 * taking the array of labels and turning it into an image for the user
		 */
		FloatProcessor fp = new FloatProcessor(width, height);
		for(int widthFP = 0; widthFP < width; widthFP++){
			for(int heightFP = 0; heightFP < height; heightFP++){
				fp.set(widthFP, heightFP, labelled[widthFP][heightFP]);
			}
		}
		
		return fp;
		
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
