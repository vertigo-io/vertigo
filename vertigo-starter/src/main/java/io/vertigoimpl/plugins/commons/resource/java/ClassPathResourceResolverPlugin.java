/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigoimpl.plugins.commons.resource.java;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigoimpl.commons.resource.ResourceResolverPlugin;

import java.net.URL;


/**
 * R�solution des URL li�es au classPath.
 * Cette r�solution est en absolue.
 * 
 * @author prahmoune
 * @version $Id: ClassPathResourceResolverPlugin.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $ 
 */
public final class ClassPathResourceResolverPlugin implements ResourceResolverPlugin {

	/** {@inheritDoc} */
	public Option<URL> resolve(final String resource) {
		Assertion.checkNotNull(resource);
		// ---------------------------------------------------------------------
		//le getClassLoader permet de se mettre en absolue (getClass().getRessource serait relatif)
		final URL url = getClassLoader().getResource(resource);
		return Option.option(url);
	}

	private ClassLoader getClassLoader() {
		//On r�cup�re le classLoader courant (celui qui a cr�� le thread).
		return Thread.currentThread().getContextClassLoader();
	}
}
