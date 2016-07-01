package watershed;

import java.util.ArrayList;
import java.util.Arrays;

import dataTypes.PixelPos;
import ij.IJ;

public class Dilate {

	public static void dilate(int[][] labelled, int backgroundLabel, int foregroundLabel, int width, int height){
		IJ.showStatus("Dilating");
		IJ.log("Dilating");
		long start = System.currentTimeMillis();
		int[][] newLabels = new int[width][height];
		
		for( int i=0; i<width; i++ ){
			Arrays.fill(newLabels[i], foregroundLabel);
		}
		
		boolean neighbourCheck;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				/* check to see if the label may need to be changed */
				if(labelled[x][y] == backgroundLabel){
					/*generate an arraylist of valid neighbours for the point*/
					ArrayList<PixelPos> neighbours = Neighbours.neighbours(x, y, width, height);
					/*check if the neighbours mean the point should be eroded*/
					neighbourCheck = false;
					for(PixelPos p : neighbours){
						int neighX = p.getX();
						int neighY = p.getY();
						if(labelled[neighX][neighY] == foregroundLabel){
							neighbourCheck = true;
						}
					}
					if(neighbourCheck == false){
						newLabels[x][y] = backgroundLabel;
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
