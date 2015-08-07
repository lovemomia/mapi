package cn.momia.mapi.common.http;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MomiaHttpResponseCollector {
    private boolean successful;
    private boolean notLogin;
    private String errmsg;
    private List<Throwable> exceptions = new ArrayList<Throwable>();
    private Map<String, Object> responses = new HashMap<String, Object>();

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public boolean isNotLogin() {
        return notLogin;
    }

    public void setNotLogin(boolean notLogin) {
        this.notLogin = notLogin;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public void addException(Throwable throwable) {
        exceptions.add(throwable);
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    public void addResponse(String name, Object object) {
        responses.put(name, object);
    }

    public Object getResponse(String name) {
        return responses.get(name);
    }

    @Override
    public String toString() {
        return "MomiaHttpResponseCollector{" +
                "successful=" + successful +
                "notLogin=" + notLogin +
                ", exceptions=" + exceptions +
                ", responses=" + responses +
                '}';
    }
}
