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
package com.evolveum.midpoint.schema;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import com.evolveum.midpoint.prism.GlobalDynamicNamespacePrefixMapper;
import com.evolveum.midpoint.prism.SchemaRegistry;
import com.evolveum.midpoint.schema.constants.MidPointConstants;
import com.evolveum.midpoint.schema.exception.SchemaException;
import com.evolveum.midpoint.schema.namespace.MidPointNamespacePrefixMapper;
import com.evolveum.midpoint.schema.namespace.PrefixMapper;
import com.evolveum.midpoint.util.DebugUtil;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * @author semancik
 *
 */
public class SchemaRegistryFactory {
	
	public SchemaRegistry createSchemaRegistry() throws SchemaException {
		SchemaRegistry schemaRegistry = new SchemaRegistry();
		registerBuiltinSchemas(schemaRegistry);
		schemaRegistry.setObjectSchemaNamespace(SchemaConstants.NS_COMMON);
		schemaRegistry.setNamespacePrefixMapper(new GlobalDynamicNamespacePrefixMapper());
		return schemaRegistry;
	}
	
	private void registerBuiltinSchemas(SchemaRegistry schemaRegistry) throws SchemaException {
		String prefix;
		prefix = MidPointNamespacePrefixMapper.getPreferredPrefix(SchemaConstants.NS_COMMON);
		schemaRegistry.registerPrismSchemaResource("xml/ns/public/common/common-1.xsd","c");
		prefix = MidPointNamespacePrefixMapper.getPreferredPrefix(SchemaConstants.NS_ANNOTATION);
		schemaRegistry.registerPrismSchemaResource("xml/ns/public/common/annotation-1.xsd","a");
		prefix = MidPointNamespacePrefixMapper.getPreferredPrefix(SchemaConstants.NS_RESOURCE);
		schemaRegistry.registerPrismSchemaResource("xml/ns/public/resource/resource-schema-1.xsd","r");
		prefix = MidPointNamespacePrefixMapper.getPreferredPrefix(SchemaConstants.NS_CAPABILITIES);
		schemaRegistry.registerPrismSchemaResource("xml/ns/public/resource/capabilities-1.xsd","cap");
		prefix = MidPointNamespacePrefixMapper.getPreferredPrefix(SchemaConstants.NS_ICF_CONFIGURATION);
		schemaRegistry.registerPrismSchemaResource("xml/ns/public/connector/icf-1/connector-schema-1.xsd","icfc");
		prefix = MidPointNamespacePrefixMapper.getPreferredPrefix(SchemaConstants.NS_ICF_SCHEMA);
		schemaRegistry.registerPrismSchemaResource("xml/ns/public/connector/icf-1/resource-schema-1.xsd","icfs");
		prefix = MidPointNamespacePrefixMapper.getPreferredPrefix(W3C_XML_SCHEMA_NS_URI);
		schemaRegistry.registerSchemaResource("xml/ns/standard/XMLSchema.xsd","xsd");
	}
	
//	private NamespacePrefixMapper createPrefixMapper() {
//			globalNamespacePrefixMap.clear();
//			globalNamespacePrefixMap.put(SchemaConstants.NS_C, SchemaConstants.NS_C_PREFIX);
//			globalNamespacePrefixMap.put(SchemaConstants.NS_ANNOTATION, "a");
//			globalNamespacePrefixMap.put(SchemaConstants.NS_ICF_SCHEMA, "icfs");
//			globalNamespacePrefixMap.put(SchemaConstants.NS_ICF_CONFIGURATION, "icfc");
//			globalNamespacePrefixMap.put(SchemaConstants.NS_CAPABILITIES, "cap");
//			globalNamespacePrefixMap.put(SchemaConstants.NS_RESOURCE, "r");
//			globalNamespacePrefixMap.put(SchemaConstants.NS_FILTER, "f");
//			globalNamespacePrefixMap.put(SchemaConstants.NS_PROVISIONING_LIVE_SYNC, "ls");
//			globalNamespacePrefixMap.put(SchemaConstants.NS_SITUATION, "sit");
//			globalNamespacePrefixMap.put(
//					"http://midpoint.evolveum.com/xml/ns/public/resource/idconnector/resource-schema-1.xsd",
//					"ids");
//			globalNamespacePrefixMap.put(W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi");
//			globalNamespacePrefixMap.put(W3C_XML_SCHEMA_NS_URI, "xsd");
//			globalNamespacePrefixMap.put("http://www.w3.org/2001/04/xmlenc#", "enc");
//			globalNamespacePrefixMap.put("http://www.w3.org/2000/09/xmldsig#", "ds");
//	}


	private void setupDebug() {
		DebugUtil.setDefaultNamespacePrefix(MidPointConstants.NS_MIDPOINT_PUBLIC_PREFIX);
	}

}
