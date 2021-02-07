package fr.polytechnique.rikudo.hexagonal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PrettyPictures {

  private static void drawAxis(Graphics2D g2d, double minX, double maxX, double minY, double maxY) {
    g2d.setColor(Color.BLACK);

    g2d.draw(new Line2D.Double(0, minY,0, maxY));
    g2d.draw(new Line2D.Double(minX, 0, maxX, 0));

    for (double x = Math.floor(minX); x <= maxX; x += 1) {
      g2d.draw(new Line2D.Double(x, -0.1, x, 0.1));
    }

    for (double y = Math.floor(minY); y <= maxY; y += 1) {
      g2d.draw(new Line2D.Double(-0.1, y, 0.1, y));
    }
  }

  private static void generateTriangularGrid() throws IOException {
    BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();

    g2d.setColor(Color.RED);
    g2d.translate(400, 300);
    g2d.scale(50, -50);
    g2d.setStroke(new BasicStroke(0.02f));

    drawAxis(g2d, -8, 8, -6, 6);

    for (int a = -10; a <= 10; ++a) {
      for (int b = -10; b <= 10; ++b) {
        EisensteinInteger point = new EisensteinInteger(a, b);
        g2d.setColor(Color.RED);
        double x = point.real();
        double y = point.imag();

        g2d.fill(new Ellipse2D.Double(x - 0.05, y - 0.05, 0.1, 0.1));
      }
    }

    ImageIO.write(img, "png", new File("triangularGrid.png"));
  }

  public static void main(String[] args) throws IOException {
    generateTriangularGrid();
  }
}
