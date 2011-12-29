/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.evo.firebuglogger;

import com.google.inject.Singleton;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class FireBugFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		try {
			HttpServletRequest req = (HttpServletRequest) request;
			String userAgent = req.getHeader("User-Agent");

			if (userAgent != null && userAgent.contains("FirePHP")) {

				HttpServletResponse resp = (HttpServletResponse) response;

				FireBugAppender.setServletResponse(resp);
				resp.setHeader("X-Wf-Protocol-1", "http://meta.wildfirehq.org/Protocol/JsonStream/0.2");
				resp.setHeader("X-Wf-1-Plugin-1",
						"http://meta.firephp.org/Wildfire/Plugin/FirePHP/Library-FirePHPCore/0.3");
				resp.setHeader("X-Wf-1-Structure-1",
						"http://meta.firephp.org/Wildfire/Structure/FirePHP/FirebugConsole/0.1");
			}

			chain.doFilter(request, response);
		} finally {
			FireBugAppender.setServletResponse(null);
		}
	}

	@Override
	public void destroy() {
	}

}
