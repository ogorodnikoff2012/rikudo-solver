package fr.polytechnique.rikudo.solver;

import java.util.Arrays;
import java.util.List;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

public class ReducingToSATSolver implements IHamPathSolver {

  private final IGraph graph;
  private final int source;
  private final int target;

  private int encodeVariable(int index, int vertex) {
    return index * graph.size() + vertex + 1;
  }

  private int decodeIndex(int variable) {
    return (variable - 1) / graph.size();
  }

  private int decodeVertex(int variable) {
    return (variable - 1) % graph.size();
  }

  public ReducingToSATSolver(IGraph graph, int source, int target) {
    this.graph = graph;
    this.source = source;
    this.target = target;
  }

  @Override
  public List<Integer> solve() {
    ISolver satSolver = SolverFactory.newDefault();

    try {
      // Each vertex appears precisely once in the path
      for (int v = 0; v < graph.size(); ++v) {
        for (int i = 0; i < graph.size(); ++i) {
          for (int j = i + 1; j < graph.size(); ++j) {
            satSolver
                .addClause(new VecInt(new int[]{-encodeVariable(i, v), -encodeVariable(j, v)}));
          }
        }

        IVecInt vecInt = new VecInt(graph.size());
        for (int i = 0; i < graph.size(); ++i) {
          vecInt.push(encodeVariable(i, v));
        }
        satSolver.addClause(vecInt);
      }

      // Each index is occupied precisely once
      for (int i = 0; i < graph.size(); ++i) {
        for (int v = 0; v < graph.size(); ++v) {
          for (int w = v + 1; w < graph.size(); ++w) {
            satSolver
                .addClause(new VecInt(new int[]{-encodeVariable(i, v), -encodeVariable(i, w)}));
          }
        }

        IVecInt vecInt = new VecInt(graph.size());
        for (int v = 0; v < graph.size(); ++v) {
          vecInt.push(encodeVariable(i, v));
        }
        satSolver.addClause(vecInt);
      }

      // Consecutive vertices along the path are adjacent in the graph
      for (int u = 0; u < graph.size(); ++u) {
        for (int v = 0; v < graph.size(); ++v) {
          if (graph.hasEdge(u, v)) {
            continue;
          }

          for (int i = 0; i < graph.size() - 1; ++i) {
            satSolver
                .addClause(new VecInt(new int[]{-encodeVariable(i, u), -encodeVariable(i + 1, v)}));
          }
        }
      }

      // The first vertex should be the source and the last vertex should be the target
      satSolver.addClause(new VecInt(1, encodeVariable(0, source)));
      satSolver.addClause(new VecInt(1, encodeVariable(graph.size() - 1, target)));
    } catch (ContradictionException e) {
      return null;
    }

    try {
      if (satSolver.isSatisfiable()) {
        int[] solution = satSolver.model();

        Integer[] result = new Integer[graph.size()];
        for (int variable : solution) {
          if (variable < 0) {
            continue;
          }

          int vertex = decodeVertex(variable);
          int index = decodeIndex(variable);
          result[index] = vertex;
        }

        return Arrays.asList(result);
      } else {
        return null;
      }
    } catch (TimeoutException e) {
      return null;
    }
  }

  public static void main(String[] args) {
    AdjListGraph graph = new AdjListGraph(4);
    graph.addEdge(0, 1);
    graph.addEdge(1, 0);
    graph.addEdge(1, 2);
    graph.addEdge(2, 1);
    graph.addEdge(2, 3);
    graph.addEdge(3, 2);
    graph.addEdge(3, 0);

    IHamPathSolver solver = new ReducingToSATSolver(graph, 0, 3);
    List<Integer> hamPath = solver.solve();
    if (hamPath == null) {
      System.out.println("No hamiltonian path found!");
    } else {
      System.out.print("Hamiltonian path found: ");
      for (int vertex : hamPath) {
        System.out.print(vertex + " ");
      }
      System.out.println();
    }
  }
}
