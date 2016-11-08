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

/**
 * A listener which is notified when an AVR is connected or disconnected.
 *
 * @author Antoine Besnard
 *
 */
public interface AvrConnectionListener {

    /**
     * Called when an AVR is disconnected.
     *
     * @param connection
     * @param cause
     */
    public void onDisconnection(AvrConnection connection, Throwable cause);

    /**
     * Called when an AVR is connected.
     *
     * @param connection
     */
    public void onConnection(AvrConnection connection);

}
