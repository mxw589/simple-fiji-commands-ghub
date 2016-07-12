package watershed;
import java.util.ArrayList;
import java.util.Collections;

import dataTypes.CellsToMerge;
import dataTypes.PixelPos;
import dataTypes.PixelsValues;
import dataTypes.ThresholdDataPoint;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class Watershed {
	
	public final static int WSHED = 9;
	public final static int CONNEC = 8;
	
	/**
	 * static method to apply the watershedding algorithm to a given image
	 * @param chosenImg the given image
	 * @param hMin the maximum value of the coloration in a pixel
	 * @param hMax the minimum value of the coloration in a pixel
	 * @param threshVal the threshold value to be applied
	 * @return the altered image
	 */
	public static ImagePlus computeWatershed(ImagePlus chosenImg, double hMin, double hMax, double threshVal, double eroDilCount) {

		ImageProcessor ip = apply(chosenImg.getProcessor(), hMin, hMax, threshVal, eroDilCount);

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
	public static ImageProcessor apply(ImageProcessor input, double hMin, double hMax, double threshVal, double eroDilCount){
		final int width = input.getWidth();
		IJ.log("Width: " + width);
		final int height = input.getHeight();
		IJ.log("Height: " + height);
		
		double minVal = input.getMin();
		double maxVal = input.getMax();

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
		
		ArrayList<PixelsValues> pixelList = extractPixelValues(input, minVal, maxVal);

		IJ.showStatus("Sorting pixels by coloration value");
		IJ.log("Sorting pixels by coloration value");
		
		Collections.sort(pixelList);
		
		/*
		 * thresholding
		 */
		Threshold.threshold(pixelList, labelled, threshVal, foregroundLabel);
		
//		/*
//		 * DEBUG printing the labels after thresholding
//		 */
//		String currLine = "";
//		
//		for(int heightPr = 0; heightPr < labelled[0].length; heightPr++){
//			for(int widthPr = 0; widthPr < labelled.length; widthPr++){
//				currLine += " " + labelled[widthPr][heightPr].getLabel();
//			}
//			IJ.log(currLine);
//			currLine = "";
//		}
		
		/*
		 * eroding
		 */
		int eroDilCountInt = (int) eroDilCount;
		for(int i = 0; i<eroDilCount; i++){
			Erode.erode(labelled, backgroundLabel, foregroundLabel, width, height);
		}
		
		/*
		 * set the neighbours
		 */
		establishNeighbours(labelled, width, height);
		
		/*
		 * set the initial labels for the cell bodies
		 */
		initialCellBodyLabel(labelled, backgroundLabel, foregroundLabel, CONNEC);
		
		/*
		 * dilating
		 */
		for(int i = 0; i<eroDilCount; i++){
			Dilate.dilate(labelled, backgroundLabel, foregroundLabel, width, height);
		}
		
		/*
		 * taking the array of labels and turning it into an image for the user
		 */
		ShortProcessor sp = new ShortProcessor(width, height);
		for(int widthFP = 0; widthFP < width; widthFP++){
			for(int heightFP = 0; heightFP < height; heightFP++){
				sp.set(widthFP, heightFP, labelled[widthFP][heightFP].getCellBody());
			}
		}
		
		return sp;
		
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
				double h = input.getf(x, y);
				
				double range = hMax - hMin;
				
				double scaling = range/255;
				h = (h - hMin)/scaling;
				h = 255 - h;
				if(h >= 0 && h <= 255){
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
	public static void establishNeighbours(ThresholdDataPoint labelled[][], int width, int height){
		
		ArrayList<ThresholdDataPoint> neighEightTDP;
		ArrayList<ThresholdDataPoint> neighFourTDP;
		int xPos;
		int yPos;
		ArrayList<PixelPos> neighEightPix;
		ArrayList<PixelPos> neighFourPix;
		PixelPos pixelPos;
		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				pixelPos = new PixelPos(x, y);
				neighEightTDP = new ArrayList<ThresholdDataPoint>();
				neighEightPix = Neighbours.neighbours(pixelPos, width, height, 8);
				
				neighFourTDP = new ArrayList<ThresholdDataPoint>();
				neighFourPix = Neighbours.neighbours(pixelPos, width, height, 4);

				for(PixelPos neigh : neighEightPix){
					xPos = neigh.getX();
					yPos = neigh.getY();
					neighEightTDP.add(labelled[xPos][yPos]);
				}
				
				labelled[x][y].setNeighbours(neighEightTDP, 8);
				
				for(PixelPos neigh : neighFourPix){
					xPos = neigh.getX();
					yPos = neigh.getY();
					neighFourTDP.add(labelled[xPos][yPos]);
				}
				
				labelled[x][y].setNeighbours(neighFourTDP, 4);
			}
		}
	}
	
	/**
	 * after the cell bodies have been eroded to the point that they are seperate
	 * entities, they can be labelled as individual cells
	 * @param labelled the threshold data points that represent the image
	 * @param backgroundLabel the integer used for labelling background elements
	 * @param foregroundLabel the integer used for labelling foreground elements
	 * @param connec the connectedness to be used (4 or 8)
	 */
	public static void initialCellBodyLabel(ThresholdDataPoint[][] labelled, int backgroundLabel, int foregroundLabel, int connec){
		int currentNextLabel = 1;
		ArrayList<CellsToMerge> cells = new ArrayList<CellsToMerge>();
		cells.add(new CellsToMerge(0, false));
		
		for(ThresholdDataPoint[] row : labelled){
			for(ThresholdDataPoint element: row){
				/*if the pixel is part of a cell body*/
				if(element.getLabel() != backgroundLabel){
					/*get its neighbours label*/
					
					getNeighboursCellLabel(element, connec, cells);

					/*if it is zero then this should be treated as an as yet 
					 * unlabelled cell body*/
					if(element.getCellBody() == 0){
						element.setCellBody(currentNextLabel);
						cells.add(new CellsToMerge(currentNextLabel, false));
						currentNextLabel++;
					}
				}
			}
		}
//		IJ.log("Before merging problem bodies");
//		/*
//		 * DEBUG log the result
//		 */
//		String currLine = "";
//
//		for(int heightPr = 110; heightPr < 160; heightPr++){
//			for(int widthPr = 170; widthPr < 220; widthPr++){
//				currLine += " " + labelled[widthPr][heightPr].getCellBody();
//			}
//			IJ.log(currLine);
//			currLine = "";
//		}
		
		mergeCellBodies(labelled, cells);
		
//		IJ.log("After merging problem bodies");
//		/*
//		 * DEBUG log the result
//		 */
//		currLine = "";
//
//		for(int heightPr = 110; heightPr < 160; heightPr++){
//			for(int widthPr = 170; widthPr < 220; widthPr++){
//				currLine += " " + labelled[widthPr][heightPr].getCellBody();
//			}
//			IJ.log(currLine);
//			currLine = "";
//		}
	}
	
	/**
	 * takes in a Set of cells that need to merged together due to their connectedness
	 * having been discovered, and applies this change for everything in the set
	 * @param labelled the data points that have cell bodies that need to be merged
	 * @param cellsToMerge the set of data that indicates which cell bodies need to
	 * be merged
	 */
	public static void mergeCellBodies(ThresholdDataPoint[][] labelled, ArrayList<CellsToMerge> cells){
		for(CellsToMerge cell : cells){
			int initialCell = cell.getCellBody();
			
			CellsToMerge parentCell = cell;
			while(parentCell.getParent() != null){
				parentCell = parentCell.getParent();
			}
			
			int masterCell = parentCell.getCellBody();
			
			if(initialCell != masterCell){
				for(ThresholdDataPoint[] row : labelled){
					for(ThresholdDataPoint element: row){
						if(element.getCellBody() == initialCell){
							element.setCellBody(masterCell);
						}
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
	 * @param cells 
	 */
	public static void getNeighboursCellLabel(ThresholdDataPoint dataPoint, int connec, ArrayList<CellsToMerge> cells){
		int neighCellLabel = 0;
		ArrayList<ThresholdDataPoint> neigh = dataPoint.getNeighbours(connec);
		int currentNeighCellLabel;
		for(ThresholdDataPoint neighPixel : neigh){
			currentNeighCellLabel = neighPixel.getCellBody();
			/*if the current assumed label is still 0, it should be changed*/
			if(neighCellLabel == 0){
				neighCellLabel = currentNeighCellLabel;
			/*ensure that there are no conflicting neighbours (that aren't background)
			 * if there are then this indicates cell bodies that should be merged*/
			} else if(currentNeighCellLabel != 0 && neighCellLabel != currentNeighCellLabel){
				CellsToMerge lockedCell = cells.get(neighCellLabel).root();
//				System.out.println("locked cell no: " + cells.get(neighCellLabel).getCellBody() + 
//						" locked cell root: " + lockedCell.getCellBody());
				CellsToMerge recentCell = cells.get(currentNeighCellLabel).root();
//				System.out.println("recent cell no: " + cells.get(currentNeighCellLabel).getCellBody() + 
//						" recent cell root: " + recentCell.getCellBody());
				
				if(lockedCell.getCellBody() < recentCell.getCellBody()){
//					System.out.println("setting parent of: " + recentCell.getCellBody() + " to: " + lockedCell.getCellBody());
					recentCell.setParent(lockedCell);
				} else if(lockedCell.getCellBody() > recentCell.getCellBody()){
//					System.out.println("setting parent of: " + lockedCell.getCellBody() + " to: " + recentCell.getCellBody());
					lockedCell.setParent(recentCell);
				}
				
				dataPoint.setCellBody(neighCellLabel);
			}
		}
		dataPoint.setCellBody(neighCellLabel);
	}

}
