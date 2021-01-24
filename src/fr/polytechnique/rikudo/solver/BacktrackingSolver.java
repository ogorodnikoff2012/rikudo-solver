package fr.polytechnique.rikudo.solver;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class BacktrackingSolver {
    private final IGraph graph;
    private final int source;
    private final int target;
    private boolean ham_path = false;

    public BacktrackingSolver(IGraph graph, int source, int target) {
        this.graph = graph;
        this.source = source;
        this.target = target;
    }

    private static void print_path(Stack<Integer> path){
        System.out.print("Hamiltonian path found: ");
        for (int elem : path){
            System.out.print(elem + " ");
        }
        System.out.println();
    }

    private void find_path(int vertex, Stack<Integer> path, int[] visited_vertices){
        if (vertex == target && path.size() == graph.size()){
            ham_path = true;
            print_path(path);
            return;
        }

        ArrayList<Integer> adjacent_vertices = graph.adjacentVertices(vertex);
        for (int adjacent_vert : adjacent_vertices){
            if (visited_vertices[adjacent_vert] == 0){
                visited_vertices[adjacent_vert] = 1;
                path.push(adjacent_vert);
                find_path(adjacent_vert, path, visited_vertices);

                visited_vertices[adjacent_vert] = 0;
                path.pop();
            }
        }
    }

    public void solve() {
        int[] visited_vertices = new int[graph.size()];
        visited_vertices[source] = 1;
        Stack<Integer> path = new Stack<>();
        path.push(source);
        find_path(source, path, visited_vertices);
        if (!ham_path){
            System.out.println("No hamiltonian path found!");
        }
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

        BacktrackingSolver solver = new BacktrackingSolver(graph, 0, 3);
        solver.solve();
    }
}
