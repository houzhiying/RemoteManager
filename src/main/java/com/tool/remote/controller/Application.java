package com.tool.remote.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.tool.remote.utils.JschUtility;
import com.tool.remote.utils.StringUtils;

/**
 * springboot 已经启动内嵌的是tomcat . not jetty
 */
@RestController
@SpringBootApplication
public class Application extends WebMvcConfigurerAdapter implements
    EmbeddedServletContainerCustomizer {

  static ApplicationContext context = null;
  private Properties prop = StringUtils.getProperties("cmd/cmd.properties");

  public static void main(String[] args) throws Exception {

    context = SpringApplication.run(Application.class, args);
  }

  // 端口操作     url规则：host/api/port/8788?open=true
  @RequestMapping(value = "/api/port/{id}", method = RequestMethod.GET)
  @ResponseBody
  public String portOpt(@PathVariable String id, final Boolean open) throws Exception {
    String cmd = null;
    if (open) {
      cmd = String.format(prop.getProperty("port.enable"), id);

    } else {
      cmd = String.format(prop.getProperty("port.disable"), id);
    }

    if (null == cmd) {
      throw new IllegalArgumentException("command cannot be found!");
    }

    return exec(cmd);
  }


  // 进程操作   url规则：host/api/process/8788?start=true
  @RequestMapping(value = "/api/process/{id}", method = RequestMethod.GET)
  @ResponseBody
  public String procOpt(@PathVariable String id, final Boolean start) throws Exception {

    String cmd = null;
    if (start) {
      cmd = String.format(prop.getProperty("process.enable"), id);

    } else {
      cmd = String.format(prop.getProperty("process.disable"), id);
    }

    if (null == cmd) {
      throw new IllegalArgumentException("command cannot be found!");
    }

    return exec(cmd);
  }

  //开启服务 url规则：host/api/server/start?serverName=
  @RequestMapping(value = "/api/server/start", method = RequestMethod.GET)
  @ResponseBody
  public String startServer(String serverName) throws Exception {
    String cmd = "sudo /opt/" + serverName + "/bin/start.sh";
    return exec(cmd);
  }

  //关闭服务 url规则：host/api/server/stop?serverName=
  @RequestMapping(value = "/api/server/stop", method = RequestMethod.GET)
  @ResponseBody
  public String closeServer(String serverName) throws Exception {
    String cmd = "sudo /opt/" + serverName + "/bin/stop.sh";
    return exec(cmd);
  }

  // 关闭本服务
  @RequestMapping(value = "/api/closeme", method = RequestMethod.GET)
  @ResponseBody
  public int doClose(@PathVariable String serviceCode) {

    return SpringApplication.exit(context);
  }

  // 容器开启80端口
  @Override
  public void customize(ConfigurableEmbeddedServletContainer container) {
    container.setPort(80);
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension(false);
  }

  private String getRequestStringBody(HttpServletRequest request) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
    StringBuilder sb = new StringBuilder();
    String line = "";
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    return sb.toString();
  }

  private String exec(String cmd) throws Exception {
    return JschUtility.cmd(cmd);
  }

}
