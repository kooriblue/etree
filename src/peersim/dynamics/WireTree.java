package peersim.dynamics;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.graph.Graph;
import peersim.graph.GraphFactory;

public class WireTree extends WireGraph {

  //--------------------------------------------------------------------------
  //Parameters
  //--------------------------------------------------------------------------

  /**
   * The number of outgoing edges to generate from each node.
   * Passed to {@link GraphFactory#wireKOut}.
   * No loop edges are generated.
   * In the undirected case, the degree
   * of nodes will be on average almost twice as much because the incoming links
   * also become links out of each node.
   * @config
   */
  private static final String PAR_DEGREE = "k";

  //--------------------------------------------------------------------------
  //Fields
  //--------------------------------------------------------------------------

  /**
   * The number of outgoing edges to generate from each node.
   */
  private final int k;

  //--------------------------------------------------------------------------
  //Initialization
  //--------------------------------------------------------------------------

  /**
   * Standard constructor that reads the configuration parameters.
   * Invoked by the simulation engine.
   * @param prefix the configuration prefix for this class
   */
  public WireTree(String prefix)
  {
      super(prefix);
      k = Configuration.getInt(prefix + "." + PAR_DEGREE);
  }

  //--------------------------------------------------------------------------
  //Methods
  //--------------------------------------------------------------------------

  /** Calls {@link GraphFactory#wireKOut}. */
  public void wire(Graph g) {

      GraphFactory.wireTree(g,k,CommonState.r);
  }

}
