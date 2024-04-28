package com.json_tool.biz;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.json_tool.console.Console;
import com.json_tool.dao.Dao;
import com.formdev.flatlaf.FlatDarculaLaf;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"FieldCanBeLocal"})
public class Biz implements MenuListener, DocumentListener {

    private static Biz INSTANCE;
    private Console console;
    private Dao dao;
    private JMenu openJson;
    private JMenu update;
    private JTextField searchBar;
    private LinkedHashMap<String,Object>data;
    private LinkedHashMap<String,Object> correlations;
    private LinkedHashMap<JPanel,LinkedHashMap<JLabel,Object>> entries;
    private JPanel representation;

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
        this.searchBar = new JTextField();
        this.searchBar.getDocument().addDocumentListener(this);
        this.console.getControlPanel().add(this.searchBar);
        this.entries = new LinkedHashMap<>();
        this.consoleCompose();
        this.console.setSize(this.console.getSIZEW(),this.console.getSIZEY());
        this.frameValidation(this.console);
    }

    private void resetGui() {
        this.console.resetView();
        this.entries = new LinkedHashMap<>();
        this.consoleCompose();
        this.frameValidation(this.console);
    }

    private void consoleCompose(){
        this.representation = new JPanel(new GridLayout(0,1));
        for (Map.Entry<String,Object> e: this.data.entrySet()){
            JPanel entry = new JPanel(new GridLayout(0,3));
            JPanel key = new JPanel(new GridLayout(0,1));
            JPanel value = new JPanel(new GridLayout(0,1));
            JPanel edit = new JPanel(new GridLayout(0,1));
            this.entries.put(entry, new LinkedHashMap<>());
            JLabel keyLabel = new JLabel(e.getKey());
            key.add(keyLabel);
            if(e.getValue() instanceof String){
                JLabel val = new JLabel(String.valueOf(e.getValue()));
                value.add(val);
                this.entries.get(entry).put(keyLabel,val);
                edit.add(((LinkedHashMap<String,JTextField>)this.correlations.get(e.getKey())).get(String.valueOf(e.getValue())));
            }else{
                if(e.getValue() instanceof LinkedHashMap<?,?>){
                    this.entries.get(entry).put(keyLabel,new LinkedHashMap<JLabel,Object>());
                    this.populateValues((LinkedHashMap<String, Object>) e.getValue(), (LinkedHashMap<String, Object>) this.correlations.get(e.getKey()),value,edit, (LinkedHashMap<JLabel, Object>) this.entries.get(entry).get(keyLabel));
                }else if (e.getValue() instanceof List<?>){
                    this.entries.get(entry).put(keyLabel,new ArrayList<>());
                    this.populateValues((List<Object>) e.getValue(), (List<Object>) this.correlations.get(e.getKey()),value,edit, (ArrayList<Object>) this.entries.get(entry).get(keyLabel));
                }
            }
            entry.add(key);
            entry.add(value);
            entry.add(edit);
            this.representation.add(entry);
        }
        this.console.getView().add(this.representation);
    }

    private void populateValues(LinkedHashMap<String,Object> map,LinkedHashMap<String,Object> correlation, JPanel value,JPanel edit,LinkedHashMap<JLabel,Object> entries){
        for(Map.Entry<String,Object> e :map.entrySet()){
            JLabel keyLabel = new JLabel(e.getKey());
            if(e.getValue() instanceof String){
                JLabel val = new JLabel(String.valueOf(e.getValue()));
                entries.put(keyLabel,val);
                value.add(val);
                edit.add(((LinkedHashMap<String,JTextField>)correlation.get(e.getKey())).get(String.valueOf(e.getValue())));
            }else{
                if(e.getValue() instanceof LinkedHashMap<?,?>){
                    entries.put(keyLabel,new LinkedHashMap<JLabel,Object>());
                    this.populateValues((LinkedHashMap<String, Object>) e.getValue(), (LinkedHashMap<String, Object>) correlation.get(e.getKey()),value,edit, (LinkedHashMap<JLabel, Object>) entries.get(keyLabel));
                }else if (e.getValue() instanceof List<?>){
                    entries.put(keyLabel,new ArrayList<>());
                    this.populateValues((List<Object>) e.getValue(), (List<Object>) correlation.get(e.getKey()),value,edit,(ArrayList<Object>) entries.get(keyLabel));
                }
            }
        }
    }

    private void populateValues(List<Object> map,List<Object> correlation, JPanel value,JPanel edit,ArrayList<Object> entriesList){
        for (int i = 0; i < map.size(); i++) {
            if(map.get(i) instanceof String){
                JLabel val = new JLabel(String.valueOf(map.get(i)));
                entriesList.add(val);
                value.add(val);
                edit.add(((LinkedHashMap<String, JTextField>) correlation.get(i)).get(String.valueOf(map.get(i))));
            }else{
                if(map.get(i) instanceof LinkedHashMap<?,?>){
                    LinkedHashMap<JLabel,Object> subEntries = new LinkedHashMap<>();
                    entriesList.add(subEntries);
                    this.populateValues((LinkedHashMap<String, Object>)map.get(i), (LinkedHashMap<String, Object>) correlation.get(i),value,edit,subEntries);
                }else if (map.get(i) instanceof List<?>){
                    ArrayList<Object> subEntries = new ArrayList<>();
                    entriesList.add(subEntries);
                    this.populateValues((List<Object>) map.get(i), (List<Object>) correlation.get(i),value,edit,subEntries);
                }
            }
        }
    }

    private void updateValues(LinkedHashMap<String,Object> map,LinkedHashMap<String,Object> correlation){
        for(Map.Entry<String,Object> e :map.entrySet()){
            if(e.getValue() instanceof String){
                if(!((LinkedHashMap<String,JTextField>)correlation.get(e.getKey())).get(String.valueOf(e.getValue())).getText().isEmpty()){
                    e.setValue(((LinkedHashMap<String,JTextField>)correlation.get(e.getKey())).get(String.valueOf(e.getValue())).getText());
                }
            }else{
                if(e.getValue() instanceof LinkedHashMap<?,?>){
                    this.updateValues((LinkedHashMap<String, Object>) e.getValue(), (LinkedHashMap<String, Object>) correlation.get(e.getKey()));
                }else if (e.getValue() instanceof List<?>){
                    this.updateValues((List<Object>) e.getValue(), (List<Object>) correlation.get(e.getKey()));
                }
            }
        }
    }

    private void updateValues(List<Object> map,List<Object> correlation){
        for (int i = 0; i < map.size(); i++) {
            if(map.get(i) instanceof String){
                if(!(((LinkedHashMap<String, JTextField>) correlation.get(i)).get(String.valueOf(map.get(i)))).getText().isEmpty()){
                    map.remove(map.get(i));
                    map.add(i,(((LinkedHashMap<String, JTextField>) correlation.get(i)).get(String.valueOf(map.get(i)))).getText());
                }
            }else{
                if(map.get(i) instanceof LinkedHashMap<?,?>){
                    this.updateValues((LinkedHashMap<String, Object>)map.get(i), (LinkedHashMap<String, Object>) correlation.get(i));
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

    private void updateVisibleValues(String text) {
        this.entries.forEach((panel,map) ->{
            AtomicBoolean invisible = new AtomicBoolean(true);
            map.forEach((keyLabel,content)->{
                if(!keyLabel.getText().contains(text)) {
                    if (content instanceof JLabel) {
                        if (((JLabel) content).getText().contains(text)) {
                            invisible.set(false);
                            ((JLabel) content).setVisible(true);
                        } else {
                            ((JLabel) content).setVisible(false);
                        }
                    } else if (content instanceof LinkedHashMap<?, ?>) {
                       this.updateVisibleNestedValues((LinkedHashMap<JLabel,Object>) content,invisible,text);
                    } else if (content instanceof ArrayList<?>) {
                        this.updateVisibleListedValues((ArrayList<Object>) content,invisible,text);
                    }
                    if(invisible.get()){
                        keyLabel.setVisible(false);
                    }
                }else{
                    invisible.set(false);
                    if (content instanceof JLabel) {
                        ((JLabel) content).setVisible(((JLabel) content).getText().contains(text));
                    } else if (content instanceof LinkedHashMap<?, ?>) {
                        this.updateVisibleNestedValues((LinkedHashMap<JLabel,Object>) content,invisible,text);
                    } else if (content instanceof ArrayList<?>) {
                        this.updateVisibleListedValues((ArrayList<Object>) content,invisible,text);
                    }
                }
            });
            if(invisible.get()){
                panel.setVisible(false);
            }
        });
        this.representation.revalidate();
        this.representation.repaint();
    }

    private void updateVisibleNestedValues(LinkedHashMap<JLabel, Object> map, AtomicBoolean invisible, String text) {
        map.forEach((keyLabel,content)->{
            if(!keyLabel.getText().contains(text)) {
                if (content instanceof JLabel) {
                    if (((JLabel) content).getText().contains(text)) {
                        invisible.set(false);
                        ((JLabel) content).setVisible(true);
                    } else {
                        ((JLabel) content).setVisible(false);
                    }
                } else if (content instanceof LinkedHashMap<?, ?>) {
                    this.updateVisibleNestedValues((LinkedHashMap<JLabel,Object>) content, invisible,text);
                } else if (content instanceof ArrayList<?>) {
                    this.updateVisibleListedValues((ArrayList<Object>) content,invisible,text);
                }
                if(invisible.get()){
                    keyLabel.setVisible(false);
                }
            }else{
                invisible.set(false);
                if (content instanceof JLabel) {
                    ((JLabel) content).setVisible(((JLabel) content).getText().contains(text));
                } else if (content instanceof LinkedHashMap<?, ?>) {
                    this.updateVisibleNestedValues((LinkedHashMap<JLabel,Object>) content,invisible,text);
                } else if (content instanceof ArrayList<?>) {
                    this.updateVisibleListedValues((ArrayList<Object>) content,invisible,text);
                }
            }
        });
    }

    private void updateVisibleListedValues(ArrayList<Object> list,AtomicBoolean invisible, String text) {
        for(Object o: list){
            if(o instanceof JLabel){
                if (((JLabel) o).getText().contains(text)) {
                    invisible.set(false);
                    ((JLabel) o).setVisible(true);
                } else {
                    ((JLabel) o).setVisible(false);
                }
            } else if (o instanceof LinkedHashMap<?,?>) {
                this.updateVisibleNestedValues((LinkedHashMap<JLabel, Object>) o,invisible,text);
            } else if (o instanceof ArrayList<?>) {
                this.updateVisibleListedValues((ArrayList<Object>) o,invisible,text);
            }
        }
    }
    
    @Override
    public void menuSelected(MenuEvent e) {
        if(e.getSource().equals(this.openJson)){
            this.dao=new Dao();
            this.data= this.dao.getData();
            this.entries = new LinkedHashMap<>();
            this.correlations=this.dao.getGuiData();
            this.resetGui();
        }else if(e.getSource().equals(this.update)){
            this.updateValues(this.data,this.correlations);
            this.dao.saveData();
            this.data= this.dao.getData();
            this.entries = new LinkedHashMap<>();
            this.correlations=this.dao.getGuiData();
            this.resetGui();
        }
    }

    @Override
    public void menuDeselected(MenuEvent e) {}

    @Override
    public void menuCanceled(MenuEvent e) {}

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.updateVisibleValues(this.searchBar.getText());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        this.updateVisibleValues(this.searchBar.getText());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        this.updateVisibleValues(this.searchBar.getText());
    }
}
