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
package org.atteo.moonshine.shiro.simple;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.shiro.realm.Realm;
import org.atteo.moonshine.shiro.RealmService;

import com.google.inject.Module;
import com.google.inject.PrivateModule;

/**
 * Realm with accounts hardcoded in the configuration.
 */
@XmlRootElement(name = "in-place")
public class InPlaceRealmService extends RealmService {

	@XmlElementWrapper(name = "accounts")
	@XmlElement(name = "account")
	private List<Account> accounts;

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Override
			protected void configure() {
				InPlaceRealm realm = new InPlaceRealm();
				if (accounts != null) {
					for (Account account : accounts) {
						String roles[] = {};

						if (account.getRoles() != null) {
							roles = account.getRoles().
							    toArray(new String[account.getRoles().size()]);
						}

						realm.addAccount(account.getUsername(), account.getPassword(),
						    account.isAdministrator(), roles);
					}
				}

				bind(Realm.class).toInstance(realm);
				expose(Realm.class);
			}
		};
	}
}
