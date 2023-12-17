package top.ticho.intranet.core.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.ticho.intranet.core.constant.CommConst;
import top.ticho.intranet.core.entity.TichoMsg;

@Slf4j
@AllArgsConstructor
public class AppListenHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final AppHandler appHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        Channel responseChannel = ctx.channel();
        Channel serverChannel = responseChannel.attr(CommConst.CHANNEL).get();
        String uri = responseChannel.attr(CommConst.URI).get();
        if (serverChannel == null) {
            responseChannel.close();
            return;
        }
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        TichoMsg msg = new TichoMsg();
        msg.setType(TichoMsg.TRANSFER);
        msg.setUri(uri);
        msg.setData(data);
        serverChannel.writeAndFlush(msg);
        log.warn("[9][客户端]响应信息回传，响应通道{}，回传通道{}，消息{}", responseChannel, serverChannel, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel requestCHannel = ctx.channel();
        String uri = requestCHannel.attr(CommConst.URI).get();
        appHandler.removeRequestChannel(uri);
        Channel clientChannel = requestCHannel.attr(CommConst.CHANNEL).get();
        if (null != clientChannel) {
            TichoMsg msg = new TichoMsg();
            msg.setType(TichoMsg.DISCONNECT);
            msg.setUri(uri);
            clientChannel.writeAndFlush(msg);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel requestCHannel = ctx.channel();
        Channel channel = requestCHannel.attr(CommConst.CHANNEL).get();
        if (null != channel) {
            channel.config().setOption(ChannelOption.AUTO_READ, requestCHannel.isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("客户端异常 {} {}", ctx.channel(), cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }

}
