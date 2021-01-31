package fr.polytechnique.rikudo.solver;

import java.util.List;

public interface IGraph {
  boolean hasVertex(int vertex);
  boolean hasEdge(int from, int to);
  int size();
  List<Integer> adjacentVertices(int vertex);
}
