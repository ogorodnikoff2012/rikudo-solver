package fr.polytechnique.rikudo.hexagonal;

import fr.polytechnique.rikudo.solver.AdjListGraph;
import fr.polytechnique.rikudo.solver.IGraph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class GridGraph implements IGraph {
  private final ArrayList<Cell> cells;
  private final Hashtable<Cell, Integer> cellIndex;
  private final AdjListGraph graph;

  public GridGraph(HashSet<Cell> cellSet) {
    cells = new ArrayList<>(cellSet);
    cellIndex = new Hashtable<>();

    for (int i = 0; i < cells.size(); ++i) {
      cellIndex.put(cells.get(i), i);
    }

    graph = new AdjListGraph(cells.size());
    for (int cellIdx = 0; cellIdx < cells.size(); ++cellIdx) {
      Cell cell = cells.get(cellIdx);
      for (Cell neighbour : cell.getNeighbours()) {
        Integer neighbourIdx = cellIndex.get(neighbour);
        if (neighbourIdx != null) {
          graph.addEdge(cellIdx, neighbourIdx);
        }
      }
    }
  }

  @Override
  public boolean hasVertex(int vertex) {
    return graph.hasVertex(vertex);
  }

  @Override
  public boolean hasEdge(int from, int to) {
    return graph.hasEdge(from, to);
  }

  @Override
  public int size() {
    return graph.size();
  }

  @Override
  public List<Integer> adjacentVertices(int vertex) {
    return graph.adjacentVertices(vertex);
  }

  public Cell getCell(int vertex) {
    if (!hasVertex(vertex)) {
      return null;
    }
    return cells.get(vertex);
  }
}
