/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Data;

import java.io.Serializable;

/**
 *
 * @author Alexander
 */
public interface IData extends Serializable {
    int getId();
    String getTitle();
    String getDescription();
    String getLocation();
    String getSource();
}
