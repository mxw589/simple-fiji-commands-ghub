package watershed;

import java.util.ArrayList;

import dataTypes.PixelPos;

/**
 * a class that contains a static method for the calculation of the
 * neighbouring pixels for a given pixel reference
 * @author Mark
 *
 */
public class Neighbours {

	/**
	 * a static method that returns an Arraylist of potential neighbours
	 * (as PixelPos objects) for a given pixel reference
	 * @param x the x co-ordinate of the pixel
	 * @param y the y co-ordinate of the pixel
	 * @param width the total width of the image
	 * @param height the total height of the image
	 * @return an Arraylist of potential neighbours (as PixelPos objects) 
	 * for the given pixel reference
	 */
	public static ArrayList<PixelPos> neighbours(PixelPos pixelPos, int width, int height, int connec){
		int x = pixelPos.getX();
		int y = pixelPos.getY();
		
		ArrayList<PixelPos> neighbourList = new ArrayList<PixelPos>();
		
		int[] xPotential;
		int[] yPotential;
		
		PixelPos[] neighPotential;
		
		if(connec == 8){
			xPotential = new int[8];
			yPotential = new int[8];

			xPotential[0] = x-1;
			xPotential[1] = x;
			xPotential[2] = x+1;
			xPotential[3] = x+1;
			xPotential[4] = x+1;
			xPotential[5] = x;
			xPotential[6] = x-1;
			xPotential[7] = x-1;

			yPotential[0] = y+1;
			yPotential[1] = y+1;
			yPotential[2] = y+1;
			yPotential[3] = y;
			yPotential[4] = y-1;
			yPotential[5] = y-1;
			yPotential[6] = y-1;
			yPotential[7] = y;

			neighPotential = new PixelPos[8];

		} else if(connec == 4) {
			xPotential = new int[4];
			yPotential = new int[4];
			
			xPotential[0] = x-1;
			xPotential[1] = x;
			xPotential[2] = x+1;
			xPotential[3] = x;
			
			yPotential[0] = y;
			yPotential[1] = y+1;
			yPotential[2] = y;
			yPotential[3] = y-1;
			
			neighPotential = new PixelPos[4];
		} else {
			throw new IllegalArgumentException("Invalid number of neighbours!");
		}
		
		for(int neigh = 0; neigh < neighPotential.length; neigh++){
			neighPotential[neigh] = new PixelPos(xPotential[neigh], yPotential[neigh]);
		}
		
		for(PixelPos p : neighPotential){
			if(isValid(p, width, height)){
				neighbourList.add(p);
			}
		}
		
		return neighbourList;
	}
	
	/**
	 * method that checks if a PixelPos is inside the valid bound of the region
	 * for an image of a certain height and width
	 * @param position the PixelPos of the pixel
	 * @param width the width of the image
	 * @param height the height of the image
	 * @return result of test
	 */
	public static boolean isValid(PixelPos position, int width, int height){
		int xPos = position.getX();
		int yPos = position.getY();
		
		if(xPos >= 0 && xPos < width){
			if(yPos >= 0 && yPos < height){
				return true;
			}
		}
		
		return false;
	}
}
