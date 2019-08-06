package group.sw.spos.lib;

public class GoodItemsList {
    private  String goodname;
    private String goodcode;
    private String unit;
    private String language;

    public GoodItemsList(){}

    public GoodItemsList(String goodname, String goodcode, String unit, String language) {
        this.goodname = goodname;
        this.goodcode = goodcode;
        this.unit = unit;
        this.language = language;
    }

    public String getGoodname() {
        return goodname;
    }

    public void setGoodname(String goodname) {
        this.goodname = goodname;
    }

    public String getGoodcode() {
        return goodcode;
    }

    public void setGoodcode(String goodcode) {
        this.goodcode = goodcode;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
