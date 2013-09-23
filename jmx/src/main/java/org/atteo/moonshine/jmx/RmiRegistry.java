/*
 * Copyright 2013 Atteo.
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
package org.atteo.moonshine.jmx;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.moonshine.TopLevelService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;

import sun.rmi.registry.RegistryImpl;
import sun.rmi.server.UnicastRef;

/**
 * Starts RMI registry.
 */
@XmlRootElement(name = "rmiRegistry")
public class RmiRegistry extends TopLevelService {
	/**
	 * Port for RMI registry.
	 *
	 * <p>
	 * By default zero, which means any open port will be used. Use {@link RmiRegistryPort} annotation
	 * to inject selected port.
	 * </p>
	 */
	@XmlElement
	private int rmiRegistryPort = 0;

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				Registry registry;
				try {
					registry = LocateRegistry.createRegistry(rmiRegistryPort);
				} catch (RemoteException e) {
					throw new RuntimeException("Cannot create RMI registry on port: " + rmiRegistryPort, e);
				}
				UnicastRef ref = (UnicastRef) ((RegistryImpl)registry).getRef();
				final int port = ref.getLiveRef().getPort();

				RmiRegistryPort portProvider = new RmiRegistryPort() {
					@Override
					public int getPort() {
						return port;
					}
				};

				if (getId() != null) {
					bind(RmiRegistryPort.class).annotatedWith(Names.named(getId())).toInstance(portProvider);
				} else {
					bind(RmiRegistryPort.class).toInstance(portProvider);
				}
			}
		};
	}
}
