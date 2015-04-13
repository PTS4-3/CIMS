/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Connection;

/**
 *
 * @author Kargathia + Alexander
 */
public enum ConnCommand {
    CLIENT_ID_GET,
    SORTED_SEND,
    SORTED_GET,
    SORTED_GET_NEW,
    SORTED_SUBSCRIBE,
    SORTED_UNSUBSCRIBE,
    UNSORTED_SEND,
    UNSORTED_GET,
    UNSORTED_GET_ID,
    UNSORTED_GET_SOURCE,
    UNSORTED_STATUS_RESET,
    UNSORTED_UPDATE_SEND,
    UNSORTED_DISCARD,
    UNSORTED_GET_NEW,
    UNSORTED_SUBSCRIBE,
    UNSORTED_UNSUBSCRIBE,
    UPDATE_REQUEST_SEND,
    UPDATE_REQUEST_GET,
    UPDATE_REQUEST_GET_NEW,
    UPDATE_REQUEST_SUBSCRIBE,
    UPDATE_REQUEST_UNSUBSCRIBE,
    TASK_SEND_NEW,
    PLAN_SEND_NEW;
}

/*
 Server: ConnState.CONNECTED
 Connection start ->
 ------
 Option 1 - Client: ConnState.DONE
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
 Option 13 - Client: ConnCommand.CLIENT_ID_GET
 -> Server: int ID
 Option 14 - Client: ConnCommand.SORTED_SUBSCRIBE
 -> Client: int clientID
 -> Server: ConnState.COMMAND_<success y/n>
 Option 15 - Client: ConnCommand.SORTED_GET_NEW
 -> Client: int clientID
 -> Server: List<ISortedData>
 Option 16 - Client: ConnCommand.SORTED_UNSUBSCRIBE
 -> Client: int clientID
 -> Server: ConnState.COMMAND_<success y/n>
 Option 17 - Client: ConnCommand.UPDATE_REQUEST_SUBSCRIBE
 -> Client: int clientID
 -> Server: ConnState.COMMAND_<success y/n>
 Option 18 - Client: ConnCommand.UPDATE_REQUEST_UNSUBSCRIBE
 -> Client: int clientID
 -> Server: ConnState.COMMAND_<success y/n>
 Option 19 - Client: ConnCommand.UPDATE_REQUEST_GET_NEW
 -> Client: int clientID
 -> Server: List<IDataRequest>
 Option 20 - Client: ConnCommand.UNSORTED_SUBSCRIBE
 -> Client: int clientID
 -> Server: ConnState.COMMAND_<success y/n>
 Option 21 - Client: ConnCommand.UNSORTED_UNSUBSCRIBE
 -> Client: int clientID
 -> Server: ConnState.COMMAND_<success y/n>
 Option 22 - Client: ConnCommand.UNSORTED_GET_NEW
 -> Client: int clientID
 -> Server: List<IData>
 Option 23 - Client: ConnCommand.TASK_SEND_NEW
 -> Client: ITask task
 -> Server: ConnState.COMMAND_<success y/n>
 Option 24 - Client: ConnCommand.PLAN_SEND_NEW
 -> Client: IPlan plan
 -> Server: ConnState.COMMAND_<success y/n>
 -----
 Return to start, except on closed conn
 */
