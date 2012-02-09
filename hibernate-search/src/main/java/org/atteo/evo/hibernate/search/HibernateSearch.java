/*
 * Copyright 2012 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.hibernate.search;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.evo.hibernate.HibernatePlugin;
import org.hibernate.search.Environment;
import org.hibernate.search.indexes.spi.IndexManager;

@XmlRootElement(name = "search")
public class HibernateSearch extends HibernatePlugin {
	/**
	 * By default, every time an object is inserted, updated or deleted through Hibernate,
	 * Hibernate Search updates the according Lucene index. It is sometimes desirable
	 * to disable that features if either your index is read-only
	 * or if index updates are done in a batch way.
	 * To disable event based indexing, set this property to "manual".
	 */
	@XmlElement
	private String indexing_strategy = "event";

	/**
	 * Select implementations for {@link IndexManager} interface.
	 * Hibernate Search provides two implementations for this interface.
	 * <ul>
	 * <li>directory-based: the default implementation which uses the Lucene Directory
	 * abstraction to mange index files.</li>
	 * <li>near-real-time: avoid flushing writes to disk at each commit.
	 * This index manager is also Directory based, but also makes uses of Lucene's NRT functionality.
	 * </li>
	 * </ul>
	 * It is also possible to configure a custom IndexManager implementation
	 * by specifying the fully qualified class name of your custom implementation.
	 * This implementation must have a no-argument constructor.
	 */
	@XmlElement
	private String indexmanager = "directory-based";

	/**
	 * Directory provider to use.
	 * <ul>
	 * <li>ram: Memory based directory, the directory will be uniquely identified
	 * (in the same deployment unit) by the @Indexed.index element</li>
	 * <li>filesystem: File system based directory.
	 * The directory used will be &lt;indexBase&gt;/&lt;indexName&gt;</li>
	 * </ul>
	 * See Hibernate Search <a ref="http://docs.jboss.org/hibernate/search/4.1/reference/en-US/html/search-configuration.html#directory-provider-table">documentation</a> for more options and details.
	 */
	@XmlElement
	private String directory_provider = "filesystem";

	/**
	 * Base directory for "filesystem" directory provider.
	 */
	@XmlElement
	@XmlDefaultValue("${applicationHome}/indexes")
	private String indexBase;
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Environment.INDEXING_STRATEGY, indexing_strategy);
		map.put("hibernate.search.default." + Environment.INDEX_MANAGER_IMPL_NAME, indexmanager);
		map.put("hibernate.search.default.directory_provider", directory_provider);
		map.put("hibernate.search.default.indexBase", indexBase);
		return map;
	}
}
