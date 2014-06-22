package org.atteo.moonshine.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.atteo.moonshine.Moonshine;
import org.atteo.moonshine.tests.MoonshineConfiguration.Alternatives;
import org.atteo.moonshine.tests.MoonshineConfiguration.Config;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

/**
 * Runs the tests inside {@link Moonshine} container.
 *
 * <p>
 * You can configure the container by annotating the class with
 * {@link MoonshineConfiguration}.
 * </p>
 * <p>
 * The test class will be instantiated using global Guice injector of the
 * Moonshine container.
 * </p>
 */
public class MoonshineMultiRunner extends ParentRunner<Runner> {
	private final static String CONFIG_IDS = "configIds";
	private final List<Runner> runners = new ArrayList<>();
	private final Class<?> klass;

	public MoonshineMultiRunner(Class<?> klass, RunnerBuilder builder) throws InitializationError {
		super(null);
		this.klass = klass;

		String configIdsProperty = System.getProperty(CONFIG_IDS, "");
		List<String> configIds = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(configIdsProperty);

		List<Set<Config>> alternatives = collectAlternatives(klass);

		if (!configIds.isEmpty()) {
			filterAlternatives(alternatives, configIds);
		}

		for (List<Config> list : Sets.cartesianProduct(alternatives)) {
			runners.add(new MoonshineRunner(klass, list));
		}
	}

	private void filterAlternatives(List<Set<Config>> alternatives, List<String> configIds) {
		for (Set<Config> alternative : alternatives) {
			if (containsAnyConfigId(alternative, configIds)) {
				Iterator<Config> iterator = alternative.iterator();

				while (iterator.hasNext()) {
					Config config = iterator.next();
					if (!configIds.contains(config.id())) {
						iterator.remove();
					}
				}
			}
		}
	}

	private boolean containsAnyConfigId(Set<Config> alternative, List<String> configIds) {
		for (Config config : alternative) {
			if (configIds.contains(config.id())) {
				return true;
			}
		}

		return false;
	}

	private static List<Set<Config>> collectAlternatives(Class<?> klass) {
		@SuppressWarnings("unchecked")
		Set<Class<?>> ancestorSet = (Set<Class<?>>) TypeToken.of(klass).getTypes().rawTypes();
		List<Class<?>> ancestors = Lists.reverse(new ArrayList<>(ancestorSet));

		List<Set<Config>> alternatives = new ArrayList<>();
		for (Class<?> ancestor : ancestors) {
			MoonshineConfiguration annotation = ancestor.getAnnotation(MoonshineConfiguration.class);
			if (annotation != null) {
				if (annotation.forEach().length != 0) {
					alternatives.add(new LinkedHashSet<>(Arrays.asList(annotation.forEach())));
				}
				for (Alternatives alternative : annotation.forCartesianProductOf()) {
					alternatives.add(new LinkedHashSet<>(Arrays.asList(alternative.value())));
				}
			}
		}
		return alternatives;
	}

	@Override
	protected void runChild(Runner runner, RunNotifier notifier) {
		runner.run(notifier);
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@Override
	protected Description describeChild(Runner child) {
		return child.getDescription();
	}
}
