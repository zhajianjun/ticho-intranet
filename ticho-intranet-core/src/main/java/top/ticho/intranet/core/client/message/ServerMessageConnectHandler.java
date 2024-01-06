package top.ticho.intranet.core.client.message;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import top.ticho.intranet.core.client.handler.AppConnectAfterHander;
import top.ticho.intranet.core.entity.Message;

import java.nio.charset.StandardCharsets;

/**
 * 服务端通道连接消息处理器
 *
 * @author zhajianjun
 * @date 2023-12-17 08:30
 */
@Slf4j
public class ServerMessageConnectHandler extends AbstractServerMessageHandler {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message message) {
        Channel serverChannel = ctx.channel();
        String data = StrUtil.str(message.getData(), StandardCharsets.UTF_8);
        String requestId = message.getUri();
        String host = StrUtil.subBefore(data, ":", false);
        String portStr = StrUtil.subAfter(data, ":", false);
        if (StrUtil.isBlank(host)) {
            log.error("地址不能为空 {}", host);
            return;
        }
        if (!StrUtil.isNumeric(portStr)) {
            log.error("端口不能为空 {}", portStr);
            return;
        }
        int port = Integer.parseInt(portStr);
        AppConnectAfterHander listener = new AppConnectAfterHander(clientHander, appHandler, clientProperty, serverChannel, requestId);
        log.debug("[客户端]连接{}:{}", host, port);
        log.warn("[4][客户端]接收连接信息, 连接通道{}, 消息{}", serverChannel, message);
        appHandler.connect(host, port, listener);
    }

}
