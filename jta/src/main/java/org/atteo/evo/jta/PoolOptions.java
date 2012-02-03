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
package org.atteo.evo.jta;

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
	private Integer minPoolSize;

	/**
	 * Sets the maximum pool size. The amount of pooled connections won't go
	 * above this value. Optional, defaults to 1.
	 */
	@XmlElement
	private Integer maxPoolSize;

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
}
