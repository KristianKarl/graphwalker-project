package org.graphwalker.core.algorithm;

import org.graphwalker.core.generator.SingletonRandomGenerator;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.*;
import org.junit.Assert;
import org.junit.Test;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NewYorkStreetSweeperTest {
  private final Vertex START = new Vertex().setName("START");
  private final Vertex A = new Vertex().setName("A");
  private final Vertex B = new Vertex().setName("B");
  private final Vertex C = new Vertex().setName("C");
  private final Vertex D = new Vertex().setName("D");

  private Model createKoenigsbegsProblem() {
    return new Model()
      .addEdge(new Edge().setSourceVertex(A).setTargetVertex(B).setName("a"))
      .addEdge(new Edge().setSourceVertex(B).setTargetVertex(C).setName("b"))
      .addEdge(new Edge().setSourceVertex(C).setTargetVertex(B).setName("c"))
      .addEdge(new Edge().setSourceVertex(A).setTargetVertex(D).setName("d"))
      .addEdge(new Edge().setSourceVertex(D).setTargetVertex(C).setName("e"))
      .addEdge(new Edge().setSourceVertex(C).setTargetVertex(D).setName("f"))
      .addEdge(new Edge().setSourceVertex(C).setTargetVertex(A).setName("g"));
  }

  @Test
  public void koenigsbergsProblemTest()  {
    SingletonRandomGenerator.setSeed(1349327921);

    Model model = createKoenigsbegsProblem();
    NewYorkStreetSweeper newYorkStreetSweeper = new NewYorkStreetSweeper(new TestExecutionContext().setModel(model.build()));
    assertThat(newYorkStreetSweeper.getEulerianType(), is(Eulerian.EulerianType.EULERIAN));

    List<String> path = new ArrayList<String>();
    newYorkStreetSweeper.getEulerPath(A.build()).stream().forEach(e -> path.add(e.getName()));
    Assert.assertEquals(path, new ArrayList<>(Arrays.asList("a", "B", "b", "C", "f", "D", "e", "C", "g", "A", "d", "D", "e", "C", "c", "B", "b", "C", "g", "A")));
  }

  @Test
  public void koenigsbergsProblemSemiEulerianTest()  {
    SingletonRandomGenerator.setSeed(1349327921);

    Model model = createKoenigsbegsProblem().addEdge(new Edge().setSourceVertex(START).setTargetVertex(A).setName("a"));
    NewYorkStreetSweeper newYorkStreetSweeper = new NewYorkStreetSweeper(new TestExecutionContext().setModel(model.build()));
    assertThat(newYorkStreetSweeper.getEulerianType(), is(Eulerian.EulerianType.SEMI_EULERIAN));

    List<String> path = new ArrayList<String>();
    newYorkStreetSweeper.getEulerPath(START.build()).stream().forEach(e -> path.add(e.getName()));
    Assert.assertEquals(path, new ArrayList<>(Arrays.asList("a", "B", "b", "C", "f", "D", "e", "C", "g", "A", "d", "D", "e", "C", "c", "B", "b", "C", "g", "A")));
  }
}
