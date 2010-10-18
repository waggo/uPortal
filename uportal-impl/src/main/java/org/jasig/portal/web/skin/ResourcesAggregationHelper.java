/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * 
 */
package org.jasig.portal.web.skin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.rendering.IPortalRenderingPipeline;
import org.jasig.resource.aggr.om.Included;
import org.jasig.resource.aggr.util.ResourcesElementsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

/**
 * Class to facilitate enabling/disabling Resources aggregation.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
@Service
public class ResourcesAggregationHelper {
    /**
     * Name of {@link System} property used to toggle default/aggregated skin output.
     */
    public static final String AGGREGATED_THEME_PARAMETER = "org.jasig.portal.web.skin.aggregated_theme";
    public static final String DEFAULT_AGGREGATION_ENABLED = Boolean.TRUE.toString();
    
    protected final Log logger = LogFactory.getLog(this.getClass());

    private ResourcesElementsProvider resourcesElementsProvider;
    private IPortalRenderingPipeline portalRenderingPipeline;
    
    /**
     * @param portalRenderingPipeline the portalRenderingPipeline to set
     */
    @Required
    public void setPortalRenderingPipeline(
            IPortalRenderingPipeline portalRenderingPipeline) {
        this.portalRenderingPipeline = portalRenderingPipeline;
    }
    
    @Autowired
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }
    
    
    /**
     * 
     * @return true if aggregation is currently enabled.
     */
    public boolean isAggregationEnabled() {
        return Included.AGGREGATED == this.resourcesElementsProvider.getIncludedType(null);
    }
    
    /**
     * Toggle resources aggregation (if and only if value parameter differs from current value).
     */
    public void setAggregationEnabled(boolean enabled) {
        if (enabled) {
            this.resourcesElementsProvider.setDefaultIncludedType(Included.AGGREGATED);
            portalRenderingPipeline.clearSystemCharacterCache();
        }
        else {
            this.resourcesElementsProvider.setDefaultIncludedType(Included.PLAIN);
        }
    }
    
    /**
     * shortcut to {@link #setAggregationEnabled(boolean)} with true.
     */
    public void enableAggregation() {
        setAggregationEnabled(true);
    }
    
    /**
     * shortcut to {@link #setAggregationEnabled(boolean)} with false.
     */
    public void disableAggregation() {
        setAggregationEnabled(false);
    }
}