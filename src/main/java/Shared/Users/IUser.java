/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Users;

import java.io.Serializable;

/**
 *
 * @author Alexander
 */
public interface IUser extends Serializable {
    String getUsername();
    String getName();
}
