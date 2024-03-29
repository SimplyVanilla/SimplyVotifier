/*
 * Copyright (C) 2011 Vex Software LLC
 * This file is part of Votifier.
 *
 * Votifier is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Votifier is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Votifier.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vexsoftware.votifier.model;

import lombok.Getter;
import lombok.Setter;

/**
 * A model for a vote.
 *
 * @author Blake Beaupain
 */
@Getter
public class Vote {

    /**
     * The name of the vote service.
     */
    @Setter
    private String serviceName;

    /**
     * The username of the voter.
     */
    private String username;

    /**
     * The address of the voter.
     */
    @Setter
    private String address;

    /**
     * The date and time of the vote.
     */
    @Setter
    private String timeStamp;

    @Override
    public String toString() {
        return "Vote (from:" + serviceName + " username:" + username
            + " address:" + address + " timeStamp:" + timeStamp + ")";
    }

    /**
     * Sets the username.
     *
     * @param username The new username
     */
    public void setUsername(String username) {
        this.username = username.length() <= 16 ? username : username.substring(0, 16);
    }

}
