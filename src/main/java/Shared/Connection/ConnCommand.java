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
    UNSORTED_GET_SOURCE,
    UNSORTED_STATUS_RESET,
    UNSORTED_UPDATE_SEND,
    UNSORTED_DISCARD,
    UPDATE_REQUEST_SEND,
    UPDATE_REQUEST_GET,
}

/*
 -> Client: ConnState.CONNECTION_START
 -> Server: ConnState.CONNECTION_START
 # start ->
 ------
 Option 1 - Client: ConnState.CONNECTION_END
 -> Closes down connection
 Option 2 - Client: ConnCommand.SORTED_GET
 -> Client: Set<Tag>
 -> Server: List<ISortedData>
 Option 3 - Client: ConnCommand.SORTED_SEND
 -> Client: ISortedData
 -> Server: ConnState.COMMAND_<success y/n>
 Option 4 - Client: ConnCommand.UNSORTED_GET
 -> Server: List<IData>
 Option 5 - Client: ConnCommand.UNSORTED_SEND
 -> Client: IData
 -> Server: ConnState.COMMAND_<success y/n>
 Option 6 - Client: ConnCommand.UNSORTED_STATUS_RESET
 -> Client: List<IData>
 -> Server: ConnState.COMMAND_<success y/n>
 Option 7 - Client: ConnCommand.UNSORTED_UPDATE_SEND
 -> Client: IData
 -> Server: ConnState.COMMAND_<success y/n>
 Option 8 - Client: ConnCommand.UNSORTED_DISCARD
 -> Client: IData
 -> Server: ConnState.COMMAND_<success y/n>
 Option 9 - Client: ConnCommand.UPDATE_REQUEST_SEND
 -> Client: IDataRequest
 -> Server: ConnState.COMMAND_<success y/n>
 Option 10 - Client: ConnCommand.UPDATE_REQUEST_GET
 -> Client: Set<Tag>
 -> Server: List<IDataRequest>
 Option 11 - Client: ConnCommand.UNSORTED_GET_ID
 -> Client: int id
 -> Server: IData
 Option 12 - Client: ConnCommand.UNSORTED_GET_SOURCE
 -> Client: String source
 -> Server: List<IData>
 -----
 Return to start, except on closed conn

 NOTES:
 if Client input was incorrect, server replies ConnState.COMMAND_ERROR
 if Database output was incorrect, server replies ConnState.COMMAND_FAIL instead of expected object
 */
