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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.rewrite.handler.VirtualHostRuleContainer;

@XmlRootElement(name = "virtualHost")
public class VirtualHostRuleConfig extends RuleConfig {
	@XmlElementWrapper(name = "hosts")
	@XmlElement(name = "host")
	private String[] virtualHosts = new String[0];

	@XmlElementWrapper(name = "rules")
	@XmlElementRef
	private List<RuleConfig> rules = new ArrayList<RuleConfig>();

	@Override
	public Rule getRule() {
		VirtualHostRuleContainer ruleContainer = new VirtualHostRuleContainer();
		for (RuleConfig rule : rules) {
			ruleContainer.addRule(rule.getRule());
		}
		ruleContainer.setVirtualHosts(virtualHosts);
		return ruleContainer;
	}
}
