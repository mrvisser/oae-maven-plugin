/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.oae.maven;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generate an OSGi bundle.
 * 
 * @requiresProject false
 * @goal osgi
 */
public class CreateOsgiMojo extends TemplateExtractorPlugin {

  /**
   * The parent directory of the new project.
   * 
   * @parameter expression="${basedir}"
   */
  private String baseDir;

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.oae.maven.TemplateExtractorPlugin#getPackageUrl()
   */
  @Override
  public String getPackageUrl() {
    return "http://www.mrvisser.ca/sakai/oae-plugin/osgi-simple.tar";
  }

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.oae.maven.TemplateExtractorPlugin#getTargetDir()
   */
  @Override
  public String getTargetDir() {
    return baseDir;
  }

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.oae.maven.TemplateExtractorPlugin#getDefaults()
   */
  @Override
  public Map<String, Object> getDefaults() {
    Map<String, Object> defaults = new HashMap<String, Object>();
    defaults.put("groupId", "org.sakaiproject");
    defaults.put("version", "0.1-SNAPSHOT");
    defaults.put("nakamuraVersion", "1.3.0");
    defaults.put("baseDir", baseDir);
    return defaults;
  }
  
  /**
   * {@inheritDoc}
   * @see org.sakaiproject.oae.maven.TemplateExtractorPlugin#buildContextProperties(java.util.List, java.util.Map)
   */
  @Override
  protected Map<String, Object> buildContextProperties(List<ConfigurationProperty> configProperties,
      Map<String, Object> allProps) {
    Map<String, Object> context = super.buildContextProperties(configProperties, allProps);
    
    // ensure the packageAlias is set.
    if (!context.containsKey("packageAlias")) {
      context.put("packageAlias", context.get("artifactId"));
    }
    
    return context;
    
  }

  @Override
  public List<ConfigurationProperty> getConfigurationProperties() {
    return Arrays.asList(
        new ConfigurationProperty("groupId", null, "org.sakaiproject"),
        new ConfigurationProperty("artifactId", null, null),
        new ConfigurationProperty("version", null, "0.1-SNAPSHOT"),
        new ConfigurationProperty("nakamuraVersion", "parent nakamura version", "1.3.0"),
        new ConfigurationProperty("packageAlias", "class package (e.g., myapp)", "The artifactId"),
        new ConfigurationProperty("baseDir", "target directory", "Current directory")
      );
  }
  
}
