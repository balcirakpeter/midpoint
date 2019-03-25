/*
 * Copyright (c) 2010-2018 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.gui.impl.prism;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

import com.evolveum.midpoint.gui.api.prism.PrismContainerWrapper;
import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.web.component.prism.ContainerValueWrapper;
import com.evolveum.midpoint.web.component.prism.ContainerWrapperFactory;
import com.evolveum.midpoint.web.component.prism.ValueStatus;
import com.evolveum.midpoint.web.component.util.VisibleBehaviour;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;

/**
 * @author katka
 *
 */
public class PrismContainerHeaderPanel<C extends Containerable> extends ItemHeaderPanel<PrismContainerValue<C>, PrismContainer<C>, PrismContainerDefinition<C>, PrismContainerWrapper<C>> {

	private static final long serialVersionUID = 1L;

	private static final String ID_ADD_BUTTON = "addButton";
	
	
	public PrismContainerHeaderPanel(String id, IModel<PrismContainerWrapper<C>> model) {
		super(id, model);
	}

	@Override
	protected void initButtons() {
		add(new VisibleBehaviour(() -> getModelObject().isMultiValue()));
		
		
		 AjaxLink<Void> addButton = new AjaxLink<Void>(ID_ADD_BUTTON) {
				private static final long serialVersionUID = 1L;

				@Override
	            public void onClick(AjaxRequestTarget target) {
	                addValue(target);
	            }
	        };
	        addButton.add(new VisibleBehaviour(() -> isAddButtonVisible()));
	        add(addButton);
	        
	        //TODO: sorting
	        //TODO add/remove
	}
	
	private void addValue(AjaxRequestTarget target) {
//		ContainerWrapperFactory cwf = new ContainerWrapperFactory(getPageBase());
//		ContainerWrapperImpl<C> containerWrapper = getModelObject();
//		Task task = getPageBase().createSimpleTask("Creating new container");
//		ContainerValueWrapper<C> newContainerValue = cwf.createContainerValueWrapper(containerWrapper,
//				containerWrapper.getItem().createNewValue(), containerWrapper.getObjectStatus(), ValueStatus.ADDED,
//				containerWrapper.getPath(), task);
//		newContainerValue.setShowEmpty(true, false);
//		getModelObject().addValue(newContainerValue);
//		onButtonClick(target);
	}
	
	private boolean isAddButtonVisible() {
		return getModelObject().isExpanded();
	}
	
	

}
