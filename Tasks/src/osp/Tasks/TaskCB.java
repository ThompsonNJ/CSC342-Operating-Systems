package osp.Tasks;

import java.util.ArrayList;
import java.util.Vector;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Ports.*;
import osp.Memory.*;
import osp.FileSys.*;
import osp.Utilities.*;
import osp.Hardware.*;

/**
 * The student module dealing with the creation and killing of tasks. A task
 * acts primarily as a container for threads and as a holder of resources.
 * Execution is associated entirely with threads. The primary methods that the
 * student will implement are do_create(TaskCB) and do_kill(TaskCB). The student
 * can choose how to keep track of which threads are part of a task. In this
 * implementation, an array is used.
 * 
 * @OSPProject Tasks
 */
public class TaskCB extends IflTaskCB {
	private ArrayList<ThreadCB> threads;
	private ArrayList<PortCB> ports;
	private ArrayList<OpenFile> openFiles;

	/**
	 * The task constructor. Must have
	 * 
	 * super();
	 * 
	 * as its first statement.
	 * 
	 * @OSPProject Tasks
	 */
	public TaskCB() {
		super();
	}

	/**
	 * This method is called once at the beginning of the simulation. Can be used to
	 * initialize static variables.
	 * 
	 * @OSPProject Tasks
	 */
	public static void init() {

	}

	/**
	 * Sets the properties of a new task, passed as an argument.
	 * 
	 * Creates a new thread list, sets TaskLive status and creation time, creates
	 * and opens the task's swap file of the size equal to the size (in bytes) of
	 * the addressable virtual memory.
	 * 
	 * @return task or null
	 * 
	 * @OSPProject Tasks
	 */
	static public TaskCB do_create() {
		TaskCB newTask = new TaskCB();
		PageTable pT = new PageTable(newTask);
		newTask.setPageTable(pT);
		newTask.threads = new ArrayList<ThreadCB>();

		newTask.ports = new ArrayList<PortCB>();
		newTask.openFiles = new ArrayList<OpenFile>();
		newTask.setCreationTime(HClock.get());
		newTask.setPriority(0);
		newTask.setStatus(TaskLive);
		newTask.setStatus(TaskLive);
		FileSys.create(TaskCB.SwapDeviceMountPoint + newTask.getID(), (int) Math.pow(2, MMU.getVirtualAddressBits()));
		OpenFile swapFile = OpenFile.open(TaskCB.SwapDeviceMountPoint + newTask.getID(), newTask);
		// newTask.setSwapFile(swapFile);
		if (swapFile == null) {
			ThreadCB.dispatch();
			return null;
		} else {
			newTask.setSwapFile(swapFile);
		}

		ThreadCB.create(newTask);
		return newTask;

	}

	/**
	 * Kills the specified task and all of it threads.
	 * 
	 * Sets the status TaskTerm, frees all memory frames (reserved frames may not be
	 * unreserved, but must be marked free), deletes the task's swap file.
	 * 
	 * @OSPProject Tasks
	 */
	public void do_kill() {
		// to avoid ConcurrentModificationException
		int threadsSize = threads.size();
		for (int i = threadsSize - 1; i >= 0; i--) {
			threads.get(i).kill();
		}

		int portsSize = ports.size();
		for (int i = portsSize - 1; i >= 0; i--) {
			ports.get(i).destroy();
		}

		setStatus(TaskTerm);
		getSwapFile().close();
		getPageTable().deallocateMemory();

		int openFilesSize = openFiles.size();
		for (int i = openFilesSize - 1; i >= 0; i--) {
			openFiles.get(i).close();
		}
		FileSys.delete(SwapDeviceMountPoint + getID());
	}

	/**
	 * Returns a count of the number of threads in this task.
	 * 
	 * @OSPProject Tasks
	 */
	public int do_getThreadCount() {
		return threads.size();

	}

	/**
	 * Adds the specified thread to this task.
	 * 
	 * @return FAILURE, if the number of threads exceeds MaxThreadsPerTask; SUCCESS
	 *         otherwise.
	 * 
	 * @OSPProject Tasks
	 */
	public int do_addThread(ThreadCB thread) {
		if (threads.size() >= ThreadCB.MaxThreadsPerTask) {
			return FAILURE;
		} else {
			threads.add(thread);
			return SUCCESS;
		}

	}

	/**
	 * Removes the specified thread from this task.
	 * 
	 * @OSPProject Tasks
	 */
	public int do_removeThread(ThreadCB thread) {
		int i = 0;
		while (i < threads.size()) {
			if (thread.equals(threads.get(i))) {
				threads.remove(i);
				return SUCCESS;
			} else {
				i++;
			}
		}
		return FAILURE;
	}

	/**
	 * Return number of ports currently owned by this task.
	 * 
	 * @OSPProject Tasks
	 */
	public int do_getPortCount() {
		return ports.size();

	}

	/**
	 * Add the port to the list of ports owned by this task.
	 * 
	 * @OSPProject Tasks
	 */
	public int do_addPort(PortCB newPort) {
		if (ports.size() >= PortCB.MaxPortsPerTask) {
			return FAILURE;
		} else {
			ports.add(newPort);
			return SUCCESS;
		}
	}

	/**
	 * Remove the port from the list of ports owned by this task.
	 * 
	 * @OSPProject Tasks
	 */
	public int do_removePort(PortCB oldPort) {
		int i = 0;
		while (i < ports.size()) {
			if (oldPort.equals(ports.get(i))) {
				ports.remove(i);
				return SUCCESS;
			} else {
				i++;
			}
		}
		return FAILURE;

	}

	/**
	 * Insert file into the open files table of the task.
	 * 
	 * @OSPProject Tasks
	 */
	public void do_addFile(OpenFile file) {
		openFiles.add(file);

	}

	/**
	 * Remove file from the task's open files table.
	 * 
	 * @OSPProject Tasks
	 */
	public int do_removeFile(OpenFile file) {
		TaskCB tempFile = file.getTask();
		if (tempFile.equals(this)) {
			openFiles.remove(file);
			return SUCCESS;
		} else {
			return FAILURE;
		}

	}

	/**
	 * Called by OSP after printing an error message. The student can insert code
	 * here to print various tables and data structures in their state just after
	 * the error happened. The body can be left empty, if this feature is not used.
	 * 
	 * @OSPProject Tasks
	 */
	public static void atError() {
		// your code goes here

	}

	/**
	 * Called by OSP after printing a warning message. The student can insert code
	 * here to print various tables and data structures in their state just after
	 * the warning happened. The body can be left empty, if this feature is not
	 * used.
	 * 
	 * @OSPProject Tasks
	 */
	public static void atWarning() {
		// your code goes here

	}

	/*
	 * Feel free to add methods/fields to improve the readability of your code
	 */

}

/*
 * Feel free to add local classes to improve the readability of your code
 */
