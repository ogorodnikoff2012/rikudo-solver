package fr.polytechnique.rikudo.solver;

import java.util.ArrayList;
import java.util.List;

public class BacktrackingSolver implements IHamPathSolver {

  private final IGraph graph;
  private final int source;
  private final int target;
  private List<Integer> found_path;
  private long path_cnt;

  public BacktrackingSolver(IGraph graph, int source, int target) {
    this.graph = graph;
    this.source = source;
    this.target = target;
    this.found_path = null;
    this.path_cnt = 0;
  }

  private void reset() {
    found_path = null;
    path_cnt = 0;
  }

  private boolean find_path(int vertex, ArrayList<Integer> path, boolean[] is_visited,
      int max_path_cnt) {
    if (vertex == target) {
      if (path.size() == graph.size()) {
        found_path = path;
        ++path_cnt;
        return max_path_cnt > 0 && path_cnt >= max_path_cnt;
      } else {
        return false;
      }
    }

    List<Integer> adjacent_vertices = graph.adjacentVertices(vertex);
    for (int adjacent_vert : adjacent_vertices) {
      if (!is_visited[adjacent_vert]) {
        is_visited[adjacent_vert] = true;
        path.add(adjacent_vert);

        if (find_path(adjacent_vert, path, is_visited, max_path_cnt)) {
          return true;
        }

        is_visited[adjacent_vert] = false;
        path.remove(path.size() - 1);
      }
    }

    return false;
  }

  private boolean find_path_trampoline(int max_path_cnt) {
    boolean[] visited_vertices = new boolean[graph.size()];
    visited_vertices[source] = true;
    ArrayList<Integer> path = new ArrayList<>();
    path.add(source);
    return find_path(source, path, visited_vertices, max_path_cnt);
  }

  @Override
  public List<Integer> solve() {
    reset();
    find_path_trampoline(1);
    return found_path;
  }

  public long count() {
    reset();
    find_path_trampoline(0);
    return path_cnt;
  }

  public static void main(String[] args) {
    MatrixGraph graph = new MatrixGraph(4);
    graph.addEdge(0, 1);
    graph.addEdge(0, 2);
    graph.addEdge(1, 2);
    graph.addEdge(1, 3);
    graph.addEdge(2, 1);
    graph.addEdge(2, 3);
    graph.addEdge(3, 0);

    IHamPathSolver solver = new BacktrackingSolver(graph, 0, 3);
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
