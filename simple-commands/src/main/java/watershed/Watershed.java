package watershed;
import java.util.ArrayList;
import java.util.Collections;

import dataTypes.PixelPos;
import dataTypes.PixelsValues;
import dataTypes.ThresholdDataPoint;
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
		final ThresholdDataPoint[][] labelled = new ThresholdDataPoint[width][height];
		
		final int backgroundLabel = 1;
		final int foregroundLabel = 0;
		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				PixelPos pixelPos = new PixelPos(x,y);
				labelled[x][y] = new ThresholdDataPoint(backgroundLabel, pixelPos);
			}
		}
		
		IJ.showStatus( "Extracting coloration values");
		IJ.log("Extracting coloration values");
		
		ArrayList<PixelsValues> pixelList = extractPixelValues(input, hMin, hMax );

		IJ.showStatus("Sorting pixels by coloration value");
		IJ.log("Sorting pixels by coloration value");
		
		Collections.sort(pixelList);
		
		/*
		 * thresholding
		 */
		Threshold.threshold(pixelList, labelled, threshVal, foregroundLabel);
		
		/*
		 * DEBUG printing the labels after thresholding
		 */
		String currLine = "";
		
		for(int heightPr = 0; heightPr < labelled[0].length; heightPr++){
			for(int widthPr = 0; widthPr < labelled.length; widthPr++){
				currLine += " " + labelled[widthPr][heightPr].getLabel();
			}
			IJ.log(currLine);
			currLine = "";
		}
		
		/*
		 * eroding
		 */
		Erode.erode(labelled, backgroundLabel, foregroundLabel, width, height);
		
		
		/*
		 * set the neighbours
		 */
		establishNeighbours(labelled, width, height);
		
		/*
		 * set the initial labels for the cell bodies
		 */
		initialCellBodyLabel(labelled, backgroundLabel, foregroundLabel);
		
		/*
		 * dilating
		 */
		Dilate.dilate(labelled, backgroundLabel, foregroundLabel, width, height);
		
		/*
		 * taking the array of labels and turning it into an image for the user
		 */
		FloatProcessor fp = new FloatProcessor(width, height);
		for(int widthFP = 0; widthFP < width; widthFP++){
			for(int heightFP = 0; heightFP < height; heightFP++){
				fp.set(widthFP, heightFP, labelled[widthFP][heightFP].getLabel());
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
	
	/**
	 * a static method that adds an ArrayList of the neighbours of a
	 * ThresholdDataPoint to each ThresholdDataPoint in a 2x2 array 
	 * @param labelled the 2x2 array of ThresholdDataPoints for which the
	 * neighbours need to be set
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	private static void establishNeighbours(ThresholdDataPoint labelled[][], int width, int height){
		IJ.showStatus("Establishing neighbours");
		IJ.log("Establishing neighbours");
		long start = System.currentTimeMillis();
		
		ArrayList<ThresholdDataPoint> neighTDP;
		int xPos;
		int yPos;
		ArrayList<PixelPos> neighPix;
		PixelPos pixelPos;
		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				pixelPos = new PixelPos(x, y);
				neighTDP = new ArrayList<ThresholdDataPoint>();
				neighPix = Neighbours.neighbours(pixelPos, width, height);

				for(PixelPos neigh : neighPix){
					xPos = neigh.getX();
					yPos = neigh.getY();
					neighTDP.add(labelled[xPos][yPos]);
				}
				
				labelled[x][y].setNeighbours(neighTDP);
			}
		}
		
		long end = System.currentTimeMillis();
		IJ.log("Establishing neighbours took " + (end-start) + " ms.");
		
	}
	
	public static void initialCellBodyLabel(ThresholdDataPoint[][] labelled, int backgroundLabel, int foregroundLabel){
		int currentNextLabel = 1;
		
		for(ThresholdDataPoint[] row : labelled){
			for(ThresholdDataPoint element: row){
				/*if the pixel is part of a cell body*/
				if(element.getLabel() != backgroundLabel){
					/*get its neighbours label*/
					getNeighboursCellLabel(element);
					/*if it is zero then this should be treated as an as yet 
					 * unlabelled cell body*/
					if(element.getCellBody() == 0){
						element.setCellBody(currentNextLabel);
						currentNextLabel++;
					}
				}
			}
		}
	}
	/**
	 * sets the value of a ThresholdDataPoint's cell body to that of its
	 * neighbours. This should return 0 if none of the neighbours have 
	 * been labelled yet
	 * @param dataPoint the point to be labelled
	 */
	public static void getNeighboursCellLabel(ThresholdDataPoint dataPoint){
		int neighCellLabel = 0;
		ArrayList<ThresholdDataPoint> neigh = dataPoint.getNeighbours();
		int currentNeighCellLabel;
		for(ThresholdDataPoint neighPixel : neigh){
			currentNeighCellLabel = neighPixel.getCellBody();
			/*if the current assumed label is still 0, it should be changed*/
			if(neighCellLabel == 0){
				neighCellLabel = currentNeighCellLabel;
			/*ensure that there are no conflicting neighbours (that aren't background)*/
			} else if(currentNeighCellLabel != 0 && neighCellLabel != currentNeighCellLabel){
				throw new IllegalStateException("Neighbouring pixels have "
						+ "conflicting cell body labels");
			}
		}
		dataPoint.setCellBody(neighCellLabel);
	}

}
