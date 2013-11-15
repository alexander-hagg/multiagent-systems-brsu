package multiagent_scheduler;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class SchedulingVisualizerGui extends JFrame{
    /**
     * 
     */
    private static final long serialVersionUID = -2270153591712211843L;
    JPanel panel;
    MyComponent component;
    
    public void showGui(ArrayList<Job> schedule, int totalTime) {
        panel = new JPanel();
        
        getContentPane().add(panel, BorderLayout.CENTER);
        component = new MyComponent();
        component.setOriginAndSize(schedule, totalTime);
        panel.add(component, BorderLayout.CENTER);
        
        
        pack();
        super.setVisible(true);
    }
    
    public void refreshGui(ArrayList<Job> schedule, int totalTime) {
    	component.setOriginAndSize(schedule, totalTime);
    	panel.revalidate();
    }
}

class MyComponent extends JComponent
{
    /**
     * 
     */
    private static final long serialVersionUID = -7469521325933120496L;
    private int width = 800;
    private int windowHeight = 18;
    private int graphHeight = 15;
    private int graphBorder = 3;
    private int bottomPadding = 10;
    private ArrayList<Job> schedule;
    private int totalTime;
    private  ArrayList<Color> color = new ArrayList<Color>();;
    
    MyComponent()
    {
        repaint();
		
    }

    public void setOriginAndSize(ArrayList<Job> schedule, int totalTime)
    {
        this.schedule = schedule;
        this.totalTime = totalTime;
        windowHeight = 18 * schedule.size();
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
        Font font = new Font("Verdana", Font.BOLD, 12);
        
        color.add(Color.yellow);
        color.add(Color.green);

        //draw jobs
        for (Job job : schedule) {
        	y = graphBorder;
            g.setColor(color.get(count));
            jobTime = (width * job.duration) / totalTime;
            g.fillRect(x, y, jobTime, graphHeight);
            g.setColor(Color.black);
            g.setFont(font);
            g.drawString(job.name, x + fontBorder, y + graphHeight - graphBorder);
            x += jobTime;
            ++count;
            if (count >= color.size())
                count = 0;
        }
        
        //draw timeline
        count = 0;
        Font timelineFont = new Font("Verdana", Font.PLAIN, 8);
        g.setFont(timelineFont);
        g.drawLine(0, windowHeight-bottomPadding, width, windowHeight-bottomPadding);
        for (int i =  0; i < width; i += width/totalTime) {
            g.drawString("o", i+count, windowHeight-bottomPadding+graphBorder);
            g.drawString(Integer.toString(count), i+count, windowHeight);
            ++count;
        }

    }
}
