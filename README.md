# What is it?

It's the maven plugin for Sakai OAE.

# Prerequisites

### 1. Download and install Maven 2 or 3

### 2. Clone and build this repo:

```sh
~$ git clone http://github.com/mrvisser/oae-maven-plugin.git
~$ cd oae-maven-plugin
~$ mvn clean install
```

### 3. Add the pluginGroups and repositories entry to your ~/.m2/settings.xml file. Here is an example:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <pluginGroups>
    <pluginGroup>org.sakaiproject</pluginGroup>
  </pluginGroups>
  
  <profiles>
    <profile>
      <id>oae</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <repositories>
        <repository>
          <id>sakai</id>
          <name>Sakai repository</name>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
            <checksumPolicy>warn</checksumPolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
            <checksumPolicy>fail</checksumPolicy>
          </snapshots>
          <url>http://source.sakaiproject.org/maven2</url>
          <layout>default</layout>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>sakai-plugin</id>
          <name>Sakai Plugins</name>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
          <url>http://source.sakaiproject.org/maven2/</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
</settings>
```

The pluginGroups entry allows maven to search the org.sakaiproject group to locate the OAE plugin.

# How to use it

Run `mvn oae:help` from the command-line and the plugin should be able to take you from there.

# Extending the packaging / scaffolding template

## Creating a new scaffolding goal

To create a new goal for scaffolding, you can extend the AbstractTemplateExtractorPlugin and follow the javadocs. Don't forget to add the standard mojo doclets like @goal and '@requiresProject false'. See OsgiEmptyMojo as a demonstration.

## Package template

* The template archive must be stored in a `tar` file.
* Filename expression: A simple filename expression that can be used to name files dynamically based on input parameters.
* *.vtdl files: A file that should be run through the velocity template processor. The ".vtlg" at the end is automatically choppoed off of the file extension after it is processed.

For example:

Consider file /tmp/template.tar that has the following content:

`${artifactId}/src/main/java/file-${artifactId}.txt.vtlg`

and the file `file.txt.vtlg` has text: "ArtifactId: $artifactId".

If you execute `mvn oae:osgi -DtemplateUrl=file:///tmp/template.tar -DartifactId=todo`, then the generated content in the working directory will be:

The resulting content is: `todo/src/main/java/file-todo.txt`

and the file `file-todo.txt` has content: "ArtifactId: todo"

## Registering in the help directory

To register a new goal into the `mvn oae:help` directory, you must place a class that implements HelpProvider inside the package `org.sakaiproject.oae.maven.plugins`. For simplicity, we use convention over configuration here. Your HelpProvider class must have static method "createHelp()" to provide the help contents, please see the HelpProvider javadoc for more info. If you extend AbstractTemplateExtractorPlugin and placed it in the proper package, then this is all taken care of for you.
