/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared;

/**
 *
 * @author Kargathia
 */
public enum ConnState {

    CONNECTED,
    DONE,
    ERROR,
    PING,
    PONG,
    TRANSFER_START,
    TRANSFER_END
}

/*
 Connection start ->
 Server: ConnState.CONNECTED
 ------
 Option 1 - Client: ConnState.DONE
 -> Closes down connection
 Option 2 - Client: DataRequest.SORTED_GET
 -> Client: Set<Tag>
 -> Server: List<ISortedData>
 Option 3 - Client: DataRequest.SORTED_SEND
 -> Client: ISortedData
 Option 4 - Client: DataRequest.UNSORTED_GET
 -> Server: List<IData>
 Option 5 - Client: DataRequest.UNSORTED_SEND
 -> Client: IData
 Option 6 - Client: DataRequest.UNSORTED_STATUS_RESET
 -> Client: List<IData>
 Option 7 - Client: DataRequest.UNSORTED_UPDATE
 -> Client: int id
 -> Client: IData
 Option 8 - Client: DataRequest.UNSORTED_DISCARD
 -> Client: IData
 -----
 Return to start, except on closed conn
 */
