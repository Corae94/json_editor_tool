package com.json_tool.console;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class Console extends JFrame{

    private final JMenuBar menu;
    private final int SIZEW = 1200;
    private final int SIZEY = 800;
    private Container c;
    private JScrollPane scrollPane;
    private JPanel guide;
    private JPanel controls;

    public Console(){
        super("Json Tool");
        this.menu = new JMenuBar();
        this.setJMenuBar(this.menu);
        this.init();
    }

    private void init(){
        this.c = getContentPane();
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        this.guide = new JPanel(new GridLayout(0,3));
        this.guide.add(new JLabel("Key"));
        this.guide.add(new JLabel("Value"));
        this.guide.add(new JLabel("Edit Field"));
        this.scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.scrollPane.getViewport().setLayout(new GridLayout(0,1));
        this.add(this.scrollPane,BorderLayout.CENTER);
        this.controls = new JPanel(new GridLayout(0,2));
        this.add(this.controls,BorderLayout.SOUTH);
        this.add(this.guide,BorderLayout.NORTH);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    public JViewport getView(){
        return this.scrollPane.getViewport();
    }public JPanel getControlPanel(){
        return this.controls;
    }
    public JMenuBar getMenu(){
        return this.menu;
    }
    public int getSIZEW(){
        return this.SIZEW;
    }
    public int getSIZEY(){
        return this.SIZEY;
    }
}
