package serverVariants.Netty;


import corePackage.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;

;

@Slf4j

public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private Path currentPath;
    private Path clientPath;
   // AuthService service = new AuthService();

    public FileMessageHandler() throws IOException {
        currentPath = Paths.get("server", "root");
        if (!Files.exists(currentPath)) {
            Files.createDirectory(currentPath);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListResponse(currentPath));

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        log.debug("received: {}", command.getType());
        switch (command.getType()) {
            case FILE_MESSAGE:
                FileMessage fileMessage = (FileMessage) command;
                Files.write(
                        currentPath.resolve(fileMessage.getName()),
                        fileMessage.getBytes()
                );
                ctx.writeAndFlush(new ListResponse(currentPath));
                log.debug("Received a file {} from the client", fileMessage.getName());
                break;

            case FILE_REQUEST:
                FileRequest fileRequest = (FileRequest) command;
                String fileName = fileRequest.getName();
                Path file = Paths.get(String.valueOf(currentPath), fileName);
                ctx.writeAndFlush(new FileMessage(file));
                log.debug("Send file {} to the client", fileName);
                break;

            case LIST_REQUEST:
                ctx.writeAndFlush(new ListResponse(currentPath));
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                log.debug("Send list of files to the client");
                break;

            case PATH_UP_REQUEST:
                if (currentPath.getParent() != null) {
                    if (currentPath.equals(clientPath)) {
                        log.debug("Above the client's folder , it is not necessary to rise");
                    } else {
                        currentPath = currentPath.getParent();
                    }
                }
                log.debug("Send list of files and current directory to the client");
                ctx.writeAndFlush(new ListResponse(currentPath));
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                break;

            case PATH_IN_REQUEST:
                PathInRequest request = (PathInRequest) command;
                Path newPAth = currentPath.resolve(request.getDir());
                if (Files.isDirectory(newPAth)) {
                    currentPath = newPAth;
                    log.debug("Send list of files and current directory to the client");
                    ctx.writeAndFlush(new ListResponse(currentPath));
                    ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                } else {
                    log.debug("{} is not a directory",request);
                }
            case AUTH_REQUEST:
                AuthRequest authRequest = (AuthRequest) command;
                String login = authRequest.getLogin();
                String password = authRequest.getPassword();
                AuthResponse authResponse = new AuthResponse();
                if (service.findByLogin(login).equals(password)) {
                    authResponse.setAuthStatus(true);
                    clientPath = Paths.get("D:\\cloud_storage_2021\\server", login);
                    if (!Files.exists(clientPath)) {
                        Files.createDirectory(clientPath);
                    }
                    currentPath = clientPath;
                } else {
                    authResponse.setAuthStatus(false);
                }
                ctx.writeAndFlush(authResponse);
                break;
            default:
                log.debug("Invalid command {}", command.getType());
                break;
        }
    }

}
