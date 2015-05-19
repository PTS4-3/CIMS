/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Connection;

import java.io.Serializable;

/**
 *
 * @author Kargathia
 */
public class ClientBoundTransaction implements Serializable {

    public int ID;
    public ConnCommand command;
    public ConnState result;
    public Object data;

    /**
     * Convenience constructor. Uses given transaction, and sets outgoing values
     * to error state.
     *
     * @param tract
     */
    public ClientBoundTransaction(ServerBoundTransaction tract) {
        this.ID = tract.ID;
        this.command = tract.command;
        this.result = ConnState.COMMAND_ERROR;
        this.data = null;
    }

    public ClientBoundTransaction setResult(boolean isSuccess) {
        if (isSuccess) {
            this.result = ConnState.COMMAND_SUCCESS;
        } else {
            this.result = ConnState.COMMAND_FAIL;
        }
        return this;
    }

    public ClientBoundTransaction setResult(Object data) {
        this.data = data;
        if (data == null) {
            this.result = ConnState.COMMAND_FAIL;
        } else {
            this.result = ConnState.COMMAND_SUCCESS;
        }
        return this;
    }

    public ClientBoundTransaction setError() {
        this.result = ConnState.COMMAND_ERROR;
        this.data = null;
        return this;
    }

}
