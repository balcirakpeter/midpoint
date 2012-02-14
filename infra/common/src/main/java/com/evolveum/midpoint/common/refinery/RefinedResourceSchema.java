/**
 * Copyright (c) 2011 Evolveum
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
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.common.refinery;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.Definition;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.Schema;
import com.evolveum.midpoint.prism.SchemaRegistry;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.exception.SchemaException;
import com.evolveum.midpoint.schema.processor.ResourceAttributeContainerDefinition;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.schema.util.ResourceTypeUtil;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.Dumpable;
import com.evolveum.midpoint.xml.ns._public.common.common_1.AccountShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ResourceAccountTypeDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.SchemaHandlingType;

/**
 * @author semancik
 *
 */
public class RefinedResourceSchema extends Schema implements Dumpable, DebugDumpable {
	
	private SchemaRegistry schemaRegistry;
	private Schema originalResourceSchema;
	
	private RefinedResourceSchema(ResourceType resourceType, Schema originalResourceSchema, SchemaRegistry schemaRegistry) {
		super(resourceType.getNamespace());
		this.originalResourceSchema = originalResourceSchema;
		this.schemaRegistry = schemaRegistry;
	}
	
	public Collection<RefinedAccountDefinition> getAccountDefinitions() {
		Set<RefinedAccountDefinition> accounts = new HashSet<RefinedAccountDefinition>();
		for (Definition def: definitions) {
			if (def instanceof RefinedAccountDefinition) {
				RefinedAccountDefinition rad = (RefinedAccountDefinition)def;
				accounts.add(rad);
			}
		}
		return accounts;
	}
	
	public Schema getOriginalResourceSchema() {
		return originalResourceSchema;
	}

	/**
	 * if null accountType is provided, default account definition is returned.
	 */
	public RefinedAccountDefinition getAccountDefinition(String accountType) {
		for (RefinedAccountDefinition acctDef: getAccountDefinitions()) {
			if (accountType == null && acctDef.isDefault()) {
				return acctDef;
			}
			if (acctDef.getAccountTypeName().equals(accountType)) {
				return acctDef;
			}
		}
		return null;
	}
	
	public RefinedAccountDefinition getDefaultAccountDefinition() {
		return getAccountDefinition(null);
	}
	
	public PrismObjectDefinition<AccountShadowType> getObjectDefinition(String accountType) {
		return getAccountDefinition(accountType).getObjectDefinition();
	}
	
	public PrismObjectDefinition<AccountShadowType> getObjectDefinition(AccountShadowType shadow) {
		return getObjectDefinition(shadow.getAccountType());
	}
		
	private void add(RefinedAccountDefinition refinedAccountDefinition) {
		definitions.add(refinedAccountDefinition);
	}
	
	/**
	 * If already refined, return the version created before
	 */
	public static RefinedResourceSchema getRefinedSchema(ResourceType resourceType, SchemaRegistry schemaRegistry) throws SchemaException {
		if (resourceType instanceof EnhancedResourceType) {
			EnhancedResourceType enh = (EnhancedResourceType) resourceType;
			if (enh.getRefinedSchema() != null) {
				return enh.getRefinedSchema();
			} else {
				RefinedResourceSchema refinedSchema = parse(resourceType, schemaRegistry);
				enh.setRefinedSchema(refinedSchema);
				return refinedSchema;
			}
		}
		RefinedResourceSchema refinedSchema = parse(resourceType, schemaRegistry);
		return refinedSchema;
	}
	
	public static Schema getResourceSchema(ResourceType resource) throws SchemaException {
		Element resourceXsdSchema = ResourceTypeUtil.getResourceXsdSchema(resource);
		if (resourceXsdSchema == null) {
			return null;
		}
		if (resource instanceof EnhancedResourceType) {
			EnhancedResourceType enh = (EnhancedResourceType) resource;
			if (enh.getParsedSchema() != null) {
				return enh.getParsedSchema();
			} else {
				Schema parsedSchema = Schema.parse(resourceXsdSchema);
				enh.setParsedSchema(parsedSchema);
				return parsedSchema;
			}
		}
		Schema parsedSchema = Schema.parse(resourceXsdSchema);
		return parsedSchema;
	}


