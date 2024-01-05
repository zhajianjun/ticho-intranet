package top.ticho.intranet.core.enums;

import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 消息类型
 *
 * @author zhajianjun
 * @date 2023-12-17 08:30
 */
public enum MsgType {

    /** 验证消息以检查accessKey是否正确 */
    AUTH((byte) 1, "验证消息以检查accessKey是否正确"),
    /** 无效的访问密钥 */
    DISABLED_ACCESS_KEY((byte) 2, "禁用访问密钥"),
    /** 客户端链接 */
    CONNECT((byte) 3, "客户端通道连接"),
    /** 客户端断开链接 */
    DISCONNECT((byte) 4, "客户端断开通道连接"),
    /** 数据传输 */
    TRANSFER((byte) 5, "数据传输"),
    /** 客户端心跳 */
    HEARTBEAT((byte) 6, "心跳检测"),
    ;

    private final byte code;
    private final String msg;


    MsgType(byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public byte code() {
        return this.code;
    }

    public String msg() {
        return this.msg;
    }

    private static final Map<Byte, MsgType> map;


    static {
        // @formatter:off
        map = Arrays.stream(values()).collect(Collectors.toMap(MsgType::code, Function.identity()));
        // @formatter:on
    }

    public static MsgType getMsgType(Number number) {
        Byte i = number.byteValue();
        return map.get(i);
    }

    public static void main(String[] args) {
        for (MsgType value : values()) {
            System.out.println(StrUtil.format("/** {} */\npublic static final byte {} = MsgType.{}.getCode();", value.msg(), value.name(), value.name()));
        }
    }

}
