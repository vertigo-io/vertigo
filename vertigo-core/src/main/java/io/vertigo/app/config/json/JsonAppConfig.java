package io.vertigo.app.config.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.JsonObject;

public final class JsonAppConfig {

	public JsonBootConfig boot;
	public JsonModulesConfig modules;
	public List<String> initializers = new ArrayList<>();

	public static final class JsonBootConfig {
		public JsonParamsConfig params;
		public List<JsonPluginConfig> plugins = new ArrayList<>();

	}

	public static final class JsonModulesConfig extends LinkedHashMap<String, JsonModuleConfig> {
		private static final long serialVersionUID = 8170148586508859017L;

	}

	public static class JsonModuleConfig {
		public JsonFeaturesConfig features;
		public List<JsonPluginConfig> plugins = new ArrayList<>();
	}

	public static class JsonFeaturesConfig extends LinkedHashMap<String, List<JsonObject>> {
		private static final long serialVersionUID = 1L;
		//nothing more
	}

	public static class JsonPluginConfig extends LinkedHashMap<String, JsonObject> {
		private static final long serialVersionUID = 1L;
		//nothing more
	}

	public static class JsonParamsConfig extends LinkedHashMap<String, String> {
		private static final long serialVersionUID = 1L;
		// nothing more
	}

}