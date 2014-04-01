
package org.atteo.moonshine.tests;

import java.util.Collections;
import java.util.Map;

public class EmptyChangelogParametersProvider implements ChangelogParametersProvider {

	@Override
	public Map<String, Object> getChangelogParameters() {
		return Collections.EMPTY_MAP;
	}

}
