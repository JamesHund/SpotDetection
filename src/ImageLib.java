import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * ImageLib provides a library of functions that perform various image
 * manipulations. All the functions build on top of one another and all perform
 * various intermediate steps in the spot detection process.
 */
public class ImageLib {

  // image type used for BufferedImage objects
  public static final int IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;
  // suffixes for output image files
  public static final String[] IMAGE_SUFFIXES = {"_GS", "_NR", "_ED", "_SD"};
  // constants used to convert an image to greyscale
  private static final double RED_WEIGHT = 0.299;
  private static final double GREEN_WEIGHT = 0.587;
  private static final double BLUE_WEIGHT = 0.114;

  /**
   * This method takes in a BufferedImage and returns a new greyscale version of
   * that image. The pixel values are calculated using a weighted average of the
   * red, green and blue components of the original pixel. (It does not alter
   * the input image)
   *
   * @param image The image to be manipulated.
   * @return Returns a new greyscale image.
   */
  public static BufferedImage greyscale(BufferedImage image) {

    BufferedImage greyScale = new BufferedImage(image.getWidth(),
        image.getHeight(), IMAGE_TYPE);

    for (int y = 0; y < greyScale.getHeight(); y++) {
      for (int x = 0; x < greyScale.getWidth(); x++) {
        int rgb = image.getRGB(x, y);
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = (rgb) & 0xff;
        int weightedAvg =
            (int)
                ((double) red * RED_WEIGHT
                    + (double) green * GREEN_WEIGHT
                    + (double) blue * BLUE_WEIGHT);
        int adjustedRGB = (255 << 24) + (weightedAvg << 16) + (weightedAvg << 8)
            + weightedAvg;
        greyScale.setRGB(x, y, adjustedRGB);
      }
    }
    return greyScale;
  }

  /**
   * This function takes in a BufferedImage and returns a new greyscale and
   * noise-reduced version of that image. It uses a NoiseReductionCA to perform
   * the noise reduction. (It does not alter the input image)
   *
   * @param image The image to be manipulated.
   * @return Returns a noise-reduced and greyscale image.
   */
  public static BufferedImage reduceNoise(BufferedImage image) {
    BufferedImage greyscaleImage = greyscale(image);
    NoiseReductionCA nr = new NoiseReductionCA(greyscaleImage);
    return nr.getReduced();
  }

  /**
   * This function takes in a BufferedImage and returns a black and white image
   * which has the edges highlighted in white. It uses an EdgeDetectionCA to
   * perform this operation. The image first has noise reduction applied to it.
   * (It does not alter the input image)
   *
   * @param image   The image to be manipulated.
   * @param epsilon A value between 0 and 255 that provides a threshold value
   *                for the EdgeDetectionCA
   * @return Returns a black and white image with edges highlighted.
   */
  public static BufferedImage detectEdges(BufferedImage image, int epsilon) {
    BufferedImage noiseReduced = reduceNoise(image);
    EdgeDetectionCA ed = new EdgeDetectionCA(noiseReduced, epsilon);
    return ed.getEdgeDetected();
  }

  /**
   * This function takes in BufferedImage and returns a SpotDetector object. The
   * image first has edge detection applied and then is used to create the
   * SpotDetector.
   *
   * @param image      The image to be manipulated.
   * @param epsilon    A value between 0 and 255 that provides a threshold value
   *                   for the EdgeDetectionCA.
   * @param lowerBound The minimum radius of a spot used for the spot-detection
   *                   algorithm in SpotDetector.
   * @param upperBound The maximum radius of a spot used for the spot-detection
   *                   algorithm in SpotDetector.
   * @return Returns a new SpotDetector instance.
   */
  public static SpotDetector detectSpots(BufferedImage image, int epsilon,
      int lowerBound, int upperBound) {
    BufferedImage edgeDetected = detectEdges(image, epsilon);
    SpotDetector sd = new SpotDetector(edgeDetected, lowerBound,
        upperBound);
    return sd;
  }

  /**
   * This function takes in a File and converts it to a BufferedImage.
   *
   * @param file The file to be converted.
   * @return Returns a BufferedImage if file points to a valid image, otherwise
   * it returns null.
   */
  public static BufferedImage readImageFromFile(File file) {
    BufferedImage image;
    try {
      image = ImageIO.read(file);
    } catch (IOException e) {
      return null;
    }
    return image;
  }

  /**
   * This function saves an image to a specified location. It uses the original
   * file path to extract the name of the image and appends to it a suffix
   * specified by the image-stage. The saved image is in PNG format.
   *
   * @param image      The image to be saved.
   * @param filepath   The filepath of the original unmanipulated image.
   * @param outputPath The location the image is to be saved to.
   * @param imageStage The processing stage of the image.
   */
  public static void saveImage(
      BufferedImage image,
      String filepath,
      String outputPath,
      ImageStage imageStage) {

    String fileName = outputPath + "/";

    int lastIndexForwardSlash = filepath.lastIndexOf('/');
    int startIndex = 0;
    if (lastIndexForwardSlash != -1) {
      startIndex = lastIndexForwardSlash;
    }
    int endIndex = filepath.lastIndexOf('.');

    fileName += filepath.substring(startIndex, endIndex);
    fileName += IMAGE_SUFFIXES[imageStage.ordinal()] + ".png";

    File grayScaleFile = new File(fileName);
    try {
      boolean fileCreated = grayScaleFile.createNewFile();
    } catch (IOException e) {
    }
    try {
      ImageIO.write(image, "PNG", grayScaleFile);
    } catch (IOException e) {
    }
  }

  /**
   * The values of ImageStage represent the various stages of image processing,
   * for convenient use in method inputs. They correspond with the entries in
   * IMAGE_SUFFIXES.
   */
  public enum ImageStage {
    GREYSCALE,
    NOISE_REDUCED,
    EDGE_DETECTED,
    SPOT_DETECTED
  }
}
