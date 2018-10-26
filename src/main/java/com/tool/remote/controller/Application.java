package com.tool.remote.controller;

import com.tool.remote.utils.JschUtility;
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

/**
 * springboot 已经启动内嵌的是tomcat . not jetty
 */
@RestController
@SpringBootApplication
public class Application extends WebMvcConfigurerAdapter implements
    EmbeddedServletContainerCustomizer {

  static ApplicationContext context = null;

  public static void main(String[] args) throws Exception {
    context = SpringApplication.run(Application.class, args);
  }

  /*******************************************异常测试接口*******************************************/
  // 端口操作
  // 关闭端口：host/api/port/{port}?open=false
  // 打开端口：host/api/port/{port}?open=true
  @RequestMapping(value = "/api/port/{port}", method = RequestMethod.GET)
  @ResponseBody
  public String portOpt(@PathVariable String port, final Boolean open) throws Exception {
    String cmd = null;
    if (open) {
      cmd = "sudo /etc/init.d/iptables stop";
    } else {
      cmd = "sudo iptables -A OUTPUT -p tcp --dport " + port + " -j DROP";
    }
    return exec(cmd);
  }

  // ip操作
  // 关闭端口：host/api/ip/{ip}/?open=false
  // 打开端口：host/api/ip/{ip}/?open=true
  @RequestMapping(value = "/api/ip/{ip}/", method = RequestMethod.GET)
  @ResponseBody
  public String ipOpt(@PathVariable String ip, final Boolean open) throws Exception {
    String cmd = null;
    if (open) {
      cmd = "sudo /etc/init.d/iptables stop";
    } else {
      cmd = "sudo iptables -I INPUT -s " + ip + " -j DROP";
    }
    return exec(cmd);
  }

  // ip、port操作
  // url规则：host/api/ip/{ip}/port/{port}
  @RequestMapping(value = "/api/ip/{ip}/port/{port}", method = RequestMethod.GET)
  @ResponseBody
  public String ipAndPortOpt(@PathVariable String ip, @PathVariable String port) throws Exception {
    String cmd = "sudo iptables -I OUTPUT -d " + ip + " -p tcp  --dport " + port + " -j REJECT --reject-with tcp-reset";
    return exec(cmd);
  }

  //停止iptables
  // url规则：host/api/stop/iptables
  @RequestMapping(value = "/api/stop/iptables", method = RequestMethod.GET)
  @ResponseBody
  public String stopIptables() throws Exception {
    String command = "sudo /etc/init.d/iptables stop";
    return exec(command);
  }

  //CPU高负载
  //url:host/api/cpu/burn
  @RequestMapping(value = "/api/cpu/burn", method = RequestMethod.GET)
  @ResponseBody
  public void burncpu() {
    String cmd = "openssl speed";
    for (int i = 0; i <= 3; i++) {
      new Thread("thread " + i) {
        public void run() {
          try {
            exec(cmd);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }.start();
    }
  }

  //url:host/api/cpu/burn/recover
  @RequestMapping(value = "/api/cpu/burn/recover", method = RequestMethod.GET)
  @ResponseBody
  public void burncpuRecover() throws Exception {
    String cmd = "pkill -KILL -f 'openssl speed'";
    exec(cmd);
  }

  //磁盘高负载
  //url:host/api/io/burn
  @RequestMapping(value = "/api/io/burn", method = RequestMethod.GET)
  @ResponseBody
  public void burnio() throws Exception {
    for (int i = 0; i < 20; i++) {
      int j = i;
      new Thread("thread " + j) {
        public void run() {
          try {
            exec("pkill -KILL -f 'dd if=/dev/urandom of=.*/burnio" + j
                + " bs=1M count=1024 iflag=fullblock'");
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }.start();
    }
  }

  //url:host/api/io/burn/recover
  @RequestMapping(value = "/api/io/burn/recover", method = RequestMethod.GET)
  @ResponseBody
  public void burnioRecover() throws Exception {
    String cmd = "pkill -KILL -f 'dd if=/dev/urandom of=.*/burnio bs=1M count=1024 iflag=fullblock'";
    String cmd2 = "rm -f ~/burnio*";
    exec(cmd);
    exec(cmd2);
  }

  //磁盘空间不足
  //url规则：host/api/disk/fill?capacity=
  @RequestMapping(value = "/api/disk/fill", method = RequestMethod.GET)
  @ResponseBody
  public void fillDisk(int capacity) throws Exception {
    String cmd = "nohup fallocate -l " + capacity + "G ~/huge_file &";
    exec(cmd);
  }

  @RequestMapping(value = "/api/disk/fill/recover", method = RequestMethod.GET)
  @ResponseBody
  public void fillDiskRecover() throws Exception {
    String cmd = "rm -f ~/huge_file";
    exec(cmd);
  }

  //网络恶化：随机改变一些包数据，使数据内容不正确
  //url规则：host/api/network/corruption?percentage=
  @RequestMapping(value = "/api/network/corruption", method = RequestMethod.GET)
  @ResponseBody
  public String networkCorruption(String percentage) throws Exception {
    // 噪声百分比
    String cmd = "tc qdisc add dev eth0 root netem corrupt " + percentage;
    exec(cmd);
    //查看状态
    String cmd2 = "tc -s qdisc";
    return exec(cmd2);
  }

  @RequestMapping(value = "/api/network/corruption/recover", method = RequestMethod.GET)
  @ResponseBody
  public String networkCorruptionRecover() throws Exception {
    String cmd = "tc qdisc del dev eth0 root netem";
    return exec(cmd);
  }

  //网络延迟：将包延迟一个特定范围的时间
  //url规则：host/api/network/latency?time1=&time2&size=
  @RequestMapping(value = "/api/network/latency", method = RequestMethod.GET)
  @ResponseBody
  public String networkLatency(int time1, int time2, int size) throws Exception {
    String cmd =
        "tc qdisc add dev eth0 root netem delay " + time1 + "ms " + time2 + "ms " + size + "%";
    exec(cmd);
    //查看状态
    String cmd2 = "tc -s qdisc";
    return exec(cmd2);
  }

  @RequestMapping(value = "/api/network/latency/recover", method = RequestMethod.GET)
  @ResponseBody
  public String networkLatencyRecover() throws Exception {
    String cmd = "tc qdisc del dev eth0 root netem";
    return exec(cmd);
  }

  //网络丢包：构造一个tcp不会完全失败的丢包率
  //url规则：host/api/network/loss?loss=&success=
  @RequestMapping(value = "/api/network/loss", method = RequestMethod.GET)
  @ResponseBody
  public String networkLoss(String loss, String success) throws Exception {
    String cmd = "tc qdisc add dev eth0 root netem loss " + loss + "% " + success + "%";
    exec(cmd);

    //查看状态
    String cmd2 = "tc -s qdisc";
    return exec(cmd2);
  }

  @RequestMapping(value = "/api/network/loss/recover", method = RequestMethod.GET)
  @ResponseBody
  public String networkLossRecover() throws Exception {
    String cmd = "tc qdisc del dev eth0 root netem";
    return exec(cmd);
  }

  //网络黑洞：忽略来自某个ip的包 nullroute
  //url规则：host/api/null/route?ip=
  @RequestMapping(value = "/api/null/route", method = RequestMethod.GET)
  @ResponseBody
  public String nullroute(String ip) throws Exception {
    String cmd = "ip route add blackhole " + ip;
    return exec(cmd);
  }

  //url规则：host/api/null/route/recover?ip=
  @RequestMapping(value = "/api/null/recover/", method = RequestMethod.GET)
  @ResponseBody
  public String nullrouterecover(String ip) throws Exception {
    String cmd = "ip route delete " + ip;
    return exec(cmd);
  }

  /*******************************************tether 测试*******************************************/
  //开启微服务
  // url规则：host/api/server/start?serverName=
  @RequestMapping(value = "/api/server/start", method = RequestMethod.GET)
  @ResponseBody
  public String startServer(String serverName) throws Exception {
    String cmd = "sudo /opt/" + serverName + "/bin/start.sh";
    return exec(cmd);
  }

  //关闭微服务
  // url规则：host/api/server/stop?serverName=
  @RequestMapping(value = "/api/server/stop", method = RequestMethod.GET)
  @ResponseBody
  public String closeServer(String serverName) throws Exception {
    String cmd = "sudo /opt/" + serverName + "/bin/stop.sh";
    return exec(cmd);
  }

  //启动tether
  @RequestMapping(value = "/api/server/tether/start", method = RequestMethod.GET)
  @ResponseBody
  public String startTether() throws Exception {
    String cmd = "sudo /opt/python/bin/supervisorctl start yz-tether:yz-tether-1";
    return exec(cmd);
  }

  //关闭tether
  @RequestMapping(value = "/api/server/tether/stop", method = RequestMethod.GET)
  @ResponseBody
  public String stopTether() throws Exception {
    String cmd = "sudo /opt/python/bin/supervisorctl stop yz-tether:yz-tether-1";
    return exec(cmd);
  }

  //重启tether
  @RequestMapping(value = "/api/server/tether/restart", method = RequestMethod.GET)
  @ResponseBody
  public String restartTether() throws Exception {
    String cmd1 = "sudo rm -rf /data/logs/yz-tether/tether.log";
    String cmd2 = "sudo /opt/python/bin/supervisorctl restart yz-tether:yz-tether-1";
    //删除tether.log
    exec(cmd1);
    //重启tether服务
    return exec(cmd2);
  }

  //清理tether log
  @RequestMapping(value = "/api/log/tether/clean", method = RequestMethod.GET)
  @ResponseBody
  public String cleanFile() throws Exception {
    String cmd = "sudo su - app && echo '' > /data/logs/yz-tether/tether.log";
    return exec(cmd);
  }

  //统计tether log中的关键词
  @RequestMapping(value = "/api/count/log", method = RequestMethod.GET)
  @ResponseBody
  public String countLogs(String keywords) throws Exception {
    String cmd = "cat /data/logs/yz-tether/tether.log | grep -o " + keywords + " | wc -l";
    return exec(cmd);
  }


  /*******************************************统一接入测试*******************************************/
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

  /*******************************************DTS测试*******************************************/
  @RequestMapping(value = "/api/server/sinker/start", method = RequestMethod.GET)
  @ResponseBody
  public void startSinker() throws Exception {
    String cmd = "cd /tmp && bash /tmp/jenkins-ci.sh";
    exec(cmd);
  }

  @RequestMapping(value = "/api/server/sinker/stop", method = RequestMethod.GET)
  @ResponseBody
  public void stopSinker() throws Exception {
    String cmd = "netstat -anput | grep 8085 | awk -F'/' '{print $1}' | awk '{print $7}' | xargs kill -9";
    exec(cmd);
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
    container.setPort(8090);
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension(false);
  }

//  private String getRequestStringBody(HttpServletRequest request) throws IOException {
//    BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
//    StringBuilder sb = new StringBuilder();
//    String line = "";
//    while ((line = reader.readLine()) != null) {
//      sb.append(line);
//    }
//    return sb.toString();
//  }

  private String exec(String cmd) throws Exception {
    return JschUtility.cmd(cmd);
  }

}
