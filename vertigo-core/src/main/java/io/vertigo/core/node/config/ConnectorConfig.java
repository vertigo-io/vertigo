/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.node.config;

import java.util.List;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Connector;
import io.vertigo.core.param.Param;

/**
 * This class defines the configuration of a connector.
 *
 * A connector is defined by
 *  - a implemenation class
 *  - a map of params
 *
 * The same connector can be used many times with distincts params
 *  - for example : two connections to differents timeseries databases
 *
 * @author mlaroche
 */
public final class ConnectorConfig {
	private final Optional<Class<? extends Connector>> apiClassOpt;
	private final Class<? extends Connector> implClass;
	private final List<Param> params;

	/**
	 * Constructor.
	 * @param apiClassOpt the api class of the connector
	 * @param implClass the impl class of the connector
	 * @param params the params
	 */
	ConnectorConfig(
			final Optional<Class<? extends Connector>> apiClassOpt,
			final Class<? extends Connector> implClass,
			final List<Param> params) {
		Assertion.check()
				.notNull(apiClassOpt)
				.notNull(implClass)
				.argument(Connector.class.isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, Connector.class)
				.notNull(params);
		Assertion.when(apiClassOpt.isPresent())
				.state(() -> Connector.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must implement {1}", apiClassOpt, Connector.class)
				.state(() -> apiClassOpt.get().isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, apiClassOpt)
				.state(() -> apiClassOpt.get().isInterface(), "api class {0} must be an interface", apiClassOpt);
		//-----
		this.apiClassOpt = apiClassOpt;
		this.implClass = implClass;
		this.params = params;
	}

	/**
	 * @return the api class
	 */
	public Optional<Class<? extends Connector>> getApiClassOpt() {
		return apiClassOpt;
	}

	/**
	 * @return the impl class
	 */
	public Class<? extends Connector> getImplClass() {
		return implClass;
	}

	/**
	 * @return the params
	 */
	public List<Param> getParams() {
		return params;
	}
}
