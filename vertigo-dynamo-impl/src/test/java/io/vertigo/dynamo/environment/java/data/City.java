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
package io.vertigo.dynamo.environment.java.data;

import io.vertigo.dynamo.domain.model.DtMasterData;
import io.vertigo.dynamo.domain.stereotype.DtDefinition;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données City
 */
@javax.persistence.Entity
@javax.persistence.Table(name = "CITY")
@DtDefinition
public final class City implements DtMasterData {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long citId;
	private String label;
	private String postalCode;

	/**
	 * Champ : PRIMARY_KEY.
	 * Récupère la valeur de la propriété 'id'.
	 * @return Long citId <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_CITY")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO, generator = "sequence")
	@javax.persistence.Column(name = "CIT_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "PRIMARY_KEY", notNull = true, label = "id")
	public Long getCitId() {
		return citId;
	}

	/**
	 * Champ : PRIMARY_KEY.
	 * Définit la valeur de la propriété 'id'.
	 * @param citId Long <b>Obligatoire</b>
	 */
	public void setCitId(final Long citId) {
		this.citId = citId;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Label'.
	 * @return String label <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "LABEL")
	@Field(domain = "DO_FULL_TEXT", notNull = true, label = "Label")
	public String getLabel() {
		return label;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Label'.
	 * @param label String <b>Obligatoire</b>
	 */
	public void setLabel(final String label) {
		this.label = label;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Postal code'.
	 * @return String postalCode <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "POSTAL_CODE")
	@Field(domain = "DO_KEYWORD", notNull = true, label = "Postal code")
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Postal code'.
	 * @param postalCode String <b>Obligatoire</b>
	 */
	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	// Association : Command non navigable

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
