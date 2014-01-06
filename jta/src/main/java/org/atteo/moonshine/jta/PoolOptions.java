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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.Configurable;

@XmlRootElement(name = "pool")
public class PoolOptions extends Configurable {

	/**
	 * Sets the minimum pool size.
	 * The amount of pooled connections won't go below that value.
	 * The pool will open this amount of connections during initialization.
	 */
	@XmlElement
	private Integer minPoolSize = 2;

	/**
	 * Sets the maximum pool size. The amount of pooled connections won't go
	 * above this value.
	 */
	@XmlElement
	private Integer maxPoolSize = 25;

	/**
	 * Sets the maximum amount of seconds that unused excess connections should stay in the pool. Optional.
	 *
	 * Note: excess connections are connections that are created above the minPoolSize limit.
	 * Note that this value is only an indication; the pool will check regularly as indicated
	 * by the maintenanceInteval property.
	 */
	@XmlElement
	private Integer maxIdleTime;

	/**
	 * Sets the amount of time (in seconds) that the connection pool will allow a connection
	 * to be in use, before claiming it back. Optional.
	 *
	 * The timeout in seconds. Zero means unlimited. Note that this value is
	 * only an indication; the pool will check regularly as indicated by the maintenanceInteval property.
	 */
	@XmlElement
	private Integer reapTimeout;

	/**
	 * Sets the maximum amount of seconds that a connection is kept in the pool before it is destroyed automatically.
	 * Optional, defaults to 0 (no limit).
	 * <p>
	 * This makes transaction manager aware of how long it can keep connection and so removes the need for a test query.
	 * This, in turn, improves performance of the pool because borrowing a connection no longer implies
	 * a roundtrip to the database (inside a synchronized block!).
	 * </p>
	 * <p>
	 * Set this to 0, to use test queries instead.
	 * </p>
	 */
	@XmlElement
	private Integer maxLifeTime = 120;

	public Integer getMinPoolSize() {
		return minPoolSize;
	}

	public Integer getMaxPoolSize() {
		return maxPoolSize;
	}

	public Integer getMaxIdleTime() {
		return maxIdleTime;
	}

	public Integer getReapTimeout() {
		return reapTimeout;
	}

	public Integer getMaxLifeTime() {
		return maxLifeTime;
	}
}
