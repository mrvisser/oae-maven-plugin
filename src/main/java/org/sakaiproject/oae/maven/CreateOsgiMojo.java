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
import java.util.Map;

/**
 * Generate an OSGi bundle.
 * 
 * @requiresProject false
 * @goal osgi
 */
public class CreateOsgiMojo extends AbstractMojo {

  private static final Runner<URL> runner = new PackageRunner();
  
  /**
   * The groupId of the generated project.
   * 
   * @parameter expression="${groupId}" default-value="org.sakaiproject"
   */
  private String groupId;
  
  /**
   * The artifactId of the generated project.
   * 
   * @parameter expression="${artifactId}" default-value="org.sakaiproject.oae-generated-project"
   */
  private String artifactId;
  
  /**
   * The version of the generated project.
   * 
   * @parameter expression="${version}" default-value="0.1-SNAPSHOT"
   */
  private String version;
  
  /**
   * The version of the parent nakamura project.
   * 
   * @parameter expression="${nakamuraVersion}" default-value="1.4.0-SNAPSHOT"
   */
  private String nakamuraVersion;
  
  /**
   * The java package context of the generated project.
   * 
   * @parameter expression="${packageAlias}"
   */
  private String packageAlias;
  
  /**
   * The location of the template package for the generated project.
   * 
   * @parameter expression="${templateUrl}" default-value="http://www.mrvisser.ca/sakai/oae-plugin/osgi-simple.tar"
   */
  private String templateUrl;
  
  /**
   * The parent directory of the new project.
   * 
   * @parameter expression="${basedir}"
   */
  private String baseDir;
  
  /**
   * Whether or not we simply want to show the help options.
   * 
   * @parameter expression="${help}" default-value="false"
   */
  private String help;
  
  /**
   * {@inheritDoc}
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    
    if ("true".equals(help)) {
      printHelp();
      return;
    }
    
    Map<String, Object> ctx = new HashMap<String, Object>();
    ctx.put("groupId", getGroupId());
    ctx.put("artifactId", getArtifactId());
    ctx.put("version", getVersion());
    ctx.put("nakamuraVersion", getNakamuraVersion());
    ctx.put("packageAlias", getPackageAlias());
    
    try {
      runner.run(new URL(templateUrl), new File(getBaseDir()), ctx);
    } catch (IOException e) {
      throw new MojoExecutionException("Could not generate project from template", e);
    }
    
  }

  /**
   * @return the groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * @return the artifactId
   */
  public String getArtifactId() {
    return artifactId;
  }

  /**
   * @param artifactId the artifactId to set
   */
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @return the nakamuraVersion
   */
  public String getNakamuraVersion() {
    return nakamuraVersion;
  }

  /**
   * @param nakamuraVersion the nakamuraVersion to set
   */
  public void setNakamuraVersion(String nakamuraVersion) {
    this.nakamuraVersion = nakamuraVersion;
  }

  /**
   * @return the packageAlias
   */
  public String getPackageAlias() {
    if (packageAlias == null)
      return artifactId.substring(artifactId.lastIndexOf(".")+1, artifactId.length());
    return packageAlias;
  }

  /**
   * @param packageAlias the packageAlias to set
   */
  public void setPackageAlias(String packageAlias) {
    this.packageAlias = packageAlias;
  }

  /**
   * @return the baseDir
   */
  public String getBaseDir() {
    return baseDir;
  }

  /**
   * @param baseDir the baseDir to set
   */
  public void setBaseDir(String baseDir) {
    this.baseDir = baseDir;
  }

  /**
   * @return the templateUrl
   */
  public String getTemplateUrl() {
    return templateUrl;
  }

  /**
   * @param templateUrl the templateUrl to set
   */
  public void setTemplateUrl(String templateUrl) {
    this.templateUrl = templateUrl;
  }

  /**
   * @return the help
   */
  public String getHelp() {
    return help;
  }

  /**
   * @param help the help to set
   */
  public void setHelp(String help) {
    this.help = help;
  }

  private void printHelp() {
    getLog().info("*** USAGE ***");
    getLog().info("mvn oae:osgi -DtemplateUrl=<URL to tar package template> [");
    getLog().info("\t-DgroupId=<project group id (default: org.sakaiproject)>");
    getLog().info("\t-DartifactId=<project artifact id>");
    getLog().info("\t-Dversion=<project version>");
    getLog().info("\t-DnakamuraVersion=<parent nakamura version>");
    getLog().info("*** /USAGE ***");
  }
  
}
