package multiagent_scheduler;

import jade.core.AID;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class SchedulingVisualizerGui extends JFrame{
    private static final long serialVersionUID = -2270153591712211843L;
    JPanel panel;
    MyComponent component;
    
    public void showGui( HashMap< AID,Schedule > schedules, int totalTime ) {
        panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        component = new MyComponent();
        component.setOriginAndSize( schedules, totalTime );
        panel.add(component, BorderLayout.CENTER);
        
        pack();
        super.setVisible(true);
    }
    
    public void refreshGui( HashMap< AID,Schedule > schedules, int totalTime ) {
    	component.setOriginAndSize( schedules, totalTime );
    	panel.revalidate();
    }
}

class MyComponent extends JComponent
{
    private static final long serialVersionUID = -7469521325933120496L;
    private int width = 800;
    private int windowHeight = 18;
    private int graphHeight = 15;
    private int graphBorder = 3;
    private int bottomPadding = 10;
    private HashMap< AID,Schedule > schedules = new HashMap< AID,Schedule>();
    private int totalTime;
    private ArrayList<Color> color = new ArrayList<Color>();
    private int currentScheduleNr;
    
    MyComponent()
    {
        repaint();
    }

    public void setOriginAndSize( HashMap< AID,Schedule > schedules, int totalTime )
    {
    	this.schedules = schedules;
        this.totalTime = totalTime;
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
        
        color.add(Color.blue);
        color.add(Color.cyan);
        //draw jobs
        Iterator it = schedules.entrySet().iterator();
        int currentScheduleNr = 0;
        
        while (it.hasNext()) {
            // Map.Entry pairs = (Map.Entry)it.next();
            // System.out.println(pairs.getKey() + " = " + pairs.getValue());
            // it.remove(); // avoids a ConcurrentModificationException
        	
        	Map.Entry pairs = (Map.Entry)it.next();
        	Schedule currentSchedule = (Schedule) pairs.getValue();
  	
        	for (Job job : currentSchedule.schedule ) {
            	y = graphBorder + currentScheduleNr*50;
                g.setColor(color.get(count));
                jobTime = (width * job.getProcessingTime()) / totalTime;
                g.fillRect(x, y, jobTime, graphHeight);
                g.setColor(Color.black);
                g.setFont(font);
                g.drawString(Integer.toString(job.getJobNumber()), x + fontBorder, y + graphHeight - graphBorder);
                x += jobTime;
                ++count;
                if (count >= color.size())
                    count = 0;
            }
        	x = 0;
        	currentScheduleNr++;
        }
     
        
        
        //draw timeline
        count = 0;
        Font timelineFont = new Font("Verdana", Font.PLAIN, 8);
        g.setFont(timelineFont);
        g.drawLine(0, windowHeight-bottomPadding, width, windowHeight-bottomPadding);
        for (int i =  0; i < width; i += width/totalTime) {
        	if ( i%stepwidth==0 ) {
        		g.drawString("o", i+count, windowHeight-bottomPadding+graphBorder);
                g.drawString(Integer.toString(count), i+count, windowHeight);
        	}
            ++count;
        }

    }
}
