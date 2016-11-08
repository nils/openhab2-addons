/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.discovery;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.pioneeravr.PioneerAvrBindingConstants;
import org.openhab.binding.pioneeravr.internal.models.ConfigurableModel;
import org.openhab.binding.pioneeravr.internal.protocol.ip.IpAvrConnection;
import org.openhab.binding.pioneeravr.internal.thingtype.ThingTypeManager;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An UpnpDiscoveryParticipant which allows to discover Pioneer AVRs.
 *
 * @author Antoine Besnard
 *
 */
public class PioneerAvrDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(PioneerAvrDiscoveryParticipant.class);

    private ThingTypeManager modelManager;
    private boolean isAutoDiscoveryEnabled;
    private Set<ThingTypeUID> supportedThingTypes;

    public PioneerAvrDiscoveryParticipant() {
        this.isAutoDiscoveryEnabled = true;
    }

    /**
     * Called at the service activation.
     *
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {
        if (componentContext.getProperties() != null) {
            String autoDiscoveryPropertyValue = (String) componentContext.getProperties().get("enableAutoDiscovery");
            if (StringUtils.isNotEmpty(autoDiscoveryPropertyValue)) {
                isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryPropertyValue);
            }
        }
        supportedThingTypes = isAutoDiscoveryEnabled ? modelManager.getRegisteredThingTypesUIDs()
                : new HashSet<ThingTypeUID>();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return supportedThingTypes;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        DiscoveryResult result = null;
        ThingUID thingUid = getThingUID(device);
        if (thingUid != null) {
            String label = StringUtils.isEmpty(device.getDetails().getFriendlyName()) ? device.getDisplayString()
                    : device.getDetails().getFriendlyName();
            Map<String, Object> properties = new HashMap<>(2, 1);
            properties.put(PioneerAvrBindingConstants.PARAMETER_HOST,
                    device.getIdentity().getDescriptorURL().getHost());
            properties.put(PioneerAvrBindingConstants.PARAMETER_TCP_PORT,
                    getTcpPort(device.getIdentity().getDescriptorURL().getHost()));
            properties.put(PioneerAvrBindingConstants.PARAMETER_USE_SERIAL, false);

            result = DiscoveryResultBuilder.create(thingUid).withLabel(label).withProperties(properties).build();
        }

        return result;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        ThingUID result = null;
        if (isAutoDiscoveryEnabled) {
            if (StringUtils.containsIgnoreCase(device.getDetails().getManufacturerDetails().getManufacturer(),
                    PioneerAvrBindingConstants.MANUFACTURER)) {
                logger.debug("Manufacturer matched: search: {}, device value: {}.",
                        PioneerAvrBindingConstants.MANUFACTURER,
                        device.getDetails().getManufacturerDetails().getManufacturer());
                if (StringUtils.containsIgnoreCase(device.getType().getType(),
                        PioneerAvrBindingConstants.UPNP_DEVICE_TYPE)) {
                    logger.debug("Device type matched: search: {}, device value: {}.",
                            PioneerAvrBindingConstants.UPNP_DEVICE_TYPE, device.getType().getType());

                    String deviceModel = device.getDetails().getModelDetails() != null
                            ? device.getDetails().getModelDetails().getModelName()
                            : null;
                    ThingTypeUID thingTypeUID = getThingTypeUID(deviceModel);
                    logger.info("AVR model {} found.", deviceModel);
                    if (thingTypeUID.equals(ConfigurableModel.THING_TYPE_UID)) {
                        logger.warn(
                                "Device model {} not officialy supported. You may have to try different configurations for best results.",
                                deviceModel);
                    }

                    result = new ThingUID(thingTypeUID, device.getIdentity().getUdn().getIdentifierString());
                }
            }
        }

        return result;
    }

    /**
     * Return the {@link ThingTypeUID} based on the deviceModel.
     *
     * @param deviceModel
     * @return
     */
    private ThingTypeUID getThingTypeUID(String deviceModel) {
        ThingTypeUID result = ConfigurableModel.THING_TYPE_UID;
        for (ThingTypeUID thingTypeUID : supportedThingTypes) {
            if (StringUtils.startsWithIgnoreCase(deviceModel, thingTypeUID.getId())) {
                result = thingTypeUID;
                break;
            }
        }
        return result;
    }

    /**
     * Try to connect to the to possible telnet TCP ports of the AVR (depending of the model).
     *
     * @return
     */
    private Integer getTcpPort(String host) {
        Integer port = null;
        try (Socket socket = new Socket()) {
            try {
                // Test the first possible telnet port.
                port = IpAvrConnection.DEFAULT_TELNET_PORT_1;
                socket.connect(new InetSocketAddress(host, port), 1000);
                logger.info("Detected telnet port {} on AVR @{}", port, host);
            } catch (IOException e) {
                try {
                    // Test the second possible telnet port if the first has failed.
                    port = IpAvrConnection.DEFAULT_TELNET_PORT_2;
                    socket.connect(new InetSocketAddress(host, port), 1000);
                    logger.info("Detected telnet port {} on AVR @{}", port, host);
                } catch (IOException e2) {
                    logger.warn("Unable to detect the telnet port on AVR @{}", host);
                }
            }
        } catch (IOException e1) {
            logger.error("Error closing telnet port auto detect socket.");
        }

        return port;
    }

    public void setModelManager(ThingTypeManager modelManager) {
        this.modelManager = modelManager;
    }

}
