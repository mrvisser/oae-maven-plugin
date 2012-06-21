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
package org.sakaiproject.vtlgen;

import org.apache.commons.io.IOUtils;
import org.sakaiproject.vtlgen.api.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Run the processor from a source that is a file-system directory.
 */
public class FilesystemDirectoryRunner implements Runner<File> {

  private final static Logger LOGGER = LoggerFactory.getLogger(FilesystemDirectoryRunner.class);
  private final static Pattern FILENAME_EXPR_PATTERN = Pattern.compile("(\\$\\{([^}]+)\\})");
  
  private final static VelocityFileProcessor processor = new VelocityFileProcessor();
  
  /**
   * {@inheritDoc}
   * @see org.sakaiproject.vtlgen.api.Runner#run(java.lang.Object, java.io.File, java.util.Map)
   */
  public void run(File packageRoot, File target, Map<String, Object> context) {
    LOGGER.debug("Beginning FilesystemDirectoryRunner with context: {}", context);
    prepareAndValidate(packageRoot, target);
    for (File child : packageRoot.listFiles()) {
      runInternal(child, target, context);
    }
  }
  
  /**
   * Perform the run operation on the given source file, applying it to the destination. This
   * method is almost identical to the {@link #run(File, File, Map)} method, except it will not
   * check the pre-requisites that only need to be run the first time.
   * 
   * @param source The source file that should be (if necessary) processed by the generator
   * @param targetParent The *parent* of the target location. The actual filename will be determined by the generator
   * @param context The context data
   */
  private void runInternal(File source, File targetParent, Map<String, Object> context) {
    LOGGER.debug("runInternal from '{}' to target parent '{}'", source.getAbsolutePath(),
        targetParent.getAbsolutePath());
    File target = process(source, targetParent, context);
    LOGGER.debug("Finished processing of '{}'", source.getAbsolutePath());
    if (source.isDirectory()) {
      for (File child : source.listFiles()) {
        runInternal(child, target, context);
      }
    }
  }
  
  /**
   * Process the file and copy it to the destination. This includes expanding filename
   * expressions enclosed in ${...}, and expanding .vtlg files with a velocity template
   * processor.  
   * 
   * @param source
   * @param targetParent
   * @param context
   * @return
   */
  private File process(File source, File targetParent, Map<String, Object> context) {
    String targetPath = new StringBuilder(targetParent.getAbsolutePath()).append(File.separator)
        .append(processVtlgFilename(source.getName(), context)).toString();
    
    if (new File(targetPath).exists()) {
      throw new RuntimeException("Target file "+targetPath+" already exists. Not overwriting.");
    }
    
    LOGGER.debug("Filtering file to {}", targetPath);
    
    if (isVtlgFile(source)) {
      LOGGER.debug("File needs to be processed.");
      File targetFile = new File(targetPath);
      FileInputStream fis = null;
      FileOutputStream fos = null;
      try {
        targetFile.createNewFile();
        processor.processFile(new FileInputStream(source), new FileOutputStream(targetFile),
            context);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        IOUtils.closeQuietly(fis);
        IOUtils.closeQuietly(fos);
      }
    } else {
      LOGGER.debug("Does not need processing. Will just copy.");
      copyFile(source, targetPath);
    }
    
    LOGGER.info("Extracted file: "+targetPath);
    
    return new File(targetPath);
  }
  
  /**
   * Given a file-name that possibly has ${...} expressions, expand it into the
   * actual target file name.
   * 
   * @param name
   * @param context
   * @return
   */
  private String processVtlgFilename(String name, Map<String, Object> context) {
    String processedName = name;
    
    if (processedName.endsWith(".vtlg")) {
      processedName = processedName.substring(0, processedName.length() - 5);
    }
    
    if (isExprFilename(processedName)) {
      Matcher m = FILENAME_EXPR_PATTERN.matcher(name);
      while (m.find()) {
        String toReplace = m.group(1);
        String propertyName = m.group(2);
        if (context.containsKey(propertyName)) {
          Object val = context.get(propertyName);
          if (val != null) {
            processedName = processedName.replace(toReplace, val.toString());
          }
        }
      }
    }
    return processedName;
  }
  
  /**
   * Determine whether or not the given file name should be parsed for ${...} expressions.
   * 
   * @param name
   * @return
   */
  private boolean isExprFilename(String name) {
    return name.contains("${");
  }

  /**
   * Determine whether or not this file should be run through the velocity template
   * processor.
   * 
   * @param f
   * @return
   */
  private boolean isVtlgFile(File f) {
    return !f.isDirectory() && f.getName().endsWith(".vtlg");
  }
  
  /**
   * Copy the {@code from} file or directory to the {@code toPath} location.
   * 
   * @param from
   * @param toPath
   */
  private void copyFile(File from, String toPath) {
    File to = new File(toPath);
    if (from.isDirectory()) {
      to.mkdir();
    } else {
      try {
        to.createNewFile();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
          fis = new FileInputStream(from);
          fos = new FileOutputStream(to);
          IOUtils.copy(fis, fos);
        } finally {
          IOUtils.closeQuietly(fis);
          IOUtils.closeQuietly(fos);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  /**
   * Ensure the state of the source and target root directories, and validate that
   * the runner may run.
   * 
   * @param packageRoot
   * @param target
   */
  private void prepareAndValidate(File packageRoot, File target) {
    if (packageRoot == null)
      throw new IllegalArgumentException("Package root may not be null.");
    if (!packageRoot.exists() || !packageRoot.isDirectory())
      throw new IllegalArgumentException(String.format("Package root %s must be an existing directory.",
          packageRoot.getAbsolutePath()));
    if (target == null)
      throw new IllegalArgumentException("The target argument may not be null");
    
    if (target.exists() && !target.isDirectory())
      throw new IllegalArgumentException(String.format("Target directory %s must be a directory.",
          target.getAbsolutePath()));
    
    if (!target.exists())
      target.mkdirs();
    
  }
}
