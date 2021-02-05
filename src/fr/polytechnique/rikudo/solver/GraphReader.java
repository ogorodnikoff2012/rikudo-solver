package fr.polytechnique.rikudo.solver;

import fr.polytechnique.rikudo.benchmark.Benchmark.ProblemInstance;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class GraphReader {
  private final String graphPath;
  private final String constraintsPath;

  public GraphReader(String graphPath, String constraintsPath) {
    this.graphPath = graphPath;
    this.constraintsPath = constraintsPath;
  }

  private IGraph readGraph() throws IOException {
    Scanner scanner = new Scanner(new FileInputStream(graphPath));
    int vertexCount = scanner.nextInt();
    int edgeCount = scanner.nextInt();

    AdjListGraph graph = new AdjListGraph(vertexCount);
    for (int i = 0; i < edgeCount; ++i) {
      int u = scanner.nextInt();
      int v = scanner.nextInt();
      graph.addEdge(u, v);
    }

    scanner.close();
    return graph;
  }

  private Constraints readConstraints() throws IOException {
    Scanner scanner = new Scanner(new FileInputStream(constraintsPath));
    Constraints constraints = new Constraints();

    int vertexConstraintsCnt = scanner.nextInt();
    int diamondConstraintsCnt = scanner.nextInt();

    for (int i = 0; i < vertexConstraintsCnt; ++i) {
      int vertex = scanner.nextInt();
      int pos = scanner.nextInt();
      constraints.addVertexConstraint(vertex, pos);
    }

    for (int i = 0; i < diamondConstraintsCnt; ++i) {
      int u = scanner.nextInt();
      int v = scanner.nextInt();
      constraints.addDiamondConstraint(u, v);
    }

    return constraints;
  }

  public ProblemInstance readProblem() {
    try {
      IGraph graph = readGraph();
      Constraints constraints = readConstraints();
      int sourceVertex = constraints.getVertexByPos(0);
      int targetVertex = constraints.getVertexByPos(graph.size() - 1);
      return new ProblemInstance(graph, sourceVertex, targetVertex, constraints);
    } catch (NoSuchElementException e) {
      System.err.println("Bad file format!");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("An I/O error occurred!");
      e.printStackTrace();
    } catch (NullPointerException e) {
      System.err.println("Cannot deduce source and/or target vertex!");
      e.printStackTrace();
    }
    return null;
  }
}
