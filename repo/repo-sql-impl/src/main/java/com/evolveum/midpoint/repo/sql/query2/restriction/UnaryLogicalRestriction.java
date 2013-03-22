/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2013 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql.query2.restriction;

import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.UnaryLogicalFilter;
import com.evolveum.midpoint.repo.sql.query.QueryException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * @author lazyman
 */
public abstract class UnaryLogicalRestriction<T extends UnaryLogicalFilter> extends LogicalRestriction<T> {

    private static final Trace LOGGER = TraceManager.getTrace(UnaryLogicalRestriction.class);

    @Override
    public boolean canHandle(ObjectFilter filter) {
        if (filter instanceof UnaryLogicalFilter) {
            return true;
        }

        return false;
    }

    protected boolean isFilterValid(UnaryLogicalFilter filter) throws QueryException {
        if (filter.getFilter() == null) {
            LOGGER.trace("UnaryLogicalFilter filter must have child filter defined in it.");
            return false;
        }

        return true;
    }
}
