package fr.polytechnique.rikudo.puzzle;

import fr.polytechnique.rikudo.solver.BacktrackingSolver;
import fr.polytechnique.rikudo.solver.Constraints;
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
        Set<Integer> setOfVertices = vertexConstraints.keySet();

        for(Integer vertex : setOfVertices) {
            int position = vertexConstraints.get(vertex);
            constraints.removeVertexConstraint(vertex, position);
            if (verifyUniqueness()) {
                constraints.addVertexConstraint(vertex, position);
                return false;
            }
            else {
                constraints.addVertexConstraint(vertex, position);
            }
        }

        Hashtable<Integer, HashSet<Integer>> diamondConstraints = constraints.getDiamondConstraints();
        Set<Integer> listOfDiamonds = diamondConstraints.keySet();

        for (Integer v1 : listOfDiamonds) {
            HashSet<Integer> neighbours = diamondConstraints.get(v1);
            for (Integer v2 : neighbours) {
                if (v1 < v2) {
                    continue;
                }
                constraints.removeDiamondConstraint(v1, v2);
                if (verifyUniqueness()) {
                    constraints.addDiamondConstraint(v1, v2);
                    return false;
                }
                else {
                    constraints.addDiamondConstraint(v1, v2);
                }
            }
        }
        return true;
    }

}
