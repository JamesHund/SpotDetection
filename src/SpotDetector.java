import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * This class runs an edge-detected (or edgemap) image through a spot detection
 * algorithm. It stores the number of spots detected and the resulting
 * 'spot-map' as an image.
 */
public class SpotDetector {

  private final MaskParam[] maskParams = new MaskParam[]{
      new MaskParam(4, 6, 0, 4800),
      new MaskParam(5, 9, 1, 6625),
      new MaskParam(6, 12, 1, 11000),
      new MaskParam(7, 15, 1, 15000),
      new MaskParam(8, 18, 1, 19000),
      new MaskParam(9, 21, 1, 23000),
      new MaskParam(10, 24, 2, 28000),
      new MaskParam(11, 27, 2, 35000)
  };
  private final BufferedImage spotMap;
  private final boolean[][] spotAdded; //x,y
  private int spotCount = 0;

  /**
   * This method is the constructor for SpotDetector. It takes in a lower-bound
   * and an upper-bound. The lower-bound is strictly less than the upper-bound
   * and both bounds can only take on values between 4 and 11 (4 and 11
   * included).
   *
   * @param edgeMap    The edge-map to be scanned for spots.
   * @param lowerBound The minimum radius of a spot to be scanned.
   * @param upperBound The maximum radius of a spot to be scanned.
   */
  public SpotDetector(BufferedImage edgeMap, int lowerBound, int upperBound) {
    spotMap = new BufferedImage(edgeMap.getWidth(), edgeMap.getHeight(),
        edgeMap.getType());
    spotAdded = new boolean[edgeMap.getWidth()][edgeMap.getHeight()];
    findSpots(lowerBound, upperBound, edgeMap);
  }

  /**
   * This method returns the final spot-map image.
   *
   * @return Returns a BufferedImage containing the final spot-map.
   */
  public BufferedImage getSpotMap() {
    return spotMap;
  }

  /**
   * This method returns the final spot-count.
   *
   * @return Returns the final spot count.
   */
  public int getSpotCount() {
    return spotCount;
  }

  /**
   * This method applies the spot detection algorithm to an edge-map for all
   * integer radii between the lower-bound and the upper-bound (both bounds
   * included).
   *
   * @param lowerBound The lower-bound
   * @param upperBound The upper-bound
   * @param edgeMap    The edge-map.
   */
  private void findSpots(int lowerBound, int upperBound,
      BufferedImage edgeMap) {

    for (int i = lowerBound; i <= upperBound; i++) {
      findSpots(edgeMap, createMask(i));
    }

  }

