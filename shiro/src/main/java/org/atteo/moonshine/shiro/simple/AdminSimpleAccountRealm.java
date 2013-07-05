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

import java.util.Set;

import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.AllPermission;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.util.CollectionUtils;

import com.google.common.collect.Sets;

public class AdminSimpleAccountRealm extends SimpleAccountRealm {
    public void addAccount(String username, String password, boolean isAdmin, String... roles) {
        Set<String> roleNames = CollectionUtils.asSet(roles);
		Set<Permission> permissions = null;
		if (isAdmin) {
			permissions = Sets.<Permission>newHashSet(new AllPermission());
		}
        SimpleAccount account = new SimpleAccount(username, password, getName(), roleNames, permissions);
        add(account);
    }
}
