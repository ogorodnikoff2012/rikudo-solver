package fr.polytechnique.rikudo.benchmark;

import fr.polytechnique.rikudo.benchmark.Benchmark.ProblemInstance;
import fr.polytechnique.rikudo.solver.AdjListGraph;
import fr.polytechnique.rikudo.solver.MatrixGraph;

public class GraphBuilder {

  public static ProblemInstance buildFullGraph(int size) {
    MatrixGraph graph = new MatrixGraph(size);
    for (int i = 0; i < size; ++i) {
      for (int j = 0; j < size; ++j) {
        graph.addEdge(i, j);
      }
    }

    return new ProblemInstance(graph, 0, size - 1);
  }

  public static ProblemInstance buildCyclicGraph(int size) {
    AdjListGraph graph = new AdjListGraph(size);
    graph.addEdge(0, size - 1);
    graph.addEdge(size - 1, 0);
    for (int i = 1; i < size; ++i) {
      graph.addEdge(i, i - 1);
      graph.addEdge(i - 1, i);
    }
    return new ProblemInstance(graph, 0, size - 1);
  }

  public static ProblemInstance buildGridGraph(int rows, int cols) {
    AdjListGraph graph = new AdjListGraph(rows * cols);

    for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j) {
        int vertex = i * cols + j;
        int right_neighbour = vertex + 1;
        int down_neighbour = vertex + cols;

        if (j + 1 < cols) {
          graph.addEdge(vertex, right_neighbour);
          graph.addEdge(right_neighbour, vertex);
        }
        if (i + 1 < rows) {
          graph.addEdge(vertex, down_neighbour);
          graph.addEdge(down_neighbour, vertex);
        }
      }
    }

    return new ProblemInstance(graph, 0, rows * cols - 1);
  }
}
