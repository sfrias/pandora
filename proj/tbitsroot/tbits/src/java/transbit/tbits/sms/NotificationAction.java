package transbit.tbits.sms;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: yes
 * Date: Jun 1, 2007
 * Time: 11:00:15 PM
 * To change this template use File | Settings | File Templates.
 */

/*
* An Object of this Class represents an action in a rule
* */



public class NotificationAction {
    String name;
    String op;
    String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}