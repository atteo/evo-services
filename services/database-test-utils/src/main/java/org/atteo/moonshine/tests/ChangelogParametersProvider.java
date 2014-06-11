
package org.atteo.moonshine.tests;

import java.util.Map;

/**
 * Provides parameters that can be used inside a Liquibase changelog.
 */
public interface ChangelogParametersProvider {

	/**
	 *
	 * @return map of changelog parameters
	 */
	Map<String, Object> getChangelogParameters();
}
