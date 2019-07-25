/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.pjlinkdevice.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PJLinkDeviceConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class PJLinkDeviceConfiguration {
    @Nullable
    public String ipAddress;

    public int tcpPort;

    @Nullable
    public String adminPassword;

    public int refresh;

    public boolean refreshPower;
    public boolean refreshMute;
    public boolean refreshInputChannel;
}
