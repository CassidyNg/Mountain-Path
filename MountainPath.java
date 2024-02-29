/**
 * MountainPath class attempts to draw the path of least elevation change through a mountain range.
 * @author
 * @version
 */

import bridges.base.Color;
import bridges.base.ColorGrid;
import bridges.connect.Bridges;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// In the case of this project, we use the Scanner object in order to read an image file.

public class MountainPath {
    // These are static variables, meaning that only one instance of each exists in the class.
    // Notice that this class has only static methods.
    public static int elevDataNumOfRows = 0;
    public static int elevDataNumOfCols = 0;
    public static int elevDataMaxElevation = Integer.MIN_VALUE;
    public static int elevDataMinElevation = Integer.MAX_VALUE;

    public static void main(String[] args) throws Exception {
        // BRIDGES object initialization
        Bridges bridges = new Bridges(3, "cng", "1048575341021");

        // Set title of the visualization
        bridges.setTitle("Mountain Paths - The Greedy Algorithm");

        // elevationData is an int array that stores the elevation data from a .dat file.
        int[][] elevationData = new int[480][844];

        // startRow is the row in which the path starts. When startRow = 0, the path starts at the top of the map.
        int startRow = 0;

        /*
            IMPORTANT:
            Recall that the main method header includes main(String[] args)
            In the following code, we can finally see the purpose of this parameter!
            When the program is run, the user can input values to the String array called args.
            In this program, there are two possible numbers of arguments:
                1. Zero arguments, such that the default case is run.
                2. Two arguments: args[0], which is the file path, and args[1], which is the startRow.
            The way to input these values varies depending on your IDE. See Step 6.2.1
         */

        // Case #1, where no arguments are passed
        if (args.length == 0) {

            // Since this is the default case, elevationData should be set to the default file you wish to use in your project.
            // TO DO: Provide the correct file path operation (String) so that the  method can access the file.
            elevationData = readData("./Data Files/Colorado_844x480.dat");
            // elevationData = readData(filePath:"./Data Files/Colorado_480x480.dat");
            // elevationData = readData(filePath:"./Data Files/BHBLarea.dat")

            // startRow can be any value such that -1 < startRow < elevDataHeight. 100 is just an example value.
            startRow = 240;
        }

        // Case #2, where two parameters are passed: (String fileName, String startRow)
        else if (args.length == 2) {
            elevationData = readData(args[0]);
            startRow = Integer.parseInt(args[1]); // The row number was originally a String
        }

        // Accounts for if the startRow is negative or is greater than the elevDataNumOfRows
        if (startRow < 0 || startRow > elevDataNumOfRows - 1) {
            System.out.println("Bad starting row number, must be in the range 0 to " + (elevDataNumOfRows - 1));
            System.exit(-1); // This means leave the program.
        }

        // Generate the grayscale image of the elevation data
        ColorGrid grid = getImage(elevationData, elevDataNumOfCols, elevDataNumOfRows, elevDataMaxElevation, elevDataMinElevation);

        // Run the greedy algorithm given elevationData
        findPath(elevationData, elevDataNumOfCols, elevDataNumOfRows, startRow, grid);

        // Visualize
        bridges.setDataStructure(grid);
        bridges.visualize();
    }

    /**
     * The readData method reads the inputted file and stores the values in elevationData. It also updates the value of elevDataMaxElevation if necessary.
     *
     * @param filePath file path operation for the method to process
     * @return int array that contains elevation data
     */

    public static int[][] readData(String filePath) throws FileNotFoundException {
        // TO DO:  Fully comment this method

        /** 
         * Takes the String parameter and uses it as an input for the Scanner object to locate the file
         * It takes the nextInt(), which are the two numbers at the top of the file, and stores them as the number of columns and the number of rows 
         * After creating a 2D Array with those values, it loops through all the rows and columns of the file and stores them in the corresponding index in the array
         * While in the for loop, it constantly checks to see if the the current value is greater than or less than the max/min value to find the maximum and minimum elevation
         * Following the for loop, the method returns the 2D Array with the data from the specified file
        */

        File inputFile = new File(filePath);
        Scanner sc = new Scanner(inputFile);

        elevDataNumOfCols = sc.nextInt();
        elevDataNumOfRows = sc.nextInt();

        int[][] elevationData = new int[elevDataNumOfRows][elevDataNumOfCols];

        for (int i = 0; i < elevDataNumOfRows; i++) {
            for(int k = 0; k < elevDataNumOfCols; k++) {
                elevationData[i][k] = sc.nextInt();
                if (elevationData[i][k] > elevDataMaxElevation)
                    elevDataMaxElevation = elevationData[i][k];
                if (elevationData[i][k] < elevDataMinElevation)
                    elevDataMinElevation = elevationData[i][k];
            }
        }
        return elevationData;
    }

