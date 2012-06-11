package org.sakaiproject.vtlgen;

import org.apache.commons.io.IOUtils;
import org.sakaiproject.vtlgen.api.PackageUtil;
import org.sakaiproject.vtlgen.api.Runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

/**
 *
 */
public class PackageRunner implements Runner<URL> {

  private final static FilesystemDirectoryRunner fsRunner =
      new FilesystemDirectoryRunner();

  public void run(URL url, File targetRoot, Map<String, Object> context) {
    InputStream is = null;
    OutputStream os = null;
    File pkg = null;
    try {
      pkg = File.createTempFile("vtlg-pkg", String.valueOf(System.currentTimeMillis()));
      is = url.openStream();
      os = new FileOutputStream(pkg);
      IOUtils.copy(is, os);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(os);
    }
    
    try {
      File dir = File.createTempFile("vtlg-dir", String.valueOf(System.currentTimeMillis()));
      dir.delete();
      dir.mkdir();
      PackageUtil.untar(pkg.getAbsolutePath(), dir.getAbsolutePath());
      fsRunner.run(dir, targetRoot, context);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
