package corePackage;

import java.io.Serializable;

public class Command implements Serializable {

    public CommandType getType() {

        return type;
    }

    CommandType type;

}
