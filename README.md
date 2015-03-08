# p2prouter

##背景
以前使用花生壳（动态域名），外网访问内网，但是我在linux下面，花生壳不能正常工作，网上查找资料没有找到最终解决办法，bbs提问也无人解答；于是自己尝试做一个类似功能：在外网也能访问内网服务器。  
刚好，我有一台阿里云主机，于是一个想法产生了：阿里云作为代理，访问内网。

##方案
1. 内网主机和阿里云主机绑定
2. 客户端的请求通过阿里云转发给内网主机，内网主机执行并将结果通过阿里云主机返回给客户端

##流程
1. 阿里云：在阿里云主机上运行一个路由服务程序（RouterServer），在端口（Port）等待前来注册的内网服务器（InnerServer）
2. 内网服务器：内网服务器（InnerServer）启动后，就去RouterServer申请注册一个对外服务的端口（ServerPort）
3. 阿里云：RouterServer读取InnerServer申请的ServerPort，并且将这个socket一直保持连接，然后传递给InnerServerProxy，随后再启动一个HttpServer监听公网请求，这样阿里云和内网主机绑定完成
4. 外网客户端：访问阿里云上的HttpServer，请求http://HttpServer：ServerPort/cgi?command=CommandFileName
5. 阿里云：HttpServer提取CommandFileName，传递给InnerServerProxy,InnerServerProxy通过socket，发送到InnerServer
6. 内网主机：InnerServer获得CommandFileName，然后执行CommandFileName，将结果返回InnerServerProxy
7. 阿里云：InnerServerProxy将结果转给HttpServer，HttpServer响应客户端

##使用
###路由服务器启动
在阿里云主机上启动路由服务器，监听端口8888
```shell
java com.whatwhatgame.p2p.router.P2PRouterServer 8888
```
或者
```shell
java -jar p2p-router-server.jar 8888
```
或者(默认8888端口，进入sh脚本里面修改)
```shell
chmod +x p2p-router-server.sh
./p2p-router-server.sh
```
启动后在8888等待前来注册的内网服务器

###内网服务器启动
启动需要传递参数：  
1. 阿里云主机IP  
2. 阿里云监听的端口 8888  
3. 希望在阿里云上开启的服务端口 ServicePort  
```shell
java com.whatwhatgame.p2p.server.P2PInnerServer IP 8888 ServicePort
```
或者
```shell
java -jar p2p-router-server.jar IP 8888 ServicePort
```
或者(端口信息在p2p-inner-server.sh里面，使用时需要修改)
```shell
chmod +x p2p-inner-server.sh
./p2p-inner-server.sh
```
启动连接阿里云的路由服务器，并且绑定这个链接的socket，这样客户端请求能够转发到内网

###客户端访问
在浏览器的地址栏里面输入  
http://HttpServer：ServerPort/cgi?command=CommandFileName  
通过阿里云主机转发到内网，执行并返回结果  

##补充
由于tcp链接的不稳定，在shell中加入了failover的思路，在RouterServer和InnerServer都添加两种机制  
1. 死掉自动重启  
2. 按周期主动重启
p2p-inner-server
```shell
chmod +x p2p-inner-server-kill.sh
./p2p-inner-server-kill.sh
```
p2p-router-server
```shell
chmod +x p2p-router-server-kill.sh
./p2p-router-server-kill.sh
```

##应用
外网请求转换为内网命令执行  
我的应用场景就是远程遥控树莓派小车，http://whatwhatgame.com/   这样在浏览器里面发送【前后左右停】命令，通过阿里云转发到内网执行，从而控制小车的运动。（同时把小车的拍摄的视频传回客户端，实现远程收看，远程遥控，写到此处发现和srs（流式媒体推送服务器：上面网站使用此提供视频服务）的思路很类似：https://github.com/winlinvip/simple-rtmp-server/wiki/v1_CN_Home）
