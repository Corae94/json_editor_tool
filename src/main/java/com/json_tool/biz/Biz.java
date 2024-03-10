package com.json_tool.biz;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.json_tool.console.Console;
import com.json_tool.dao.Dao;
import com.formdev.flatlaf.FlatDarculaLaf;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"FieldCanBeLocal", "unchecked"})
public class Biz implements MenuListener {

    private static Biz INSTANCE;
    private Console console;
    private Dao dao;
    private JMenu openJson;
    private JMenu update;
    private HashMap<String,Object>data;
    private HashMap<String,Object> correlations;

    public static synchronized Biz getInstance(){
        if(INSTANCE == null){
            INSTANCE = new Biz();
        }
        return INSTANCE;
    }

    private Biz(){
        try{
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Unable to load FlatDarculaLaf...Loading standard UI","Error",JOptionPane.ERROR_MESSAGE);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }catch(ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException bulkE){
                JOptionPane.showMessageDialog(null,"An error has occurred while loading the GUI library","Error",JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
        this.init();
    }

    private void init(){
        this.dao = new Dao();
        this.data = this.dao.getData();
        this.correlations=this.dao.getGuiData();
        this.console = new Console();
        this.openJson = new JMenu("Open another json (discard values if not updated)");
        this.openJson.addMenuListener(this);
        this.update = new JMenu("Update values in file");
        this.update.addMenuListener(this);
        this.console.getMenu().add(this.openJson);
        this.console.getMenu().add(Box.createHorizontalGlue());
        this.console.getMenu().add(this.update);
        this.consoleCompose();
        this.console.setSize(this.console.getSIZEW(),this.console.getSIZEY());
        this.frameValidation(this.console);
    }

    private void consoleCompose(){
        for (Map.Entry<String,Object> e: this.data.entrySet()){
            JPanel entry = new JPanel(new GridLayout(0,3));
            JPanel key = new JPanel(new GridLayout(0,1));
            JPanel value = new JPanel(new GridLayout(0,1));
            JPanel edit = new JPanel(new GridLayout(0,1));

            key.add(new JLabel(e.getKey()));
            if(e.getValue() instanceof String){
                value.add(new JLabel(String.valueOf(e.getValue())));
                edit.add(((HashMap<String,JTextField>)this.correlations.get(e.getKey())).get(String.valueOf(e.getValue())));
            }else{
                if(e.getValue() instanceof HashMap<?,?>){
                    this.populateValues((HashMap<String, Object>) e.getValue(), (HashMap<String, Object>) this.correlations.get(e.getKey()),value,edit);
                }else if (e.getValue() instanceof List<?>){
                    this.populateValues((List<Object>) e.getValue(), (List<Object>) this.correlations.get(e.getKey()),value,edit);
                }
            }
            entry.add(key);
            entry.add(value);
            entry.add(edit);
            this.console.getView().add(entry);
        }
    }

    private void populateValues(HashMap<String,Object> map,HashMap<String,Object> correlation, JPanel value,JPanel edit){
        for(Map.Entry<String,Object> e :map.entrySet()){
            if(e.getValue() instanceof String){
                value.add(new JLabel(String.valueOf(e.getValue())));
                edit.add(((HashMap<String,JTextField>)correlation.get(e.getKey())).get(String.valueOf(e.getValue())));
            }else{
                if(e.getValue() instanceof HashMap<?,?>){
                    this.populateValues((HashMap<String, Object>) e.getValue(), (HashMap<String, Object>) correlation.get(e.getKey()),value,edit);
                }else if (e.getValue() instanceof List<?>){
                    this.populateValues((List<Object>) e.getValue(), (List<Object>) correlation.get(e.getKey()),value,edit);
                }
            }
        }
    }

    private void populateValues(List<Object> map,List<Object> correlation, JPanel value,JPanel edit){
        for (int i = 0; i < map.size(); i++) {
            if(map.get(i) instanceof String){
                value.add(new JLabel(String.valueOf(map.get(i))));
                edit.add(((HashMap<String, JTextField>) correlation.get(i)).get(String.valueOf(map.get(i))));
            }else{
                if(map.get(i) instanceof HashMap<?,?>){
                    this.populateValues((HashMap<String, Object>)map.get(i), (HashMap<String, Object>) correlation.get(i),value,edit);
                }else if (map.get(i) instanceof List<?>){
                    this.populateValues((List<Object>) map.get(i), (List<Object>) correlation.get(i),value,edit);
                }
            }
        }
    }

    private void updateValues(HashMap<String,Object> map,HashMap<String,Object> correlation){
        for(Map.Entry<String,Object> e :map.entrySet()){
            if(e.getValue() instanceof String){
                if(!((HashMap<String,JTextField>)correlation.get(e.getKey())).get(String.valueOf(e.getValue())).getText().isEmpty()){
                    e.setValue(((HashMap<String,JTextField>)correlation.get(e.getKey())).get(String.valueOf(e.getValue())).getText());
                }
            }else{
                if(e.getValue() instanceof HashMap<?,?>){
                    this.updateValues((HashMap<String, Object>) e.getValue(), (HashMap<String, Object>) correlation.get(e.getKey()));
                }else if (e.getValue() instanceof List<?>){
                    this.updateValues((List<Object>) e.getValue(), (List<Object>) correlation.get(e.getKey()));
                }
            }
        }
    }

    private void updateValues(List<Object> map,List<Object> correlation){
        for (int i = 0; i < map.size(); i++) {
            if(map.get(i) instanceof String){
                if(!(((HashMap<String, JTextField>) correlation.get(i)).get(String.valueOf(map.get(i)))).getText().isEmpty()){
                    map.remove(map.get(i));
                    map.add(i,(((HashMap<String, JTextField>) correlation.get(i)).get(String.valueOf(map.get(i)))).getText());
                }
            }else{
                if(map.get(i) instanceof HashMap<?,?>){
                    this.updateValues((HashMap<String, Object>)map.get(i), (HashMap<String, Object>) correlation.get(i));
                }else if (map.get(i) instanceof List<?>){
                    this.updateValues((List<Object>) map.get(i), (List<Object>) correlation.get(i));
                }
            }
        }
    }

    private void frameValidation(JFrame j){
        j.setVisible(true);
        Dimension d = j.getSize();
        j.validate();
        j.repaint();
        j.setSize(d);
        j.setLocationRelativeTo(null);
    }

    @Override
    public void menuSelected(MenuEvent e) {
        if(e.getSource().equals(this.openJson)){
            this.init();
        }else if(e.getSource().equals(this.update)){
            this.updateValues(this.data,this.correlations);
            this.dao.setData(this.data);
            this.dao.saveData();
        }
    }

    @Override
    public void menuDeselected(MenuEvent e) {}

    @Override
    public void menuCanceled(MenuEvent e) {}

}
