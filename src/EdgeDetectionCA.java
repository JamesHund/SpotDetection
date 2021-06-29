import java.awt.image.BufferedImage;

/**
 * This class provides an abstraction for a computational automata that performs
 * edge detection on a greyscale image.
 */
public class EdgeDetectionCA {

  private static final int BLACK = 0;
  private static final int WHITE = 0xFFFFFF;

  private final BufferedImage edgeDetected;
  private final int epsilon, width, height;

  /**
   * This is the constructor for a EdgeDetectionCA. It creates a new image with
   * the same dimensions as the input image and performs all necessary
   * operations required to detect edges in the image.
   *
   * @param image   A greyscale image to be edge-detected.
   * @param epsilon A threshold value for edge detection.
   */
  public EdgeDetectionCA(BufferedImage image, int epsilon) {
    this.epsilon = epsilon;
    edgeDetected =
        new BufferedImage(image.getWidth(), image.getHeight(),
            BufferedImage.TYPE_INT_RGB);
    width = edgeDetected.getWidth();
    height = edgeDetected.getHeight();
    detectEdges(image);
  }

  /**
   * This method takes an input image, and iterates through each pixel of an
   * input image and applies the relevant edge detection CA rules. The result of
   * this is stored in edgeDetected. The von Neumann neighbourhood for each
   * pixel need not necessarily be defined. For border pixels and corner pixels,
   * a reduced von Neumann neighbourhood is used.
   *
   * @param original The input image.
   */
  private void detectEdges(BufferedImage original) {

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int n = Coordinate.NEIGHBOUR_OFFSETS.length;
        int[] rgbValues = new int[n];
        int size = 0;
        for (Coordinate neighbourOffset : Coordinate.NEIGHBOUR_OFFSETS) {
          Coordinate neighbour = (new Coordinate(x, y)).add(neighbourOffset);
          if (withinImageBounds(neighbour)) {
            rgbValues[size++] = original
                .getRGB(neighbour.getX(), neighbour.getY());
          }
        }
        int newRGB = determineRGB(rgbValues, size);
        edgeDetected.setRGB(x, y, newRGB);
      }
    }
  }

  /**
   * This function takes in a coordinate and determines whether the pixel with
   * that coordinate is within the bounds of the image being operated on.
   *
   * @param c The input coordinate.
   * @return Returns true if the pixel is within image bounds, false otherwise.
   */
  private boolean withinImageBounds(Coordinate c) {
    if (c.getX() < 0 || c.getX() >= edgeDetected.getWidth()) {
      return false;
    }
    if (c.getY() < 0 || c.getY() >= edgeDetected.getHeight()) {
      return false;
    }
    return true;
  }

  /**
   * This function is the inner loop of the edge detection algorithm. It takes
   * in an array of RGB values, the first of which is the value of the central
   * pixel. It returns an RGB value: white if the central pixel is determined to
   * be an edge, black if not. A center pixel is on an edge if the difference
   * between its colour value and any of its neighbour's colour values is larger
   * than or equal to the supplied epsilon value. 'Colour value' in this context
   * means either the red, green or blue value since they are all the same.
   *
   * @param rgbValues The array of RGB values (rgbValues[0] is the center).
   * @param size      The size of the array.
   * @return Return's the RGB value for white or black.
   */
  private int determineRGB(int[] rgbValues, int size) {
    int centerRGB = rgbValues[0] & 0xff;
    for (int i = 1; i < size; i++) {
      if (Math.abs(centerRGB - (rgbValues[i] & 0xff)) >= epsilon) {
        return WHITE;
      }
    }
    return BLACK;
  }

  /**
   * This method returns the final edge-detected image.
   *
   * @return Returns the edge-detected image.
   */
  public BufferedImage getEdgeDetected() {
    return edgeDetected;
  }
}
