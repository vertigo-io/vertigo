/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.peg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.vertigo.lang.Assertion;

/**
 * As wikipedia says
 * The sequence operator e1 e2 first invokes e1,
 * and if e1 succeeds, subsequently invokes e2 on the remainder of the input string left unconsumed by e1,
 * and returns the result.
 * If either e1 or e2 fails, then the sequence expression e1 e2 fails.
 *
 * @author pchretien
 */
final class PegSequenceRule implements PegRule<List<?>> {
	private final List<PegRule<?>> rules;
	private final String expression;

	/**
	 * Constructor.
	 */
	PegSequenceRule(final PegRule<?>... rules) {
		Assertion.checkNotNull(rules);
		//-----
		this.rules = Collections.unmodifiableList(Arrays.asList(rules));
		expression = createExpression(rules);
	}

	/*A sequence of rules/expressions is like that : e1 e2 e3 */
	private static String createExpression(final PegRule<?>[] rules) {
		final StringBuilder buffer = new StringBuilder();
		for (final PegRule<?> rule : rules) {
			if (buffer.length() > 0) {
				buffer.append(' ');
			}
			buffer.append(rule.getExpression());
		}
		return buffer.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	/** {@inheritDoc} */
	@Override
	public PegResult<List<?>> parse(final String text, final int start) throws PegNoMatchFoundException {
		final List results = new ArrayList<>();
		int index = start;
		try {
			for (final PegRule<?> rule : rules) {
				final PegResult<?> cursor = rule
						.parse(text, index);
				index = cursor.getIndex();
				results.add(cursor.getResult());
			}
		} catch (final PegNoMatchFoundException e) {
			throw new PegNoMatchFoundException(text, e.getIndex(), e, getExpression());
		}
		return new PegResult<>(index, results);
	}
}