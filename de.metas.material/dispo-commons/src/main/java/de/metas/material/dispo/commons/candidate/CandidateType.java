package de.metas.material.dispo.commons.candidate;

import de.metas.material.dispo.model.X_MD_Candidate;

/*
 * #%L
 * metasfresh-material-dispo-commons
 * %%
 * Copyright (C) 2017 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


/**
 * Please keep in sync with the values of {@link X_MD_Candidate#MD_CANDIDATE_TYPE_AD_Reference_ID}
 */
public enum CandidateType
{
	DEMAND, SUPPLY, STOCK_UP, UNRELATED_INCREASE, UNRELATED_DECREASE,

	/** TODO add Inventar-Verminderung to MD_CANDIDATE_TYPE */
	INVENTORY_DOWN,

	/** TODO add Inventar-Erhoehung to MD_CANDIDATE_TYPE */
	INVENTORY_UP,

	/** TODO: remove this type; instead, "just" add an ATP column to candidate. */
	STOCK
}