/*
 * Copyright (c) 2010-2013 Evolveum
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

package com.evolveum.midpoint.web.page.admin.reports.dto;

import com.evolveum.midpoint.xml.ns._public.common.common_2a.ExportType;

import java.io.Serializable;

/**
 * @author lazyman
 */
public class ReconciliationReportDto implements Serializable {

    private static final String F_RESOURCE_OID = "resourceOid";
    private static final String F_RESOURCE_NAME_ = "resourceName";
    private static final String F_DESCRIPTION = "description";
    private static final String F_EXPORT_TYPE = "exportType";

    private String resourceOid;
    private String resourceName;
    private String description;
    private ExportType exportType;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExportType getExportType() {
        return exportType;
    }

    public void setExportType(ExportType exportType) {
        this.exportType = exportType;
    }

    public String getResourceOid() {
        return resourceOid;
    }

    public void setResourceOid(String resourceOid) {
        this.resourceOid = resourceOid;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
}
