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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.CoreComponent;
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
public final class CoreComponentConfig {
	private final String id;
	private final boolean proxy;
	private final Optional<Class<? extends CoreComponent>> implClassOpt;
	private final Optional<Class<? extends CoreComponent>> apiClassOpt;
	private final Map<String, String> params;

	/**
	 * Constructor.
	 * @param apiClassOpt api of the component
	 * @param implClass impl class of the component
	 * @param params params
	 */
	CoreComponentConfig(
			final String id,
			final boolean proxy,
			final Optional<Class<? extends CoreComponent>> apiClassOpt,
			final Optional<Class<? extends CoreComponent>> implClassOpt,
			final List<Param> params) {
		Assertion.checkArgNotEmpty(id);
		Assertion.checkNotNull(apiClassOpt);
		Assertion.checkNotNull(implClassOpt);
		if (proxy) {
			Assertion.checkArgument(!implClassOpt.isPresent(), "When a proxy is declared there is no impl");
			Assertion.checkArgument(apiClassOpt.isPresent(), "When a proxy is declared, an api is required");
		} else {
			Assertion.checkArgument(implClassOpt.isPresent(), "When a classic component -no proxy-  is declared, an impl is required");
			Assertion.when(apiClassOpt.isPresent()).check(() -> CoreComponent.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, CoreComponent.class);
			Assertion.checkArgument(apiClassOpt.orElse(CoreComponent.class).isAssignableFrom(implClassOpt.get()), "impl class {0} must implement {1}", implClassOpt.get(), apiClassOpt.orElse(CoreComponent.class));
		}
		Assertion.checkNotNull(params);
		//-----
		this.id = id;
		this.proxy = proxy;
		//-----
		this.apiClassOpt = apiClassOpt;
		this.implClassOpt = implClassOpt;

		this.params = params
				.stream()
				.collect(Collectors.toMap(Param::getName, Param::getValue));
	}

	/**
	 * Static method factory for ComponentConfigBuilder
	 * By default a pure component is expected
	 * @return ComponentConfigBuilder
	 */
	public static CoreComponentConfigBuilder builder() {
		final boolean proxy = false;
		return new CoreComponentConfigBuilder(proxy);
	}

	/**
	 * Static method factory for ComponentConfigBuilder
	 * @param proxy if the component is a proxy
	 * @return ComponentConfigBuilder
	 */
	public static CoreComponentConfigBuilder builder(final boolean proxy) {
		return new CoreComponentConfigBuilder(proxy);
	}

	/**
	 * @return impl class of the component
	 */
	public Class<? extends CoreComponent> getImplClass() {
		Assertion.checkState(!proxy, "a proxy has no impl");
		//---
		return implClassOpt.get();
	}

	/**
	 * @return api of the component
	 */
	public Optional<Class<? extends CoreComponent>> getApiClass() {
		return apiClassOpt;
	}

	/**
	 * @return id of the component
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return if the component is a proxy
	 */
	public boolean isProxy() {
		return proxy;
	}

	/**
	 * @return params
	 */
	public Map<String, String> getParams() {
		return params;
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return id;
	}
}
