package org.windwant.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.windwant.common.util.ConfigUtil;

/**
 * Hello world!
 *
 */
public class BootProxy
{
    private static final Logger logger = LoggerFactory.getLogger(BootProxy.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public static void main(String[] args) throws Exception {
        BootProxy bootProxy = new BootProxy();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                bootProxy.shutdownGraceFully();
            }
        });
        bootProxy.start();
    }


    private void start() throws InterruptedException {
        logger.info("bootProxy server start... ");
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap;
        ChannelFuture channelFuture = null;
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_RCVBUF, 256 * 1024)
                    .option(ChannelOption.SO_SNDBUF, 256 * 1024)
                    .childHandler(new BootProxyInitializer());
            //绑定端口
            channelFuture = bootstrap.bind(ConfigUtil.getInteger("server.port"));
        } catch (Exception e) {
            logger.error("bootProxy Server Start ERROR", e);
            throw new RuntimeException(e);
        } finally {
            if (null != channelFuture) {
                channelFuture.sync().channel().closeFuture().sync();
            }
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void shutdownGraceFully() {
        if (bossGroup != null) {
            logger.info("the boss group is shutdown gracefully!");
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            logger.info("the work group is shutdown gracefully!");
            workerGroup.shutdownGracefully();
        }
    }
}
