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

/**
 * A configuration property that should be read for the packaging context.
 */
public class ConfigurationProperty {
  
  public String key;
  public String description;
  public String defaultValue;
  
  /**
   * @param key The property key
   * @param description The description (make it short) of the property
   * @param defaultValue The default value if not specified. If this is null, then the property is considered required.
   */
  public ConfigurationProperty(String key, String description, String defaultValue) {
    this.key = key;
    this.description = description;
    this.defaultValue = defaultValue;
  }
}
