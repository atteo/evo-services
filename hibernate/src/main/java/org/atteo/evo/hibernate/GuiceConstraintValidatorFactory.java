package org.atteo.evo.hibernate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class GuiceConstraintValidatorFactory implements
		ConstraintValidatorFactory {

	private final Injector injector;
	private ConstraintValidatorFactory defaultFactory;

	@Inject
	public GuiceConstraintValidatorFactory(final Injector injector) {
		this.injector = injector;
	}

	public void setDefaultFactory(ConstraintValidatorFactory defaultFactory) {
		this.defaultFactory = defaultFactory;
	}

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		T validator = defaultFactory.getInstance(key);
		injector.injectMembers(validator);
		return validator;
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> cv) {
	}
}
