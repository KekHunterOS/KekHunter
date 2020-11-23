package com.offsec.nethunter.models;

/*
    KaliServices Model class, each model object represent the data of each recyclerview item.
 */
public class KaliServicesModel {
    private String ServiceName;
    private String CommandforStartService;
    private String CommandforStopService;
    private String CommandforCheckServiceStatus;
    private String RunOnChrootStart;
    private String Status;

    public KaliServicesModel(String ServiceName, String CommandforStartService, String CommandforStopService, String CommandforCheckServiceStatus, String RunOnChrootStart, String Status) {
        this.ServiceName = ServiceName;
        this.CommandforStartService = CommandforStartService;
        this.CommandforStopService = CommandforStopService;
        this.CommandforCheckServiceStatus = CommandforCheckServiceStatus;
        this.RunOnChrootStart = RunOnChrootStart;
        this.Status = Status;
    }

    public KaliServicesModel() {

    }

    public String getServiceName() { return ServiceName; }

    public String getCommandforStartService() { return CommandforStartService; }

    public String getCommandforStopService() { return CommandforStopService; }

    public String getCommandforCheckServiceStatus() { return CommandforCheckServiceStatus; }

    public String getRunOnChrootStart() { return RunOnChrootStart; }

    public String getStatus() { return Status; }

    public void setServiceName(String ServiceName) {this.ServiceName = ServiceName; }

    public void setCommandforStartService(String CommandforStartService) {this.CommandforStartService = CommandforStartService; }

    public void setCommandforStopService(String CommandforStopService) { this.CommandforStopService = CommandforStopService; }

    public void setCommandforCheckServiceStatus(String CommandforCheckServiceStatus) { this.CommandforCheckServiceStatus = CommandforCheckServiceStatus; }

    public void setRunOnChrootStart(String RunOnChrootStart) { this.RunOnChrootStart = RunOnChrootStart; }

    public void setStatus(String Status) { this.Status = Status; }
}
