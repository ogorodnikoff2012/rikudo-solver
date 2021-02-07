package fr.polytechnique.rikudo.hexagonal;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Cell {
  private final EisensteinInteger center;
  private final double sideLength;

  public static Cell findNearestCell(double x, double y, double sideLength) {
    // Some linear algebra magic...
    double a1 = Math.floor((x + y * Math.sqrt(3)) / (3 * sideLength));
    double a2 = Math.floor(2 * x / (3 * sideLength));

    EisensteinInteger possibleCenter = new EisensteinInteger((long) (a1 + a2), (long) (2 * a1 - a2));
    Cell possibleCell = new Cell(possibleCenter, sideLength);
    double bestDistanceSq = possibleCell.getCenterAsPoint().distanceSq(x, y);

    List<Cell> neighbours = possibleCell.getNeighbours();
    for (Cell cell : neighbours) {
      Point2D point = cell.getCenterAsPoint();
      double distanceSq = point.distanceSq(x, y);
      if (distanceSq < bestDistanceSq) {
        possibleCell = cell;
        bestDistanceSq = distanceSq;
      }
    }

    return possibleCell;
  }

  public Cell(EisensteinInteger center, double sideLength) {
    this.center = center;
    this.sideLength = sideLength;
  }

  public EisensteinInteger getCenter() {
    return center;
  }

  public double getSideLength() {
    return sideLength;
  }

  public Cell rescale(double newSideLength) {
    return new Cell(center, newSideLength);
  }

  public List<Cell> getNeighbours() {
    EisensteinInteger angle = EisensteinInteger.ONE.add(EisensteinInteger.OMEGA);
    EisensteinInteger neighbourCenterOffset = EisensteinInteger.ONE.sub(EisensteinInteger.OMEGA);

    ArrayList<Cell> neighbours = new ArrayList<>(6);
    for (int i = 0; i < 6; ++i) {
      EisensteinInteger neighbourCenter = center.add(neighbourCenterOffset);
      neighbours.add(new Cell(neighbourCenter, sideLength));
      neighbourCenterOffset = neighbourCenterOffset.mul(angle);
    }

    return neighbours;
  }

  Path2D getBorder() {
    double[] xPoints = new double[6];
    double[] yPoints = new double[6];

    EisensteinInteger angle = EisensteinInteger.ONE.add(EisensteinInteger.OMEGA);
    EisensteinInteger cornerOffset = EisensteinInteger.ONE;
    for (int i = 0; i < 6; ++i) {
      EisensteinInteger corner = center.add(cornerOffset);
      xPoints[i] = corner.real() * sideLength;
      yPoints[i] = corner.imag() * sideLength;
      cornerOffset = cornerOffset.mul(angle);
    }

    Path2D path = new Path2D.Double();
    path.moveTo(xPoints[5], yPoints[5]);
    for (int i = 0; i < 6; ++i) {
      path.lineTo(xPoints[i], yPoints[i]);
    }
    return path;
  }

  Point2D getCenterAsPoint() {
    return new Point2D.Double(center.real() * sideLength, center.imag() * sideLength);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Cell cell = (Cell) o;
    return java.lang.Double.compare(cell.sideLength, sideLength) == 0 &&
        center.equals(cell.center);
  }

  @Override
  public int hashCode() {
    return Objects.hash(center, sideLength);
  }

  @Override
  public String toString() {
    return "Cell{" +
        "center=" + center +
        ", sideLength=" + sideLength +
        '}';
  }
}
