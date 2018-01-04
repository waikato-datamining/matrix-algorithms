/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * TestHelper.java
 * Copyright (C) 2010-2018 University of Waikato, Hamilton, New Zealand
 */
package com.github.waikatodatamining.matrix.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper classes for tests.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class TestHelper {

  /** the owning test case. */
  protected AbstractTestCase m_Owner;

  /** the data directory to use. */
  protected String m_DataDirectory;

  /**
   * Initializes the helper class.
   *
   * @param owner	the owning test case
   * @param dataDir	the data directory to use
   */
  public TestHelper(AbstractTestCase owner, String dataDir) {
    super();

    m_Owner         = owner;
    m_DataDirectory = dataDir;
  }

  /**
   * Returns the data directory in use.
   *
   * @return		the directory
   */
  public String getDataDirectory() {
    return m_DataDirectory;
  }

  /**
   * Returns the tmp directory.
   *
   * @return		the tmp directory
   */
  public String getTmpDirectory() {
    return System.getProperty("java.io.tmpdir");
  }

  /**
   * Returns the location in the tmp directory for given resource.
   *
   * @param resource	the resource (path in project) to get the tmp location for
   * @return		the tmp location
   * @see		#getTmpDirectory()
   */
  public String getTmpLocationFromResource(String resource) {
    String	result;
    File	file;

    file   = new File(resource);
    result = getTmpDirectory() + File.separator + file.getName();

    return result;
  }

  /**
   * Closes the stream, if possible, suppressing any exception.
   *
   * @param is		the stream to close
   */
  protected void closeQuietly(InputStream is) {
    if (is != null) {
      try {
	is.close();
      }
      catch (Exception e) {
	// ignored
      }
    }
  }

  /**
   * Closes the stream, if possible, suppressing any exception.
   *
   * @param os		the stream to close
   */
  protected void closeQuietly(OutputStream os) {
    if (os != null) {
      try {
	os.flush();
      }
      catch (Exception e) {
	// ignored
      }
      try {
	os.close();
      }
      catch (Exception e) {
	// ignored
      }
    }
  }

  /**
   * Copies the given resource to the tmp directory.
   *
   * @param resource	the resource (path in project) to copy
   * @return		false if copying failed
   * @see		#getTmpLocationFromResource(String)
   */
  public boolean copyResourceToTmp(String resource) {
    boolean			result;
    BufferedInputStream		input;
    BufferedOutputStream	output;
    FileOutputStream		fos;
    byte[]			buffer;
    int				read;
    String			ext;

    input    = null;
    output   = null;
    resource = getDataDirectory() + "/" + resource;

    fos = null;
    try {
      input  = new BufferedInputStream(ClassLoader.getSystemResourceAsStream(resource));
      fos    = new FileOutputStream(getTmpLocationFromResource(resource));
      output = new BufferedOutputStream(fos);
      buffer = new byte[1024];
      while ((read = input.read(buffer)) != -1) {
	output.write(buffer, 0, read);
	if (read < buffer.length)
	  break;
      }
      result = true;
    }
    catch (IOException e) {
      if (e.getMessage().equals("Stream closed")) {
	ext = resource.replaceAll(".*\\.", "");
	System.err.println(
	    "Resource '" + resource + "' not available? "
	    + "Or extension '*." + ext + "' not in pom.xml ('project.build.testSourceDirectory') listed?");
      }
      e.printStackTrace();
      result = false;
    }
    catch (Exception e) {
      e.printStackTrace();
      result = false;
    }

    closeQuietly(input);
    closeQuietly(output);
    closeQuietly(fos);

    return result;
  }

  /**
   * Removes the file from the tmp directory.
   *
   * @param filename	the file in the tmp directory to delete (no path!)
   * @return		true if deleting succeeded or file not present
   */
  public boolean deleteFileFromTmp(String filename) {
    boolean	result;
    File	file;

    result = true;
    file   = new File(getTmpDirectory() + File.separator + filename);
    if (file.exists())
      result = file.delete();

    return result;
  }

  /**
   * Attempts to create a directory in the temp directory.
   * 
   * @param dirname	the relative directory path
   * @return		true if successfully created
   */
  public boolean createDirInTmp(String dirname) {
    File	file;
    
    file = new File(getTmpDirectory() + File.separator + dirname);
    return file.mkdirs();
  }
  
  /**
   * Removes the directory recursively from the tmp directory.
   *
   * @param dirname	the directory in the tmp directory to delete (relative path!)
   * @return		true if deleting succeeded or dir not present
   */
  public boolean deleteDirFromTmp(String dirname) {
    return new File(getTmpDirectory() + File.separator + dirname).delete();
  }
}
