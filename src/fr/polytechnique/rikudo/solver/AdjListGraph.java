package fr.polytechnique.rikudo.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AdjListGraph implements IGraph {
  private final int kVertexCount;
  private final HashMap<Integer, HashSet<Integer>> edges;

  public AdjListGraph(int vertexCount) {
    this.kVertexCount = vertexCount;
    edges = new HashMap<>();
  }

  public boolean addEdge(int from, int to) {
    if (!hasVertex(from) || !hasVertex(to)) {
      return false;
    }

    if (!edges.containsKey(from)) {
      edges.put(from, new HashSet<>());
    }
    if (edges.get(from).contains(to)) {
      return false;
    }
    edges.get(from).add(to);
    return true;
  }

  @Override
  public boolean hasVertex(int vertex) {
    return 0 <= vertex && vertex < kVertexCount;
  }

  @Override
  public boolean hasEdge(int from, int to) {
    HashSet<Integer> neighbours = edges.get(from);
    return neighbours != null && neighbours.contains(to);
  }

  @Override
  public int size() {
    return kVertexCount;
  }

  @Override
  public List<Integer> adjacentVertices(int vertex) {
    if (!hasVertex(vertex)) {
      return null;
    }

    HashSet<Integer> neighbours = edges.get(vertex);
    if (neighbours == null) {
      return new ArrayList<Integer>();
    }

    return new ArrayList<>(neighbours);
  }
}
