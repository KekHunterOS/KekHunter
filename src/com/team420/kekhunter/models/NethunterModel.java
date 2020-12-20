package com.team420.kekhunter.models;

/*
    Nethunter Model class, each model object represent the data of each recyclerview item.
 */
public class NethunterModel {
    private String title;
    private String command;
    private String delimiter;
    private String runOnCreate;
    private String[] result;

    public NethunterModel(String title, String command, String delimiter, String runOnCreate, String[] result) {
        this.title = title;
        this.command = command;
        this.delimiter = delimiter;
        this.runOnCreate = runOnCreate;
        this.result = result;
    }

    public NethunterModel() {

    }

    public String getTitle() { return title; }

    public String getCommand() { return command; }

    public String getDelimiter() { return delimiter; }

    public String getRunOnCreate() { return runOnCreate; }

    public String[] getResult() { return result; }

    public void setTitle(String title) {this.title = title; }

    public void setCommand(String command) {this.command = command; }

    public void setDelimiter(String delimiter) {this.delimiter = delimiter; }

    public void setRunOnCreate(String runOnCreate) { this.runOnCreate = runOnCreate; }

    public void setResult(String[] result) { this.result = result; }
}
