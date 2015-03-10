/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared;

/**
 *
 * @author Alexander
 */
public interface IUnsortedData {
    public int getId();

    public String getTitle();

    public String getDescription();

    public String getLocation();

    public String getSource();
    
    public Status getStatus();
}
