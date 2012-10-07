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

import org.eclipse.jetty.rewrite.handler.RewritePatternRule;
import org.eclipse.jetty.rewrite.handler.Rule;

/**
 * Rewrite the URI by replacing the matched PathMap path with a fixed string.
 * <p>
 * The replacement string may use $n" to replace the nth capture group.
 * </p>
 */
@XmlRootElement(name = "rewritePattern")
public class RewritePatternRuleConfig extends RuleConfig {
	@XmlElement(required = true)
	private String pattern;

	@XmlElement(required = true)
	private String replacement;

	@Override
	public Rule getRule() {
		RewritePatternRule rule = new RewritePatternRule();
		rule.setPattern(pattern);
		rule.setReplacement(replacement);

		return rule;
	}
}
