package fr.polytechnique.rikudo.hexagonal;

import fr.polytechnique.rikudo.puzzle.RikudoPuzzle;
import fr.polytechnique.rikudo.puzzle.RikudoPuzzle.DesignProperties;
import fr.polytechnique.rikudo.solver.Constraints;
import fr.polytechnique.rikudo.solver.ReducingToSATSolver;
import fr.polytechnique.rikudo.solver.ReducingToSATSolver.Mode;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import javax.imageio.ImageIO;

public class GridGenerator {
  public static class Palette {
    public final Color fill;
    public final Color border;
    public final Color source;
    public final Color target;
    public final Color constraint;
    public final Color diamond;

    public Palette(Color fill, Color border, Color source, Color target, Color constraint, Color diamond) {
      this.fill = fill;
      this.border = border;
      this.source = source;
      this.target = target;
      this.constraint = constraint;
      this.diamond = diamond;
    }

    public static final Palette DEFAULT_PALETTE = new Palette(Color.WHITE, Color.BLACK, Color.ORANGE, Color.ORANGE, Color.YELLOW, Color.BLUE);
  }

  public static class GridGeneratorProperties {
    public final File inputFile;
    public final File outputFile;
    public final File solutionFile;
    public final double inputSideLength;
    public final double outputSideLength;
    public final RikudoPuzzle.DesignProperties designProperties;
    public final Palette palette;

    public GridGeneratorProperties(File inputFile, File outputFile, File solutionFile,
        double inputSideLength,
        double outputSideLength, RikudoPuzzle.DesignProperties designProperties, Palette palette) {
      this.inputFile = inputFile;
      this.outputFile = outputFile;
      this.solutionFile = solutionFile;
      this.inputSideLength = inputSideLength;
      this.outputSideLength = outputSideLength;
      this.designProperties = designProperties;
      this.palette = palette;
    }

    void storeToXML(OutputStream output) throws IOException {
      Properties properties = new Properties();

      properties.setProperty("input-file", inputFile.getCanonicalPath());
      properties.setProperty("output-file", outputFile.getCanonicalPath());
      properties.setProperty("solution-file", solutionFile.getCanonicalPath());
      properties.setProperty("input-side-length", inputSideLength + "");
      properties.setProperty("output-side-length", outputSideLength + "");
      properties.setProperty("allow-vertex-constraints", designProperties.isEnableVertexConstraints() + "");
      properties.setProperty("allow-diamond-constraints", designProperties.isEnableDiamondConstraints() + "");

      properties.storeToXML(output, "");
    }

    static GridGeneratorProperties loadFromXML(InputStream input) throws IOException {
      Properties properties = new Properties();
      properties.loadFromXML(input);

      File inputFile = new File(properties.getProperty("input-file"));
      File outputFile = new File(properties.getProperty("output-file"));
      File solutionFile = new File(properties.getProperty("solution-file"));
      double inputSideLength = Double.parseDouble(properties.getProperty("input-side-length"));
      double outputSideLength = Double.parseDouble(properties.getProperty("output-side-length"));
      boolean allowVertexConstraints = Boolean.parseBoolean(properties.getProperty("allow-vertex-constraints"));
      boolean allowDiamondConstraints = Boolean.parseBoolean(properties.getProperty("allow-diamond-constraints"));

      DesignProperties designProperties = new DesignProperties();
      designProperties.setEnableVertexConstraints(allowVertexConstraints);
      designProperties.setEnableDiamondConstraints(allowDiamondConstraints);

      return new GridGeneratorProperties(
          inputFile,
          outputFile,
          solutionFile,
          inputSideLength,
          outputSideLength,
          designProperties,
          Palette.DEFAULT_PALETTE
      );
    }
  }

  public enum VisualisationMode {
    E_MODE_PUZZLE,
    E_MODE_SOLUTION
  }

  private final GridGeneratorProperties properties;
  private static final Path2D DIAMOND;

  static {
    DIAMOND = new Path2D.Double();
    double step = 1. / 8;
    DIAMOND.moveTo(step, 0);
    DIAMOND.lineTo(0, step);
    DIAMOND.lineTo(-step, 0);
    DIAMOND.lineTo(0, -step);
    DIAMOND.lineTo(step, 0);
  }

  public GridGenerator(GridGeneratorProperties properties) {
    this.properties = properties;
  }

