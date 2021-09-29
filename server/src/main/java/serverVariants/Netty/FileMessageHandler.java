package serverVariants.Netty;

import corePackage.Command;
import io.netty.channel.ChannelHandlerContext;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private static final Path ROOT = Paths.get("server", "root");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
        // TODO: 23.09.2021 Разработка системы команд
//        Files.write(
//                ROOT.resolve(fileMessage.getName()),
//                fileMessage.getBytes()
//        );
//
//        ctx.writeAndFlush("OK");
        switch (cmd.getType()) {

        }

    }
}