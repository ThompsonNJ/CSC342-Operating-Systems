package osp.Threads;

import java.util.Vector;
import java.util.Enumeration;
import java.util.LinkedList;

import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Interrupts.InterruptVector;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

/**
 * This class is responsible for actions related to threads, including creating,
 * killing, dispatching, resuming, and suspending threads.
 * 
 * @OSPProject Threads
 */
public class ThreadCB extends IflThreadCB {
	/**
	 * The thread constructor. Must call
	 * 
	 * super();
	 * 
	 * as its first statement.
	 * 
	 * @OSPProject Threads
	 */
	private static int quantum;
	private static LinkedList<ThreadCB> readyQueue;

	public ThreadCB() {
		super();

	}

	/**
	 * This method will be called once at the beginning of the simulation. The
	 * student can set up static variables here.
	 * 
	 * @OSPProject Threads
	 */
	public static void init() {
		quantum = 150;
		readyQueue = new LinkedList<ThreadCB>();
	}

	/**
	 * Sets up a new thread and adds it to the given task. The method must set the
	 * ready status and attempt to add thread to task. If the latter fails because
	 * there are already too many threads in this task, so does this method,
	 * otherwise, the thread is appended to the ready queue and dispatch() is
	 * called.
	 * 
	 * The priority of the thread can be set using the getPriority/setPriority
	 * methods. However, OSP itself doesn't care what the actual value of the
	 * priority is. These methods are just provided in case priority scheduling is
	 * required.
	 * 
	 * @return thread or null
	 * 
	 * @OSPProject Threads
	 */
	static public ThreadCB do_create(TaskCB task) {
		if (task.getThreadCount() >= MaxThreadsPerTask) {
			dispatch();
			return null;
		}
		ThreadCB thread = new ThreadCB();
		thread.setTask(task);
		int checkFailure = task.addThread(thread);
		if (checkFailure == FAILURE) {
			dispatch();
			return null;
		}
		thread.setPriority(task.getPriority());
		thread.setStatus(ThreadReady);
		readyQueue.add(thread);
		dispatch();
		return thread;
	}

	/**
	 * Kills the specified thread.
	 * 
	 * The status must be set to ThreadKill, the thread must be removed from the
	 * task's list of threads and its pending IORBs must be purged from all device
	 * queues.
	 * 
	 * If some thread was on the ready queue, it must removed, if the thread was
	 * running, the processor becomes idle, and dispatch() must be called to resume
	 * a waiting thread.
	 * 
	 * @OSPProject Threads
	 */
	public void do_kill() {
		if (getStatus() == ThreadReady) {
			readyQueue.remove(this);
		}
		if (getStatus() == ThreadRunning) {
			MMU.setPTBR(null);
			getTask().setCurrentThread(null);
		}
		setStatus(ThreadKill);
		for (int i = 0; i < Device.getTableSize(); i++) {
			Device.get(i).cancelPendingIO(this);
		}

		ResourceCB.giveupResources(this);
		int temp = getTask().removeThread(this);
		if (temp == FAILURE) {
			return;
		}
		if (getTask().getThreadCount() == 0) {
			getTask().kill();
		}
		dispatch();
	}

	/**
	 * Suspends the thread that is currenly on the processor on the specified event.
	 * 
	 * Note that the thread being suspended doesn't need to be running. It can also
	 * be waiting for completion of a pagefault and be suspended on the IORB that is
	 * bringing the page in.
	 * 
	 * Thread's status must be changed to ThreadWaiting or higher, the processor set
	 * to idle, the thread must be in the right waiting queue, and dispatch() must
	 * be called to give CPU control to some other thread.
	 * 
	 * @param event - event on which to suspend this thread.
	 * 
	 * @OSPProject Threads
	 */
	public void do_suspend(Event event) {
		int threadStatus = getStatus();
		if (threadStatus == ThreadRunning) {
			MMU.setPTBR(null);
			getTask().setCurrentThread(null);
			setStatus(ThreadWaiting);

		}
		if (threadStatus >= ThreadWaiting) {
			setStatus(threadStatus + 1);
		}
		event.addThread(this);
		dispatch();
	}

	/**
	 * Resumes the thread.
	 * 
	 * Only a thread with the status ThreadWaiting or higher can be resumed. The
	 * status must be set to ThreadReady or decremented, respectively. A ready
	 * thread should be placed on the ready queue.
	 * 
	 * @OSPProject Threads
	 */
	public void do_resume() {
		int threadStatus = getStatus();
		if (threadStatus > ThreadWaiting) {
			setStatus(threadStatus - 1);
		} else {
			setStatus(ThreadReady);
			readyQueue.add(this);
		}
		dispatch();
	}

	/**
	 * Selects a thread from the run queue and dispatches it.
	 * 
	 * If there is just one theread ready to run, reschedule the thread currently on
	 * the processor.
	 * 
	 * In addition to setting the correct thread status it must update the PTBR.
	 * 
	 * @return SUCCESS or FAILURE
	 * 
	 * @OSPProject Threads
	 */
	public static int do_dispatch() {
		if (InterruptVector.getInterruptType() == TimerInterrupt && MMU.getPTBR() != null
				&& MMU.getPTBR().getTask() != null && MMU.getPTBR().getTask().getCurrentThread() != null) {
			InterruptVector.setInterruptType(-1);
			MMU.getPTBR().getTask().getCurrentThread().setStatus(ThreadReady);
			readyQueue.add(MMU.getPTBR().getTask().getCurrentThread());
			MMU.getPTBR().getTask().setCurrentThread(null);
			MMU.setPTBR(null);
		}
		if (MMU.getPTBR() == null) {
			if (readyQueue.isEmpty()) {
				return FAILURE;
			} else {
				ThreadCB thread = readyQueue.removeFirst();
				thread.setStatus(ThreadRunning);
				MMU.setPTBR(thread.getTask().getPageTable());
				thread.getTask().setCurrentThread(thread);
				HTimer.set(quantum);
				return SUCCESS;
			}
		}
		return SUCCESS;
	}

	/**
	 * Called by OSP after printing an error message. The student can insert code
	 * here to print various tables and data structures in their state just after
	 * the error happened. The body can be left empty, if this feature is not used.
	 * 
	 * @OSPProject Threads
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
	 * @OSPProject Threads
	 */
	public static void atWarning() {

	}

	/*
	 * Feel free to add methods/fields to improve the readability of your code
	 */

}

/*
 * Feel free to add local classes to improve the readability of your code
 */