	public static RefinedResourceSchema parse(ResourceType resourceType, SchemaRegistry schemaRegistry) throws SchemaException {
		
		Schema originalResourceSchema = getResourceSchema(resourceType);
		
		SchemaHandlingType schemaHandling = resourceType.getSchemaHandling();
		
		RefinedResourceSchema rSchema = new RefinedResourceSchema(resourceType, originalResourceSchema, schemaRegistry);
		
		if (schemaHandling != null) {
		
			if (schemaHandling.getAccountType() != null && !schemaHandling.getAccountType().isEmpty()) {
		
				parseAccountTypesFromSchemaHandling(rSchema, resourceType, schemaHandling, schemaRegistry, 
						"definition of "+ObjectTypeUtil.toShortString(resourceType));
				
			} else {
				parseAccountTypesFromSchema(rSchema, resourceType, schemaRegistry, 
						"definition of "+ObjectTypeUtil.toShortString(resourceType));
			}
			
		} else {
			parseAccountTypesFromSchema(rSchema, resourceType, schemaRegistry, 
					"definition of "+ObjectTypeUtil.toShortString(resourceType));
		}
		
		return rSchema;
	}

	private static void parseAccountTypesFromSchemaHandling(RefinedResourceSchema rSchema, ResourceType resourceType,
			SchemaHandlingType schemaHandling, SchemaRegistry schemaRegistry, String contextDescription) throws SchemaException {
		
		RefinedAccountDefinition rAccountDefDefault = null;
		for (ResourceAccountTypeDefinitionType accountTypeDefType: schemaHandling.getAccountType()) {
			String accountTypeName = accountTypeDefType.getName();
			RefinedAccountDefinition rAccountDef = RefinedAccountDefinition.parse(accountTypeDefType, resourceType, rSchema, schemaRegistry, "account type '"+accountTypeName+"', in "+contextDescription);
			
			if (rAccountDef.isDefault()) {
				if (rAccountDefDefault == null) {
					rAccountDefDefault = rAccountDef;
				} else {
					throw new SchemaException("More than one default account definitions ("+rAccountDefDefault+", "+rAccountDef+") in " + contextDescription);
				}
			}
				
			rSchema.add(rAccountDef);
		}		
	}

	private static void parseAccountTypesFromSchema(RefinedResourceSchema rSchema, ResourceType resourceType,
			SchemaRegistry schemaRegistry, String contextDescription) throws SchemaException {

		RefinedAccountDefinition rAccountDefDefault = null;
		for(ResourceAttributeContainerDefinition accountDef: rSchema.getOriginalResourceSchema().getAccountDefinitions()) {
			QName objectClassname = accountDef.getTypeName();
			RefinedAccountDefinition rAccountDef = RefinedAccountDefinition.parse(accountDef, resourceType, rSchema, schemaRegistry, 
					"object class "+objectClassname+" (interpreted as account type definition), in "+contextDescription);
			
			if (rAccountDef.isDefault()) {
				if (rAccountDefDefault == null) {
					rAccountDefDefault = rAccountDef;
				} else {
					throw new SchemaException("More than one default account definitions ("+rAccountDefDefault+", "+rAccountDef+") in " + contextDescription);
				}
			}
				
			rSchema.add(rAccountDef);
		}
		
	}
	
	@Override
	public <T extends ObjectType> PrismObject<T> parseObjectType(T objectType) throws SchemaException {
		if (objectType instanceof AccountShadowType) {
			AccountShadowType accountShadowType = (AccountShadowType)objectType;
			String accountType = accountShadowType.getAccountType();
			RefinedAccountDefinition accountDefinition = null;
			if (accountType == null) {
				accountDefinition = getDefaultAccountDefinition();
				if (accountDefinition == null) {
					throw new IllegalArgumentException("Definition for default account type was not found in "+this+", the type was specified in "+ObjectTypeUtil.toShortString(accountShadowType));
				}
			} else {
				accountDefinition = getAccountDefinition(accountType);
				if (accountDefinition == null) {
					throw new IllegalArgumentException("Definition for account type "+accountType+" was not found in "+this+", the type was specified in "+ObjectTypeUtil.toShortString(accountShadowType));
				}
			}
			return (PrismObject<T>) accountDefinition.getObjectDefinition().parseObjectType(accountShadowType);
		} else {
			throw new IllegalArgumentException("Refined resource schema can only parse instances of AccountShadowType");
		}
	}

	@Override
	public String toString() {
		return "RSchema(ns=" + namespace + ")";
	}
}
