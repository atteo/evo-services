/*
 * Copyright 2011 Atteo.
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
package org.atteo.moonshine.jta;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import javax.sql.XADataSource;

/**
 * Wraps {@link XADataSource} into {@link DataSource} adding transaction support.
 */
public interface JtaDataSourceWrapper {
	/**
	 * Wrap given {@link XADataSource} into {@link DataSource} adding transaction support.
	 * @param name unique name for the returned {@link DataSource}
	 * @param xaDataSource {@link XADataSource} to wrap
	 * @param poolOptions options for the connection pool
	 * @param testQuery SQL query or statement used to validate a connection before returning it
	 * @return wrapped {@link DataSource}
	 */
	DataSource wrap(String name, XADataSource xaDataSource, @Nullable PoolOptions poolOptions, String testQuery);

	/**
	 * Close previously wrapped data source.
	 * @param dataSource data source to close
	 */
	void close(DataSource dataSource);
}
