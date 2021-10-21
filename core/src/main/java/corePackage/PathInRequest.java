package corePackage;

public class PathInRequest extends Command{

    private final String root;

    public PathInRequest(String dir) {
        this.root = dir;
    }

    public String getDir() {
        return root;
    }

    @Override
    public CommandType getType() {
        return CommandType.PATH_IN_REQUEST;
    }
}
