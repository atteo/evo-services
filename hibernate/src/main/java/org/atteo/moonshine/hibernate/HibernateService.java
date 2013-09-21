/*
 * Copyright 2011 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.hibernate;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.moonshine.jpa.JpaService;
import org.atteo.moonshine.jpa.TransactionScopedEntityManager;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.service.jta.platform.spi.JtaPlatform;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.binder.ScopedBindingBuilder;

/**
 * HibernateService - JPA implementation.
 */
@XmlRootElement(name = "hibernate")
public class HibernateService extends JpaService {
	/**
	 * Automatically initialize database schema.
	 *
	 * <p>
	 * <ul>
	 * <li>validate: validate the schema, makes no changes to the database.</li>
	 * <li>update: update the schema.</li>
	 * <li>create: creates the schema, destroying previous data.</li>
	 * <li>create-drop: drop the schema at the end of the session.</li>
	 * </ul>
	 * Use migrations in production setups.
	 * </p>
	 */
	@XmlElement
	private String initSchema = "validate";

	/**
	 * Setting is relevant when using @GeneratedValue. It indicates whether
	 * or not the new IdentifierGenerator implementations are used for javax.persistence.GenerationType.AUTO,
	 * javax.persistence.GenerationType.TABLE and javax.persistence.GenerationType.SEQUENCE.
	 */
	@XmlElement
	private boolean useNewIdGeneratorMappings = true;

	/**
	 * If turned on, HibernateService will generate comments inside the SQL, for easier debugging.
	 */
	@XmlElement
	private boolean useSqlComments = true;

	/**
	 * Pretty print the SQL in the log and console.
	 */
	@XmlElement
	private boolean formatSql = false;

	/**
	 * Write all SQL statements to console. This is an alternative to setting the log category org.hibernate.SQL
	 * to debug.
	 */
	@XmlElement
	private boolean showSql= false;

	/**
	 * List of HibernateService plugins.
	 */
	@XmlElementRef
	@XmlElementWrapper(name = "plugins")
	private List<HibernatePlugin> plugins;

	/**
	 * Should HibernateService be loaded on first use.
	 */
	@XmlElement
	private boolean lazyLoading = false;

	private EntityManagerFactory factory;

	private class EntityManagerFactoryProvider implements Provider<EntityManagerFactory> {
		@Inject
		private DataSource dataSource;

		@Inject
		private JtaPlatform jtaPlatform;

		@Inject
		private ValidatorFactory validatorFactory;

		@Override
		public EntityManagerFactory get() {
			PersistenceUnitInfo info = new PersistenceUnitInfo() {
				@Override
				public String getPersistenceUnitName() {
					String id = getId();
					if (id == null) {
						id = "default";
					}
					return id;
				}

				@Override
				public String getPersistenceProviderClassName() {
					return null;
				}

				@Override
				public PersistenceUnitTransactionType getTransactionType() {
					return PersistenceUnitTransactionType.JTA;
				}

				@Override
				public DataSource getJtaDataSource() {
					return dataSource;
				}

				@Override
				public DataSource getNonJtaDataSource() {
					return null;
				}

				@Override
				public List<String> getMappingFileNames() {
					return Collections.emptyList();
				}

				@Override
				public List<URL> getJarFileUrls() {
					return Collections.emptyList();
				}

				@Override
				public URL getPersistenceUnitRootUrl() {
					return null;
				}

				@Override
				public List<String> getManagedClassNames() {
					List<String> names = new ArrayList<>();
					for (Class<?> klass : ClassIndex.getAnnotated(Entity.class)) {
						names.add(klass.getCanonicalName());
					}
					return names;
				}

				@Override
				public boolean excludeUnlistedClasses() {
					return true;
				}

				@Override
				public Properties getProperties() {
					Properties properties = new Properties();
					return properties;
				}

				@Override
				public ClassLoader getClassLoader() {
					return Thread.currentThread().getContextClassLoader();
				}

				@Override
				public void addTransformer(ClassTransformer transformer) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public ClassLoader getNewTempClassLoader() {
					return getClassLoader();
				}

				@Override
				public SharedCacheMode getSharedCacheMode() {
					return SharedCacheMode.UNSPECIFIED;
				}

				@Override
				public ValidationMode getValidationMode() {
					return ValidationMode.AUTO;
				}

				@Override
				public String getPersistenceXMLSchemaVersion() {
					return "";
				}
			};

			PersistenceProvider provider = new HibernatePersistence();

			Map<String, Object> map = new HashMap<>();
			if (plugins != null) {
				for (HibernatePlugin plugin : plugins) {
					map.putAll(plugin.getProperties());
				}
			}
			map.put(AvailableSettings.JTA_PLATFORM, jtaPlatform);
			map.put(AvailableSettings.HBM2DDL_AUTO, initSchema);
			map.put(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, useNewIdGeneratorMappings);
			map.put(AvailableSettings.USE_SQL_COMMENTS, useSqlComments);
			map.put(AvailableSettings.FORMAT_SQL, formatSql);
			map.put(AvailableSettings.SHOW_SQL, showSql);
			map.put("javax.persistence.validation.factory", validatorFactory);

			factory = provider.createContainerEntityManagerFactory(info, map);
			return factory;
		}
	}

	@Override
	public Module configure() {
		return new PrivateModule() {
			@Provides
			@Singleton
			public ValidatorFactory provideValidatorFactory(Injector injector) {
				GuiceConstraintValidatorFactory factory = new GuiceConstraintValidatorFactory(
						injector);
				factory.setDefaultFactory(Validation.byDefaultProvider()
						.configure().getDefaultConstraintValidatorFactory());
				return Validation.byDefaultProvider().configure()
						.constraintValidatorFactory(factory)
						.buildValidatorFactory();
			}

			@Override
			protected void configure() {
				bind(JtaPlatform.class).to(CustomJtaPlatform.class).in(Scopes.SINGLETON);

				ScopedBindingBuilder binding;
				binding = bind(EntityManagerFactory.class).toProvider(new EntityManagerFactoryProvider());
				if (lazyLoading) {
					binding.in(Scopes.SINGLETON);
				} else {
					binding.asEagerSingleton();
				}
				expose(EntityManagerFactory.class);

				bind(EntityManager.class).to(TransactionScopedEntityManager.class).in(Scopes.SINGLETON);
				expose(EntityManager.class);
			}
		};
	}

	@Override
	public void close() {
		if (factory != null && factory.isOpen()) {
			factory.close();
		}
	}
}
