package org.atteo.moonshine.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.atteo.moonshine.tests.MoonshineConfiguration.Config;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

public class MoonshineSuiteRunner extends Suite {

	private final List<Runner> runners;

	private final Class<?> klass;

	private boolean firstConfig = false;

	public MoonshineSuiteRunner(Class<?> klass, RunnerBuilder builder) throws InitializationError {
		super(builder, new Class<?>[0]);

		firstConfig = System.getProperty("firstConfig") != null;

		this.klass = klass;

		Set<Class<?>> ancestorSet = (Set<Class<?>>) TypeToken.of(klass).getTypes().rawTypes();
		List<Class<?>> ancestors = Lists.reverse(new ArrayList<>(ancestorSet));

		List<Config> oneOfList = new ArrayList<>();

		for (Class<?> ancestor : ancestors) {

			final MoonshineConfiguration annotation = ancestor.getAnnotation(MoonshineConfiguration.class);
			if (annotation == null) {
				continue;
			}

			Config[] combine = annotation.forEachConfig();

			oneOfList.addAll(Lists.newArrayList(combine));
		}

		runners = createRunnersForCombinations(oneOfList, 0, new ArrayList<String>());
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	private List<Runner> createRunnersForCombinations(List<Config> oneOfList, int current, List<String> combination)
	    throws InitializationError {
		List<Runner> runnersForCombinations = new ArrayList();

		if (current == oneOfList.size()) {
			runnersForCombinations.add(new MoonshineRunner(klass, Lists.newArrayList(combination)));
		} else {
			Config oneOf = oneOfList.get(current);

			for (String config : oneOf.value()) {
				combination.add(config);

				runnersForCombinations.addAll(createRunnersForCombinations(oneOfList, current + 1, combination));

				combination.remove(current);

				if (firstConfig) {
					break;
				}
			}
		}

		return runnersForCombinations;
	}

}
