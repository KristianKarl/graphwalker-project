package org.graphwalker.core.condition;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.generator.SingletonRandomGenerator;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.*;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class ComplexConditionsTest {

  Vertex v_ClientNotRunning = new Vertex().setName("v_ClientNotRunning").setId("n1");
  Vertex v_LoginPrompted = new Vertex().setName("v_LoginPrompted").setId("n2");
  Vertex v_Browse = new Vertex().setName("v_Browse").setId("n3");

  Model model = new Model().
      addEdge(new Edge().setTargetVertex(v_ClientNotRunning).setName("e_Init").setId("e0").addAction(new Action("rememberMe=false;"))
                  .addAction(new Action("validLogin=true;"))).
      addEdge(new Edge().setSourceVertex(v_ClientNotRunning).setTargetVertex(v_LoginPrompted).setName("e_StartClient").setId("e1")
                  .setGuard(new Guard("!rememberMe||!validLogin"))).
      addEdge(new Edge().setSourceVertex(v_LoginPrompted).setTargetVertex(v_Browse).setName("e_ValidPremiumCredentials").setId("e2")
                  .addAction(new Action("validLogin=true;"))).
      addEdge(new Edge().setSourceVertex(v_Browse).setTargetVertex(v_LoginPrompted).setName("e_Logout").setId("e3")).
      addEdge(new Edge().setSourceVertex(v_Browse).setTargetVertex(v_ClientNotRunning).setName("e_Exit").setId("e4")).
      addEdge(new Edge().setSourceVertex(v_LoginPrompted).setTargetVertex(v_LoginPrompted).setName("e_ToggleRememberMe").setId("e5")
                  .addAction(new Action("rememberMe=!rememberMe;"))).
      addEdge(new Edge().setSourceVertex(v_LoginPrompted).setTargetVertex(v_ClientNotRunning).setName("e_Close").setId("e6")).
      addEdge(new Edge().setSourceVertex(v_ClientNotRunning).setTargetVertex(v_Browse).setName("e_StartClient").setId("e7")
                  .setGuard(new Guard("rememberMe&&validLogin"))).
      addEdge(new Edge().setSourceVertex(v_LoginPrompted).setTargetVertex(v_LoginPrompted).setName("e_InvalidCredentials").setId("e8")
                  .addAction(new Action("valdiLogin=false;")));

  @Test
  public void combinedCondition_2_vertices() throws Exception {
    SingletonRandomGenerator.setSeed(1111);
    Context context = new TestExecutionContext();
    CombinedCondition condition = new CombinedCondition();
    condition.addStopCondition(new ReachedVertex("v_Browse"));
    condition.addStopCondition(new ReachedVertex("v_LoginPrompted"));
    context.setModel(model.build()).setPathGenerator(new RandomPath(condition));
    context.setNextElement(context.getModel().findElements("e_Init").get(0));

    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getCurrentContext().getCurrentElement().getName(), is("v_LoginPrompted"));
  }

  @Test
  public void combinedCondition_2_edges() throws Exception {
    SingletonRandomGenerator.setSeed(1111);
    Context context = new TestExecutionContext();
    CombinedCondition condition = new CombinedCondition();
    condition.addStopCondition(new ReachedEdge("e_ToggleRememberMe"));
    condition.addStopCondition(new ReachedEdge("e_InvalidCredentials"));
    context.setModel(model.build()).setPathGenerator(new RandomPath(condition));
    context.setNextElement(context.getModel().findElements("e_Init").get(0));

    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getCurrentContext().getCurrentElement().getName(), is("e_InvalidCredentials"));
  }

  @Test
  public void combinedCondition_vertex_edge() throws Exception {
    SingletonRandomGenerator.setSeed(1111);
    Context context = new TestExecutionContext();
    CombinedCondition condition = new CombinedCondition();
    condition.addStopCondition(new ReachedVertex("v_Browse"));
    condition.addStopCondition(new ReachedEdge("e_InvalidCredentials"));
    context.setModel(model.build()).setPathGenerator(new RandomPath(condition));
    context.setNextElement(context.getModel().findElements("e_Init").get(0));

    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getCurrentContext().getCurrentElement().getName(), is("e_InvalidCredentials"));
  }

  @Test
  public void alternativeCondition_2_vertices() throws Exception {
    SingletonRandomGenerator.setSeed(1111);
    Context context = new TestExecutionContext();
    AlternativeCondition condition = new AlternativeCondition();
    condition.addStopCondition(new ReachedVertex("v_Browse"));
    condition.addStopCondition(new ReachedVertex("v_LoginPrompted"));
    context.setModel(model.build()).setPathGenerator(new RandomPath(condition));
    context.setNextElement(context.getModel().findElements("e_Init").get(0));

    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getCurrentContext().getCurrentElement().getName(), is("v_LoginPrompted"));
  }

  @Test
  public void alternativeCondition_2_edges() throws Exception {
    SingletonRandomGenerator.setSeed(1111);
    Context context = new TestExecutionContext();
    AlternativeCondition condition = new AlternativeCondition();
    condition.addStopCondition(new ReachedEdge("e_ToggleRememberMe"));
    condition.addStopCondition(new ReachedEdge("e_InvalidCredentials"));
    context.setModel(model.build()).setPathGenerator(new RandomPath(condition));
    context.setNextElement(context.getModel().findElements("e_Init").get(0));

    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getCurrentContext().getCurrentElement().getName(), is("e_InvalidCredentials"));
  }

  @Test
  public void alternativeCondition_vertex_edge() throws Exception {
    SingletonRandomGenerator.setSeed(1111);
    Context context = new TestExecutionContext();
    AlternativeCondition condition = new AlternativeCondition();
    condition.addStopCondition(new ReachedVertex("v_Browse"));
    condition.addStopCondition(new ReachedEdge("e_InvalidCredentials"));
    context.setModel(model.build()).setPathGenerator(new RandomPath(condition));
    context.setNextElement(context.getModel().findElements("e_Init").get(0));

    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getCurrentContext().getCurrentElement().getName(), is("e_InvalidCredentials"));
  }

  // random((reached_vertex(v_Browse) AND edge_coverage(100)) OR time(1000))
  @Test
  public void and_plus_or_a() throws Exception {
    SingletonRandomGenerator.setSeed(1111);
    Context context = new TestExecutionContext();

    CombinedCondition c1 = new CombinedCondition();
    c1.addStopCondition(new ReachedVertex("v_Browse"));
    c1.addStopCondition(new EdgeCoverage(100));

    AlternativeCondition c2 = new AlternativeCondition();
    c2.addStopCondition(new TimeDuration(1000, TimeUnit.SECONDS));
    c2.addStopCondition(c1);

    context.setModel(model.build()).setPathGenerator(new RandomPath(c2));
    context.setNextElement(context.getModel().findElements("e_Init").get(0));

    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getCurrentContext().getCurrentElement().getName(), is("v_Browse"));
  }

  // random((reached_vertex(v_Browse) AND edge_coverage(100)) OR reached_vertex(v_LoginPrompted))
  @Test
  public void and_plus_or_b() throws Exception {
    SingletonRandomGenerator.setSeed(1111);
    Context context = new TestExecutionContext();

    CombinedCondition c1 = new CombinedCondition();
    c1.addStopCondition(new ReachedVertex("v_Browse"));
    c1.addStopCondition(new EdgeCoverage(100));

    AlternativeCondition c2 = new AlternativeCondition();
    c2.addStopCondition(new ReachedVertex("v_LoginPrompted"));
    c2.addStopCondition(c1);

    context.setModel(model.build()).setPathGenerator(new RandomPath(c2));
    context.setNextElement(context.getModel().findElements("e_Init").get(0));

    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getCurrentContext().getCurrentElement().getName(), is("v_LoginPrompted"));
  }

  // random((reached_vertex(v_Browse) AND edge_coverage(100)) OR (reached_vertex(v_ClientNotRunning) AND edge_coverage(100)))
  @Test
  public void and_plus_or_c() throws Exception {
    SingletonRandomGenerator.setSeed(1111);
    Context context = new TestExecutionContext();

    CombinedCondition c1 = new CombinedCondition();
    c1.addStopCondition(new ReachedVertex("v_Browse"));
    c1.addStopCondition(new EdgeCoverage(100));

    CombinedCondition c2 = new CombinedCondition();
    c2.addStopCondition(new ReachedVertex("v_ClientNotRunning"));
    c2.addStopCondition(new EdgeCoverage(100));

    AlternativeCondition c3 = new AlternativeCondition();
    c3.addStopCondition(c1);
    c3.addStopCondition(c2);


    context.setModel(model.build()).setPathGenerator(new RandomPath(c3));
    context.setNextElement(context.getModel().findElements("e_Init").get(0));

    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getCurrentContext().getCurrentElement().getName(), is("v_Browse"));
  }
}
