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
public enum ConnState {

    CONNECTION_START,
    CONNECTION_END,
    TRANSFER_START,
    TRANSFER_END,
    COMMAND_ERROR,
    COMMAND_SUCCESS,
    COMMAND_FAIL,
}
