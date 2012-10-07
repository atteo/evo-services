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
package org.atteo.evo.jetty;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.injection.InjectMembers;
import org.atteo.evo.jetty.rewriterules.RuleConfig;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;

@XmlRootElement(name = "rewrite")
public class RewriteHandlerConfig extends HandlerWrapperConfig {
	@XmlElementWrapper(name = "rules")
	@XmlElementRef
	@InjectMembers
	private List<RuleConfig> rules;

	/**
	 * true if this handler should rewrite the value returned by HttpServletRequest.getRequestURI().
	 */
	private boolean rewriteURI = true;

	@Override
	protected HandlerWrapper createHandler() {
		RewriteHandler handler = new RewriteHandler();

		for (RuleConfig rule : rules) {
			handler.addRule(rule.getRule());
		}
		handler.setRewriteRequestURI(rewriteURI);

		return handler;
	}
}
