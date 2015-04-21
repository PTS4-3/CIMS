/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Tasks;

/**
 *
 * @author Alexander
 */
public enum TaskStatus {
    /**
     * Task is refused, insert reason
     */
    REFUSED,
    /**
     * Task is succesfully executed
     */
    SUCCEEDED,
    /**
     * Task is not succesfully executed
     */
    FAILED,
    /**
     * Task is accepted, until task is executed
     */
    INPROCESS,
    /**
     * Task is assigned to a person, until task is accepted or refused
     */
    SENT, 
    /**
     * Task is not assigned yet
     */
    UNASSIGNED,
}
