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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sakaiproject.vtlgen.PackageRunner;
import org.sakaiproject.vtlgen.api.Runner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class bakes in common functionality for scaffolding a project based on a
 * template package.
 */
public abstract class AbstractTemplateExtractorPlugin extends AbstractMojo {

  public static final String PROP_HELP = "help";
  
  private static final Runner<URL> runner = new PackageRunner();
  
  /**
   * {@inheritDoc}
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public final void execute() throws MojoExecutionException, MojoFailureException {
    Map<String, Object> allProps = buildAllAvailablePluginProperties();
    if ("true".equals(allProps.get(PROP_HELP))) {
      printHelp();
      return;
    }
    
    List<ConfigurationProperty> configProps = getConfigurationProperties();
    Map<String, Object> ctx = buildContextProperties(configProps, allProps);
    validate(configProps, ctx);
    
    try {
      runner.run(new URL(getPackageUrl()), new File(getTargetDir()), ctx);
    } catch (IOException e) {
      throw new MojoExecutionException("Could not generate project from template", e);
    }
  }

  /**
   * @return The URL to the template package that should be used to process the scaffolding. 
   */
  public abstract String getPackageUrl();
  
  /**
   * @return The directory in which the scaffold should be extracted.
   */
  public abstract String getTargetDir();
  
  /**
   * @return The configuration properties used in the package context. These properties
   * are used in the following ways:
   * <p>
   * <strong>key:</strong> Used for display purposes in the help content, as well as used
   * to pull input parameters from the environment into the template context. Unless you
   * override {@link #buildContextProperties(List, Map)}, only properties whose key is
   * provided as a configuration property will be included in the context.
   * <p>
   * <strong>description:</strong> Used for display purposes only in the help content.
   * <p>
   * <strong>defaultValue:</strong> Used for <strong>display purposes</strong> to show the
   * default value. Also, if defaultValue is {@code null}, the property will be considered
   * as reauired, and will be validated as such. If you want to provide the actual default
   * context value for the property, do so using {@link #getDefaults()}.
   */
  public abstract List<ConfigurationProperty> getConfigurationProperties();
  
  /**
   * @return The default property values if some are missing.
   */
  public abstract Map<String, Object> getDefaults();
  
  /**
   * Validate that the context is consistent with the requirements just before
   * the package is extracted.
   * 
   * @param ctx
   */
  protected void validate(List<ConfigurationProperty> configurationProperties,
      Map<String, Object> ctx) {
    for (ConfigurationProperty cp : configurationProperties) {
      if (cp.defaultValue == null && !ctx.containsKey(cp.key)) {
        throw new RuntimeException("Missing property: " + getPropertyHelpLine(cp));
      }
    }
  }
  
  /**
   * Get all context properties that will be passed for package extraction.
   * 
   * @param configProps
   * @param allProps
   * @return
   */
  protected Map<String, Object> buildContextProperties(List<ConfigurationProperty> configProps,
      Map<String, Object> allProps) {
    Map<String, Object> availableProperties = new HashMap<String, Object>(getDefaults());
    
    if (configProps == null) {
      return allProps;
    }
    
    for (ConfigurationProperty cp : configProps) {
      if (allProps.containsKey(cp.key)) {
        Object valObj = allProps.get(cp.key);
        String val = (valObj == null) ? null : valObj.toString();
        availableProperties.put(cp.key, getPropertyValue(cp.key, val));
      }
    }
    
    return availableProperties;
  }
  
  /**
   * Parse the given String system property into the appropriate context property. It is
   * expected that this would be overridden to handle situations where you want to bind
   * more complex objects to the context (e.g., convert property "1,2,3" to an array
   * [1, 2, 3]).
   * 
   * @param key
   * @param value
   * @return
   */
  protected Object getPropertyValue(String key, String value) {
    return value;
  }
  
  /**
   * @return The aggregated list of all properties that are available for the context. These
   * are to be filtered out according to the list of properties required.
   */
  private Map<String, Object> buildAllAvailablePluginProperties() {
    Map<String, Object> result = new HashMap<String, Object>();
    
    for (Object keyObj : System.getProperties().keySet()) {
      String key = keyObj.toString();
      result.put(key, System.getProperty(key));
    }
    
    for (Object keyObj : getPluginContext().keySet()) {
      result.put(keyObj.toString(), getPluginContext().get(keyObj).toString());
    }
    
    return result;
  }
  
  /**
   * Print the help content for the plugin user.
   */
  private void printHelp() {
    List<ConfigurationProperty> cps = getConfigurationProperties();
    getLog().info("*** USAGE ***");
    if (cps != null && !cps.isEmpty()) {
      getLog().info("Available Properties:");
      for (ConfigurationProperty cp : cps) {
        getLog().info("\t"+getPropertyHelpLine(cp));
      }
    } else {
      getLog().info("This plugin has no parameters, just run the goal.");
    }
    getLog().info("*** /USAGE ***");
  }
  
  /**
   * Get the help content for one individual property.
   * 
   * @param cp
   * @return
   */
  private String getPropertyHelpLine(ConfigurationProperty cp) {
    StringBuilder line = new StringBuilder(String.format("-D%s ", cp.key));
    
    if (cp.description != null) {
      line.append("= <"+cp.description+"> ");
    }
    
    if (cp.defaultValue == null) {
      line.append("(Required)");
    } else {
      line.append("(Default: "+cp.defaultValue+")");
    }
    
    return line.toString();
  }
  
}
