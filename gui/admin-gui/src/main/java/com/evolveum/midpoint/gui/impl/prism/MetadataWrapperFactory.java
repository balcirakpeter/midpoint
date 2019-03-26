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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.evolveum.midpoint.gui.api.prism.ItemStatus;
import com.evolveum.midpoint.gui.api.prism.PrismContainerWrapper;
import com.evolveum.midpoint.gui.impl.factory.ContainerWrapperFactoryImpl;
import com.evolveum.midpoint.gui.impl.factory.WrapperContext;
import com.evolveum.midpoint.gui.impl.registry.GuiComponentRegistryImpl;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.web.component.prism.ValueStatus;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MetadataType;

/**
 * @author katka
 *
 */
public class MetadataWrapperFactory extends ContainerWrapperFactoryImpl<MetadataType>{

	
	@Autowired private GuiComponentRegistryImpl registry;
	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.impl.factory.WrapperFactory#match(com.evolveum.midpoint.prism.ItemDefinition)
	 */
	@Override
	public boolean match(ItemDefinition<?> def) {
		return QNameUtil.match(MetadataType.COMPLEX_TYPE, def.getTypeName());
	}

	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.impl.factory.WrapperFactory#register()
	 */
	@Override
	public void register() {
		registry.addToRegistry(this);
	}

	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.impl.factory.WrapperFactory#getOrder()
	 */
	@Override
	public int getOrder() {
		return 10;
	}
	
	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.impl.factory.ContainerWrapperFactoryImpl#createNewValue(com.evolveum.midpoint.prism.PrismContainer)
	 */
	@Override
	protected PrismContainerValue<MetadataType> createNewValue(PrismContainer<MetadataType> item) {
		// TODO Auto-generated method stub
		return super.createNewValue(item);
	}
	
	
	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.impl.factory.ContainerWrapperFactoryImpl#createValueWrapper(com.evolveum.midpoint.gui.api.prism.PrismContainerWrapper, com.evolveum.midpoint.prism.PrismContainerValue, com.evolveum.midpoint.web.component.prism.ValueStatus, com.evolveum.midpoint.gui.impl.factory.WrapperContext)
	 */
	@Override
	public PrismValueWrapper<?> createValueWrapper(PrismContainerWrapper<MetadataType> parent,
			PrismContainerValue<MetadataType> value, ValueStatus status, WrapperContext context) throws SchemaException {
		// TODO Auto-generated method stub
		return super.createValueWrapper(parent, value, status, context);
	}
	
	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.impl.factory.ItemWrapperFacotryImpl#createWrapper(com.evolveum.midpoint.gui.impl.prism.PrismContainerValueWrapper, com.evolveum.midpoint.prism.ItemDefinition, com.evolveum.midpoint.gui.impl.factory.WrapperContext)
	 */
	@Override
	public PrismContainerWrapper<MetadataType> createWrapper(PrismContainerValueWrapper<?> parent, ItemDefinition<?> def,
			WrapperContext context) throws SchemaException {
		// TODO Auto-generated method stub
		return super.createWrapper(parent, def, context);
	}

	
	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.impl.factory.ContainerWrapperFactoryImpl#createWrapper(com.evolveum.midpoint.gui.impl.prism.PrismContainerValueWrapper, com.evolveum.midpoint.prism.PrismContainer, com.evolveum.midpoint.gui.api.prism.ItemStatus)
	 */
	@Override
	protected PrismContainerWrapper<MetadataType> createWrapper(PrismContainerValueWrapper<?> parent,
			PrismContainer<MetadataType> childContainer, ItemStatus status) {
		// TODO Auto-generated method stub
		return super.createWrapper(parent, childContainer, status);
	}
	
}
