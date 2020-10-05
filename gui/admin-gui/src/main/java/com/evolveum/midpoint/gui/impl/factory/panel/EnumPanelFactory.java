/*
 * Copyright (C) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.gui.impl.factory.panel;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.evolveum.midpoint.gui.api.prism.wrapper.ItemWrapper;
import com.evolveum.midpoint.gui.api.prism.wrapper.PrismPropertyWrapper;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.web.component.prism.InputPanel;

/**
 * @author katka
 */
@Component
public class EnumPanelFactory<T extends Enum<?>> extends AbstractInputGuiComponentFactory<T> {

    @PostConstruct
    public void register() {
        getRegistry().addToRegistry(this);
    }

    private boolean isEnum(ItemWrapper<?, ?> property) {

        if (!(property instanceof PrismPropertyWrapper)) {
            return false;
        }

        //noinspection unchecked
        Class<T> valueType = property.getTypeClass();
        if (valueType == null) {
            valueType = property.getPrismContext() != null ?
                    property.getPrismContext().getSchemaRegistry().getCompileTimeClass(property.getTypeName()) : null;
        }

        if (valueType != null) {
            return valueType.isEnum();
        }

        return (((PrismPropertyWrapper<?>) property).getAllowedValues() != null
                && ((PrismPropertyWrapper<?>) property).getAllowedValues().size() > 0);
    }

    @Override
    public <IW extends ItemWrapper<?, ?>> boolean match(IW wrapper) {
        return (isEnum(wrapper));
    }

    @Override
    protected InputPanel getPanel(PrismPropertyPanelContext<T> panelCtx) {
        Class<T> clazz = panelCtx.getTypeClass();

        if (clazz != null) {
            return WebComponentUtil.createEnumPanel(clazz, panelCtx.getComponentId(), panelCtx.getRealValueModel(),
                    panelCtx.getParentComponent());
        }

        return WebComponentUtil.createEnumPanel(panelCtx.unwrapWrapperModel(), panelCtx.getComponentId(),
                panelCtx.getRealValueModel());

    }
}
