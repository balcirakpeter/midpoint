/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.repo.sql.helpers.mapper;

import com.evolveum.midpoint.repo.sql.data.common.embedded.ROperationalState;
import com.evolveum.midpoint.repo.sql.helpers.modify.MapperContext;
import com.evolveum.midpoint.repo.sql.util.DtoTranslationException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationalStateType;

/**
 * Created by Viliam Repan (lazyman).
 */
public class OperationalStateMapper implements Mapper<OperationalStateType, ROperationalState> {

    @Override
    public ROperationalState map(OperationalStateType input, MapperContext context) {
        try {
            ROperationalState rstate = new ROperationalState();
            ROperationalState.fromJaxb(input, rstate);
            return rstate;
        } catch (DtoTranslationException ex) {
            throw new SystemException("Couldn't translate operational state to entity", ex);
        }
    }
}
