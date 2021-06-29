/**
 * Coordinate is an immutable datatype which provides an abstraction for a 2
 * dimensional cartesian Coordinate.
 */
public class Coordinate {

  //the relative Von Neumann neighbourhood of a pixel
  public static final Coordinate[] NEIGHBOUR_OFFSETS =
      new Coordinate[]{
          new Coordinate(0, 0),
          new Coordinate(-1, 0),
          new Coordinate(1, 0),
          new Coordinate(0, -1),
          new Coordinate(0, 1)
      };
  private final int x, y;

  /**
   * The constructor for Coordinate
   *
   * @param x x-coordinate
   * @param y y-coordinate
   */
  public Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * This function returns a new coordinate which is the sum of the current
   * Coordinate instance and an input coordinate.
   *
   * @param c The input coordinate.
   * @return Returns a new Coordinate (the sum).
   */
  public Coordinate add(Coordinate c) {
    return new Coordinate(x + c.x, y + c.y);
  }

  /**
   * Returns the x-coordinate.
   *
   * @return Returns the x-coordinate.
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the y-coordinate.
   *
   * @return Returns the y-coordinate.
   */
  public int getY() {
    return y;
  }
}
