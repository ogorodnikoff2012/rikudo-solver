package fr.polytechnique.rikudo.solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class Constraints {
    private final Hashtable<Integer, Integer> pos_to_vertex_constraints = new Hashtable<>();
    private final Hashtable<Integer, Integer> vertex_to_pos_constraints = new Hashtable<>();
    private final Hashtable<Integer, HashSet<Integer>> diamond_constraints = new Hashtable<>();

    private void addToTable(int v1, int v2) {
      if (!diamond_constraints.contains(v1)) {
        diamond_constraints.put(v1, new HashSet<>());
      }
      diamond_constraints.get(v1).add(v2);
    }

    public void addVertexConstraint(int vertex, int position) {
      pos_to_vertex_constraints.put(position, vertex);
      vertex_to_pos_constraints.put(vertex, position);
    }

    public void addDiamondConstraint(int v1, int v2) {
      addToTable(v1, v2);
      addToTable(v2, v1);
    }

    public boolean isAllowedVertex(int vertex, int position) {
      if (pos_to_vertex_constraints.containsKey(position) && pos_to_vertex_constraints.get(position) != vertex) {
          return false;
      }
      if (vertex_to_pos_constraints.containsKey(vertex) && vertex_to_pos_constraints.get(vertex) != position) {
          return false;
      }
      return true;
    }

    public Hashtable<Integer, Integer> getVertexConstraints() {
          return vertex_to_pos_constraints;
    }

    public Integer getVertexByPos(int pos) {
      return pos_to_vertex_constraints.get(pos);
    }

    public HashSet<Integer> getDiamondedNeighbours(int vertex) {
      return diamond_constraints.get(vertex);
    }
}
