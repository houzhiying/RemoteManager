package com.tool.remote.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;
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
      cmd = prop.getProperty("port.enable");

    } else {
      cmd = String.format(prop.getProperty("port.disable"), id);
    }

    if (null == cmd) {
      throw new IllegalArgumentException("command cannot be found!");
    }

    return exec(cmd);
  }

  // ip操作     url规则：host/api/ip/10.9.97.221/?open=true
  @RequestMapping(value = "/api/ip/{ip}/", method = RequestMethod.GET)
  @ResponseBody
  public String ipOpt(@PathVariable String ip, final Boolean open) throws Exception {
    String cmd = null;
    if (open) {
      cmd = prop.getProperty("ip.enable");

    } else {
      cmd = String.format(prop.getProperty("ip.disable"), ip);
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

  @RequestMapping(value = "/api/server/restart/tether", method = RequestMethod.GET)
  @ResponseBody
  public String restartTether() throws Exception {
    String cmd1 = "sudo rm -rf /data/logs/yz-tether/tether.log";
    String cmd2 = "sudo /opt/python/bin/supervisorctl restart yz-tether:yz-tether-1";
    //删除tether.log
    exec(cmd1);
    //重启tether服务
    return exec(cmd2);
  }

  @RequestMapping(value = "/api/log/tether/clean", method = RequestMethod.GET)
  @ResponseBody
  public String cleanFile() throws Exception {
    String cmd = "sudo su - app && echo '' > /data/logs/yz-tether/tether.log";
    return exec(cmd);
  }

  @RequestMapping(value = "/api/count/log", method = RequestMethod.GET)
  @ResponseBody
  public String countLogs(String keywords) throws Exception {
    String cmd = "cat /data/logs/yz-tether/tether.log | grep -o " + keywords + " | wc -l";
    return exec(cmd);
  }

  @RequestMapping(value = "/api/yz7/action/cors", method = RequestMethod.GET)
  @ResponseBody
  public String testCors(HttpServletResponse response) {
    response.addHeader("Access-Control-Allow-Origin", "http://1234.example.com");
    response.addHeader("Access-Control-Allow-Methods", "OPTIONS,HEAD,GET,POST,PUT,DELETE");
    response.addHeader("Access-Control-Allow-Credentials", "true");
    response.addHeader("Access-Control-Max-Age", "86400");
    response.addHeader("Access-Control-Expose-Headers", "x-req-id,x-z-id");
    return "Hello World";
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
    container.setPort(8089);
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
