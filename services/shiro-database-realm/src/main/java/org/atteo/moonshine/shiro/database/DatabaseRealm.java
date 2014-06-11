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
import org.atteo.moonshine.jta.Transactional;

import com.google.inject.Inject;

public class DatabaseRealm extends AuthenticatingRealm {
	private final int hashIterations = 1536;
	private final String hashAlgorithm = "SHA-256";
	private final RandomNumberGenerator randomNumberGenerator;

	@Inject
	private AccountRepository accountRepository;

	public DatabaseRealm() {
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
	@Transactional
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
			throws AuthenticationException {
		String principal = (String) token.getPrincipal();
		Account loginAccount = accountRepository.findOne(principal);

		if (loginAccount == null) {
			return null;
		}

		SimplePrincipalCollection principalCollection = new SimplePrincipalCollection(
				loginAccount.getLogin(), getName());

		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(principalCollection,
				loginAccount.getHashedPassword(), new SimpleByteSource(Hex.decode(loginAccount
				.getSalt())));

		return info;
	}
}
