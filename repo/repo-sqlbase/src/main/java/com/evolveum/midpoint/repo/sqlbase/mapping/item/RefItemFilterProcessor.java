/*
 * Copyright (C) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.repo.sqlbase.mapping.item;

import java.util.function.Function;

import com.querydsl.core.types.*;

import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.Referencable;
import com.evolveum.midpoint.prism.query.RefFilter;
import com.evolveum.midpoint.repo.sqlbase.SqlPathContext;

/**
 * Filter processor for a reference attribute path.
 */
public class RefItemFilterProcessor extends SinglePathItemFilterProcessor<RefFilter> {

    /**
     * Returns the mapper function creating the string filter processor from context.
     */
    public static ItemSqlMapper mapper(Function<EntityPath<?>, Path<?>> rootToQueryItem) {
        return new ItemSqlMapper(ctx -> new RefItemFilterProcessor(ctx, rootToQueryItem));
    }

    private RefItemFilterProcessor(
            SqlPathContext<?, ?, ?> context, Function<EntityPath<?>, Path<?>> rootToQueryItem) {
        super(context, rootToQueryItem);
    }

    @Override
    public Predicate process(RefFilter filter) {
        PrismReferenceValue singleValue = filter.getSingleValue();
        Referencable ref = singleValue != null ? singleValue.getRealValue() : null;
        return ref != null
                ? ExpressionUtils.predicate(Ops.EQ, path, ConstantImpl.create(ref.getOid()))
                : ExpressionUtils.predicate(Ops.IS_NULL, path);
    }
}
