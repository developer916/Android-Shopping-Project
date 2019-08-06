package group.sw.spos.lib;

public class BillProductList {
    public int id;
    public String billNo;
    public String name;
    public String code;
    public String unit;
    public int quantity;
    public boolean isclicked;
    public String createDate;
    public BillProductList(){}

    public BillProductList(int id, String billNo, String name, String code, String unit, int quantity, boolean isclicked, String createDate) {
        this.id = id;
        this.billNo = billNo;
        this.name = name;
        this.code = code;
        this.unit = unit;
        this.quantity = quantity;
        this.isclicked = isclicked;
        this.createDate = createDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isIsclicked() {
        return isclicked;
    }

    public void setIsclicked(boolean isclicked) {
        this.isclicked = isclicked;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
