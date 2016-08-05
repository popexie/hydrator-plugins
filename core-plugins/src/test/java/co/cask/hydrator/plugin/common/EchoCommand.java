package co.cask.hydrator.plugin.common;

import co.cask.cdap.api.common.Bytes;
import org.apache.sshd.common.util.ValidateUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class EchoCommand implements Command {

  private static final String ECHO_PREFIX = "echo ";

  private final String command;
  private final String message;

  private OutputStream out;
  private ExitCallback callback;

  public EchoCommand(String command) {
    this.command = ValidateUtils.checkNotNullAndNotEmpty(command, "No command provided.");
    this.message = parseEchoCommand(command);
  }

  private String parseEchoCommand(String command) {
    return command.substring(command.indexOf(ECHO_PREFIX) + ECHO_PREFIX.length());
  }

  public String getCommand() {
    return command;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public void setInputStream(InputStream in) {
    // No-op; unused
  }

  @Override
  public void setOutputStream(OutputStream out) {
    this.out = out;
  }

  @Override
  public void setErrorStream(OutputStream err) {
    // No-op; unused
  }

  @Override
  public void setExitCallback(ExitCallback callback) {
    this.callback = callback;
  }

  @Override
  public void start(Environment environment) throws IOException {
    ValidateUtils.checkNotNull(out, "No output stream");
    out.write(Bytes.toBytes(message));
    out.write('\n');
    out.flush();
    if (callback != null) {
      callback.onExit(0, message);
    }
  }

  @Override
  public void destroy() {
    // No-op; unused
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    } else {
      return obj == this || Objects.equals(getCommand(), ((EchoCommand) obj).getCommand());
    }
  }
}
