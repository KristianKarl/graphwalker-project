package org.graphwalker.core.algorithm;

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NewYorkStreetSweeper extends Eulerian {

  private final Map<Vertex, PolarityCounter> balance;

  // Since elements are going to be added to the model, we need a
  // copy model to work on.
  private Model modelToBalance;
  private Model finalModel;

  private List<Edge> addedBalancedEdges = new ArrayList<>();

  public NewYorkStreetSweeper(Context context) {
    super(context);
    this.context = context;
    this.balance = new HashMap<>(context.getModel().getVertices().size());
    this.modelToBalance = new  Model(context.getModel());
    this.finalModel = new  Model(context.getModel());
  }

  public Path<Element> getEulerPath(Element element) {
    removeNoneCyclicEntry();
    getBalance();
    doBalance();

    context.setModel(modelToBalance.build());
    this.polarities = new HashMap<>(context.getModel().getVertices().size());
    polarize();

    addedBalancedEdges.stream().forEach(e -> finalModel.addEdge(e));
    context.setModel(finalModel.build());

    if (EulerianType.NOT_EULERIAN.equals(getEulerianType())) {
      throw new AlgorithmException("The model is not eulerian or semi eulerian, no single path can cover the entire graph");
    }
    return context.getAlgorithm(Fleury.class).getTrail(element);
  }

  private void removeNoneCyclicEntry() {
    Vertex toBeRemovedStartVertex = null;
    Vertex toBeRemovedEndVertex = null;


    // Check for vertices that has only out-edges
    // There is only 1 such vertex allowed
    for (Vertex vertex : modelToBalance.getVertices()) {
      Stream<Edge> inEdges = modelToBalance.getEdges().stream().filter(e -> e.getTargetVertex() == vertex );
      if (inEdges.count() == 0) {
        Stream<Edge> outEdges = modelToBalance.getEdges().stream().filter(e -> e.getSourceVertex() == vertex );
        if (outEdges.count() > 1) {
          throw new AlgorithmException("The model cannot be balanced. This vertex has no in-edges and more than 1 out-edge: " + vertex.getName());
        }
        if (toBeRemovedStartVertex != null ) {
          throw new AlgorithmException("The model cannot be balanced. It has more than 1 entry vertex");
        }
        toBeRemovedStartVertex = vertex;
      }
    }


    // Check for vertices that has only 1 in-edge
    // There is only 1 such vertex allowed
    // Only 1 in-edge is allowed
    for (Vertex vertex : modelToBalance.getVertices()) {
      List<Edge> inEdges = modelToBalance.getEdges().stream().filter(e -> e.getTargetVertex() == vertex ).collect(Collectors.toList());
      List<Edge> outEdges = modelToBalance.getEdges().stream().filter(e -> e.getSourceVertex() == vertex ).collect(Collectors.toList());;

      if (outEdges.size() == 0 && inEdges.size() == 1) {
        if (toBeRemovedEndVertex != null ) {
          throw new AlgorithmException("The model cannot be balanced. It has more than 1 exit vertex");
        }
        toBeRemovedEndVertex = vertex;
      } else if (outEdges.size() == 0 && inEdges.size() > 1) {
        throw new AlgorithmException("The model cannot be balanced. This vertex has more then 1 in-edges: " + vertex.getName());
      }
    }


    // Check for edges that has no start vertex set
    // There is only 1 such edge allowed
    List<Edge> noStartVertexEdges = new ArrayList<>();
    modelToBalance.getEdges().stream().forEach(e -> {
      if (e.getSourceVertex() == null) {
        noStartVertexEdges.add(e);
      }
    });
    if ( noStartVertexEdges.size() > 1 ) {
      throw new AlgorithmException("The model cannot be balanced. Only 1 edge with no start vertex is allowed. Found: "
        + noStartVertexEdges.stream().map(Edge::getName).collect(Collectors.joining(", ")));
    } else if ( noStartVertexEdges.size() == 1 ) {
      modelToBalance.deleteEdge(noStartVertexEdges.get(0));
    }


    if (toBeRemovedStartVertex != null) {
      modelToBalance.deleteVertex(toBeRemovedStartVertex);
    }
    if (toBeRemovedEndVertex != null) {
      modelToBalance.deleteVertex(toBeRemovedEndVertex);
    }
  }

  private void doBalance() {
    while (notInBalance()) {
//      System.out.println("===========");
//      balance.forEach((k,v) -> System.out.println( (k == null ? "null" :  k.getName()) + ": " + v.getPolarity()));
//      System.out.println("===========");
      for (Vertex vertex : modelToBalance.getVertices()) {
        if (getBalanceCounter(vertex).getPolarity() < 0) {
          List<Edge> list = getListOfEdgesWithTargetVertex(vertex);
          for (Edge edge : list) {
            Edge e = new Edge()
              .setSourceVertex(edge.getSourceVertex())
              .setTargetVertex(edge.getTargetVertex())
              .setName(edge.getName());
            addedBalancedEdges.add(e);
            modelToBalance.addEdge(e);
            getBalanceCounter(edge.getSourceVertex()).decrease();
            getBalanceCounter(edge.getTargetVertex()).increase();
          }
        }
      }
    }
//    System.out.println("===========");
//    balance.forEach((k,v) -> System.out.println(k.getName() + ": " + v.getPolarity()));
//    System.out.println("===========");
  }

  private List getListOfEdgesWithTargetVertex(Vertex vertex) {
    List list = new ArrayList<Edge>();
    for (Edge edge : modelToBalance.getEdges()) {
      if (edge.getTargetVertex() == vertex) {
        list.add(edge);
      }
    }
    return list;
  }

  private boolean inBalance() {
    for (PolarityCounter polarityCounter : balance.values()) {
      if (polarityCounter.hasPolarity()) {
        return false;
      }
    }
    return true;
  }

  private boolean notInBalance() {
    return !inBalance();
  }

  public void getBalance() {
    for (Edge edge : modelToBalance.getEdges()) {
      getBalanceCounter(edge.getSourceVertex()).decrease();
      getBalanceCounter(edge.getTargetVertex()).increase();
    }
  }

  private PolarityCounter getBalanceCounter(Vertex vertex) {
    if (!balance.containsKey(vertex)) {
      balance.put(vertex, new PolarityCounter());
    }
    return balance.get(vertex);
  }
}
