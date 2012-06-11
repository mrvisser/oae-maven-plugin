# What is it?

It's the maven plugin for Sakai OAE.

# Prerequisites

## 1. Download and install Maven 2 or 3

## 2. Add the pluginGroups entry to your ~/.m2/settings.xml file. This is mine:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <pluginGroups>
	  <pluginGroup>org.sakaiproject</pluginGroup>
  </pluginGroups>
</settings>
```

The pluginGroups entry allows maven to search the org.sakaiproject group to locate the OAE plugin.

# Goals

## OSGI

The `osgi` goal allows you to scaffold an OSGI bundle from scratch.

### Examples

* `mvn oae:osgi -Dhelp=true`: Displays all available options for the goal. Doesn't actually generate any projects
* `mvn oae:osgi`: Generates a simple OSGi bundle using all the defaults (artifactId is org.sakaiproject.oae-generated-project)
* `mvn oae:osgi -DtemplateUrl=http://www.mrvisser.ca/sakai/oae-plugin/osgi-simple.tar`: Generates a simple OSGi bundle using all the defaults, the templates are loaded from the osgi-simple.tar file.
* `mvn oae:osgi -DartifactId=todo`: Generates a simple OSGi bundle with artifactId `todo`.
