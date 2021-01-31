package fr.polytechnique.rikudo.solver;

import java.util.ArrayList;

public class MatrixGraph implements IGraph {
  private final int kVertexCount;
  private final boolean[][] edgeMatrix;

  public MatrixGraph(int vertexCount) {
    this.kVertexCount = vertexCount;
    this.edgeMatrix = new boolean[vertexCount][vertexCount];
  }

  public boolean removeEdge(int from, int to) {
    if (!hasVertex(from) || !hasVertex(to) || !hasEdge(from, to)) {
      return false;
    }

    edgeMatrix[from][to] = false;
    return true;
  }

  public boolean addEdge(int from, int to) {
    if (!hasVertex(from) || !hasVertex(to) || hasEdge(from, to)) {
      return false;
    }

    edgeMatrix[from][to] = true;
    return true;
  }

  @Override
  public boolean hasVertex(int vertex) {
    return vertex >= 0 && vertex < kVertexCount;
  }

  @Override
  public boolean hasEdge(int from, int to) {
    return hasVertex(from) && hasVertex(to) && edgeMatrix[from][to];
  }

  @Override
  public int size() {
    return kVertexCount;
  }

  @Override
  public ArrayList<Integer> adjacentVertices(int vertex) {
    if (!hasVertex(vertex)) {
      return null;
    }

    ArrayList<Integer> vertices = new ArrayList<>();
    for (int i = 0; i < kVertexCount; ++i) {
      if (edgeMatrix[vertex][i]) {
        vertices.add(i);
      }
    }

    return vertices;
  }
}
