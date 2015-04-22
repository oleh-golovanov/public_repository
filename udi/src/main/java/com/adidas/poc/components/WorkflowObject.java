package com.adidas.poc.components;

/**
 * @author Oleh_Golovanov
 */
public class WorkflowObject<P> {
    private P payload;

    private boolean isWritten;

    public WorkflowObject(P payload) {
        this.payload = payload;
    }

    public boolean isWritten(){
        return this.isWritten;
    }

    public void setIsWritten(boolean isWritten) {
        this.isWritten = isWritten;
    }

    @Override
    public String toString() {
        return "WorkflowObject{" +
                "payload=" + payload +
                ", isWritten=" + isWritten +
                '}';
    }
}
