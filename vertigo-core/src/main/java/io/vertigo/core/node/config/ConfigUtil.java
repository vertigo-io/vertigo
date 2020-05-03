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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.node.component.di.DIAnnotationUtil;
import io.vertigo.core.util.ClassUtil;

final class ConfigUtil {
	private ConfigUtil() {
		//
	}

	static List<CoreComponentConfig> buildPluginsComponentConfigs(final List<PluginConfig> pluginConfigs) {
		Assertion.check()
				.notNull(pluginConfigs);
		//---
		final List<CoreComponentConfig> componentConfigs = new ArrayList<>();
		final Set<String> pluginTypes = new HashSet<>();

		int index = 1;
		for (final PluginConfig pluginConfig : pluginConfigs) {
			final String pluginType = DIAnnotationUtil.buildId(pluginConfig.getApiClass());
			final boolean added = pluginTypes.add(pluginType);
			final String id;
			if (added) {
				id = pluginType;
			} else {
				id = pluginType + '#' + index;
				index++;
			}

			final CoreComponentConfig componentConfig = CoreComponentConfig.createPlugin(id, pluginConfig.getImplClass(), pluginConfig.getParams());
			componentConfigs.add(componentConfig);
		}
		return componentConfigs;
	}

	static List<CoreComponentConfig> buildConnectorsComponentConfigs(final List<ConnectorConfig> connectorConfigs) {
		Assertion.check()
				.notNull(connectorConfigs);
		//---
		final List<CoreComponentConfig> componentConfigs = new ArrayList<>();
		final Set<String> connectorTypes = new HashSet<>();

		int index = 1;
		for (final ConnectorConfig connectorConfig : connectorConfigs) {
			final String connectorType = DIAnnotationUtil.buildId(connectorConfig.getImplClass());

			final boolean added = connectorTypes.add(connectorType);
			final String id;
			if (added) {
				id = connectorType;
			} else {
				id = connectorType + '#' + index;
				index++;
			}

			final CoreComponentConfig componentConfig = CoreComponentConfig.createConnector(id, connectorConfig.getImplClass(), connectorConfig.getParams());
			componentConfigs.add(componentConfig);
		}
		return componentConfigs;
	}

	static List<CoreComponentConfig> buildComponentConfigs(final List<ComponentConfig> componentConfigs) {
		Assertion.check()
				.notNull(componentConfigs);
		//---
		final List<CoreComponentConfig> coreComponentConfigs = new ArrayList<>();
		for (final ComponentConfig componentConfig : componentConfigs) {
			//By convention the component id is the simpleName of the api or the impl
			final String id = DIAnnotationUtil.buildId(componentConfig.getApiClassOpt().orElse(componentConfig.getImplClass()));

			final CoreComponentConfig coreComponentConfig = CoreComponentConfig.createComponent(id,
					componentConfig.getApiClassOpt(),
					componentConfig.getImplClass(),
					componentConfig.getParams());
			coreComponentConfigs.add(coreComponentConfig);
		}
		return coreComponentConfigs;
	}

	/*
	 * We are looking for the type of the plugin.
	 * This type is the first objector interface that inherits from then 'plugin' interface.
	 */
	static Class<? extends Plugin> getPluginApi(final Class<? extends Plugin> implClass) {
		Assertion.check()
				.notNull(implClass);
		//---
		//We are seeking the first and unique Object that extends Plugin.
		//This Interface defines the type of the plugin.
		for (final Class intf : ClassUtil.getAllInterfaces(implClass)) {
			if (Arrays.asList(intf.getInterfaces()).contains(Plugin.class)) {
				return intf;
			}
		}
		throw new IllegalArgumentException("A plugin impl must extend an interface that defines its api: " + implClass);
	}

	static List<CoreComponentConfig> buildAmplifiersComponentConfigs(final List<AmplifierConfig> amplifierConfigs) {
		Assertion.check()
				.notNull(amplifierConfigs);
		//---
		final List<CoreComponentConfig> componentConfigs = new ArrayList<>();
		for (AmplifierConfig amplifierConfig : amplifierConfigs) {
			final String id = DIAnnotationUtil.buildId(amplifierConfig.getApiClass());
			final CoreComponentConfig coreComponentConfig = CoreComponentConfig.createAmplifier(id, amplifierConfig.getApiClass(), amplifierConfig.getParams());
			componentConfigs.add(coreComponentConfig);
		}
		return componentConfigs;
	}
}
