
package org.atteo.moonshine.tests;

import java.util.HashMap;
import java.util.Map;

public class UsernameProvider implements ChangelogParametersProvider {

	@Override
	public Map<String, Object> getChangelogParameters() {
		Map<String, Object> map = new HashMap<>();

		map.put("username", "john");

		return map;
	}

}
