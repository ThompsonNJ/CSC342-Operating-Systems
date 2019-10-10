package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;

/**
 * The MMU class contains the student code that performs the work of handling a
 * memory reference. It is responsible for calling the interrupt handler if a
 * page fault is required.
 * 
 * @OSPProject Memory
 */
public class MMU extends IflMMU {
	/**
	 * This method is called once before the simulation starts. Can be used to
	 * initialize the frame table and other static variables.
	 * 
	 * @OSPProject Memory
	 */
	private static LinkedList<ThreadCB> LRUQueue;
	public static void init() {
		LRUQueue = new LinkedList<ThreadCB>();
		//FrameTableEntry(getFrameTableSize());
	}

	/**
	 * This method handlies memory references. The method must calculate, which
	 * memory page contains the memoryAddress, determine, whether the page is valid,
	 * start page fault by making an interrupt if the page is invalid, finally, if
	 * the page is still valid, i.e., not swapped out by another thread while this
	 * thread was suspended, set its frame as referenced and then set it as dirty if
	 * necessary. (After pagefault, the thread will be placed on the ready queue,
	 * and it is possible that some other thread will take away the frame.)
	 * 
	 * @param memoryAddress A virtual memory address
	 * @param referenceType The type of memory reference to perform
	 * @param thread        that does the memory access (e.g., MemoryRead or
	 *                      MemoryWrite).
	 * @return The referenced page.
	 * 
	 * @OSPProject Memory
	 */
	static public PageTableEntry do_refer(int memoryAddress, int referenceType, ThreadCB thread) {
		// your code goes here

	}

	/**
	 * Called by OSP after printing an error message. The student can insert code
	 * here to print various tables and data structures in their state just after
	 * the error happened. The body can be left empty, if this feature is not used.
	 * 
	 * @OSPProject Memory
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
	 * @OSPProject Memory
	 */
	public static void atWarning() {
		// your code goes here

	}

	/*
	 * Feel free to add methods/fields to improve the readability of your code
	 */

	static public void myPrintFramesLine(StringBuffer s) {
		s.append("|----------|");
		for (int i = 0; i < getFrameTableSize(); i++)
			s.append("-");
		s.append("|\n");
	}

	/** Put the status of the frame table into the log file. */
	static public void myPrintTableVertical(StringBuffer s) {
		int i;
		int frameTableSize = getFrameTableSize();

		myPrintFramesLine(s);
		// handle most significant digits (when the number of frames > 99)
		int digits = 1 + (int) (Math.log(frameTableSize) / Math.log(10));
		for (i = digits - 1; i > 1; i--) {
			s.append("|          |");
			for (int j = 0; j < frameTableSize; j++)
				s.append((int) (j / Math.pow(10, i)) % 10);
			s.append("|\n");
		}
		// handle 2nd least significant digit
		s.append("|Frame     |");
		for (i = 0; i < frameTableSize; i++)
			s.append((int) (i / 10) % 10);
		s.append("|\n");
		// handle the least significant digit
		s.append("|number    |");
		for (i = 0; i < frameTableSize; i++)
			s.append(i % 10);
		s.append("|\n");
		myPrintFramesLine(s);

		s.append("|free      |");
		for (i = 0; i < frameTableSize; i++)
			s.append(((getFrame(i).getPage() == null) ? "F" : " "));
		s.append("|\n");

		s.append("|lock      |");
		for (i = 0; i < frameTableSize; i++) {
			int count = getFrame(i).getLockCount();
			if (count > 9)
				s.append("L");
			else
				s.append(count);
		}
		s.append("|\n");

		s.append("|reserved  |");
		for (i = 0; i < frameTableSize; i++)
			s.append((getFrame(i).isReserved() ? "R" : " "));
		s.append("|\n");

		s.append("|dirty     |");
		for (i = 0; i < frameTableSize; i++)
			s.append((getFrame(i).isDirty() ? "D" : " "));
		s.append("|\n");

		s.append("|referenced|");
		for (i = 0; i < frameTableSize; i++)
			s.append((getFrame(i).isReferenced() ? "Y" : " "));
		s.append("|\n");

		myPrintFramesLine(s);

		s.append("|          |");
		for (i = 0; i < frameTableSize; i++) {
			PageTableEntry page = getFrame(i).getPage();
			int pageID = page == null ? NONE : page.getID();
			if (pageID > 999)
				s.append("X");
			else if (pageID == NONE || pageID / 100 == 0)
				s.append(" ");
			else
				s.append(pageID / 100 % 10);
		}
		s.append("|\n");
		s.append("|page      |");
		for (i = 0; i < frameTableSize; i++) {
			PageTableEntry page = getFrame(i).getPage();
			int pageID = page == null ? NONE : page.getID();
			if (pageID > 999)
				s.append("X");
			else if (pageID == NONE || pageID / 10 == 0)
				s.append(" ");
			else
				s.append(pageID / 10 % 10);
		}
		s.append("|\n");

		s.append("|number    |");
		for (i = 0; i < frameTableSize; i++) {
			PageTableEntry page = getFrame(i).getPage();
			int pageID = page == null ? NONE : page.getID();
			if (pageID > 999)
				s.append("X");
			else if (pageID == NONE)
				s.append(" ");
			else
				s.append(pageID % 10);
		}
		s.append("|\n");
		myPrintFramesLine(s);

		s.append("|          |");
		for (i = 0; i < frameTableSize; i++) {
			int task = (getFrame(i).getPage() == null) ? NONE : getFrame(i).getPage().getTask().getID();
			if (task > 999)
				s.append("X");
			else if (task == NONE || task / 100 == 0)
				s.append(" ");
			else
				s.append(task / 100 % 10);
		}
		s.append("|\n");
		s.append("|owner     |");
		for (i = 0; i < frameTableSize; i++) {
			int task = (getFrame(i).getPage() == null) ? NONE : getFrame(i).getPage().getTask().getID();
			if (task > 999)
				s.append("X");
			else if (task == NONE || task / 10 == 0)
				s.append(" ");
			else
				s.append(task / 10 % 10);
		}
		s.append("|\n");

		s.append("|task      |");
		for (i = 0; i < frameTableSize; i++) {
			int task = (getFrame(i).getPage() == null) ? NONE : getFrame(i).getPage().getTask().getID();
			if (task > 999)
				s.append("X");
			else if (task == NONE)
				s.append(" ");
			else
				s.append(task % 10);
		}
		s.append("|\n");

		myPrintFramesLine(s);

		s.append("\n");
	}
}

/*
 * Feel free to add local classes to improve the readability of your code
 */
