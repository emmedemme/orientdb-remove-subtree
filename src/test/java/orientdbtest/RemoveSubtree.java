package orientdbtest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

public class RemoveSubtree {

	private final static String STORAGE_ENGINE = "memory";
	private final static String DATABASE_URL = STORAGE_ENGINE + ":tmp/testGraph";

	private final static String ROOT_VERTEX_TYPE = "rvt";
	private final static String CHILD_VERTEX_TYPE = "cvt";
	private final static String EDGE_LABEL = "el";

	OrientGraphFactory graphFactory;
	OrientGraph graph;

	@Before
	public void setUpGraph() {
		graphFactory = new OrientGraphFactory(DATABASE_URL);
		setSchema();
		graphFactory.setAutoStartTx(false);
		graph = graphFactory.getTx();
	}
	
	private void setSchema() {
		final OrientGraphNoTx graphNoTx = graphFactory.getNoTx();
		graphNoTx.createVertexType(ROOT_VERTEX_TYPE);
		graphNoTx.createVertexType(CHILD_VERTEX_TYPE);
		graphNoTx.createEdgeType(EDGE_LABEL);
	}

	@After
	public void tearDownGraph() {
		graphFactory.drop();
	}
	
	@Test
	public void removeSubtreeDoesNotThrowException() {
		final Vertex root = buildTree();
		for (Vertex vertex : graph.getVertices()) {
			System.out.println("Vertex " + vertex);
			for (String key : vertex.getPropertyKeys()) {
				System.out.println("\t[" + key + "] -> "
						+ vertex.getProperty(key) + "]");
			}
			for (Edge edge : vertex.getEdges(Direction.BOTH)) {
				System.out.println("\t" + edge);
			}
		}
		removeSubtree(root);
	}
	
	private void removeSubtree(final Vertex root) {
		graph.begin();
		removeSubtreeBody(root);
		graph.commit();
	}
	
	private void removeSubtreeBody(final Vertex root) {
		for (Vertex directChild: root.getVertices(Direction.IN, EDGE_LABEL)) {
			removeSubtree(directChild);
		}
		root.remove();
	}
	
	private Vertex buildTree() {
		graph.begin();
		final Vertex root = addVertex(ROOT_VERTEX_TYPE);
		attachChild(root);
		attachChild(root);
		graph.commit();
		return root;
	}
	
	private Vertex attachChild(final Vertex parent) {
		final Vertex child = addVertex(CHILD_VERTEX_TYPE);
		graph.addEdge(null, child, parent, EDGE_LABEL);
		return child;
	}
	
	private Vertex addVertex(String vertexClass) {
		return graph.addVertex("class:" + vertexClass);
	}
}
