/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol.event;

import org.openhab.binding.pioneeravr.protocol.AvrConnection;
import org.openhab.binding.pioneeravr.protocol.AvrResponse;

/**
 * The event fired when a notification is received from the AVR.
 *
 * @author Antoine Besnard
 */
public class AvrNotificationEvent {

    private AvrConnection connection;
    private AvrResponse notification;

    public AvrNotificationEvent(AvrConnection connection, AvrResponse notification) {
        this.connection = connection;
        this.notification = notification;
    }

    public AvrConnection getConnection() {
        return connection;
    }

    public AvrResponse getNotification() {
        return notification;
    }

}
