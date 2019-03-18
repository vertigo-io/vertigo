package io.vertigo.app.config.yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.app.config.BootConfigBuilder;
import io.vertigo.app.config.Feature;
import io.vertigo.app.config.Features;
import io.vertigo.app.config.LogConfig;
import io.vertigo.app.config.NodeConfig;
import io.vertigo.app.config.NodeConfigBuilder;
import io.vertigo.app.config.yaml.YamlAppConfig.YamlModuleConfig;
import io.vertigo.core.component.ComponentInitializer;
import io.vertigo.core.component.Plugin;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Tuples.Tuple2;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.Selector;
import io.vertigo.util.Selector.MethodConditions;

public final class YamlAppConfigBuilder implements Builder<AppConfig> {

	private static final Object[] EMPTY_ARRAY = new Object[0];

	private final AppConfigBuilder appConfigBuilder = AppConfig.builder();

	private final List<String> activeFlags;
	private final YamlConfigParams params;

	public YamlAppConfigBuilder(final Properties params) {
		Assertion.checkNotNull(params);
		//---
		if (params.containsKey("boot.activeFlags")) {
			activeFlags = Arrays.asList(params.getProperty("boot.activeFlags").split(";"));
			params.remove("boot.activeFlags");
		} else {
			activeFlags = Collections.emptyList();
		}
		this.params = new YamlConfigParams(params);
	}

	/**
	 * Begin the boot config of the app.
	 * @return the bootConfig builder
	 */
	public BootConfigBuilder beginBoot() {
		return appConfigBuilder.beginBoot();
	}

	/**
	* Append Config of a set of modules.
	 * @param relativeRootClass Class used to access files in a relative way.
	* @param params properties used to configure the app
	* @param jsonFileNames fileNames of the different json files
	*
	* @return this builder
	*/
	public YamlAppConfigBuilder withFiles(final Class relativeRootClass, final String... jsonFileNames) {
		Assertion.checkNotNull(relativeRootClass);
		Assertion.checkNotNull(jsonFileNames);
		//---
		Stream.of(jsonFileNames)
				.map(xmlModulesFileName -> createURL(xmlModulesFileName, relativeRootClass))
				.forEach(jsonConfigUrl -> handleJsonFileConfig(jsonConfigUrl));
		return this;
	}

	private void handleJsonFileConfig(final URL yamlConfigURL) {

		final Yaml yaml = new Yaml(new Constructor(YamlAppConfig.class));
		final YamlAppConfig yamlAppConfig = yaml.loadAs(parseFile(yamlConfigURL), YamlAppConfig.class);
		//--- node
		handleNodeConfig(yamlAppConfig);
		//--- boot
		handleBoot(yamlAppConfig);
		//--- modules
		yamlAppConfig.modules
				.entrySet()
				.stream()
				.forEach(entry -> handleJsonModuleConfig(entry.getKey(), entry.getValue()));
		//--- initializers
		yamlAppConfig.initializers
				.forEach(initializerConfig -> {
					Assertion.checkState(initializerConfig.size() == 1, "an initializer is defined by it's class");
					// ---
					final Map.Entry<String, Map<String, Object>> initializerEntry = initializerConfig.entrySet().iterator().next();
					if (isEnabledByFlag(getFlagsOfMapParams(initializerEntry.getValue()))) {
						appConfigBuilder.addInitializer(ClassUtil.classForName(initializerEntry.getKey(), ComponentInitializer.class));
					}
				});
	}

	private void handleNodeConfig(final YamlAppConfig yamlAppConfig) {
		if (yamlAppConfig.node != null) {
			final NodeConfigBuilder nodeConfigBuilder = NodeConfig.builder();
			final String appName = yamlAppConfig.node.appName;
			final String nodeId = yamlAppConfig.node.nodeId;
			final String endPoint = yamlAppConfig.node.endPoint;
			if (appName != null) {
				nodeConfigBuilder.withAppName(evalParamValue(appName));
			}
			if (nodeId != null) {
				nodeConfigBuilder.withNodeId(evalParamValue(nodeId));
			}
			if (endPoint != null) {
				nodeConfigBuilder.withEndPoint(evalParamValue(endPoint));
			}
			appConfigBuilder.withNodeConfig(nodeConfigBuilder.build());
		}
	}

