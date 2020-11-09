package xyz.ieden.learn.netty.simple.chat;

import com.sun.org.apache.bcel.internal.generic.NEW;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * ChatServer
 *
 * @author gavin
 * @version 1.0
 * @datetime 2020/11/8 22:43
 */
public class ChatServer {
    public static void main(String[] args) {
        EventLoopGroup bootEventLoopGroup = new NioEventLoopGroup();
        EventLoopGroup workEventLoopGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()))
                            .addLast(new StringEncoder(CharsetUtil.UTF_8))
                            .addLast(new StringDecoder(CharsetUtil.UTF_8))
                            .addLast(new ChatServerHandle());
                }
            });

            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bootEventLoopGroup.shutdownGracefully();
            workEventLoopGroup.shutdownGracefully();
        }
    }
}

class ChatServerHandle extends SimpleChannelInboundHandler<String> {

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        
    }

    /**
     * 新增 Channel
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        Channel channel = ctx.channel();
        // 通知已经注册的 Channel
        channelGroup.writeAndFlush(this.sendMsg("服务器通知", channel.remoteAddress().toString(), "加入"));
        // 将当前 Channel 加入组
        channelGroup.add(channel);

        super.handlerAdded(ctx);
    }

    /**
     * Channel 断开
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        // 通知已经注册的 Channel
        channelGroup.writeAndFlush(this.sendMsg("服务器通知", channel.remoteAddress().toString(), "离开"));
        // 将当前 Channel 移除组
        channelGroup.remove(channel);
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        this.printMsg(channel.remoteAddress() + "上线");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        this.printMsg(channel.remoteAddress() + "下线");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    /**
     * 发送消息
     *
     * @param msgType
     * @param msg
     * @param msgAction
     * @return
     */
    private String sendMsg(String msgType, String msg, String msgAction) {
        String msgStr = "[" + msgType + "] -- " + msg + ", Action:" + msgAction;
        printMsg(msgStr);
        return msgStr;
    }

    /**
     * 输出信息
     *
     * @param msgStr
     */
    private void printMsg(String msgStr) {
        System.out.println(msgStr);
    }
}
