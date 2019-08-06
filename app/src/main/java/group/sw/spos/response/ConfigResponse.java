package group.sw.spos.response;

import java.util.List;

import group.sw.spos.lib.GoodItemsList;

public class ConfigResponse {
    public int code;
    public String message;
    public List<GoodItemsList> goodItems;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