	private void handleBoot(final YamlAppConfig yamlAppConfig) {
		if (yamlAppConfig.boot != null) {
			final String locales = yamlAppConfig.boot.params.get("locales");
			final String defaultZoneId = yamlAppConfig.boot.params.get("defaultZoneId");
			if (locales != null) {
				if (defaultZoneId == null) {
					appConfigBuilder
							.beginBoot()
							.withLocales(locales);
				} else {
					appConfigBuilder
							.beginBoot()
							.withLocalesAndDefaultZoneId(locales, defaultZoneId);
				}
			}
			yamlAppConfig.boot.plugins.forEach(
					plugin -> {
						Assertion.checkState(plugin.size() == 1, "a plugin is defined by it's class");
						// ---
						final Map.Entry<String, Map<String, Object>> pluginEntry = plugin.entrySet().iterator().next();
						if (isEnabledByFlag(getFlagsOfMapParams(pluginEntry.getValue()))) {
							appConfigBuilder.beginBoot()
									.addPlugin(
											ClassUtil.classForName(pluginEntry.getKey(), Plugin.class),
											pluginEntry.getValue().entrySet().stream()
													.filter(entry -> !"__flags__".equals(entry.getKey()))
													.map(entry -> Param.of(entry.getKey(), evalParamValue(String.valueOf(entry.getValue()))))
													.toArray(Param[]::new));
						}
					});
		}
	}

	private void handleJsonModuleConfig(final String featuresClassName, final YamlModuleConfig yamlModuleConfig) {
		if (yamlModuleConfig == null) {
			// we have no params so no flag
			// just a simple module
			appConfigBuilder.addModule(ClassUtil.newInstance(featuresClassName, Features.class).build());
		} else {
			// more complexe module with flags and flipped features
			if (isEnabledByFlag(yamlModuleConfig.__flags__)) {
				final Features moduleConfigByFeatures = ClassUtil.newInstance(featuresClassName, Features.class);
				final Map<String, Method> featureMethods = new Selector().from(moduleConfigByFeatures.getClass())
						.filterMethods(MethodConditions.annotatedWith(Feature.class))
						.findMethods()
						.stream()
						.map(Tuple2::getVal2)
						.collect(Collectors.toMap(method -> method.getAnnotation(Feature.class).value(), Function.identity()));

				if (yamlModuleConfig.features != null) {
					yamlModuleConfig.features
							.forEach(featureConfig -> {
								Assertion.checkState(featureConfig.size() == 1, "a feature is designed by it's class");
								final Map.Entry<String, Map<String, Object>> featureEntry = featureConfig.entrySet().iterator().next();
								final String featureClassName = featureEntry.getKey();
								final Method methodForFeature = featureMethods.get(featureClassName);
								Assertion.checkNotNull(methodForFeature, "Unable to find method for feature '{0}' in feature class '{1}'", featureClassName, featuresClassName);
								final Map<String, Object> paramsMap = featureEntry.getValue();
								if (isEnabledByFlag(getFlagsOfMapParams(paramsMap))) {
									ClassUtil.invoke(moduleConfigByFeatures, methodForFeature, findmethodParameters(paramsMap, methodForFeature, featureClassName, featuresClassName));
								}
							});
				}

				if (yamlModuleConfig.featuresConfig != null) {
					yamlModuleConfig.featuresConfig
							.forEach(featureConfig -> {
								Assertion.checkState(featureConfig.size() == 1, "a feature is designed by it's class");
								final Map.Entry<String, Map<String, Object>> featureEntry = featureConfig.entrySet().iterator().next();
								final String featureClassName = featureEntry.getKey();
								final Method methodForFeature = featureMethods.get(featureClassName);
								Assertion.checkNotNull(methodForFeature, "Unable to find method for feature '{0}' in feature class '{1}'", featureClassName, featuresClassName);
								final Map<String, Object> paramsMap = featureEntry.getValue();
								if (isEnabledByFlag(getFlagsOfMapParams(paramsMap))) {
									ClassUtil.invoke(moduleConfigByFeatures, methodForFeature, findmethodParameters(paramsMap, methodForFeature, featureClassName, featuresClassName));
								}
							});
				}

				yamlModuleConfig.plugins.forEach(
						plugin -> {
							Assertion.checkState(plugin.size() == 1, "a plugin is defined by it's class");
							// ---
							final Map.Entry<String, Map<String, Object>> pluginEntry = plugin.entrySet().iterator().next();
							final String pluginClassName = pluginEntry.getKey();
							final Map<String, Object> paramsMap = pluginEntry.getValue();
							if (isEnabledByFlag(getFlagsOfMapParams(paramsMap))) {
								moduleConfigByFeatures
										.addPlugin(
												ClassUtil.classForName(pluginClassName, Plugin.class),
												plugin.get(pluginClassName).entrySet().stream()
														.filter(entry -> !"__flags__".equals(entry.getKey()))
														.map(entry -> Param.of(entry.getKey(), String.valueOf(entry.getValue())))
														.toArray(Param[]::new));
							}
						});
				appConfigBuilder.addModule(moduleConfigByFeatures.build());
			}
		}
	}

