package com.offsec.nethunter.models;

/*
    CustomCommands Model class, each model object represent the data of each recyclerview item.
 */
public class CustomCommandsModel {
    private String CommandLabel;
    private String Command;
    private String RuntimeEnv;
    private String ExecutionMode;
    private String RunOnBoot;

    public CustomCommandsModel(String CommandLabel, String Command, String RuntimeEnv, String ExecutionMode, String RunOnBoot) {
        this.CommandLabel = CommandLabel;
        this.Command = Command;
        this.RuntimeEnv = RuntimeEnv;
        this.ExecutionMode = ExecutionMode;
        this.RunOnBoot = RunOnBoot;
    }

    public CustomCommandsModel() {

    }

    public String getCommandLabel() { return CommandLabel; }

    public String getCommand() { return Command; }

    public String getRuntimeEnv() { return RuntimeEnv; }

    public String getExecutionMode() { return ExecutionMode; }

    public String getRunOnBoot() { return RunOnBoot; }

    public void setCommandLabel(String CommandLabel) {this.CommandLabel = CommandLabel; }

    public void setCommand(String Command) {this.Command = Command; }

    public void setRuntimeEnv(String RuntimeEnv) { this.RuntimeEnv = RuntimeEnv; }

    public void setExecutionMode(String ExecutionMode) { this.ExecutionMode = ExecutionMode; }

    public void setRunOnBoot(String RunOnBoot) { this.RunOnBoot = RunOnBoot; }

}
