package watershed;

import java.util.ArrayList;
import java.util.Arrays;

import dataTypes.PixelPos;
import ij.IJ;

/**
 * A class that holds the static methods for performing erosion on a binary matrix
 * of integers
 * @author Mark
 *
 */
public class Erode {

	public static void erode(int[][] labelled, int backgroundLabel, int foregroundLabel, int width, int height){
		IJ.showStatus("Eroding");
		IJ.log("Eroding");
		long start = System.currentTimeMillis();
		int[][] newLabels = new int[width][height];

		for( int i=0; i<width; i++ ){
			Arrays.fill(newLabels[i], backgroundLabel);
		}

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				/* check to see if the label may need to be changed */
				if(labelled[x][y] == foregroundLabel){
					/*generate an arraylist of valid neighbours for the point*/
					ArrayList<PixelPos> neighbours = Neighbours.neighbours(x, y, width, height);
					/*check if the neighbours mean the point should be eroded*/
					boolean neighbourCheck = true;
					for(PixelPos p : neighbours){
						int neighX = p.getX();
						int neighY = p.getY();
						int currentNeighLabel = labelled[neighX][neighY];
						if(currentNeighLabel == backgroundLabel){
							neighbourCheck = false;
						}
					}
					if(neighbourCheck){
						newLabels[x][y] = foregroundLabel;
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
		IJ.log("Eroding took " + (end-start) + " ms.");

		/*
		 * DEBUG log the result
		 */
//		String currLine = "";
//
//		for(int heightPr = 0; heightPr < height; heightPr++){
//			for(int widthPr = 0; widthPr < width; widthPr++){
//				currLine += " " + labelled[widthPr][heightPr];
//			}
//			IJ.log(currLine);
//			currLine = "";
//		}
	}
}
