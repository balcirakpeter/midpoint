/*
 * Copyright (c) 2010-2015 Evolveum
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
package com.evolveum.midpoint.web.component.prism;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

public class ReferenceWrapper extends PropertyOrReferenceWrapper<PrismReference, PrismReferenceDefinition> implements Serializable {

	private static final long serialVersionUID = 3132143219403214903L;
	
	private ObjectFilter filter;
	
	private List<QName> targetTypes;

	public ReferenceWrapper(@Nullable ContainerValueWrapper container, PrismReference reference, boolean readonly, ValueStatus status, PrismContext prismContext) {
		super(container, reference, readonly, status, null, prismContext);
	}
	
	public ReferenceWrapper(@Nullable ContainerValueWrapper container, PrismReference reference, boolean readonly,
			ValueStatus status, ItemPath path, PrismContext prismContext) {
		super(container, reference, readonly, status, path, prismContext);
	}

	public List<ValueWrapperOld> getValues() {
		if (values == null) {
			values = createValues();
		}
		return values;
	}

	private List<ValueWrapperOld> createValues() {
		List<ValueWrapperOld> values = new ArrayList<>();

		for (PrismReferenceValue prismValue : item.getValues()) {
			
			values.add(new ValueWrapperOld(this, prismValue, ValueStatus.NOT_CHANGED, prismContext));
		}

		int minOccurs = getItemDefinition().getMinOccurs();
		while (values.size() < minOccurs) {
			values.add(createAddedValue());
		}
 
		if (values.isEmpty()) {
			values.add(createAddedValue());
		}

		return values;
	}
	
	public void setTargetTypes(List<QName> targetTypes) {
		this.targetTypes = targetTypes;
	}
	
	public List<QName> getTargetTypes() {
		return targetTypes;
	}
	
	@Override
	public ValueWrapperOld createAddedValue() {
		PrismReferenceValue prv = prismContext.itemFactory().createReferenceValue();
		return new ValueWrapperOld(this, prv, ValueStatus.ADDED, prismContext);
	}
	
	public ObjectFilter getFilter() {
		return filter;
	}
	
	public void setFilter(ObjectFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getDisplayName());
		builder.append(", ");
		builder.append(status);
		builder.append("\n");
		for (ValueWrapperOld wrapper : getValues()) {
			builder.append("\t");
			builder.append(wrapper.toString());
			builder.append("\n");
		}
		return builder.toString();
	}

	@Override
	public String debugDump() {
		return debugDump(0);
	}

	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		DebugUtil.indentDebugDump(sb, indent);
		sb.append("ReferenceWrapper: ").append(PrettyPrinter.prettyPrint(getName())).append("\n");
		DebugUtil.debugDumpWithLabel(sb, "displayName", displayName, indent+1);
		sb.append("\n");
		DebugUtil.debugDumpWithLabel(sb, "status", status == null?null:status.toString(), indent+1);
		sb.append("\n");
		DebugUtil.debugDumpWithLabel(sb, "readonly", readonly, indent+1);
		sb.append("\n");
		DebugUtil.debugDumpWithLabel(sb, "itemDefinition", getItemDefinition() == null?null:getItemDefinition().toString(), indent+1);
		sb.append("\n");
		DebugUtil.debugDumpWithLabel(sb, "reference", item, indent+1);
		sb.append("\n");
		DebugUtil.debugDumpWithLabel(sb, "values", values, indent+1);
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.api.prism.ItemWrapperOld#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.api.prism.ItemWrapperOld#setDisplayName(java.lang.String)
	 */
	@Override
	public void setDisplayName(String name) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.api.prism.ItemWrapperOld#isVisible()
	 */
	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.api.prism.ItemWrapperOld#removeValue(com.evolveum.midpoint.web.component.prism.ValueWrapperOld)
	 */
	@Override
	public void removeValue(ValueWrapperOld valueWrapper) throws SchemaException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.gui.api.prism.ItemWrapperOld#getParent()
	 */
	@Override
	public ContainerValueWrapper getParent() {
		// TODO Auto-generated method stub
		return null;
	}

}
