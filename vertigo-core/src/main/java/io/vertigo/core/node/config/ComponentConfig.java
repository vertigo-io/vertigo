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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.param.Param;

/**
 * The componentconfig class defines the configuration of a component.
 *
 * A component is defined by
 *  - an id.
 *  - proxy or pure component -no proxy-
 *  - a implemenation class (empty if proxy, required if not proxy)
 *  - an optional api class. (required if proxy)
 *  - a map of params
 *
 * @author npiedeloup, pchretien
 */
public final class ComponentConfig {
	private final Class<? extends Component> implClass;
	private final Optional<Class<? extends Component>> apiClassOpt;
	private final List<Param> params;

	/**
	 * Short in-line builder.
	 * @param apiClassOpt api of the component
	 * @param params params
	 */
	static ComponentConfig of(
			final Class<? extends Component> apiClass,
			final Class<? extends Component> implClass,
			final Param[] params) {
		return new ComponentConfig(Optional.of(apiClass), implClass, Arrays.asList(params));
	}

	/**
	 * Short in-line builder.
	 * @param implClass impl class of the component
	 * @param params params
	 */
	static ComponentConfig of(
			final Class<? extends Component> implClass,
			final Param[] params) {
		return new ComponentConfig(Optional.empty(), implClass, Arrays.asList(params));
	}

	/**
	 * Constructor.
	 * @param apiClassOpt api of the component
	 * @param implClass impl class of the component
	 * @param params params
	 */
	private ComponentConfig(
			final Optional<Class<? extends Component>> apiClassOpt,
			final Class<? extends Component> implClass,
			final List<Param> params) {
		Assertion.check()
				.notNull(apiClassOpt)
				.notNull(implClass)
				.argument(apiClassOpt.orElse(Component.class).isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, apiClassOpt.orElse(Component.class))
				.notNull(params);
		Assertion.when(apiClassOpt.isPresent())
				.check(() -> Component.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, Component.class);
		//-----
		this.apiClassOpt = apiClassOpt;
		this.implClass = implClass;
		this.params = params;
	}

	/**
	 * @return impl class of the component
	 */
	public Class<? extends Component> getImplClass() {
		return implClass;
	}

	/**
	 * @return api of the component
	 */
	public Optional<Class<? extends Component>> getApiClassOpt() {
		return apiClassOpt;
	}

	/**
	 * @return params
	 */
	public List<Param> getParams() {
		return params;
	}
}
