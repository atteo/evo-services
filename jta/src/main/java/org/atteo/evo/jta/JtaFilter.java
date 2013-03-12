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
package org.atteo.evo.jta;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * {@link Filter} which wraps the request handling inside JTA transaction.
 */
@Singleton
public class JtaFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		try {
			Transaction.require(new Transaction.ThrowingRunnable<Exception>() {
				@Override
				public void run() throws IOException, ServletException {
					chain.doFilter(request, response);
				}
			});
		} catch (RuntimeException | ServletException | IOException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Internal error, unexpected exception", e);
		}
	}

	@Override
	public void destroy() {
	}
}