  public HashSet<Cell> getAllCells(Rectangle2D rect) {
    HashSet<Cell> cells = new HashSet<>();

    Queue<Cell> newCells = new ArrayDeque<>();
    Cell initial = Cell.findNearestCell(rect.getX(), rect.getY(), properties.inputSideLength);
    newCells.add(initial);
    cells.add(initial);

    while (!newCells.isEmpty()) {
      Cell cell = newCells.poll();

      for (Cell neighbour : cell.getNeighbours()) {
        if (cells.contains(neighbour) || !neighbour.getBorder().getBounds2D().intersects(rect)) {
          continue;
        }
        cells.add(neighbour);
        newCells.add(neighbour);
      }
    }

    return cells;
  }

  private static void drawCenteredString(String str, Graphics2D g, int x, int y) {
    FontMetrics metrics = g.getFontMetrics();
    g.drawString(str, x - metrics.stringWidth(str) / 2, y - metrics.getHeight() / 2 + metrics.getAscent());
  }

  public static HashSet<Cell> selectCompletelyVisible(HashSet<Cell> cells, Rectangle2D rect) {
    HashSet<Cell> selectedCells = new HashSet<>();

    for (Cell cell : cells) {
      if (rect.contains(cell.getBorder().getBounds2D())) {
        selectedCells.add(cell);
      }
    }

    return selectedCells;
  }

  public HashSet<Cell> getCellsByMask(BufferedImage img) {
    HashSet<Cell> result = new HashSet<>();
    for (int x = 0; x < img.getWidth(); ++x) {
      for (int y = 0; y < img.getHeight(); ++y) {
        Color col = new Color(img.getRGB(x, y));
        if (!col.equals(Color.BLACK)) {
          continue;
        }

        HashSet<Cell> cells = getAllCells(new Rectangle2D.Double(x, y, 1, 1));
        result.addAll(cells);
      }
    }

    return result;
  }

  private BufferedImage readMask() throws IOException {
    return ImageIO.read(properties.inputFile);
  }

  private ArrayList<Cell> getScaledCellsList(GridGraph gr) {
    ArrayList<Cell> result = new ArrayList<>(gr.size());

    for (int i = 0; i < gr.size(); ++i) {
      Cell cell = gr.getCell(i).rescale(properties.outputSideLength);
      result.add(cell);
    }

    return result;
  }

  private Rectangle2D buildBoundingRect(List<Cell> cells) {
    Rectangle2D boundingRect = null;

    for (Cell cell : cells) {
      Rectangle2D bounds = cell.getBorder().getBounds2D();
      if (boundingRect == null) {
        boundingRect = bounds;
      } else {
        boundingRect.add(bounds);
      }
    }

    Point2D upperLeft  = new Point2D.Double(boundingRect.getMinX(), boundingRect.getMinY());
    Point2D lowerRight = new Point2D.Double(boundingRect.getMaxX(), boundingRect.getMaxY());

    boundingRect.add(upperLeft.getX() - properties.outputSideLength, upperLeft.getY() - properties.outputSideLength);
    boundingRect.add(lowerRight.getX() + properties.outputSideLength, lowerRight.getY() + properties.outputSideLength);

    return boundingRect;
  }

  private void drawDiamond(Cell cell1, Cell cell2, Graphics2D g2d) {
    Point2D center1 = cell1.getCenterAsPoint();
    Point2D center2 = cell2.getCenterAsPoint();

    AffineTransform tx = g2d.getTransform();

    g2d.translate((center1.getX() + center2.getX()) / 2, (center1.getY() + center2.getY()) / 2);
    g2d.rotate(Math.atan2(center2.getY() - center1.getY(), center2.getX() - center1.getX()));
    g2d.scale(properties.outputSideLength * Math.sqrt(3), properties.outputSideLength);

    g2d.setColor(properties.palette.diamond);
    g2d.fill(DIAMOND);

    g2d.setTransform(tx);
  }

