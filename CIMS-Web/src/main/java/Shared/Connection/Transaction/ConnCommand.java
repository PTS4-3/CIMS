/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Connection.Transaction;

/**
 *
 * @author Kargathia + Alexander
 */
public enum ConnCommand {
    /**
     * params: ISortedData <br>
     * result: boolean
     *//**
     * params: ISortedData <br>
     * result: boolean
     *//**
     * params: ISortedData <br>
     * result: boolean
     *//**
     * params: ISortedData <br>
     * result: boolean
     */
    SORTED_SEND,
    /**
     * params: Set&lt;Tag> <br>
     * result: List&lt;ISortedData&gt;
     */
    SORTED_GET,
    /**
     * params: IData <br>
     * result: boolean
     */
    UNSORTED_SEND,
    /**
     * params: void <br>
     * result: List&lt;IData&gt;
     */
    UNSORTED_GET,
    /**
     * params: int <br>
     * result: IData
     */
    UNSORTED_GET_ID,
    /**
     * params: String <br>
     * result: List&lt;IData>
     */
    UNSORTED_GET_SOURCE,
    /**
     * params: List&lt;IData> <br>
     * result: boolean
     */
    UNSORTED_STATUS_RESET,
    /**
     * params: IData <br>
     * result: boolean
     */
    UNSORTED_UPDATE_SEND,
    /**
     * params: IData <br>
     * result: boolean
     */
    UNSORTED_DISCARD,
    /**
     * params: IDataRequest <br>
     * result: boolean
     */
    UPDATE_REQUEST_SEND,
    /**
     * params: Set<Tag> <br>
     * result: List&lt;IDataRequest&gt;
     */
    UPDATE_REQUEST_GET,
    /**
     * params: ITask <br>
     * result: boolean
     */
    TASK_SEND,
    /**
     * params: IPlan <br>
     * result: boolean
     */
    PLAN_SEND_NEW,
    /**
     * params: IPlan <br>
     * result: boolean
     */
    PLAN_APPLY,
    /**
     * params: String, String <br>
     * result: IUser
     */
    USERS_SIGN_IN,
    /**
     * params: ITask <br>
     * result: boolean
     */
    TASK_UPDATE,
    /**
     * params: String, HashSet&lt;Status&gt; <br>
     * result: List&lt;ITask&gt;
     */
    TASKS_GET,
    /**
     * params: HashSet&lt;String&gt; <br>
     * result: List&lt;IPlan&gt;
     */
    PLAN_SEARCH,
    /**
     * params: void <br>
     * result: List&lt;IServiceUser&gt;
     */
    USERS_GET_SERVICEUSERS,
    /**
     * params: INewsItem <br>
     * result: boolean
     */
    NEWSITEM_SEND,
    /**
     * params: INewsItem <br>
     * result: boolean
     */
    NEWSITEM_UPDATE,
    /**
     * params: void <br>
     * result: List&lt;Situation&gt;
     */
    SITUATIONS_GET,
    /**
     * params: UserRole, Tag, String (username) <br>
     * result: boolean <br>
     * If not a ServiceUser, tag and username can be null.
     */
    USERS_REGISTER,
    /**
     * params: void <br>
     * result: boolean
     */
    USERS_UNSORTED_SUBSCRIBE,
    /**
     * params: void <br>
     * result: boolean
     */
    USERS_UNSORTED_UNSUBSCRIBE, 
    /**
     * Server -> client command <br>
     * params: void <br>
     * result: ITask
     */
    TASKS_PUSH,
}