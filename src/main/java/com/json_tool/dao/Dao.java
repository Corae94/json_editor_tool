package com.json_tool.dao;
import com.google.gson.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class Dao {

    private File file;
    private Path filePath;
    private String text;
    private HashMap<String,Object> data;
    private HashMap<String,Object> guiData;
    private String fileText;
    private int beginJson;
    private int endJson;

    public Dao(){
        init();
    }

    private void init() {
        JFileChooser guide = new JFileChooser();
        guide.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = guide.showOpenDialog(null);
        if(result==JFileChooser.APPROVE_OPTION) {
            this.filePath= Paths.get(guide.getSelectedFile().getAbsolutePath());
            this.file=this.filePath.toFile();
            try {
                Scanner reader = new Scanner(new FileReader(this.file));
                StringBuilder writer = new StringBuilder();
                while(reader.hasNext()){
                    writer.append(reader.next());
                }
                reader.close();
                this.fileText = writer.toString();
                this.checkFileJson(this.fileText);
                Gson gson = new GsonBuilder().create();
                if(this.text.startsWith("{")){
                    this.data = composeMap(gson.fromJson(this.fileText, JsonObject.class));
                    this.guiData = composeGuiMap(gson.fromJson(this.fileText,JsonObject.class));
                }else if(this.text.startsWith("[")){
                    this.data = composeMap(gson.fromJson(this.fileText, JsonArray.class));
                    this.guiData = composeGuiMap(gson.fromJson(this.fileText,JsonArray.class));
                }
            }catch (FileNotFoundException|IllegalStateException fileException){
                if(fileException instanceof FileNotFoundException) {
                    JOptionPane.showMessageDialog(null,"The file could not be found, check its path","Error",JOptionPane.ERROR_MESSAGE);
                    this.init();
                } else {
                    JOptionPane.showMessageDialog(null,"An error has occurred while reading the file","Error",JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        }
    }

    /*-----DATA-EXTRACTION-FROM-JSON-----*/
    private void checkFileJson(String fileContent){
        if((fileContent.startsWith("{") && fileContent.endsWith("}")) ||
                (fileContent.startsWith("[") && fileContent.endsWith("]"))){
            this.beginJson = 0;
            this.endJson = fileContent.length()-1;
            this.text = fileContent;
        }else{
            this.beginJson = 0;
            this.endJson = 0;
            for (int i = 0; i < fileContent.length(); i++) {
                if(fileContent.charAt(i)=='{' || fileContent.charAt(i)=='['){
                    this.beginJson=i;
                    break;
                }
            }
            for (int i = fileContent.length()-1; i > 0; i--) {
                if(fileContent.charAt(i)=='}' || fileContent.charAt(i)==']'){
                    this.endJson=i;
                    break;
                }
            }
            if(beginJson==endJson){
                JOptionPane.showMessageDialog(null,"Illegal json, please select a valid json document","Error",JOptionPane.ERROR_MESSAGE);
                this.init();
            }else{
                this.text=fileContent.substring(this.beginJson,this.endJson);
            }
        }
    }

    public static HashMap<String,Object> composeMap(JsonObject json){
        HashMap<String, Object> result = new HashMap<>();
        for(Map.Entry<String, JsonElement> e : json.entrySet()){
            if(e.getValue().isJsonArray()){
                result.put(e.getKey(),composeMap(e.getValue().getAsJsonArray()));
            } else if (e.getValue().isJsonObject()) {
                result.put(e.getKey(),composeMap(e.getValue().getAsJsonObject()));
            } else if (e.getValue().isJsonPrimitive()) {
                result.put(e.getKey(),e.getValue().getAsString());
            }
        }
        return result;
    }

    public static HashMap<String,Object> composeMap(JsonArray json){
        HashMap<String,Object> resultMap = new HashMap<>();
        List<Object> result = new ArrayList<>();
        if(!json.isEmpty()){
            for (int i = 0; i < json.size(); i++) {
                if(json.get(i).isJsonObject()){
                    result.add(composeMap(json.get(i).getAsJsonObject()));
                } else if (json.get(i).isJsonArray()) {
                    result.add(composeMap(json.get(i).getAsJsonArray()));
                }else if (json.get(i).isJsonPrimitive()){
                    result.add(json.get(i).toString());
                }
            }
            resultMap.put("Collection",result);
        }
        return resultMap;
    }

    /*-----COMPOSE-MAP-FOR-GUI-----*/
    public static HashMap<String,Object> composeGuiMap(JsonObject json){
        HashMap<String, Object> result = new HashMap<>();
        for(Map.Entry<String, JsonElement> e : json.entrySet()){
            if(e.getValue().isJsonArray()){
                result.put(e.getKey(),composeGuiMap(e.getValue().getAsJsonArray()));
            } else if (e.getValue().isJsonObject()) {
                result.put(e.getKey(),composeGuiMap(e.getValue().getAsJsonObject()));
            } else if (e.getValue().isJsonPrimitive()) {
                HashMap<String,Object> map = new HashMap<>();
                map.put(e.getValue().getAsString(),new JTextField());
                result.put(e.getKey(),map);
            }
        }
        return result;
    }

    public static HashMap<String,Object> composeGuiMap(JsonArray json){
        HashMap<String,Object> resultMap = new HashMap<>();
        List<Object> result = new ArrayList<>();
        if(!json.isEmpty()){
            for (int i = 0; i < json.size(); i++) {
                if(json.get(i).isJsonObject()){
                    result.add(composeGuiMap(json.get(i).getAsJsonObject()));
                } else if (json.get(i).isJsonArray()) {
                    result.add(composeGuiMap(json.get(i).getAsJsonArray()));
                }else if (json.get(i).isJsonPrimitive()){
                    HashMap<String,Object> map = new HashMap<>();
                    map.put(json.get(i).toString(),new JTextField());
                    result.add(map);
                }
            }
            resultMap.put("Collection",result);
        }
        return resultMap;
    }

    /*-----SAVE-DATA-----*/
    public void saveData() {
        Gson gson = new GsonBuilder().create();
        try{
        Path newFile = Paths.get(this.file.toURI());
        if(!Files.exists(newFile)) {
            Files.writeString(newFile,
                    gson.toJson(this.data),
                    StandardOpenOption.CREATE);
        } else {
            if (this.beginJson == 0 && this.endJson == this.fileText.length() - 1) {
                Files.writeString(newFile,
                        gson.toJson(this.data),
                        StandardOpenOption.TRUNCATE_EXISTING);
            }else{
                Files.writeString(newFile,
                        this.fileText.substring(0,this.beginJson)+
                                gson.toJson(this.data)+
                                (this.endJson==this.fileText.length()-1?"":this.fileText.substring(this.endJson+1)),
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
        }
        }catch (IOException ioe){
            JOptionPane.showMessageDialog(null,"An error has occurred while saving the data, please retry later","Error",JOptionPane.ERROR_MESSAGE);
        }

    }

    /*-----GETTERS-&-SETTERS-----*/
    public HashMap<String, Object> getData() {
        return this.data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public HashMap<String, Object> getGuiData() {
        return this.guiData;
    }

}
