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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.io.CharStreams;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;

public class LinkGenerator {
	private Set<PackageDoc> local = new HashSet<PackageDoc>();
	private Map<String, String> remote = new HashMap<String, String>();

	public void map(RootDoc root) {
		for (String[] option : root.options()) {
			if ("-link".equals(option[0]) && option.length >= 2) {
				map(option[1]);
			} else if ("-linkoffline".equals(option[0]) && option.length >= 2) {
				map(option[1]);
			}
		}
		local.addAll(Arrays.asList(root.specifiedPackages()));
	}

	private void map(String url) {
		URI packageListUri = null;
		try {
			packageListUri = new URI(url + "/package-list");
			InputStream stream = packageListUri.toURL().openStream();
			for (String line : CharStreams.readLines(new InputStreamReader(stream))) {
				String packageName = line.trim();
				if (packageName.isEmpty()) {
					continue;
				}
				remote.put(packageName, url);
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException("Cannot resolve url: " + url, e);
		} catch (IOException e) {
			System.out.println("Cannot open address: " + packageListUri);
		}
	}

	public String getUrl(ClassDoc klass, PackageDoc from) {
		PackageDoc packageDoc = klass.containingPackage();
		if (local.contains(packageDoc)) {
			return getRelativePath(klass, from);
		}
		String url = remote.get(packageDoc.name());

		if (url == null) {
			return null;
		}

		return url + "/" + klass.qualifiedName().replaceAll("\\.", "/") + ".html";
	}

	private String getRelativePath(ClassDoc klass, PackageDoc from) {
		String fromPath = from.name();
		String toPath = klass.containingPackage().name();
		String[] fromSplitted = fromPath.split("\\.");
		String[] toSplitted = toPath.split("\\.");

		int i = 0;

		while (i < fromSplitted.length && i < toSplitted.length) {
			if (!fromSplitted[i].equals(toSplitted[i])) {
				break;
			}
			i++;
		}
		StringBuilder result = new StringBuilder();
		for (int j = i; j < fromSplitted.length; j++) {
			result.append("..");
			result.append(File.separatorChar);
		}
		for (int j = i; j < toSplitted.length; j++) {
			result.append(toSplitted[j]);
			result.append(File.separatorChar);
		}
		result.append(klass.name());
		result.append(".html");
		return result.toString();
	}
}
