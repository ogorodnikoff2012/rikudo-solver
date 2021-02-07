package fr.polytechnique.rikudo.puzzle;

import fr.polytechnique.rikudo.benchmark.Benchmark.ProblemInstance;
import fr.polytechnique.rikudo.solver.BacktrackingSolver;
import fr.polytechnique.rikudo.solver.Constraints;
import fr.polytechnique.rikudo.solver.GraphReader;
import fr.polytechnique.rikudo.solver.IGraph;
import fr.polytechnique.rikudo.solver.IHamPathSolver;
import fr.polytechnique.rikudo.solver.ReducingToSATSolver;
import fr.polytechnique.rikudo.solver.ReducingToSATSolver.Mode;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RikudoPuzzle {

  public static class DesignProperties {

    public boolean isEnableDiamondConstraints() {
      return enableDiamondConstraints;
    }

    public void setEnableDiamondConstraints(boolean enableDiamondConstraints) {
      this.enableDiamondConstraints = enableDiamondConstraints;
    }

    public boolean isEnableVertexConstraints() {
      return enableVertexConstraints;
    }

    public void setEnableVertexConstraints(boolean enableVertexConstraints) {
      this.enableVertexConstraints = enableVertexConstraints;
    }

    private boolean enableDiamondConstraints;
    private boolean enableVertexConstraints;

    public DesignProperties() {
      enableVertexConstraints = true;
      enableDiamondConstraints = true;
    }
  }

  private final IGraph graph;
  private final int source;
  private final int target;
  private final Constraints constraints;

  public RikudoPuzzle(IGraph graph, int source, int target) {
    this(graph, source, target, new Constraints());
  }

  public RikudoPuzzle(IGraph graph, int source, int target, Constraints constraints) {
    this.graph = graph;
    this.source = source;
    this.target = target;
    this.constraints = constraints;
  }

  public boolean verifyUniqueness(List<Integer> path) {
    ReducingToSATSolver solver = new ReducingToSATSolver(graph, source, target, Mode.E_MODE_PATH, constraints);
    return solver.verifyUniqueness(path);
  }

  // public boolean verifyUniqueness() {
  //   BacktrackingSolver solver = new BacktrackingSolver(graph, source, target, constraints);
  //   if (solver.findKth(2) == null) {
  //     return true;
  //   } else {
  //     return false;
  //   }
  // }

  private static boolean isEmpty(Constraints constraints) {
    if (constraints.getVertexConstraints().size() > 0) {
      return false;
    }
    if (constraints.getDiamondConstraints().size() > 0) {
      return false;
    }
    return true;
  }

  public boolean isMinimal(List<Integer> path) {
    Constraints redundantConstraints = this.redundantConstraints(path, null);
    return isEmpty(redundantConstraints);
  }

  private Constraints redundantConstraints(List<Integer> path, Constraints necessaryConstraints) {
    if (necessaryConstraints == null) {
      necessaryConstraints = new Constraints();
    }

    Hashtable<Integer, Integer> vertexConstraints = constraints.getVertexConstraints();
    Set<Integer> setOfVertices = new HashSet<Integer>(vertexConstraints.keySet());

    Constraints redundantConstraints = new Constraints();

    for (Integer vertex : setOfVertices) {
      if (vertex == source || vertex == target) {
        continue;
      }
      if (necessaryConstraints.getVertexConstraints().containsKey(vertex)) {
        continue;
      }

      int position = vertexConstraints.get(vertex);
      constraints.removeVertexConstraint(vertex, position);
      if (verifyUniqueness(path)) {
        redundantConstraints.addVertexConstraint(vertex, position);
      } else {
        necessaryConstraints.addVertexConstraint(vertex, position);
      }
      constraints.addVertexConstraint(vertex, position);
    }

    Hashtable<Integer, HashSet<Integer>> diamondConstraints = constraints.getDiamondConstraints();
    Set<Integer> listOfDiamonds = new HashSet(diamondConstraints.keySet());

    for (Integer v1 : listOfDiamonds) {
      HashSet<Integer> neighbours = new HashSet<>(diamondConstraints.get(v1));
      for (Integer v2 : neighbours) {
        if (v1 < v2) {
          continue;
        }
        if (necessaryConstraints.getDiamondConstraints().containsKey(v1) &&
          necessaryConstraints.getDiamondedNeighbours(v1).contains(v2)) {
          continue;
        }
        constraints.removeDiamondConstraint(v1, v2);
        if (verifyUniqueness(path)) {
          redundantConstraints.addDiamondConstraint(v1, v2);
        } else {
          necessaryConstraints.addDiamondConstraint(v1, v2);
        }
        constraints.addDiamondConstraint(v1, v2);
      }
    }

    return redundantConstraints;
  }

  public Constraints design(Random rng, DesignProperties properties) {
    //BacktrackingSolver solver = new BacktrackingSolver(graph, source, target, constraints);
    //List<Integer> foundPath = solver.findKth(1);
    IHamPathSolver solver = new ReducingToSATSolver(graph, source, target,
        Mode.E_MODE_PATH, constraints);
    List<Integer> foundPath = solver.solve();
    if (foundPath == null) {
      System.out.println("Rikudo puzzle can not be created: there is no hamiltonian path!");
      return null;
    }
    System.out.println("Hamiltonian path: " + foundPath);
    if (properties.isEnableVertexConstraints()) {
      for (int i = 1; i < foundPath.size() - 1; i++) {
        constraints.addVertexConstraint(foundPath.get(i), i);
      }
    } else {
      HashSet<Integer> vertexConstraints = new HashSet<>(constraints.getVertexConstraints().keySet());
      for (int v : vertexConstraints) {
        constraints.removeVertexConstraint(v);
      }
    }

    if (properties.isEnableDiamondConstraints()) {
      for (int i = 0; i < foundPath.size() - 1; i++) {
        constraints.addDiamondConstraint(foundPath.get(i), foundPath.get(i + 1));
      }
    } else {
      constraints.getDiamondConstraints().clear();
    }

    if (!verifyUniqueness(foundPath)) {
      System.out.println("Cannot build a puzzle with such properties");
      return null;
    }

    Constraints necessaryConstraints = new Constraints();
    Constraints redundantConstraints = this.redundantConstraints(foundPath, necessaryConstraints);
    int iteration = 0;
    while (!isEmpty(redundantConstraints)) {
      long timeBegin = System.nanoTime();
      ++iteration;
      System.out.println("Iteration #" + iteration);
      System.out.println("Constraints: " + constraints);
      System.out.println("Redundant:   " + redundantConstraints);
      System.out.println("Necessary:   " + necessaryConstraints);
      int vertexConstraintsCnt = redundantConstraints.countVertexConstraints();
      int diamondConstraintsCnt = redundantConstraints.countDiamondConstraints();
      int randomNumber = rng.nextInt(vertexConstraintsCnt + diamondConstraintsCnt);

      if (randomNumber < vertexConstraintsCnt) {
        // Remove a random vertex constraint
        Iterator<Integer> iterator = redundantConstraints.getVertexConstraints().keySet().iterator();
        for (int i = 0; i < randomNumber; ++i) {
          iterator.next();
        }
        int randomVertex = iterator.next();
        constraints.removeVertexConstraint(randomVertex);
      } else {
        // Remove a random diamond constraint
        int edgeIndex = randomNumber - vertexConstraintsCnt;
        boolean edgeRemoved = false;
        int currentIndex = 0;

        for (int v1 : redundantConstraints.getDiamondConstraints().keySet()) {
          if (edgeRemoved) {
            break;
          }

          for (int v2 : redundantConstraints.getDiamondConstraints().get(v1)) {
            if (v1 < v2) {
              if (currentIndex == edgeIndex) {
                constraints.removeDiamondConstraint(v1, v2);
                edgeRemoved = true;
                break;
              }
              ++currentIndex;
            }
          }
        }
      }

      redundantConstraints = this.redundantConstraints(foundPath, necessaryConstraints);
      long timeEnd = System.nanoTime();
      System.out.printf("Done in %.3f s\n", (timeEnd - timeBegin) / 1e9);
    }

    return constraints;
  }

  public static void main(String[] args) {
        /*
        GraphReader reader = new GraphReader("riXkudo_graph.txt", "riXkudo_constraints.txt");
        ProblemInstance instance = reader.readProblem();

        BacktrackingSolver solver = new BacktrackingSolver(instance.graph, instance.source,
           instance.target, instance.constraints);
        BacktrackingSolver solver = new BacktrackingSolver(instance.graph, instance.source,
               instance.target);
        long solutionCount = solver.count();
        System.out.println("Solution cnt: " + solutionCount);
        for (long i = 0; i < solutionCount; ++i) {
            List<Integer> solution = solver.findKth(i + 1);
            for (int x : solution) {
                System.out.print(x + " ");
            }
            System.out.println();
        }

        RikudoPuzzle puzzle = new RikudoPuzzle(instance.graph, instance.source, instance.target,
            instance.constraints);
        System.out.println("Is unique:  " + puzzle.verifyUniqueness());
        System.out.println("Is minimal: " + puzzle.isMinimal());
         */

    GraphReader reader = new GraphReader("riXkudo_graph.txt", "riXkudo_constraints.txt");
    ProblemInstance instance = reader.readProblem();

    RikudoPuzzle puzzle = new RikudoPuzzle(instance.graph, instance.source, instance.target,
        instance.constraints);
    DesignProperties props = new DesignProperties();
    props.setEnableVertexConstraints(false);
    Constraints constraints = puzzle.design(new Random(), props);

    System.out.println("Vertex constraints:      " + constraints.getVertexConstraints());
    System.out.println("Diamond constraints:     " + constraints.getDiamondConstraints());
    System.out.println("Vertex constraints cnt:  " + constraints.countVertexConstraints());
    System.out.println("Diamond constraints cnt: " + constraints.countDiamondConstraints());
  }
}
