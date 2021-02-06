package fr.polytechnique.rikudo.puzzle;

import fr.polytechnique.rikudo.benchmark.Benchmark.ProblemInstance;
import fr.polytechnique.rikudo.benchmark.Benchmark;
import fr.polytechnique.rikudo.solver.*;

import java.util.*;

public class RikudoPuzzle {
    private final IGraph graph;
    private final int source;
    private final int target;
    public final Constraints constraints;
    private Constraints constraints_to_replace;

    public RikudoPuzzle(IGraph graph, int source, int target){
        this(graph, source, target, new Constraints());
    }

    public RikudoPuzzle(IGraph graph, int source, int target, Constraints constraints) {
        this.graph = graph;
        this.source = source;
        this.target = target;
        this.constraints = constraints;
        this.constraints_to_replace = new Constraints();
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
                constraints_to_replace.addVertexConstraint(vertex, position);
                constraints.addVertexConstraint(vertex, position);
                //System.err.println("Vertex constraint " + vertex + " " + position);
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
                    constraints_to_replace.addDiamondConstraint(v1, v2);
                    constraints.addDiamondConstraint(v1, v2);
                    //System.err.println("Diamond constraint " + v1 + " " + v2);
                    return false;
                }
                else {
                    constraints.addDiamondConstraint(v1, v2);
                }
            }
        }
        return true;
    }

    public void design() {
        //BacktrackingSolver solver = new BacktrackingSolver(graph, source, target, constraints);
        //List<Integer> foundPath = solver.findKth(1);
        IHamPathSolver solver = new ReducingToSATSolver(graph, source, target,
                                ReducingToSATSolver.Mode.E_MODE_PATH, constraints);
        List<Integer> foundPath = solver.solve();
        if (foundPath == null) {
            System.out.println("Rikudo puzzle can not be created: there is no hamiltonian path!");
            return;
        }
        System.out.println("Hamiltonian path: " + foundPath);
        for (int i = 0; i < foundPath.size() - 1; i++) {
            constraints.addVertexConstraint(foundPath.get(i), i);
        }
        for (int i = 0; i < foundPath.size() - 1; i++) {
            constraints.addDiamondConstraint(foundPath.get(i), foundPath.get(i+1));
        }

        while (!isMinimal()) {
            Hashtable<Integer, Integer> vertexConstraints = constraints_to_replace.getVertexConstraints();
            Set<Integer> setOfVertices = vertexConstraints.keySet();

            for (Integer vertex : setOfVertices) {
                int position = vertexConstraints.get(vertex);
                constraints.removeVertexConstraint(vertex, position);
            }
            Hashtable<Integer, HashSet<Integer>> diamondConstraints = constraints_to_replace.getDiamondConstraints();
            Set<Integer> listOfDiamonds = diamondConstraints.keySet();

            for (Integer v1 : listOfDiamonds) {
                HashSet<Integer> neighbours = diamondConstraints.get(v1);
                for (Integer v2 : neighbours) {
                    if (v1 < v2) {
                        continue;
                    }
                    constraints.removeDiamondConstraint(v1, v2);
                }
            }
        }
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

        GraphReader reader = new GraphReader("riXkudo_graph.txt", "constraints.txt");
        ProblemInstance instance = reader.readProblem();

        RikudoPuzzle puzzle = new RikudoPuzzle(instance.graph, instance.source, instance.target,
                instance.constraints);
        puzzle.design();
        //System.out.println("Diamond constraints:  " + puzzle.constraints.getDiamondConstraints());
        System.out.println("Vertex constraints:  " + puzzle.constraints.getVertexConstraints());
        //System.out.println("Diamond constraints:  " + puzzle.constraints_to_replace.getDiamondConstraints());
        System.out.println("Vertex constraints:  " + puzzle.constraints_to_replace.getVertexConstraints());
    }
}