  /**
   * This method is the main spot detection algorithm. It iterates through each
   * pixel in the edge-map and determines whether the area around it is a spot.
   * To achieve this: a is mask overlaid on the edge-map and compared to each
   * corresponding pixel of the sub-image in its bounds. If it is determined
   * that the sum of the differences between each pixel of the mask and the
   * edge-map is less than a predefined threshold, the pixel contains a spot. If
   * this has been determined, the sub-image is drawn onto the spotMap and
   * spotCount is increased. The 2 dimensional array 'spotAdded' keeps track of
   * areas that have been determined to be a spot to avoid double-counting.
   *
   * @param edgeMap The edge-map to be scanned for spots
   * @param mask    The mask to apply to the image.
   */
  private void findSpots(BufferedImage edgeMap, int[][] mask) {
    int width = edgeMap.getWidth();
    int height = edgeMap.getHeight();

    //radius of the mask
    int rad = (int) Math.round((mask.length - 1) / 2.0);

    //graphics for drawing edge-map sub-images onto the spot-map
    Graphics2D graphics = spotMap.createGraphics();

    //creating an array representation of edge-map for faster access to pixel
    //values in inner loop
    int[][] edgeMapArray = new int[width][height];

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        edgeMapArray[i][j] = edgeMap.getRGB(i, j) & 0xFF;
      }
    }

    //for each pixel in the edge-map (such that the mask can fully overlap)
    for (int x = rad + 1; x < width - rad; x++) {
      for (int y = rad + 1; y < height - rad; y++) {
        //sub-image creation
        int xx = x - rad - 1; //left most x
        int yy = y - rad - 1; //top most y
        int subLength = rad * 2 + 1; // width/height of subImage/mask

        //sub-image is sub-array of edgeMapArray from with
        //x from xx to xx + subLength - 1
        //y from yy to yy + subLength - 1

        //check if sub-image has no edges contained
        boolean maxZero = true;
        for (int i = 0; i < subLength; i++) {
          for (int j = 0; j < subLength; j++) {
            if (edgeMapArray[xx + i][yy + j] != 0) {
              maxZero = false;
              break;
            }
          }
        }
        //if subImage has no edges, we can skip a large chunk of pixels
        if (maxZero) {
          y += subLength;
          continue;
        }

        //calculate the sum of the absolute differences
        //(between the mask and the sub-image)
        int sum = 0;
        for (int i = 0; i < subLength; i++) {
          for (int j = 0; j < subLength; j++) {
            int pixelValue = edgeMapArray[xx + i][yy + j];
            sum += Math.abs(pixelValue - mask[i][j]);
          }
        }

        //if the total difference < the predefined distance for the mask radius
        //and a spot has not been added: add a new spot
        if (!spotAdded[x][y] && sum < getMaskParam(rad).difference) {
          spotCount++;

          //mark all the pixels within the sub-image bounds as containing a spot
          //(avoiding double-counting)
          for (int i = xx; i < xx + subLength; i++) {
            for (int j = yy; j < yy + subLength; j++) {
              spotAdded[i][j] = true;
            }
          }

          graphics.drawImage(edgeMap.getSubimage(xx, yy, subLength, subLength),
              null, xx,
              yy);
        }

      }

    }

  }

  /**
   * This method creates a donut shaped mask with a specified radius and
   * associated predefined parameters stored in the maskParams array.
   *
   * @param radius The radius of the mask.
   * @return Returns a 2D integer array with size: 2*radius + 1. Array entries
   * can have a value of either 0 or 255.
   */
  private int[][] createMask(int radius) {
    MaskParam p = getMaskParam(radius);
    return createMask(radius, p.width, p.delta);
  }

  /**
   * This method creates a donut shaped mask with tweakable parameters.
   *
   * @param radius The radius of the mask.
   * @param width  The width of the mask.
   * @param delta  The delta used to create the hole in the mask.
   * @return Returns a 2D integer array with size: 2*radius + 1. Array entries
   * can have a value of either 0 or 255.
   */
  private int[][] createMask(int radius, int width, int delta) {
    int arrN = 2 * radius + 1;
    int[][] mask = new int[arrN][arrN];

    for (int i = 0; i < arrN; i++) {
      for (int j = 0; j < arrN; j++) {
        int circle = (i - radius) * (i - radius) + (j - radius) * (j - radius);
        boolean donut = circle < (radius - delta) * (radius - delta) + width;
        donut &= circle > (radius - delta) * (radius - delta) - width;
        if (donut) {
          mask[i][j] = 255;
        } else {
          mask[i][j] = 0;
        }
      }
    }
    return mask;
  }

  /**
   * This method returns the MaskParam instance associated with a radius.
   *
   * @param radius The radius.
   * @return MaskParam: The MaskParam with the specified radius.
   */
  private MaskParam getMaskParam(int radius) {
    if (radius > 11 || radius < 4) {
      System.err.println("Radius out of bounds");
      System.exit(0);
    }
    return maskParams[radius - 4];
  }

  /**
   * Debug method: Creates mask of specific radius and prints it to StdOut.
   *
   * @param radius Radius.
   */
  private void testMask(int radius) {
    int[][] testMask = createMask(radius);

    for (int x = 0; x < testMask.length; x++) {
      for (int y = 0; y < testMask.length; y++) {
        if (testMask[x][y] == 255) {
          System.out.print("*");
        } else {
          System.out.print("-");
        }
      }
      System.out.println();
    }
  }

  /**
   * Debug method
   *
   * @return BufferedImage
   **/
  public BufferedImage renderSpotCoordinates() {
    int width = spotMap.getWidth();
    int height = spotMap.getHeight();
    BufferedImage image = new BufferedImage(width, height, spotMap.getType());

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int rgb = 0;
        if (spotAdded[x][y]) {
          rgb = 0xFFFFFF;
        }
        image.setRGB(x, y, rgb);
      }
    }

    return image;
  }

  /**
   * This private class exists to store mask-creation parameters associated with
   * specific radii (width, delta and difference).
   */
  private class MaskParam {

    final int radius, width, delta, difference;

    /**
     * This method is the constructor for MaskParam.
     *
     * @param radius     The radius.
     * @param width      The width.
     * @param delta      The delta.
     * @param difference The difference.
     */
    MaskParam(int radius, int width, int delta, int difference) {
      this.radius = radius;
      this.width = width;
      this.delta = delta;
      this.difference = difference;
    }
  }


}
