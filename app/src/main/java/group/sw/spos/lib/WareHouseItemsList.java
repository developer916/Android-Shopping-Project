package group.sw.spos.lib;

public class WareHouseItemsList {
    private  String warehouse;
    private String name;

    public WareHouseItemsList(){}

    public WareHouseItemsList(String warehouse, String name) {
        this.warehouse = warehouse;
        this.name = name;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
