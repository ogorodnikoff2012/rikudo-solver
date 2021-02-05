package fr.polytechnique.rikudo.benchmark;

import fr.polytechnique.rikudo.solver.BacktrackingSolver;
import fr.polytechnique.rikudo.solver.Constraints;
import fr.polytechnique.rikudo.solver.IGraph;
import fr.polytechnique.rikudo.solver.IHamPathSolver;
import fr.polytechnique.rikudo.solver.ReducingToSATSolver;
import fr.polytechnique.rikudo.solver.ReducingToSATSolver.Mode;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {
  public static class ProblemInstance {
    public final IGraph graph;
    public final int source;
    public final int target;
    public final Constraints constraints;

    public ProblemInstance(IGraph graph, int source, int target) {
      this(graph, source, target, new Constraints());
    }

    public ProblemInstance(IGraph graph, int source, int target, Constraints constraints) {
      this.graph = graph;
      this.source = source;
      this.target = target;
      this.constraints = constraints;
    }
  }

  private static List<IHamPathSolver> buildSolvers(IGraph graph, int source, int target, Constraints constraints) {
    ArrayList<IHamPathSolver> solvers = new ArrayList<>();
    solvers.add(new ReducingToSATSolver(graph, source, target, Mode.E_MODE_PATH, constraints));
    solvers.add(new BacktrackingSolver(graph, source, target, constraints));
    return solvers;
  }

  private static List<IHamPathSolver> buildSolvers(ProblemInstance problem) {
    return buildSolvers(problem.graph, problem.source, problem.target, problem.constraints);
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
      ProblemInstance problem = GraphBuilder.buildFullGraph(size);
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
      ProblemInstance problem = GraphBuilder.buildCyclicGraph(size);
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
        ProblemInstance problem = GraphBuilder.buildGridGraph(rows, cols);
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
        ProblemInstance problem = GraphBuilder.buildGridGraph(rows, cols);
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
