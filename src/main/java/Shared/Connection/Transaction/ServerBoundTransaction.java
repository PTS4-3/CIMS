/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Connection.Transaction;

import java.io.Serializable;
import java.util.Iterator;

/**
 *
 * @author Kargathia
 */
public class ServerBoundTransaction implements Serializable {
    public int ID;
    public ConnCommand command;
    public Object[] objects;

    public ServerBoundTransaction(int ID, ConnCommand command, Object... params){
        System.out.println("new serverbound transaction: " + command.toString());
        this.ID = ID;
        this.command = command;
        this.objects = params;
    }
}
