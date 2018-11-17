package de.metas.material.dispo.commons.candidate.businesscase;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

import de.metas.material.dispo.commons.candidate.CandidateBusinessCase;

/*
 * #%L
 * metasfresh-material-dispo-commons
 * %%
 * Copyright (C) 2018 metas GmbH
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

@Value
@Builder
public class InventoryDetail implements BusinessCaseDetail
{
	BigDecimal plannedQty;

	int inventoryLineId;

	/** {@code MD_Stock_ID} */
	int stockId;

	/** if there was no inventory, but MD_Stock had to be reset from M_HU_Storage */
	int resetStockAdPinstanceId;

	@Override
	public CandidateBusinessCase getCandidateBusinessCase()
	{
		return CandidateBusinessCase.INVENTORY;
	}

}
