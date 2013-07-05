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
package org.atteo.moonshine.shiro.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;

import com.google.inject.Inject;

public class DatabaseShiroRealm extends AuthenticatingRealm {
	private int hashIterations = 1536;
	private String hashAlgorithm = "SHA-256";
	private RandomNumberGenerator randomNumberGenerator;

	private EntityManagerFactory entityManagerFactory;

	@Inject
	public DatabaseShiroRealm(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
		this.randomNumberGenerator = new SecureRandomNumberGenerator();

		HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher(hashAlgorithm);
		credentialsMatcher.setHashIterations(hashIterations);
		setCredentialsMatcher(credentialsMatcher);

		setName("LoginRealm");
	}

	public int getHashIterations() {
		return hashIterations;
	}

	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	public ByteSource generateSalt() {
		return randomNumberGenerator.nextBytes();
	}

	public String hashPassword(String password, ByteSource salt) {
		return new SimpleHash(hashAlgorithm, password, salt, hashIterations).toHex();
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
			throws AuthenticationException {
		EntityManager entityManager = null;
		try {
			entityManager = entityManagerFactory.createEntityManager();
			String principal = (String) token.getPrincipal();

			TypedQuery<Account> query = entityManager.createNamedQuery("Account.findByLogin",
					Account.class);

			query.setParameter("login", principal);

			Account loginAccount;

			try {
				loginAccount = query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}

			SimplePrincipalCollection principalCollection = new SimplePrincipalCollection(
					loginAccount.getLogin(), getName());

			SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(principalCollection,
					loginAccount.getHashedPassword(), new SimpleByteSource(Hex.decode(loginAccount
					.getSalt())));

			return info;
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}
}
