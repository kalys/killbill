/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.invoice.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.ning.billing.invoice.api.InvoiceItem;
import com.ning.billing.invoice.api.InvoiceItemType;
import com.ning.billing.invoice.dao.InvoiceItemModelDao;

public class InvoiceItemList extends ArrayList<InvoiceItem> {

    private static final long serialVersionUID = 192311667L;

    private static final int NUMBER_OF_DECIMALS = InvoicingConfiguration.getNumberOfDecimals();
    private static final int ROUNDING_METHOD = InvoicingConfiguration.getRoundingMode();

    public InvoiceItemList() {
        super();
    }

    public InvoiceItemList(final List<InvoiceItemModelDao> invoiceItems) {
        super();
        for (final InvoiceItemModelDao invoiceItemModelDao : invoiceItems) {
            this.add(InvoiceItemFactory.fromModelDao(invoiceItemModelDao));
        }
    }

    public BigDecimal getBalance(final BigDecimal paidAmount) {
        return getChargedAmount().add(getTotalAdjAmount()).add(getCBAAmount()).subtract(paidAmount);
    }

    public BigDecimal getTotalAdjAmount() {
        return getAmoutForItems(InvoiceItemType.CREDIT_ADJ, InvoiceItemType.REFUND_ADJ, InvoiceItemType.ITEM_ADJ);
    }

    public BigDecimal getCreditAdjAmount() {
        return getAmoutForItems(InvoiceItemType.CREDIT_ADJ);
    }

    public BigDecimal getRefundAdjAmount() {
        return getAmoutForItems(InvoiceItemType.REFUND_ADJ);
    }

    public BigDecimal getChargedAmount() {
        return getAmoutForItems(InvoiceItemType.EXTERNAL_CHARGE, InvoiceItemType.RECURRING, InvoiceItemType.FIXED, InvoiceItemType.REPAIR_ADJ);
    }

    public BigDecimal getOriginalChargedAmount() {
        BigDecimal result = BigDecimal.ZERO.setScale(NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        for (final InvoiceItem cur :  this) {
            if (cur.getInvoiceItemType() != InvoiceItemType.EXTERNAL_CHARGE &&
                cur.getInvoiceItemType() != InvoiceItemType.RECURRING &&
                cur.getInvoiceItemType() != InvoiceItemType.FIXED) {
                continue;
            }
            if (cur.getCreatedDate().compareTo(cur.getCreatedDate()) == 0) {
                result = result.add(cur.getAmount());
            }
        }
        return result;
    }

    public BigDecimal getCBAAmount() {
        return getAmoutForItems(InvoiceItemType.CBA_ADJ);
    }

    private BigDecimal getAmoutForItems(final InvoiceItemType... types) {
        BigDecimal total = BigDecimal.ZERO.setScale(NUMBER_OF_DECIMALS, ROUNDING_METHOD);
        for (final InvoiceItem item : this) {
            if (isFromType(item, types)) {
                if (item.getAmount() != null) {
                    total = total.add(item.getAmount());
                }
            }
        }
        return total.setScale(NUMBER_OF_DECIMALS, ROUNDING_METHOD);
    }

    private boolean isFromType(final InvoiceItem item, final InvoiceItemType... types) {
        for (final InvoiceItemType cur : types) {
            if (item.getInvoiceItemType() == cur) {
                return true;
            }
        }
        return false;
    }
}