    /**
     * The getImage method generates a grayscale version of the inputted image.
     *
     * @param elevData the 2D int array which stores the values of the .dat file
     * @param numCols  the number of columns in the image
     * @param numRows  the number of rows in the image
     * @param maxVal   the max elevation inputted from the file
     * @param minVal   the min elevation inputted from the file
     * @return a ColorGrid object
     */

    public static ColorGrid getImage(int[][] elevData, int numCols, int numRows, int maxVal, int minVal) {
        // TO DO:  Fully comment this method

        /**
         * Using the numRows and numCols parameters, it creates a new ColorGrid with the correct number of pixels to form the image
         * As it loops through the 2D Array elevData, it finds the elevation at each pixel and calculates its value on the grayScale
         * To calculate the value, it finds the difference between the current elevation and the minimum elevation, multiplying it by 255 (the max value an RGB can have)
         * It divides the calculated value by the difference between the max and min value as a comparison, determining whether the elevation is
         * on the higher or lower end
         * With the grayScale value, it sets each pixel to a color consisting of the same value for red, green, and blue, since the same value for all colors means its gray
         * After setting each pixel to a shade of gray, depending on its elevation, the method returns the ColorGrid object
         */

        ColorGrid grid = new ColorGrid(numRows, numCols);

        float pixelVal;
        int grayScaleVal;

        for (int i = 0; i < numRows; i++) {
            for (int k = 0; k < numCols; k++) {
                pixelVal = (float) elevData[i][k];
                grayScaleVal = (int) ((pixelVal - minVal) * 255.0f / (maxVal - minVal));
                grid.set(i, k, new Color(grayScaleVal, grayScaleVal, grayScaleVal));
            }
        }

        return grid;
    }

    /**
     * Method findPath uses a "greedy algorithm" in order to find an optimal path from west to east
     * based on terrain elevation data
     *
     * @param elevData the 2D int array which stores the values of the .dat file
     * @param numRows  the number of rows in the image
     * @param numCols  the number of columns in the image
     * @param startRow the row where path begins
     * @param grid a ColorGrid object
     */

    public static void findPath(int[][] elevData, int numCols, int numRows, int startRow, ColorGrid grid) {
        // TO DO: Write this method.
        int row = startRow;
        int col = 0;

        while(row < numRows && col < numCols - 1){
            float currElev = elevData[row][col];
            float up = 0;
            float forward = elevData[row][col + 1];
            float down = 0;
            if(row != 0){
                up = elevData[row - 1][col + 1];
            }
            if(row != numRows - 1){
                down = elevData[row + 1][col + 1];
            }
            if(up != 0 && down != 0){
                if((Math.abs(forward - currElev) <= Math.abs(up - currElev)) && (Math.abs(forward - currElev) <= Math.abs(down - currElev))){
                    col++;
                }else if((Math.abs(up - currElev) < Math.abs(forward - currElev)) && (Math.abs(up - currElev) < Math.abs(down - currElev))){
                    row--;
                    col++;
                }else if((Math.abs(down - currElev) < Math.abs(forward - currElev)) && (Math.abs(down - currElev) < Math.abs(up - currElev))){
                    row++;
                    col++;
                }else{
                    int rand = (int)(Math.random() * 2);
                    if(rand == 0){
                        row--;
                        col++;
                    }else{
                        row++;
                        col++;
                    }
                }
            }else if(up != 0){
                if(Math.abs(forward - currElev) <= Math.abs(up - currElev)){
                    col++;
                }else{
                    row--;
                    col++;
                }
            }else if(down != 0){
                if(Math.abs(forward - currElev) <= Math.abs(down - currElev)){
                    col++;
                }else{
                    row++;
                    col++;
                }
            }
            grid.set(row, col, new Color("Red"));
        }
    }
}