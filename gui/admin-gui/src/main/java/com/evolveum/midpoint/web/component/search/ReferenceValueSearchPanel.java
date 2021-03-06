/*
 * Copyright (C) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.web.component.search;

import java.util.List;
import javax.xml.namespace.QName;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.model.LoadableModel;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.prism.PrismReferenceDefinition;
import com.evolveum.midpoint.web.component.AjaxButton;
import com.evolveum.midpoint.web.component.input.TextPanel;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AreaCategoryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;

/**
 * @author honchar
 */
public class ReferenceValueSearchPanel extends SpecialPopoverSearchPanel<ObjectReferenceType> {

    private static final long serialVersionUID = 1L;

    private final PrismReferenceDefinition referenceDef;

    public ReferenceValueSearchPanel(String id, IModel<ObjectReferenceType> model, PrismReferenceDefinition referenceDef) {
        super(id, model);
        this.referenceDef = referenceDef;
    }

    @Override
    protected SpecialPopoverSearchPopupPanel createPopupPopoverPanel(String id) {
        ReferenceValueSearchPopupPanel<?> value =
                new ReferenceValueSearchPopupPanel(id, getModel()) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected List<QName> getAllowedRelations() {
                        return WebComponentUtil.getCategoryRelationChoices(AreaCategoryType.ADMINISTRATION, getPageBase());
                    }

                    @Override
                    protected List<QName> getSupportedTargetList() {
                        return WebComponentUtil.createSupportedTargetTypeList((referenceDef).getTargetTypeName());
                    }

                    @Override
                    protected void confirmPerformed(AjaxRequestTarget target) {
                        target.add(ReferenceValueSearchPanel.this);
                        referenceValueUpdated(ReferenceValueSearchPanel.this.getModelObject());
                    }
                };
        value.setRenderBodyOnly(true);
        return value;
    }

    @Override
    protected LoadableModel<String> getTextValue() {
        return new LoadableModel<String>() {
            @Override
            protected String load() {
                return WebComponentUtil.getReferenceObjectTextValue(getModelObject(), getPageBase());
            }
        };
    }

    protected void referenceValueUpdated(ObjectReferenceType ort) {
    }
}
