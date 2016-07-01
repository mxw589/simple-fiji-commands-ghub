import java.awt.Scrollbar;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import watershed.Watershed;

/**
 * First attempt at creating a working plugin, aimed at performing a basic
 * watershedding operation
 */
public class WaterShedPlugin implements PlugIn {

	
	/*
	 * 8 bit images are being processed so the bounds for the thresholding are decided here
	 */
	public static double hMin = 0;
	public static double hMax = 255;
	
	/**
	 * Run method that offers the user the choice of currently open images to run
	 * the process on, and allows the to chose an initial threshold value using
	 * a slider
	 * @param arg required, not used
	 */
	public void run(String arg) {
		
		/*
		 * count images to be processed
		 */
		int imgCount = WindowManager.getImageCount();
		
		/*
		 * error if there are no images to be used
		 */
		if( imgCount == 0 ){
			IJ.error( "WaterShed Test", 
					"ERROR: At least one image needs to be open to run a watershed.");
			return;
		}
		
		/*
		 * save image names to an array 
		 */
		String[] imgNames = new String[imgCount];
		
		for(int i = 0; i < imgCount; i++){
			imgNames[i] = WindowManager.getImage(i + 1).getShortTitle();
		}
		
		/*
		 * create a dialog box for initiating the watershed operation
		 */
		GenericDialog gd = new GenericDialog("Watershed");
		
		gd.addChoice("Input", imgNames, imgNames[0]);
		gd.addSlider("Threshold value", hMin, hMax, 126);
		
		gd.showDialog();
		
		/*
		 * retrieves the values selected in the dialog and passes them to the
		 * process method for use
		 */
		if(gd.wasOKed()){
			ImagePlus chosenImg = WindowManager.getImage(gd.getNextChoiceIndex()+1);
			Scrollbar threshScroll = (Scrollbar) gd.getSliders().get(0);
			double threshVal = 255 - threshScroll.getValue();
			
			ImagePlus result = process(chosenImg, hMin, hMax, threshVal);
			
			result.show();
		}
		
	}

	/**
	 * method that passes off the users image for watershedding
	 * @param chosenImg the chosen image
	 * @param hMin the minimum value of the pixels in the image
	 * @param hMax the maximum value of the pixels in the image
	 * @param threshVal the chosen thresholding value
	 * @return the resultant image
	 */
	private ImagePlus process(ImagePlus chosenImg, double hMin, double hMax, double threshVal) {
		final long start = System.currentTimeMillis();
		
		ImagePlus resultImg = Watershed.computeWatershed(chosenImg, hMin, hMax, threshVal);
		final long end = System.currentTimeMillis();
		IJ.log("Watershedding took " + (end-start) + " ms.");
		
		return resultImg;
	}
	
	public static void main(String[] args) {
		new ImageJ();
	    ImagePlus image1 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/large_dots.bmp");
	    image1.show();
	    ImagePlus image2 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/dots.bmp");
	    image2.show();
	    ImagePlus image3 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/dots_gradient.bmp");
	    image3.show();
	    ImagePlus image4 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/gradient.bmp");
	    image4.show();
	    ImagePlus image5 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/larger_test.bmp");
	    image5.show();
	    ImagePlus image6 = IJ.openImage("/Users/Mark/Documents/Project/Test_Images/BMP/tiny.bmp");
	    image6.show();
	    IJ.runPlugIn("WaterShedPlugin", "");
	}
	
}