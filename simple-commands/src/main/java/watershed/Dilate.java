package watershed;

import java.util.ArrayList;
import java.util.Arrays;

import dataTypes.PixelPos;
import dataTypes.ThresholdDataPoint;
import ij.IJ;

/**
 * a class that performs dilation of an image. This establishes and maintains cellbody
 * labelling (a description for which can be found in the ThresholdDataPoint class)
 * throughout dilation.
 * 
 * The dilation does not allow two uniquely labelled cell bodies to dilate into the same
 * area.
 * 
 * @author Mark
 *
 */
public class Dilate {

	public static void dilate(ThresholdDataPoint[][] labelled, int backgroundLabel, int foregroundLabel, int width, int height){
		IJ.showStatus("Dilating");
		IJ.log("Dilating");
		long start = System.currentTimeMillis();
		
		ThresholdDataPoint[][] newLabels = new ThresholdDataPoint[width][height];
		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				PixelPos pixelPos = new PixelPos(x,y);
				newLabels[x][y] = new ThresholdDataPoint(foregroundLabel, pixelPos, labelled[x][y].getCellBody(), labelled[x][y].getNeighbours());
			}
		}
		
		/*establish a matrix that will help to decide if an area should be expanded into
		 * if the current value of this matrix is 0 in the area that wishes to be
		 * expanded into, go ahead. If it is the same number as the cell body label
		 * of the expanding pixel, then go ahead. If the current value is different to
		 * the value of the cell body of the expanding pixel, then this pixel that is
		 * being expanded into should be marked as background (for this whole iteration)*/
		int[][] currCellBod = new int[width][height];
		
		for(int x = 0; x < width; x++){
			Arrays.fill(currCellBod[x], 0);
		}
		
		
		boolean neighbourCheck;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				/* check to see if the label may need to be changed */
				if(labelled[x][y].getLabel() == backgroundLabel){
					PixelPos pixelPos = new PixelPos(x,y);
					/*generate an arraylist of valid neighbours for the point*/
					ArrayList<PixelPos> neighbours = Neighbours.neighbours(pixelPos, width, height);
					/*check if the neighbours mean the point should be eroded*/
					neighbourCheck = false;
					for(PixelPos p : neighbours){
						int neighX = p.getX();
						int neighY = p.getY();
						if(labelled[neighX][neighY].getLabel() == foregroundLabel){
							if(currCellBod[neighX][neighY] != 0 && currCellBod[neighX][neighY] != labelled[neighX][neighY].getCellBody()){
								neighbourCheck = false;
								break;
							}
							currCellBod[neighX][neighY] = newLabels[x][y].getCellBody();
							neighbourCheck = true;
						}
					}
					if(neighbourCheck == false){
						newLabels[x][y].setLabel(backgroundLabel);
					}
				}
			}
		}
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				labelled[x][y] = newLabels[x][y];
			}
		}
		
		long end = System.currentTimeMillis();
		IJ.log("Dilating took " + (end-start) + " ms.");
		
		
		/*
		 * DEBUG log the result
		 */
		String currLine = "";
		
		for(int heightPr = 0; heightPr < height; heightPr++){
			for(int widthPr = 0; widthPr < width; widthPr++){
				currLine += " " + labelled[widthPr][heightPr].getCellBody();
			}
			IJ.log(currLine);
			currLine = "";
		}
	}	
}