	private static List<String> getFlagsOfMapParams(final Map<String, Object> paramsMap) {
		if (paramsMap == null || !paramsMap.containsKey("__flags__")) {
			return Collections.emptyList();
		}
		// if contains we check we have a list
		Assertion.checkState(List.class.isAssignableFrom(paramsMap.get("__flags__").getClass()), "flags are array of strings");
		return (List<String>) paramsMap.get("__flags__");
	}

	private boolean isEnabledByFlag(final List<String> flags) {
		Assertion.checkNotNull(flags);
		//---
		if (flags.isEmpty()) {
			return true;// no flags declared means always
		}
		return flags.stream()
				.anyMatch(flag -> activeFlags.contains(flag));
	}

	private static Object[] findmethodParameters(final Map<String, Object> paramsConfig, final Method method, final String featureName, final String featuresClassName) {
		Assertion.checkState(method.getParameterCount() <= 1, "A feature method can have 0 parameter or a single Param... parameter");
		if (method.getParameterCount() == 1) {
			if (paramsConfig == null) {
				return new Object[] { new Param[0] };
			}
			final Param[] params = paramsConfig.entrySet()
					.stream()
					.filter(paramEntry -> !"__flags__".equals(paramEntry.getKey()))
					.map(paramEntry -> Param.of(paramEntry.getKey(), String.valueOf(paramEntry.getValue())))
					.toArray(Param[]::new);
			return new Object[] { params };
		}
		return EMPTY_ARRAY;

	}

	/**
	 * @param logConfig Config of logs
	 * @return  this builder
	 */
	public YamlAppConfigBuilder withLogConfig(final LogConfig logConfig) {
		Assertion.checkNotNull(logConfig);
		//-----
		appConfigBuilder.beginBoot().withLogConfig(logConfig).endBoot();
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public AppConfig build() {
		return appConfigBuilder.build();
	}

	/**
	 * Retourne l'URL correspondant au nom du fichier dans le classPath.
	 *
	 * @param fileName Nom du fichier
	 * @return URL non null
	 */
	private static URL createURL(final String fileName, final Class<?> relativeRootClass) {
		Assertion.checkArgNotEmpty(fileName);
		//-----
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouvé, on recherche dans le classPath
			final URL url = relativeRootClass.getResource(fileName);
			Assertion.checkNotNull(url, "Impossible de récupérer le fichier [" + fileName + "]");
			return url;
		}
	}

	private static String parseFile(final URL url) {
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			final StringBuilder buff = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				buff.append(line);
				line = reader.readLine();
				buff.append("\r\n");
			}
			return buff.toString();
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Error reading json file : '{0}'", url);
		}
	}

	private String evalParamValue(final String paramValue) {
		if (paramValue.startsWith("${boot.") && paramValue.endsWith("}")) {
			final String property = paramValue.substring("${".length(), paramValue.length() - "}".length());
			return params.getParam(property);
		}
		return paramValue;
	}

}