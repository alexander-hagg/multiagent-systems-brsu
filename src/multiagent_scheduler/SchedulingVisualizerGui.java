package multiagent_scheduler;

import jade.core.AID;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class SchedulingVisualizerGui extends JFrame{
    private static final long serialVersionUID = -2270153591712211843L;
    JPanel panel;
    MyComponent component;
    int systemTime;
    
    public void showGui( HashMap< AID,Schedule > schedules, int systemTime ) {
    	this.systemTime = systemTime;
        panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        component = new MyComponent();
        component.setOriginAndSize( schedules, systemTime );
        panel.add(component, BorderLayout.CENTER);
        pack();
        super.setVisible(true);
    }
    
    public void refreshGui( HashMap< AID,Schedule > schedules, int systemTime ) {
    	this.systemTime = systemTime;
    	component.setOriginAndSize( schedules, systemTime );
    	panel.revalidate();
    }
}

class MyComponent extends JComponent
{
    private static final long serialVersionUID = -7469521325933120496L;
    private int width = 900;
    private int windowHeight = 18;
    private int graphHeight = 15;
    private int graphBorder = 3;
    private int bottomPadding = 10;
    private HashMap< AID,Schedule > schedules = new HashMap< AID,Schedule>();
    private int totalTime;
    private ArrayList<Color> color = new ArrayList<Color>();
    private int systemTime = 0;
    
    MyComponent()
    {
        repaint();
    }

    public void setOriginAndSize( HashMap< AID,Schedule > schedules, int systemTime )
    {
    	this.schedules = schedules;
    	this.systemTime = systemTime;
        Iterator it = schedules.entrySet().iterator();
        
        while (it.hasNext()) {
        	Map.Entry pairs = (Map.Entry)it.next();
        	Schedule currentSchedule = (Schedule) pairs.getValue();
        	int totalTimeCurrent = currentSchedule.getScheduleEndTime() - currentSchedule.getScheduleStartTime();
        	if (totalTimeCurrent > this.totalTime)
        		this.totalTime = totalTimeCurrent;
        }
        
        windowHeight = 250;
        repaint();
    }

    public Dimension getPreferredSize()
    {
        return (new Dimension(width, windowHeight));
    }

    @Override
    public void paintComponent(Graphics g)
    {
        int x = 0;
        int y = 0;
        int jobTime = 0;
        int count = 0;
        int fontBorder = 1;
        int stepwidth = 20;
        Font font = new Font("Verdana", Font.BOLD, 12);
        System.out.println("REPAINTING");
        
        color.add(new Color(185, 211, 238));
        color.add(new Color(159, 182, 205));
        //draw jobs
        Iterator it = schedules.entrySet().iterator();
        int currentScheduleNr = 0;
        
        while (it.hasNext()) {

        	Map.Entry pairs = (Map.Entry)it.next();
        	Schedule currentSchedule = (Schedule) pairs.getValue();
        	int currentJobNr = 0;
        	
        	for (Job job : currentSchedule.schedule ) {
            	y = graphBorder + currentScheduleNr*50;
                g.setColor(color.get(count));
                jobTime = (width * job.getProcessingTime()) / totalTime;
                if( currentSchedule.jobsDone.get(currentJobNr).booleanValue() ) {
                	g.setColor( Color.green);
                }
                g.fillRect(x, y, jobTime, graphHeight);
                g.setColor(Color.black);
                g.setFont(font);
                g.drawString(Integer.toString(job.getJobNumber()), x + fontBorder, y + graphHeight - graphBorder);
                x += jobTime;
                ++count;
                currentJobNr++;
                if (count >= color.size())
                    count = 0;
            }
        	x = 0;
        	currentScheduleNr++;
        }
     
        
        
        //draw timeline (as soon as 'a' schedule is received)
        if ( totalTime != 0) {
        	count = 0;
            Font timelineFont = new Font("Verdana", Font.PLAIN, 8);
            g.setFont(timelineFont);
            g.drawLine( 0, windowHeight-bottomPadding, width, windowHeight-bottomPadding );
            for ( int i =  0; i < width; i += width/totalTime ) {
            	if ( i%stepwidth==0 ) {
            		g.drawString("o", i+count, windowHeight-bottomPadding+graphBorder);
                    g.drawString(Integer.toString(count), i+count, windowHeight);
            	}
                ++count;
            }
            // draw time indicator
            // System.out.println("systemTime " + systemTime + " totalTime " + totalTime);
            g.drawLine( this.systemTime, 0, this.systemTime, windowHeight );
        }
        

    }
}
