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
package org.sakaiproject.oae.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sakaiproject.oae.maven.Help;
import org.sakaiproject.oae.maven.HelpProvider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @requiresProject false
 * @goal help
 */
public class HelpMojo extends AbstractMojo implements HelpProvider {

  public static Help createHelp() {
    return new Help("help", "Returns this help directory.");
  }
  
  /**
   * {@inheritDoc}
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    Map<String, Help> helpEntries = new TreeMap<String, Help>();
    for (Class<HelpProvider> clazz : getClassesForPackage("/org/sakaiproject/oae/maven/plugins")) {
      Help help = createHelp(clazz);
      if (help != null) {
        helpEntries.put(help.goal, help);
      } else {
        getLog().warn("Class "+clazz.getCanonicalName()+" returned a null help directory entry.");
      }
    }
    
    getLog().info("");
    getLog().info("");
    
    if (!helpEntries.isEmpty()) {
      getLog().info("Available goals:");
      getLog().info("");
      for (String goal : helpEntries.keySet()) {
        StringBuilder content = new StringBuilder("\toae:").append(goal)
            .append(" - ").append(helpEntries.get(goal).description);
        getLog().info(content);
      }
    } else {
      getLog().info("No help available.");
    }
    
    getLog().info("");
    getLog().info("");
    getLog().info("To get help on individual goals, try mvn oae:<goal> -Dhelp=true");
    getLog().info("");
    getLog().info("");
    
  }
  
  private Help createHelp(Class<HelpProvider> clazz) {
    String methodName = "createHelp";
    try {
      Method method = clazz.getMethod(methodName);
      return (Help) method.invoke(clazz);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private ArrayList<Class<HelpProvider>> getClassesForPackage(String absolutePath) {
    String relativePath = absolutePath.substring(1);
    
    ArrayList<Class<HelpProvider>> classes = new ArrayList<Class<HelpProvider>>();
    URL resource = getClass().getResource(absolutePath);
    
    getLog().debug("Found the package resource: " + resource);
    
    if (resource == null) {
      throw new RuntimeException("Could not find the classpath resource: " + absolutePath);
    }
    
    File directory = null;
    try {
      directory = new File(resource.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(String.format("%s (%s) does not appear to be a valid URL / URI.",
          absolutePath, resource), e);
    } catch (IllegalArgumentException e) {}
    
    // scan the jar dependency for classes of this package
    if (directory == null) {
      try {
        // trim out the jar extension and protocol from the jar URI
        String jarPath = resource.getFile().replaceFirst("[.]jar[!].*", ".jar")
            .replaceFirst("file:", "");
        getLog().debug("JAR Path: "+jarPath);
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          if (!entry.isDirectory()) {
            String entryName = entry.getName();
            getLog().debug("Found JAR entry: "+entryName);
            if (entryName.startsWith(relativePath)) {
              String className = entryName.substring(relativePath.length()+1).replace(".class", "");
              getLog().debug("Chopped down to: "+ className);
              if (!className.contains("/")) {
                try {
                  String classPackage = relativePath.replace("/", ".");
                  String canonicalClassName = String.format("%s.%s", classPackage, className);
                  getLog().debug("Loading class: "+canonicalClassName);
                  Class<?> clazz = Class.forName(canonicalClassName); 
                  if (HelpProvider.class.isAssignableFrom(clazz)) {
                    getLog().debug("The class was a help provider.");
                    @SuppressWarnings("unchecked")
                    Class<HelpProvider> helpClazz = (Class<HelpProvider>) clazz;
                    classes.add(helpClazz);
                  } else {
                    getLog().debug("The class was not a help provider.");
                  }
                } catch (ClassNotFoundException e) {
                  throw new RuntimeException("ClassNotFoundException loading " + className);
                }
              } else {
                getLog().debug("Skipping since it is not a direct child of the plugin package.");
              }
            } else {
              getLog().debug("Skipping since it is not in the plugin package.");
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(absolutePath + " (" + directory
            + ") does not appear to be a valid package", e);
      }
    } else {
      getLog().error("Found the package "+absolutePath+" in a directory instead of a JAR. This should not happen.");
    }
    
    return classes;
  }
}
