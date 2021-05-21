package ast.servicio.probatch.os.service.impl.as400;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.ObjectDoesNotExistException;

public class AS400Process extends Process {

	private static Logger logger = LoggerFactory.getLogger(AS400Process.class);

	private Thread thread;
	private byte[] jobId;
	private CommandCall cmd;
	private boolean result;

	public AS400Process(CommandCall command)
			throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException {
		cmd = command;
		RunCommandAS400 rc = new RunCommandAS400(cmd);
		jobId = (byte[]) cmd.getServerJob().getValue(Job.INTERNAL_JOB_IDENTIFIER);
		logger.debug("AS400Process jobID: " + this.jobId);
		thread = new Thread(rc);
		thread.start();
	}

	@Override
	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getErrorStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int waitFor() throws InterruptedException {
		thread.join();
		return exitValue();
	}

	@Override
	public int exitValue() {
		if (this.sucessfulExecution())
			return 0;
		return 1;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	public Thread getThread() {
		return thread;
	}

	public byte[] getJobId() {
		return jobId;
	}

	public boolean sucessfulExecution() {
		return result;
	}

	public CommandCall getCmd() {
		return cmd;
	}

	private class RunCommandAS400 implements Runnable {

		public RunCommandAS400(CommandCall c) {
			cmd = c;
		}

		public void run() {
			try {
				result = cmd.run();
			} catch (Exception e) {
				System.out.println("Command " + cmd.getCommand() + " issued an exception!");
				e.printStackTrace();
			}

		}

	}

}
