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
package org.atteo.evo.config.doclet;

public class HtmlWriter {
	private StringBuilder builder = new StringBuilder();

	public HtmlWriter append(String str) {
		builder.append(str);
		return this;
	}

	public HtmlWriter lt() {
		builder.append("<code class=\"xml plain\">&lt;</code>");
		return this;
	}

	public HtmlWriter gt() {
		builder.append("<code class=\"xml plain\">&gt;</code>");
		return this;
	}

	public HtmlWriter keyword(String name) {
		builder.append("<code class=\"xml keyword\">");
		builder.append(name);
		builder.append("</code>");
		return this;
	}

	public HtmlWriter newline() {
		builder.append("<br/>\n");
		return this;
	}

	@Override
	public String toString() {
		return builder.toString();
	}

	public HtmlWriter indent(int level) {
		for (int i = 0; i < level; i++) {
			builder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		return this;
	}

	public HtmlWriter comment(String comment, int level) {
		if (comment == null) {
			return this;
		}
		String[] parts = comment.split("\n");
		if (parts.length == 1) {
			indent(level);
			builder.append("&lt;!-- ");
			builder.append(comment).append(" --&gt;");
			newline();
		} else {
			indent(level);
			builder.append("&lt;!--");
			newline();

			for (int i = 0; i < parts.length; i++) {
				indent(level + 1);
				builder.append(parts[i].trim());
				newline();
			}

			indent(level);
			builder.append("--&gt;");
			newline();
		}

		return this;
	}

	public HtmlWriter defaultValue(String defaultValue) {
		if (defaultValue == null) {
			builder.append("...");
		} else {
			builder.append(defaultValue);
		}
		return this;
	}
}
