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
    
    public void showGui(ArrayList<Integer> schedule, int totalTime) {
        panel = new JPanel();
        
        getContentPane().add(panel, BorderLayout.CENTER);
        component = new MyComponent();
        component.setOriginAndSize(schedule, totalTime);
        panel.add(component, BorderLayout.CENTER);
        
        pack();
        super.setVisible(true);
    }
    
    public void refreshGui(ArrayList<Integer> schedule, int totalTime) {
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
    private int height = 200;
    private ArrayList<Integer> schedule;
    private int totalTime;
    private  ArrayList<Color> color = new ArrayList<Color>();;
    
    MyComponent()
    {
        repaint();
    }

    public void setOriginAndSize(ArrayList<Integer> schedule, int totalTime)
    {
        this.schedule = schedule;
        this.totalTime = totalTime;
        repaint();
    }

    public Dimension getPreferredSize()
    {
        return (new Dimension(width, height));
    }

    @Override
    public void paintComponent(Graphics g)
    {
        int x = 0;
        int y = 0;
        int jobTime = 0;
        int count = 0;
        
        color.add(Color.yellow);
        color.add(Color.orange);
        color.add(Color.red);
        color.add(Color.green);
        color.add(Color.cyan);
        color.add(Color.blue);
        color.add(Color.black);
        
        for (int job : schedule) {
            g.setColor(color.get(count));
            jobTime = (width * job) / totalTime;
            g.fillRect(x, y, jobTime, height);
            x += jobTime;
            ++count;
            if (count >= color.size())
                count = 0;
        }
    }
}
