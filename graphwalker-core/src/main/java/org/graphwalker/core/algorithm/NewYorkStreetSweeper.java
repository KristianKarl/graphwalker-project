package org.graphwalker.core.algorithm;

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewYorkStreetSweeper extends Eulerian {

  private final Map<Vertex, PolarityCounter> balance;
  private Model model;

  public NewYorkStreetSweeper(Context context) {
    super(context);
    this.context = context;
    this.balance = new HashMap<>(context.getModel().getVertices().size());
    this.model = new  Model(context.getModel());
    getBalance();
    doBalance();
    context.setModel(model.build());
    this.polarities = new HashMap<>(context.getModel().getVertices().size());
    polarize();
  }

  private void doBalance() {
    while (notInBalance()) {
      System.out.println("===========");
      balance.forEach((k,v) -> System.out.println(k.getName() + ": " + v.getPolarity()));
      System.out.println("===========");
      for (Vertex vertex : model.getVertices()) {
        if (getBalanceCounter(vertex).getPolarity() < 0) {
          List<Edge> list = getListOfEdgesWithTargetVertex(vertex);
          for (Edge edge : list) {
            model.addEdge(new Edge()
              .setSourceVertex(edge.getSourceVertex())
              .setTargetVertex(edge.getTargetVertex())
              .setName(edge.getName()));
            getBalanceCounter(edge.getSourceVertex()).decrease();
            getBalanceCounter(edge.getTargetVertex()).increase();
          }
        }
      }
    }
    System.out.println("===========");
    balance.forEach((k,v) -> System.out.println(k.getName() + ": " + v.getPolarity()));
    System.out.println("===========");
  }

  private List getListOfEdgesWithTargetVertex(Vertex vertex) {
    List list = new ArrayList<Edge>();
    for (Edge edge : model.getEdges()) {
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
    for (Edge edge : model.getEdges()) {
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
