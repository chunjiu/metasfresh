package de.metas.costing.methods;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.DBException;
import org.adempiere.mm.attributes.AttributeSetInstanceId;
import org.adempiere.service.OrgId;
import org.compiere.util.DB;
import org.springframework.stereotype.Component;

import de.metas.acct.api.AcctSchema;
import de.metas.acct.api.IAcctSchemaDAO;
import de.metas.costing.CostAmount;
import de.metas.costing.CostDetailCreateRequest;
import de.metas.costing.CostDetailCreateResult;
import de.metas.costing.CostPrice;
import de.metas.costing.CostSegment;
import de.metas.costing.CostingMethod;
import de.metas.costing.CurrentCost;
import de.metas.money.CurrencyId;
import de.metas.order.OrderLineId;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * de.metas.business
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@Component
public class LastInvoiceCostingMethodHandler extends CostingMethodHandlerTemplate
{
	public LastInvoiceCostingMethodHandler(@NonNull final CostingMethodHandlerUtils utils)
	{
		super(utils);
	}

	@Override
	public CostingMethod getCostingMethod()
	{
		return CostingMethod.LastInvoice;
	}

	@Override
	protected CostDetailCreateResult createCostForMatchInvoice(final CostDetailCreateRequest request)
	{
		final CurrentCost currentCosts = utils.getCurrentCost(request);
		final CostDetailCreateResult result = utils.createCostDetailRecordWithChangedCosts(request, currentCosts);

		final CostAmount amt = request.getAmt();
		final Quantity qty = request.getQty();
		final boolean isOutboundTrx = qty.signum() < 0;

		if (!isOutboundTrx)
		{
			if (qty.signum() != 0)
			{
				final CostAmount price = amt.divide(qty, currentCosts.getPrecision().toInt(), RoundingMode.HALF_UP);
				currentCosts.setCostPrice(CostPrice.ownCostPrice(price));
			}
			else
			{
				final CostAmount priceAdjust = amt;
				currentCosts.addToOwnCostPrice(priceAdjust);
			}
		}
		currentCosts.addToCurrentQty(qty);
		currentCosts.addCumulatedAmtAndQty(amt, qty);

		utils.saveCurrentCost(currentCosts);

		return result;
	}

	@Override
	protected CostDetailCreateResult createOutboundCostDefaultImpl(final CostDetailCreateRequest request)
	{
		final CurrentCost currentCosts = utils.getCurrentCost(request);
		final CostDetailCreateResult result = utils.createCostDetailRecordWithChangedCosts(request, currentCosts);

		currentCosts.addToCurrentQty(request.getQty());

		utils.saveCurrentCost(currentCosts);

		return result;
	}

	@Override
	public Optional<CostAmount> calculateSeedCosts(final CostSegment costSegment, final OrderLineId orderLineId_NOTUSED)
	{
		return getLastInvoicePrice(costSegment);
	}

	private static Optional<CostAmount> getLastInvoicePrice(final CostSegment costSegment)
	{
		final ProductId productId = costSegment.getProductId();
		final OrgId orgId = costSegment.getOrgId();
		final AttributeSetInstanceId asiId = costSegment.getAttributeSetInstanceId();
		final AcctSchema acctSchema = Services.get(IAcctSchemaDAO.class).getById(costSegment.getAcctSchemaId());
		final CurrencyId acctCurrencyId = acctSchema.getCurrencyId();

		List<Object> sqlParams = new ArrayList<>();
		String sql = "SELECT currencyConvert(il.PriceActual, i.C_Currency_ID, ?, i.DateAcct, i.C_ConversionType_ID, il.AD_Client_ID, il.AD_Org_ID) "
				// ,il.PriceActual, il.QtyInvoiced, i.DateInvoiced, il.Line
				+ "FROM C_InvoiceLine il "
				+ " INNER JOIN C_Invoice i ON (il.C_Invoice_ID=i.C_Invoice_ID) "
				+ "WHERE il.M_Product_ID=?"
				+ " AND i.IsSOTrx='N'";
		sqlParams.add(acctCurrencyId);
		sqlParams.add(productId);

		if (orgId.isRegular())
		{
			sql += " AND il.AD_Org_ID=?";
			sqlParams.add(orgId);
		}
		else if (asiId.isRegular())
		{
			sql += " AND il.M_AttributeSetInstance_ID=?";
			sqlParams.add(asiId);
		}
		sql += " ORDER BY i.DateInvoiced DESC, il.Line DESC";
		//
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, ITrx.TRXNAME_ThreadInherited);
			DB.setParameters(pstmt, sqlParams);

			rs = pstmt.executeQuery();
			if (rs.next())
			{
				final BigDecimal priceActual = rs.getBigDecimal(1);
				return Optional.ofNullable(CostAmount.of(priceActual, acctCurrencyId));
			}
			else
			{
				return Optional.empty();
			}
		}
		catch (final Exception e)
		{
			throw new DBException(e, sql);
		}
		finally
		{
			DB.close(rs, pstmt);
		}
	}	// getLastInvoicePrice

}
