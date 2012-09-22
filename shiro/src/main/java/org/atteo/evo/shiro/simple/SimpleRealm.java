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
package org.atteo.evo.shiro.simple;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.shiro.realm.Realm;
import org.atteo.evo.shiro.ShiroRealm;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

@XmlRootElement(name = "simple")
public class SimpleRealm extends ShiroRealm {
	@XmlElementWrapper(name = "accounts")
	@XmlElement(name = "account")
	private List<Account> accounts;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				AdminSimpleAccountRealm realm = new AdminSimpleAccountRealm();
				if (accounts != null) {
					for (Account account : accounts) {
						realm.addAccount(account.getUsername(), account.getPassword(), account.isAdministrator(),
								account.getRoles().toArray(new String[0]));
					}
				}

				Multibinder.newSetBinder(binder(), Realm.class).addBinding().toInstance(realm);
			}
		};
	}
}
