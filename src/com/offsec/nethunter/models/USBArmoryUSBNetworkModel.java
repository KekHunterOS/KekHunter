package com.offsec.nethunter.models;

/*
    CustomCommands Model class, each model object represent the data of each recyclerview item.
 */
public class USBArmoryUSBNetworkModel {
    private String upstream_iface;
    private String usb_iface;
    private String ip_address_for_target;
    private String ip_gateway;
    private String ip_subnetmask;

    public USBArmoryUSBNetworkModel(String upstream_iface, String usb_iface, String ip_address_for_target, String ip_gateway, String ip_subnetmask) {
        this.upstream_iface = upstream_iface;
        this.usb_iface = usb_iface;
        this.ip_address_for_target = ip_address_for_target;
        this.ip_gateway = ip_gateway;
        this.ip_subnetmask = ip_subnetmask;
    }

    public USBArmoryUSBNetworkModel() {

    }

    public String getupstream_iface() { return upstream_iface; }

    public String getusb_iface() { return usb_iface; }

    public String getip_address_for_target() { return ip_address_for_target; }

    public String getip_gateway() { return ip_gateway; }

    public String getip_subnetmask() { return ip_subnetmask; }

    public void setupstream_iface(String upstream_iface) {this.upstream_iface = upstream_iface; }

    public void setusb_iface(String usb_iface) { this.usb_iface = usb_iface; }

    public void setip_address_for_target(String ip_address_for_target) { this.ip_address_for_target = ip_address_for_target; }

    public void setip_gateway(String ip_gateway) { this.ip_gateway = ip_gateway; }

    public void setip_subnetmask(String ip_subnetmask) { this.ip_subnetmask = ip_subnetmask; }

}
