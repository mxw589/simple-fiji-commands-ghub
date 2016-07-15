package watershed;

import java.util.ArrayList;
import java.util.Arrays;

import dataTypes.PixelPos;
import dataTypes.ThresholdDataPoint;
import ij.IJ;

/**
 * A class that holds the static methods for performing erosion on a binary matrix
 * of integers
 * @author Mark
 *
 */
public class Erode {

	public static void erode(ThresholdDataPoint[][] labelled, int backgroundLabel, int foregroundLabel, int width, int height){
		IJ.showStatus("Eroding");
		IJ.log("Eroding");
		long start = System.currentTimeMillis();
		ThresholdDataPoint[][] newLabels = new ThresholdDataPoint[width][height];

		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				PixelPos pixelPos = new PixelPos(x,y);
				newLabels[x][y] = new ThresholdDataPoint(backgroundLabel, pixelPos);
			}
		}

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				/* check to see if the label may need to be changed */
				if(labelled[x][y].getLabel() == foregroundLabel){
					PixelPos pixelPos = new PixelPos(x,y);
					/*generate an arraylist of valid neighbours for the point*/
					ArrayList<PixelPos> neighbours = Neighbours.neighbours(pixelPos, width, height, 8);
					/*check if the neighbours mean the point should be eroded*/
					boolean neighbourCheck = true;
					for(PixelPos p : neighbours){
						int neighX = p.getX();
						int neighY = p.getY();
						int currentNeighLabel = labelled[neighX][neighY].getLabel();
						if(currentNeighLabel == backgroundLabel){
							neighbourCheck = false;
						}
					}
					if(neighbourCheck){
						newLabels[x][y].setLabel(foregroundLabel);
					}
				}
			}
		}

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				labelled[x][y] = newLabels[x][y];
			}
		}

		Watershed.establishNeighbours(labelled, width, height);
		
		long end = System.currentTimeMillis();
		IJ.log("Eroding took " + (end-start) + " ms.");

		/*
		 * DEBUG log the result
		 */
		String currLine = "";

		for(int heightPr = 90; heightPr < 160; heightPr++){
			for(int widthPr = 170; widthPr < 220; widthPr++){
				currLine += " " + labelled[widthPr][heightPr].getLabel();
			}
			IJ.log(currLine);
			currLine = "";
		}
	}
}
