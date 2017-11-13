package com.tool.remote.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * prop文件工具类
 *
 * @author zhiyinghou
 */
public class StringUtils {

  public static String getJsonString(String fileName) {

    return String.format(loadFile(fileName));

  }

  // 加载数据
  public static String loadFile(String file) {
    try {
      InputStream stream = StringUtils.class.getClassLoader().getResourceAsStream(file);
      return IOUtils.toString(stream);
    } catch (IOException ex) {
      ex.printStackTrace();
      throw new RuntimeException("Unable load file " + file);
    }
  }

  //
  public static String getJsonStringAsAbsolute(File file) {

    return String.format(loadFileAsAbsolute(file));

  }

  public static String loadFileAsAbsolute(File file) {

    try {
      InputStream stream = new FileInputStream(file);
      return IOUtils.toString(stream);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException("Unable load file " + file);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Unable load file " + file);
    }
  }

  public static Properties getProperties(String envfile) {
    Properties prop = new Properties();

    try {
      prop.load(StringUtils.class.getClassLoader().getResourceAsStream(envfile));
    } catch (Exception ex) {
      throw new RuntimeException("Unable to load property file " + envfile);
    }
    return prop;
  }
}
