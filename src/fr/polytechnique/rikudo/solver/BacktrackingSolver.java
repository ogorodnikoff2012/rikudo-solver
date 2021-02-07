package fr.polytechnique.rikudo.solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BacktrackingSolver implements IHamPathSolver {

  private final IGraph graph;
  private final int source;
  private final int target;
  private final Constraints constraints;
  private List<Integer> foundPath;
  private long pathCnt;

  public BacktrackingSolver(IGraph graph, int source, int target){
    this(graph, source, target, new Constraints());
  }

  public BacktrackingSolver(IGraph graph, int source, int target, Constraints constraints) {
    this.graph = graph;
    this.source = source;
    this.target = target;
    this.foundPath = null;
    this.pathCnt = 0;
    this.constraints = constraints;
  }

  private void reset() {
    foundPath = null;
    pathCnt = 0;
  }

  private List<Integer> applyDiamondConstraints(int vertex, boolean[] isVisited){
    List<Integer> adjacentVertices = graph.adjacentVertices(vertex);
    HashSet<Integer> diamonds = constraints.getDiamondedNeighbours(vertex);
    if (diamonds == null){
      return adjacentVertices;
    }
    HashSet<Integer> diamondsTmp = new HashSet<>(diamonds);
    diamonds.retainAll(adjacentVertices);
    for (int vert : diamonds){
      if (isVisited[vert]){
        diamondsTmp.remove(vert);
      }
    }
    if (!diamondsTmp.isEmpty()){
      return new ArrayList<>(diamondsTmp);
    } else{
      return adjacentVertices;
    }
  }

  private boolean findPath(int vertex, ArrayList<Integer> path, boolean[] isVisited,
      long maxPathCnt) {
    if (vertex == target) {
      if (path.size() == graph.size()) {
        foundPath = path;
        ++pathCnt;
        return maxPathCnt > 0 && pathCnt >= maxPathCnt;
      } else {
        return false;
      }
    }
    //System.out.println(path);
    List<Integer> adjacentVertices = applyDiamondConstraints(vertex, isVisited);
    for (int adjacentVert : adjacentVertices) {
      if (!isVisited[adjacentVert] && constraints.isAllowedVertex(adjacentVert, path.size())) {
        isVisited[adjacentVert] = true;
        path.add(adjacentVert);

        if (findPath(adjacentVert, path, isVisited, maxPathCnt)) {
          return true;
        }

        isVisited[adjacentVert] = false;
        path.remove(path.size() - 1);
      }
    }

    return false;
  }

  private boolean findPathTrampoline(long max_path_cnt) {
    boolean[] visitedVertices = new boolean[graph.size()];
    visitedVertices[source] = true;
    ArrayList<Integer> path = new ArrayList<>();
    path.add(source);
    return findPath(source, path, visitedVertices, max_path_cnt);
  }

  @Override
  public List<Integer> solve() {
    reset();
    findPathTrampoline(1);
    return foundPath;
  }

  public long count() {
    reset();
    findPathTrampoline(0);
    return pathCnt;
  }

  public List<Integer> findKth(long k) {
    reset();
    if (!findPathTrampoline(k)) {
      return null;
    }
    return foundPath;
  }

  public static void main(String[] args) {
    AdjListGraph graph = new AdjListGraph(5);
    for (int i = 0; i < graph.size(); ++i) {
      for (int j = 0; j < graph.size(); ++j) {
        if (i != j) {
          graph.addEdge(i, j);
        }
      }
    }

    Constraints constraints = new Constraints();
    constraints.addDiamondConstraint(0, 3);
    constraints.addVertexConstraint(2,2);

    IHamPathSolver solver = new BacktrackingSolver(graph, 0, graph.size() - 1, constraints);
    List<Integer> hamPath = solver.solve();
    if (hamPath == null) {
      System.out.println("No hamiltonian path found!");
    } else {
      System.out.print("Hamiltonian path found: ");
      for (int vertex : hamPath) {
        System.out.print(vertex + " ");
      }
      System.out.println();
    }
  }
}
