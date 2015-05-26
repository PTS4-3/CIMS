/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Connection;

/**
 *
 * @author Kargathia
 */
public interface IResponseHandler extends Runnable {
 public boolean handleResponse(byte[] rsp);

}
