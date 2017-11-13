package com.tool.remote.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * 远端执行sh命令工具类
 *
 * @author zhiyinghou
 */
public class JschUtility {

  public static String cmd(String cmd) throws InterruptedException {
    StringBuilder sb = null;
    Process process = null;
    System.out.println("Start to execute command: " + cmd);
    try {
      // 使用Runtime来执行command，生成Process对象
      process = Runtime.getRuntime().exec(cmd);
      // 取得命令结果的输出流
      InputStream is = process.getInputStream();
      // 用一个读输出流类去读
      InputStreamReader isr = new InputStreamReader(is, "UTF-8");
      // 用缓冲器读行
      BufferedReader br = new BufferedReader(isr);
      String line = null;
      sb = new StringBuilder();
      while ((line = br.readLine()) != null) {
//                    logger.info(line);
        sb.append(line);
      }
      // 关闭流
      is.close();
      isr.close();
      br.close();

      int exitCode = process.waitFor();

    } catch (NullPointerException e) {
      System.err.println("NullPointerException " + e.getMessage());
//                logger.error(cmd);
    } catch (IOException e) {
      System.err.println("IOException " + e.getMessage());
    } finally {
      process.destroy();
    }

    return sb.toString();
  }

  // exec
  public static String ssh(String host, String command) throws Exception {
    JSch jsch = new JSch();

//		Session session = jsch.getSession(user, host, port);
    Session session = jsch.getSession(host);
//		session.setPassword(psw);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect();

    ChannelExec channel = (ChannelExec) session.openChannel("exec");
    channel.setCommand(command);
    channel.connect();

    InputStream in = channel.getInputStream();
    byte[] tmp = new byte[1024];
    StringBuffer builder = new StringBuffer();
    while (true) {
      while (in.available() > 0) {
        int i = in.read(tmp, 0, 1024);
        if (i < 0) {
          break;
        }
        builder.append(new String(tmp, 0, i));
        System.out.print(new String(tmp, 0, i));
      }
      if (channel.isClosed()) {
        System.out.println("exit-status: " + channel.getExitStatus());
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (Exception ee) {
      }
    }
    channel.disconnect();
    session.disconnect();
    return builder.toString();
  }

  // sftpUpload
  public void sftpUpload(String host, String user, String psw, int port, String sourcefile,
      String destfile)
      throws Exception {
    JSch jsch = new JSch();

    Session session = jsch.getSession(user, host, port);
    session.setPassword(psw);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect();

    ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
    channel.connect();

    try {
      channel = (ChannelSftp) session.openChannel("sftp");
      channel.connect(1000);
      ChannelSftp sftp = (ChannelSftp) channel;

      OutputStream outstream = sftp.put(destfile);
      InputStream instream = new FileInputStream(new File(sourcefile));

      byte b[] = new byte[1024];
      int n;
      while ((n = instream.read(b)) != -1) {
        outstream.write(b, 0, n);
      }
      outstream.flush();
      outstream.close();
      instream.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.disconnect();
      channel.disconnect();
    }
  }

  // sftp
  public void sftpDownload(String host, String user, String psw, int port, String sourcefile,
      String destfile)
      throws Exception {
    JSch jsch = new JSch();

    Session session = jsch.getSession(user, host, port);
    session.setPassword(psw);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect();

    ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
    channel.connect();

    try {
      channel = (ChannelSftp) session.openChannel("sftp");
      channel.connect(1000);
      ChannelSftp sftp = (ChannelSftp) channel;

      InputStream is = sftp.get(sourcefile);
      OutputStream os = new FileOutputStream(new File(destfile));

      byte b[] = new byte[1024];
      int n;
      while ((n = is.read(b)) != -1) {
        os.write(b, 0, n);
      }
      os.flush();
      os.close();
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      session.disconnect();
      channel.disconnect();
    }
  }
}
