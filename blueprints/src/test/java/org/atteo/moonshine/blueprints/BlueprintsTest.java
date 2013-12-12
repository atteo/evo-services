/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.blueprints;

import java.sql.SQLException;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.moonshine.tests.MoonshineConfiguration;
import org.atteo.moonshine.tests.MoonshineTest;
import org.junit.Test;

import com.google.inject.name.Named;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;


@MoonshineConfiguration(fromString = ""
		+ "<config>"
		+ "</config>")
public abstract class BlueprintsTest extends MoonshineTest {
	@Inject
	//This is because we want to let implementations (see Neo4j service for example) to perform tests on multiple
	//instances and same service defined multiple time is forbidden with none ID.
	@Named("default")
	private Graph graph;

	@Test
	public void shouldInjectGraph() {
		assertThat(graph).isNotNull();
	}

	@Test
	public void shouldPerformBasicOperationsOnGraph() throws SQLException {
		Vertex v = graph.addVertex(null);
		assertThat(v).isNotNull();
		assertThat(graph.getVertex(v.getId())).isEqualTo(v);
		graph.removeVertex(v);
		if (graph instanceof TransactionalGraph) {
			((TransactionalGraph)graph).commit();
		}
		assertThat(graph.getVertex(v.getId())).isNull();
	}
}
