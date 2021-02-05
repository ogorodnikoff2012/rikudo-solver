package fr.polytechnique.rikudo.puzzle;

import fr.polytechnique.rikudo.benchmark.Benchmark.ProblemInstance;
import fr.polytechnique.rikudo.solver.BacktrackingSolver;
import fr.polytechnique.rikudo.solver.Constraints;
import fr.polytechnique.rikudo.solver.GraphReader;
import fr.polytechnique.rikudo.solver.IGraph;

import java.util.*;

public class RikudoPuzzle {
    private final IGraph graph;
    private final int source;
    private final int target;
    public final Constraints constraints;

    public RikudoPuzzle(IGraph graph, int source, int target, Constraints constraints) {
        this.graph = graph;
        this.source = source;
        this.target = target;
        this.constraints = constraints;
    }

    public boolean verifyUniqueness() {
        BacktrackingSolver solver = new BacktrackingSolver(graph, source, target, constraints);
        if (solver.findKth(2) == null){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isMinimal() {
        Hashtable<Integer, Integer> vertexConstraints = constraints.getVertexConstraints();
        Set<Integer> setOfVertices = new HashSet<Integer>(vertexConstraints.keySet());

        for(Integer vertex : setOfVertices) {
            if (vertex == source || vertex == target) {
                continue;
            }
            int position = vertexConstraints.get(vertex);
            constraints.removeVertexConstraint(vertex, position);
            if (verifyUniqueness()) {
                constraints.addVertexConstraint(vertex, position);
                System.err.println("Vertex constraint " + vertex + " " + position);
                return false;
            }
            else {
                constraints.addVertexConstraint(vertex, position);
            }
        }

        Hashtable<Integer, HashSet<Integer>> diamondConstraints = constraints.getDiamondConstraints();
        Set<Integer> listOfDiamonds = new HashSet(diamondConstraints.keySet());

        for (Integer v1 : listOfDiamonds) {
            HashSet<Integer> neighbours = diamondConstraints.get(v1);
            for (Integer v2 : neighbours) {
                if (v1 < v2) {
                    continue;
                }
                constraints.removeDiamondConstraint(v1, v2);
                if (verifyUniqueness()) {
                    constraints.addDiamondConstraint(v1, v2);
                    System.err.println("Diamond constraint " + v1 + " " + v2);
                    return false;
                }
                else {
                    constraints.addDiamondConstraint(v1, v2);
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        GraphReader reader = new GraphReader("riXkudo_graph.txt", "riXkudo_constraints.txt");
        ProblemInstance instance = reader.readProblem();

        BacktrackingSolver solver = new BacktrackingSolver(instance.graph, instance.source,
            instance.target, instance.constraints);
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
    }
}
