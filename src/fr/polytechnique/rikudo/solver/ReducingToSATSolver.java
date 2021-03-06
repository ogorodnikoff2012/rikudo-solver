package fr.polytechnique.rikudo.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
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
  private final Constraints constraints;
  private final Mode mode;

  public static enum Mode {
    E_MODE_PATH,
    E_MODE_CYCLE,
  }

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
    this(graph, source, target, Mode.E_MODE_PATH);
  }

  public ReducingToSATSolver(IGraph graph, int source, int target, Mode mode) {
    this(graph, source, target, mode, new Constraints());
  }

  public ReducingToSATSolver(IGraph graph, int source, int target, Mode mode, Constraints constraints) {
    this.graph = graph;
    this.source = source;
    this.target = target;
    this.mode = mode;
    this.constraints = constraints;
  }

  private ISolver prepareSolver() {
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

          if (mode == Mode.E_MODE_CYCLE) {
            satSolver
                .addClause(new VecInt(new int[]{-encodeVariable(graph.size() - 1, u), -encodeVariable(0, v)}));
          }
        }
      }

      if (mode == Mode.E_MODE_PATH) {
        // The first vertex should be the source and the last vertex should be the target
        satSolver.addClause(new VecInt(1, encodeVariable(0, source)));
        satSolver.addClause(new VecInt(1, encodeVariable(graph.size() - 1, target)));
      }

      // Applying constraints
      Hashtable<Integer, Integer> vertex_to_pos = constraints.getVertexConstraints();
      for (int vertex : vertex_to_pos.keySet()) {
        int pos = vertex_to_pos.get(vertex);
        satSolver.addClause(new VecInt(1, encodeVariable(pos, vertex)));
      }

      for (int v = 0; v < graph.size(); ++v) {
        HashSet<Integer> diamonds = constraints.getDiamondedNeighbours(v);
        if (diamonds == null) {
          continue;
        }
        for (int u  : diamonds) {
          for (int i = 1; i < graph.size() - 1; ++i) {
            satSolver.addClause(new VecInt(new int[]{
                -encodeVariable(i,v), encodeVariable(i + 1, u), encodeVariable(i - 1, u)
            }));
          }

          if (mode == Mode.E_MODE_PATH) {
            satSolver.addClause(new VecInt(new int[]{
                -encodeVariable(0, v), encodeVariable(1, u)
            }));
            satSolver.addClause(new VecInt(new int[]{
                -encodeVariable(graph.size() - 1, v), encodeVariable(graph.size() - 2, u)
            }));
          } else if (mode == Mode.E_MODE_CYCLE) {
            satSolver.addClause(new VecInt(new int[]{
                -encodeVariable(0, v), encodeVariable(1, u), encodeVariable(graph.size() - 1, u)
            }));
            satSolver.addClause(new VecInt(new int[]{
                -encodeVariable(graph.size() - 1, v), encodeVariable(graph.size() - 2, u), encodeVariable(0, u)
            }));
          }
        }
      }

      return satSolver;
    } catch (ContradictionException e) {
      return null;
    }
  }

  private List<Integer> solveHelper(ISolver satSolver) {
    if (satSolver == null) {
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

  @Override
  public List<Integer> solve() {
    ISolver satSolver = prepareSolver();
    return solveHelper(satSolver);
  }

  public boolean verifyUniqueness(List<Integer> path) {
    ISolver satSolver = prepareSolver();
    if (satSolver == null) {
      return false;
    }

    // Forbid the found solution
    int restriction[] = new int[path.size()];
    for (int i = 0; i < path.size(); ++i) {
      int v = path.get(i);
      restriction[i] = -encodeVariable(i, v);
    }
    try {
      satSolver.addClause(new VecInt(restriction));
    } catch (ContradictionException e) {
      // e.printStackTrace();
      return true;
    }

    try {
      return !satSolver.isSatisfiable();
    } catch (TimeoutException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static void main(String[] args) {
    AdjListGraph graph = new AdjListGraph(5);
    for (int i = 0; i < graph.size(); ++i) {
      for (int j = 0; j < graph.size(); ++j) {
        if (i != j) {
          graph.addEdge(i, j);
        }
      }
    }

    Constraints constraints = new Constraints();
    constraints.addDiamondConstraint(1, 3);
    constraints.addVertexConstraint(2,3);
    constraints.addVertexConstraint(3, 2);

    IHamPathSolver solver = new ReducingToSATSolver(graph, 0, graph.size() - 1, Mode.E_MODE_PATH, constraints);
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
