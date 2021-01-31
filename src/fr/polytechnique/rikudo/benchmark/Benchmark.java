package fr.polytechnique.rikudo.benchmark;

import fr.polytechnique.rikudo.solver.AdjListGraph;
import fr.polytechnique.rikudo.solver.BacktrackingSolver;
import fr.polytechnique.rikudo.solver.IGraph;
import fr.polytechnique.rikudo.solver.IHamPathSolver;
import fr.polytechnique.rikudo.solver.MatrixGraph;
import fr.polytechnique.rikudo.solver.ReducingToSATSolver;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {
  static class ProblemInstance {
    public final IGraph graph;
    public final int source;
    public final int target;

    public ProblemInstance(IGraph graph, int source, int target) {
      this.graph = graph;
      this.source = source;
      this.target = target;
    }
  }

  private static List<IHamPathSolver> buildSolvers(IGraph graph, int source, int target) {
    ArrayList<IHamPathSolver> solvers = new ArrayList<>();
    solvers.add(new ReducingToSATSolver(graph, source, target));
    solvers.add(new BacktrackingSolver(graph, source, target));
    return solvers;
  }

  private static List<IHamPathSolver> buildSolvers(ProblemInstance problem) {
    return buildSolvers(problem.graph, problem.source, problem.target);
  }

  private static ProblemInstance buildFullGraph(int size) {
    MatrixGraph graph = new MatrixGraph(size);
    for (int i = 0; i < size; ++i) {
      for (int j = 0; j < size; ++j) {
        graph.addEdge(i, j);
      }
    }

    return new ProblemInstance(graph, 0, size - 1);
  }

  private static ProblemInstance buildCyclicGraph(int size) {
    AdjListGraph graph = new AdjListGraph(size);
    graph.addEdge(0, size - 1);
    graph.addEdge(size - 1, 0);
    for (int i = 1; i < size; ++i) {
      graph.addEdge(i, i - 1);
      graph.addEdge(i - 1, i);
    }
    return new ProblemInstance(graph, 0, size - 1);
  }

  private static ProblemInstance buildGridGraph(int rows, int cols) {
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

  private static void fullGraphTest() {
    System.out.println("Full Graph Test");
    final int graph_sizes[] = {
        2, 4, 6, 8,
        10, 20, 30, 40,
        50, 100, 150, 200, 250, 300,
    };

    for (int size : graph_sizes) {
      System.out.println("+ Graph size: " + size);
      ProblemInstance problem = buildFullGraph(size);
      for (IHamPathSolver solver : buildSolvers(problem)) {
        System.out.println("+ + Solver: " + solver.getClass().getSimpleName());
        long begin = System.nanoTime();
        List<Integer> solution = solver.solve();
        long end = System.nanoTime();
        System.out.println("+ + + Correct: " + (solution != null));
        System.out.println("+ + + Time elapsed: " + (end - begin) / 1e6  + " ms ("+ (end - begin) + " ns)");
      }
    }
  }

  private static void cyclicGraphTest() {
    System.out.println("Cyclic Graph Test");
    final int graph_sizes[] = {
        2, 4, 6, 8,
        10, 20, 30, 40,
        50, 100, 150, 200, 250, 300,
    };

    for (int size : graph_sizes) {
      System.out.println("+ Graph size: " + size);
      ProblemInstance problem = buildCyclicGraph(size);
      for (IHamPathSolver solver : buildSolvers(problem)) {
        System.out.println("+ + Solver: " + solver.getClass().getSimpleName());
        long begin = System.nanoTime();
        List<Integer> solution = solver.solve();
        long end = System.nanoTime();
        System.out.println("+ + + Correct: " + (solution != null));
        System.out.println("+ + + Time elapsed: " + (end - begin) / 1e6  + " ms ("+ (end - begin) + " ns)");
      }
    }
  }

  private static void gridGraphTest() {
    System.out.println("Grid Graph Test");

    for (int rows = 1; rows <= 15; ++rows) {
      for (int cols = 1; cols <= 15; cols += 2) {
        if (rows * cols >= 55) {
          continue;
        }
        System.out.println("+ Graph size: " + rows + " x " + cols);
        ProblemInstance problem = buildGridGraph(rows, cols);
        for (IHamPathSolver solver : buildSolvers(problem)) {
          System.out.println("+ + Solver: " + solver.getClass().getSimpleName());
          long begin = System.nanoTime();
          List<Integer> solution = solver.solve();
          long end = System.nanoTime();
          System.out.println("+ + + Correct: " + (solution != null));
          System.out.println(
              "+ + + Time elapsed: " + (end - begin) / 1e6 + " ms (" + (end - begin) + " ns)");
        }
      }
    }
  }

  private static void evenGridGraphTest() {
    System.out.println("Even Grid Graph Test");

    for (int rows = 2; rows <= 15; rows += 2) {
      for (int cols = 2; cols <= 15; cols += 2) {
        if (rows * cols >= 55 ) {
          continue;
        }
        System.out.println("+ Graph size: " + rows + " x " + cols);
        ProblemInstance problem = buildGridGraph(rows, cols);
        for (IHamPathSolver solver : buildSolvers(problem)) {
          System.out.println("+ + Solver: " + solver.getClass().getSimpleName());
          long begin = System.nanoTime();
          List<Integer> solution = solver.solve();
          long end = System.nanoTime();
          System.out.println("+ + + Correct: " + (solution == null));
          System.out.println(
              "+ + + Time elapsed: " + (end - begin) / 1e6 + " ms (" + (end - begin) + " ns)");
        }
      }
    }
  }

  private static void runAllTests() {
    fullGraphTest();
    cyclicGraphTest();
    gridGraphTest();
    evenGridGraphTest();
  }

  public static void main(String[] args) {
    runAllTests();
  }
}
