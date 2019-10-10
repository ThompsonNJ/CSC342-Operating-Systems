package osp.Memory;
/**
    The PageTable class represents the page table for a given task.
    A PageTable consists of an array of PageTableEntry objects.  This
    page table is of the non-inverted type.

    @OSPProject Memory
*/
import java.lang.Math;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Hardware.*;

public class PageTable extends IflPageTable
{
    /** 
	The page table constructor. Must call
	
	    super(ownerTask)

	as its first statement.

	@OSPProject Memory
    */
    public PageTable(TaskCB ownerTask)
    {
        // your code goes here

    }

    /**
       Frees up main memory occupied by the task.
       Then unreserves the freed pages, if necessary.

       @OSPProject Memory
    */
    public void do_deallocateMemory()
    {
        // your code goes here

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

    private final void myPrintPagesLine(StringBuffer s)
    {
        s.append("|------|");
        for(int i = 0; i < MMU.getNumberOfPages(); i++)
            s.append("-");
        s.append("|\n");
    }

    private int getNumberOfValidPages()
    {
    	int numValidPages = 0;
    	for (int i = 0; i < MMU.getNumberOfPages(); i++) {
    		if (pages[i].isIflValid()) {
    			numValidPages++;
    		}
    	}
    	return numValidPages;
    }
   
    public void myPrintTableVertical(StringBuffer s)
    {
    	TaskCB ownerTask = getTask();
    	int i;
	    int numOfPages = pages.length;
	
	    s.append("Page table for Task " +ownerTask.getID()+":\n");
	
	    s.append("  Valid pages: " + numOfPages
	             +" out of "+pages.length+"\n");
	
	    myPrintPagesLine(s);
	    // this handles the most significant digits 
	    // (when the number of pages > 99)
	    int digits=1+(int)(Math.log(numOfPages)/Math.log(10));
	    for(i=digits-1;i>1;i--) {
	        s.append("|      |");
	        for(int j=0;j<numOfPages;j++)
	            s.append((int)(j/Math.pow(10,i))%10);
	        s.append("|\n");
	    }
	    // handle 2nd least significant digit
	    s.append("|Page  |");
	    for(i=0;i<numOfPages;i++)
	        s.append((int)(i/10)%10);
	    s.append("|\n");
	    // handle the least significant digit
	    s.append("|number|");
	    for(i=0;i<numOfPages;i++)
	        s.append(i % 10);
	    s.append("|\n");
	    myPrintPagesLine(s);
	
	    s.append("|valid |");
	    for(i=0;i<numOfPages;i++)
	        s.append((pages[i].isValid()?"Y":" "));
	    s.append("|\n");
	
	    myPrintPagesLine(s);
	    s.append("|      |");
	    for(i=0;i<numOfPages;i++) {
	        FrameTableEntry frame=pages[i].getFrame();
	        int frameID=frame==null? NONE : frame.getID();
	        if(frameID>999)
	            s.append("X");
	        else if(frameID==NONE || frameID/100==0)
	            s.append(" ");
	        else
	            s.append(frameID/100%10);
	    }
	    s.append("|\n");
	    s.append("|frame |");
	    for(i=0;i<numOfPages;i++) {
	        FrameTableEntry frame=pages[i].getFrame();
	        int frameID=frame==null? NONE : frame.getID();
	        if(frameID>999)
	            s.append("X");
	        else if(frameID==NONE || frameID/10==0)
	            s.append(" ");
	        else
	            s.append(frameID/10%10);
	    }
	    s.append("|\n");
	
	    s.append("|number|");
	    for(i=0;i<numOfPages;i++)
	    {
	        FrameTableEntry frame=pages[i].getFrame();
	        int frameID=frame==null? NONE : frame.getID();
	        if(frameID>999)
	            s.append("X");
	        else if(frameID==NONE)
	            s.append(" ");
	        else
	            s.append(frameID%10);
	    }
	    s.append("|\n");
	    myPrintPagesLine(s);
	    
	    s.append("\n");
    }
}

/*
      Feel free to add local classes to improve the readability of your code
*/
