import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is primarily a client of ImageLib. It's intended purpose is to
 * handle the input from a commandline interface (including error checking). The
 * main method takes in an image path and runs the image through various stages
 * of the spot detection process (depending on the mode), and saves it to the
 * "out" folder in the project directory. Mode 0: Converts the image to
 * greyscale. Mode 1: Mode 0 + Image Noise Reduction. Mode 2: Mode 1 + Edge
 * Detection. Mode 3: Mode 2 + Spot Detection. If mode 3 is chosen the spot
 * detection process is completed and the number of detected spots is printed to
 * StdOut.
 */
public class Animal {

  /**
   * This is the main method which takes in args, checks for errors in the
   * input, calls the relevant image processing method from ImageLib, and saves
   * the resulting image.
   *
   * @param args Command-line arguments: args[0] is the mode args[1] is the file
   *             location args[2] is the epsilon value for modes 2 and 3 args[3]
   *             and args[4] are the lower and upper bounds for mode 3
   */
  public static void main(String[] args) {

    if (!validateArgsLength(args)) {
      System.err.println("ERROR: invalid number of arguments");
      return;
    }
    if (!validateArgsType(args)) {
      System.err.println("ERROR: invalid argument type");
      return;
    }

    int mode = Integer.parseInt(args[0]);
    if (!validateMode(mode)) {
      System.err.println("ERROR: invalid mode");
      return;
    }

    int epsilon = -1;
    if (mode == 2 || mode == 3) {
      epsilon = Integer.parseInt(args[2]);
      if (!validateEpsilon(epsilon)) {
        System.err.println("ERROR: invalid epsilon");
        return;
      }
    }

    String filePath = args[1];
    if (!validateFilePath(filePath)) {
      System.err.println("ERROR: invalid or missing file");
      return;
    }

    BufferedImage image = ImageLib.readImageFromFile(new File(filePath));
    if (image == null) {
      System.err.println("ERROR: invalid or missing file");
      return;
    }

    ImageLib.ImageStage stage;
    switch (mode) {
      case 0:
        image = ImageLib.greyscale(image);
        stage = ImageLib.ImageStage.GREYSCALE;
        break;
      case 1:
        image = ImageLib.reduceNoise(image);
        stage = ImageLib.ImageStage.NOISE_REDUCED;
        break;
      case 2:
        image = ImageLib.detectEdges(image, epsilon);
        stage = ImageLib.ImageStage.EDGE_DETECTED;
        break;
      case 3:
        int lowerBound = Integer.parseInt(args[3]);
        int upperBound = Integer.parseInt(args[4]);
        SpotDetector sd = ImageLib.detectSpots(image, epsilon, lowerBound,
            upperBound);
        image = sd.getSpotMap();
        stage = ImageLib.ImageStage.SPOT_DETECTED;
        int spotcount = sd.getSpotCount();
        System.out.println(spotcount);
        //writeSpotsToFile(filePath,"../out",spotcount);
        //debugging-------------
//        BufferedImage debugImage = sd.renderSpotCoordinates();
//        ImageLib.saveImage(debugImage, "debug.png", "../out", stage);
        //---------------------
        break;
      case 4:
        GUI gui = new GUI("Spot Detector", filePath);
        return;
      default:
        System.err.println("The code should not reach this point :)");
        return;
    }

    ImageLib.saveImage(image, filePath, "../out", stage);
  }

  /**
   * This method verifies that the number of commandline arguments are correct
   * given the mode (args[0]).
   *
   * @param args The commandline arguments.
   * @return boolean Returns true if number of arguments is correct, false
   * otherwise.
   */
  private static boolean validateArgsLength(String[] args) {
    int n = args.length;
    if (n == 0 || n == 1) {
      return false;
    }
    int appropriateLength;
    switch (args[0]) {
      case "0":
      case "1":
      case "4":
        appropriateLength = 2;
        break;
      case "2":
        appropriateLength = 3;
        break;
      case "3":
        appropriateLength = 5;
        break;
      default:
        appropriateLength = n;
    }
    return n == appropriateLength;
  }

  /**
   * This method verifies whether the supplied commandline arguments are of the
   * correct type.
   *
   * @param args The commandline arguments.
   * @return boolean Returns true if all arguments of valid type, false
   * otherwise
   */
  private static boolean validateArgsType(String[] args) {
    // validate mode type
    int mode;
    try {
      mode = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      return false;
    }

    // if mode = 2, validate epsilon type
    if (mode == 2 || mode == 3) {
      try {
        Integer.parseInt(args[2]);
      } catch (NumberFormatException e) {
        return false;
      }
    }
    // if mode = 3, validate other argument types
    if (mode == 3) {
      try {
        Integer.parseInt(args[3]);
        Integer.parseInt(args[4]);
      } catch (NumberFormatException e) {
        return false;
      }
    }
    return true;
  }

  /**
   * This method validates whether the mode is a valid mode (0,1,2 or 3).
   *
   * @param mode The mode.
   * @return boolean Returns true if mode is valid, false otherwise.
   */
  private static boolean validateMode(int mode) {
    switch (mode) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
        return true;
      default:
        return false;
    }
  }

  /**
   * This method checks whether the supplied epsilon value lies within the range
   * [0,255].
   *
   * @param epsilon An integer value.
   * @return boolean Returns true if epsilon is within the correct range, false
   * otherwise.
   */
  private static boolean validateEpsilon(int epsilon) {
    return epsilon >= 0 && epsilon <= 255;
  }

  /**
   * This method validates whether the file located at a file path exists. (not
   * whether the file is a valid image file)
   *
   * @param filePath The file path.
   * @return boolean Returns true if the file exists, false otherwise.
   */
  //
  private static boolean validateFilePath(String filePath) {
    File file = new File(filePath);
    return file.isFile();
  }

  /**
   * Debug method. Writes the number of spots to file.
   *
   * @param filepath   Filepath.
   * @param outputPath Output path.
   * @param spots      Number of spots.
   */
  private static void writeSpotsToFile(String filepath,
      String outputPath, int spots) {
    String fileName = outputPath + "/";

    int lastIndexForwardSlash = filepath.lastIndexOf('/');
    int startIndex = 0;
    if (lastIndexForwardSlash != -1) {
      startIndex = lastIndexForwardSlash;
    }
    int endIndex = filepath.lastIndexOf('.');

    fileName += filepath.substring(startIndex, endIndex);

    fileName += ".out";
    File file = new File(fileName);
    try {
      boolean fileCreated = file.createNewFile();
    } catch (IOException e) {
    }
    try {
      FileWriter fileWriter = new FileWriter(file);
      fileWriter.write("" + spots);
      fileWriter.close();
    } catch (IOException e) {
    }
  }
}
