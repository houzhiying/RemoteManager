package com.tool.remote.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 序列化工具
 *
 * @author zhiyinghou
 */
public class SerialUtility {

  /**
   * 保存对象到文件中
   */
  public static void serialize(Object object, File file) throws IOException {
    file.createNewFile();
    FileOutputStream fos = new FileOutputStream(file);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(object);
    oos.flush();
    oos.close();
    fos.close();
  }

  /**
   * 反序列化，从文件中还原对象
   */
  public static Object deserialize(File file) throws IOException, ClassNotFoundException {
    FileInputStream fis = new FileInputStream(file);
    ObjectInputStream ois = new ObjectInputStream(fis);
    Object object = ois.readObject();
    ois.close();
    fis.close();
    return object;
  }

}
