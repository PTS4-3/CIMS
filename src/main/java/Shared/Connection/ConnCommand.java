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
public enum ConnCommand {

    SORTED_SEND,
    SORTED_GET,
    UNSORTED_SEND,
    UNSORTED_GET,
    UNSORTED_GET_ID,
    UNSORTED_STATUS_RESET,
    UNSORTED_UPDATE_SEND,
    UNSORTED_DISCARD,
    UPDATE_REQUEST_SEND,
    UPDATE_REQUEST_GET,
}

/*
 Connection start ->
 Server: ConnState.CONNECTED
 ------
 Option 1 - Client: ConnState.DONE
 -> Closes down connection
 Option 2 - Client: ConnCommand.SORTED_GET
 -> Client: Set<Tag>
 -> Server: List<ISortedData>
 Option 3 - Client: ConnCommand.SORTED_SEND
 -> Client: ISortedData
 Option 4 - Client: ConnCommand.UNSORTED_GET
 -> Server: List<IData>
 Option 5 - Client: ConnCommand.UNSORTED_SEND
 -> Client: IData
 Option 6 - Client: ConnCommand.UNSORTED_STATUS_RESET
 -> Client: List<IData>
 Option 7 - Client: ConnCommand.UNSORTED_UPDATE_SEND
 -> Client: int id
 -> Client: IData
 Option 8 - Client: ConnCommand.UNSORTED_DISCARD
 -> Client: IData
 Option 9 - Client: ConnCommand.UPDATE_REQUEST_SEND
 -> Client: IDataRequest
 Option 10 - Client: ConnCommand.UPDATE_REQUEST_GET
 -> Client: Set<Tag>
 -> Server: List<IDataRequest>
 Option 11 - Client: ConnCommand.UNSORTED_GET_ID
 -> Client: int id
 -> Server: IData
 -----
 Return to start, except on closed conn
 */