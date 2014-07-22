package io.vertigo.rest.impl.security;

import io.vertigo.kernel.component.Plugin;

import java.io.Serializable;

/**
 * Cache plugin.
 * 
 * @author pchretien, npiedeloup
 */
public interface UiSecurityTokenCachePlugin extends Plugin {

	/**
	 * Add Object in cache.
	 * If key is already in cache, object is replaced.
	 * @param key data key
	 * @param data value
	 */
	void put(String key, final Serializable data);

	/**
	 * Get object from cache by its key.
	 * Return null, if not found or expired.
	 *
	 * @param key data key
	 * @return data value
	 */
	Serializable get(String key);

	/**
	 * Get and remove object from cache by its key.
	 * Return null, if not found or expired.
	 * Support concurrent calls.
	 * 
	 * @param key data key
	 * @return data value
	 */
	Serializable getAndRemove(String key);
}
