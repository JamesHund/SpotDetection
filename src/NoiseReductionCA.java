import java.awt.image.BufferedImage;

/**
 * This class provides an abstraction for a computational automata that performs
 * noise reduction on a greyscale image.
 */
public class NoiseReductionCA {

  private final int width, height;
  private final BufferedImage noiseReduced;

  /**
   * This is the constructor for a NoiseReductionCA. It creates a new image with
   * the same dimensions as the input image and performs all necessary
   * operations required to reduce noise in the image.
   *
   * @param image A greyscale image to be noise-reduced.
   */
  public NoiseReductionCA(BufferedImage image) {
    noiseReduced =
        new BufferedImage(image.getWidth(), image.getHeight(),
            BufferedImage.TYPE_INT_RGB);
    width = image.getWidth();
    height = image.getHeight();
    copyBorderPixels(image);
    reduce(image);
  }

  /**
   * This method copies the border pixels from an input image to the new
   * noiseReduced image.
   *
   * @param original The input image.
   */
  private void copyBorderPixels(BufferedImage original) {
    // top row
    for (int x = 0; x < width; x++) {
      noiseReduced.setRGB(x, 0, original.getRGB(x, 0));
    }
    // bottom row
    for (int x = 0; x < width; x++) {
      noiseReduced.setRGB(x, height - 1, original.getRGB(x, height - 1));
    }
    // left column
    for (int y = 0; y < height; y++) {
      noiseReduced.setRGB(0, y, original.getRGB(0, y));
    }
    // right column
    for (int y = 0; y < height; y++) {
      noiseReduced.setRGB(width - 1, y, original.getRGB(width - 1, y));
    }
  }

  /**
   * This method takes an input image, and iterates through each pixel with a
   * defined Von Neumann neighbourhood (eg. non-border pixels) and applies the
   * relevant noise reduction CA rules. The result of which is stored in
   * noiseReduced.
   *
   * @param original The input image.
   */
  private void reduce(BufferedImage original) {
    for (int y = 1; y < height - 1; y++) {
      for (int x = 1; x < width - 1; x++) {
        int n = Coordinate.NEIGHBOUR_OFFSETS.length;
        int[] rgbValues = new int[n];
        for (int i = 0; i < n; i++) {
          Coordinate neighbour = (new Coordinate(x, y))
              .add(Coordinate.NEIGHBOUR_OFFSETS[i]);
          rgbValues[i] = original.getRGB(neighbour.getX(), neighbour.getY());
        }
        int newRGB = determineRGB(rgbValues);
        noiseReduced.setRGB(x, y, newRGB);
      }
    }
  }

  /**
   * This function is the inner loop of the noise reduction algorithm. It takes
   * an array of RGB values, the first of which is the value of the central
   * pixel. It then determines the new RGB value based on the method outlined in
   * "Cellular Automata in Image Processing" by Popovici.
   *
   * @param rgbValues The array of RGB values (rgbValues[0] is the center).
   * @return Return's the new RGB value.
   */
  private int determineRGB(int[] rgbValues) {
    int n = rgbValues.length;
    int[] uniqueValues = new int[n];
    int[] frequencies = new int[n];
    int count = 0;

    // create frequency table of pixel neighbourhood values
    for (int rgbValue : rgbValues) {
      boolean exists = false;
      // iterate through existing rgb values
      for (int j = 0; j < count; j++) {
        if (rgbValue == uniqueValues[j]) {
          exists = true;
          frequencies[j]++;
        }
      }
      // if it does not exist
      if (!exists) {
        uniqueValues[count] = rgbValue;
        frequencies[count] = 1;
        count++;
      }
    }
    // determine using histogram the final rgb value
    int maxFrequency = 0;
    int maxFrequencyRGB = -1;
    for (int i = 0; i < count; i++) {
      if (frequencies[i] > maxFrequency) {
        maxFrequency = frequencies[i];
        maxFrequencyRGB = uniqueValues[i];
      } else if (frequencies[i] == maxFrequency) {
        if (uniqueValues[i] == rgbValues[0]) {
          maxFrequencyRGB = uniqueValues[i];
        }
      }
    }
    return maxFrequencyRGB;
  }

  /**
   * This method returns the final noise reduced image.
   *
   * @return Returns the noise-reduced image.
   */
  public BufferedImage getReduced() {
    return noiseReduced;
  }
}
