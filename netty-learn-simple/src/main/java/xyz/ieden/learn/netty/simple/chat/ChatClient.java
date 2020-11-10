package xyz.ieden.learn.netty.simple.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * ChatClient
 *
 * @author gavin
 * @version 1.0
 * @datetime 2020/11/10 22:51
 */
public class ChatClient {
    public static void main(String[] args) {
        EventLoopGroup eventExecutors = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()))
                                    .addLast(new StringEncoder(CharsetUtil.UTF_8))
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                    .addLast(new ChatClientHandle());
                        }
                    });
            Channel channel = bootstrap.connect("127.0.0.1", 8090).sync().channel();
            System.out.println("请输入内容");

            for (; ; ) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                channel.writeAndFlush(bufferedReader.readLine() + "\r\n");
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            eventExecutors.shutdownGracefully();
        }
    }
}

class ChatClientHandle extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
    }
}
