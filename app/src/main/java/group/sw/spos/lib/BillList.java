package group.sw.spos.lib;

public class BillList {

    private String no;
    private String datTime;
    private String status;
    private String timestamp;
    private String warehouse;
    private String warehoseName;

    public BillList(){}

    public BillList(String no, String datTime, String status, String timestamp, String warehouse, String warehoseName) {
        this.no = no;
        this.datTime = datTime;
        this.status = status;
        this.timestamp = timestamp;
        this.warehouse = warehouse;
        this.warehoseName = warehoseName;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getDatTime() {
        return datTime;
    }

    public void setDatTime(String datTime) {
        this.datTime = datTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getWarehoseName() {
        return warehoseName;
    }

    public void setWarehoseName(String warehoseName) {
        this.warehoseName = warehoseName;
    }
}