  private BufferedImage visualize(GridGraph gr, int source, int target, Constraints constraints, VisualisationMode mode) throws IOException {
    List<Integer> solution = null;
    if (mode == VisualisationMode.E_MODE_SOLUTION) {
      solution = new ReducingToSATSolver(gr, source, target, Mode.E_MODE_PATH, constraints).solve();
    }

    ArrayList<Cell> scaledCells = getScaledCellsList(gr);
    Rectangle2D boundingRect = buildBoundingRect(scaledCells);

    BufferedImage image = new BufferedImage((int)Math.ceil(boundingRect.getWidth()), (int)Math.ceil(boundingRect.getHeight()), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();

    g2d.translate(-boundingRect.getX(), -boundingRect.getY());

    // Draw vertices with constraints
    for (int vertex = 0; vertex < gr.size(); ++vertex) {
      Cell cell = scaledCells.get(vertex);
      String text = null;
      Path2D border = cell.getBorder();

      if (vertex == source) {
        g2d.setColor(properties.palette.source);
        text = "1";
      } else if (vertex == target) {
        g2d.setColor(properties.palette.target);
        text = Integer.toString(gr.size());
      } else if (constraints.getVertexConstraints().containsKey(vertex)) {
        g2d.setColor(properties.palette.constraint);
        text = Integer.toString(constraints.getVertexConstraints().get(vertex) + 1);
      } else {
        g2d.setColor(properties.palette.fill);
        if (mode == VisualisationMode.E_MODE_SOLUTION) {
          text = Integer.toString(solution.indexOf(vertex) + 1);
        }
      }

      g2d.fill(border);
      if (text != null) {
        g2d.setColor(properties.palette.border);
        Point2D center = cell.getCenterAsPoint();
        drawCenteredString(text, g2d, (int)center.getX(), (int)center.getY());
      }

      g2d.setColor(properties.palette.border);
      g2d.draw(border);
    }

    // Draw diamonds
    for (int v1 : constraints.getDiamondConstraints().keySet()) {
      for (int v2 : constraints.getDiamondConstraints().get(v1)) {
        if (v1 < v2) {
          continue;
        }

        drawDiamond(scaledCells.get(v1), scaledCells.get(v2), g2d);
      }
    }

    return image;
  }

  public void buildPuzzle() throws IOException {
    BufferedImage mask = readMask();
    HashSet<Cell> cells = getCellsByMask(mask);
    GridGraph graph = new GridGraph(cells);

    int source = -1;
    int target = -1;

    Random rng = new Random();
    Constraints constraints = null;
    while (constraints == null) {
      source = rng.nextInt(graph.size());
      target = rng.nextInt(graph.size());

      RikudoPuzzle puzzle = new RikudoPuzzle(graph, source, target);
      constraints = puzzle.design(rng, properties.designProperties);
    }

    constraints.addVertexConstraint(source, 0);
    constraints.addVertexConstraint(target, graph.size() - 1);

    BufferedImage puzzle = visualize(graph, source, target, constraints, VisualisationMode.E_MODE_PUZZLE);
    BufferedImage solution = visualize(graph, source, target, constraints, VisualisationMode.E_MODE_SOLUTION);
    ImageIO.write(puzzle, "png", properties.outputFile);
    ImageIO.write(solution, "png", properties.solutionFile);
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.err.println("Usage: prog settings1.xml settings2.xml ... settingsN.xml");
      GridGeneratorProperties exampleProperties = new GridGeneratorProperties(
          new File("mask.png"),
          new File("puzzle.png"),
          new File("solution.png"),
          20,
          25,
          new DesignProperties(),
          Palette.DEFAULT_PALETTE
      );
      System.err.println("Example:");
      exampleProperties.storeToXML(System.err);
      System.exit(1);
    }

    for (String filename : args) {
      System.out.println("Reading " + filename);
      GridGeneratorProperties properties = GridGeneratorProperties.loadFromXML(new FileInputStream(filename));
      GridGenerator generator = new GridGenerator(properties);
      generator.buildPuzzle();
    }
  }

  // public static void main(String[] args) throws IOException {
  //   // if (args.length != 2) {
  //   //   System.err.println("Usage: ./prog <path_to_image> <side_length>");
  //   //   System.exit(1);
  //   // }

  //   String[] names = new String[]{
  //       "bowtie",
  //       "christmasTree",
  //       "X",
  //   };

  //   for (String name : names) {
  //     System.out.println("Pattern: " + name);
  //     GridGeneratorProperties properties = new GridGeneratorProperties(
  //         new File(name + ".png"),
  //         new File(name + "Puzzle.png"),
  //         new File(name + "Solution.png"),
  //         20,
  //         25,
  //         new DesignProperties(),
  //         Palette.DEFAULT_PALETTE
  //     );

  //     GridGenerator generator = new GridGenerator(properties); // Double.parseDouble(args[1]));
  //     generator.buildPuzzle();
  //   }
  // }
}
