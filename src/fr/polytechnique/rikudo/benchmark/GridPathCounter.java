package fr.polytechnique.rikudo.benchmark;

import fr.polytechnique.rikudo.benchmark.Benchmark.ProblemInstance;
import fr.polytechnique.rikudo.solver.BacktrackingSolver;

public class GridPathCounter {

  public static void main(String[] args) {
    for (int n = 1; n <= 7; ++n) {
      ProblemInstance problem = GraphBuilder.buildGridGraph(n, n);
      BacktrackingSolver solver = new BacktrackingSolver(problem.graph, problem.source,
          problem.target);
      System.out.println(n + " -> " + solver.count());
    }
  }
}
