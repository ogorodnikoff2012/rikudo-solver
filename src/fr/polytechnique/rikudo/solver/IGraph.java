package fr.polytechnique.rikudo.solver;

import java.util.ArrayList;

public interface IGraph {
  boolean hasVertex(int vertex);
  boolean hasEdge(int from, int to);
  int size();
  ArrayList<Integer> adjacentVertices(int vertex);
}
