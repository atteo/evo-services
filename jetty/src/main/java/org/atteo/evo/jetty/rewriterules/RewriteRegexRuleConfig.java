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
package org.atteo.evo.jetty.rewriterules;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.rewrite.handler.Rule;

/**
 * Rewrite the URI by matching with a regular expression.
 *
 * <p>
 * The replacement string may use $n" to replace the nth capture group.
 * If the replacement string contains ? character, then it is split
 * into a path and query string component. The replacement query string
 * may also contain $Q, which is replaced with the original query string.
 * The returned target contains only the path.
 * </p>
 */
@XmlRootElement(name = "rewriteRegex")
public class RewriteRegexRuleConfig extends RuleConfig {
	@XmlElement(required = true)
	private String regex;

	@XmlElement(required = true)
	private String replacement;

	@Override
	public Rule getRule() {
		RewriteRegexRule rule = new RewriteRegexRule();
		rule.setRegex(regex);
		rule.setReplacement(replacement);

		return rule;
	}
}
