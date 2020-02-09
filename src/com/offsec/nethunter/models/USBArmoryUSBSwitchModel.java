package com.offsec.nethunter.models;

/*
    CustomCommands Model class, each model object represent the data of each recyclerview item.
 */
public class USBArmoryUSBSwitchModel {
    private String idVendor;
    private String idProduct;
    private String manufacturer;
    private String product;
    private String serialnumber;

    public USBArmoryUSBSwitchModel(String idVendor, String idProduct, String manufacturer, String product, String serialnumber) {
        this.idVendor = idVendor;
        this.idProduct = idProduct;
        this.manufacturer = manufacturer;
        this.product = product;
        this.serialnumber = serialnumber;
    }

    public USBArmoryUSBSwitchModel() {

    }

    public String getidVendor() { return idVendor; }

    public String getidProduct() { return idProduct; }

    public String getmanufacturer() { return manufacturer; }

    public String getproduct() { return product; }

    public String getserialnumber() { return serialnumber; }

    public void setidVendor(String idVendor) {this.idVendor = idVendor; }

    public void setidProduct(String idProduct) {this.idProduct = idProduct; }

    public void setmanufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public void setproduct(String product) { this.product = product; }

    public void setserialnumber(String serialnumber) { this.serialnumber = serialnumber; }

}
