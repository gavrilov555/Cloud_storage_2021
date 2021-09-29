package serverVariants.Netty;

import corePackage.Command;
import io.netty.channel.ChannelHandlerContext;

public abstract class SimpleChannelInboundHandler<C> {
    protected abstract void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception;
}
