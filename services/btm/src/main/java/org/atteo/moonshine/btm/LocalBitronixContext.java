/*
 * Copyright 2015 Atteo.
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
package org.atteo.moonshine.btm;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.BitronixTransactionSynchronizationRegistry;
import bitronix.tm.Configuration;
import bitronix.tm.journal.Journal;
import bitronix.tm.recovery.Recoverer;
import bitronix.tm.resource.ResourceLoader;
import bitronix.tm.spi.BitronixContext;
import bitronix.tm.spi.DefaultBitronixContext;
import bitronix.tm.timer.TaskScheduler;
import bitronix.tm.twopc.executor.Executor;
import bitronix.tm.utils.ExceptionAnalyzer;

/**
 * Keeps context for BTM local to current thread.
 */
public class LocalBitronixContext implements BitronixContext {
	private static final InheritableThreadLocal<BitronixContext> context = new InheritableThreadLocal<>();

	public static void initNewContext() {
		context.set(new DefaultBitronixContext());
	}

	@Override
	public BitronixTransactionManager getTransactionManager() {
		return context.get().getTransactionManager();
	}

	@Override
	public BitronixTransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
		return context.get().getTransactionSynchronizationRegistry();
	}

	@Override
	public Configuration getConfiguration() {
		return context.get().getConfiguration();
	}

	@Override
	public Journal getJournal() {
		return context.get().getJournal();
	}

	@Override
	public TaskScheduler getTaskScheduler() {
		return context.get().getTaskScheduler();
	}

	@Override
	public ResourceLoader getResourceLoader() {
		return context.get().getResourceLoader();
	}

	@Override
	public Recoverer getRecoverer() {
		return context.get().getRecoverer();
	}

	@Override
	public Executor getExecutor() {
		return context.get().getExecutor();
	}

	@Override
	public ExceptionAnalyzer getExceptionAnalyzer() {
		return context.get().getExceptionAnalyzer();
	}

	@Override
	public boolean isTransactionManagerRunning() {
		return context.get().isTransactionManagerRunning();
	}

	@Override
	public boolean isTaskSchedulerRunning() {
		return context.get().isTaskSchedulerRunning();
	}

	@Override
	public void clear() {
		context.get().clear();
	}
}
