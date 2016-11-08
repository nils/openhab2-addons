/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.pioneeravr.PioneerAvrBindingConstants;
import org.openhab.binding.pioneeravr.internal.models.properties.ModelProperties;
import org.openhab.binding.pioneeravr.internal.models.properties.ModelPropertiesImpl;
import org.openhab.binding.pioneeravr.internal.protocol.Response.ResponseType;
import org.openhab.binding.pioneeravr.internal.protocol.StreamAvrConnection;
import org.openhab.binding.pioneeravr.internal.protocol.ip.IpAvrConnection;
import org.openhab.binding.pioneeravr.internal.protocol.serial.SerialAvrConnection;
import org.openhab.binding.pioneeravr.protocol.AvrConnection;
import org.openhab.binding.pioneeravr.protocol.AvrConnectionException;
import org.openhab.binding.pioneeravr.protocol.AvrResponse;
import org.openhab.binding.pioneeravr.protocol.CommandTypeNotSupportedException;
import org.openhab.binding.pioneeravr.protocol.event.AvrConnectionListener;
import org.openhab.binding.pioneeravr.protocol.event.AvrNotificationEvent;
import org.openhab.binding.pioneeravr.protocol.event.AvrNotificationListener;
import org.openhab.binding.pioneeravr.protocol.states.MuteStateValues;
import org.openhab.binding.pioneeravr.protocol.states.PowerStateValues;
import org.openhab.binding.pioneeravr.protocol.utils.DisplayInformationConverter;
import org.openhab.binding.pioneeravr.protocol.utils.VolumeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AvrHandler} is responsible for handling commands, which are sent to one of the channels through an
 * {@link AvrConnectionHandler}.
 *
 * @author Antoine Besnard - Initial contribution
 */
public class AvrHandler extends BaseThingHandler implements AvrNotificationListener, AvrConnectionListener {

    private Logger logger = LoggerFactory.getLogger(AvrHandler.class);

    private ScheduledFuture<?> connectionCheckerFuture;

    private ModelProperties modelProperties;

    private AvrConnectionHandler avrConnectionHandler;

    private VolumeConverter volumeConverter;

    @SuppressWarnings("null")
    public AvrHandler(Thing thing, ModelProperties modelProperties) {
        super(thing);
        this.modelProperties = modelProperties;
    }

    /**
     * Initialize the AVR. Also called each times the configuration has changed.
     */
    @Override
    public void initialize() {
        // Update the model properties with the thing configuration (configuration contains overridden properties only
        // if the thing is a ConfigurableAvr).
        modelProperties = ModelPropertiesImpl.cloneModelPropertiesAndOverrideWithConfiguration(modelProperties,
                getThing().getConfiguration());

        logger.debug("Initializing handler for Pioneer AVR {}", getThing().getUID());
        if (avrConnectionHandler != null) {
            avrConnectionHandler.close();
        }

        this.volumeConverter = new VolumeConverter(modelProperties);

        avrConnectionHandler = new AvrConnectionHandler(createConnection(), volumeConverter, modelProperties);
        logger.debug("Handler for Pioneer AVR @{} initialized", avrConnectionHandler.getConnectionName());

        checkStatus();
    }

    @Override
    public void handleRemoval() {
        avrConnectionHandler.close();

        super.handleRemoval();
    }

    /**
     * Create a new connection to the AVR.
     *
     * @return
     */
    private AvrConnection createConnection() {
        Boolean useSerial = (Boolean) this.getConfig().get(PioneerAvrBindingConstants.PARAMETER_USE_SERIAL);
        StreamAvrConnection connection = null;

        // If the useSerial parameter is set to true
        if (useSerial != null && useSerial) {
            // Use a serial connection
            String serialPort = (String) this.getConfig().get(PioneerAvrBindingConstants.PARAMETER_SERIAL_PORT);

            // Create a Serial connection
            connection = new SerialAvrConnection(scheduler, serialPort);
        } else {
            // Else use an IP connection.
            String host = (String) this.getConfig().get(PioneerAvrBindingConstants.PARAMETER_HOST);
            Number tcpPort = (Number) this.getConfig().get(PioneerAvrBindingConstants.PARAMETER_TCP_PORT);

            // Create an IP Connection
            connection = new IpAvrConnection(scheduler, host, tcpPort != null ? tcpPort.intValue() : null);
        }
        connection.addNotificationListener(this);
        connection.addConnectionListener(this);

        return connection;
    }

    /**
     * Close the connection and stop the status checker.
     */
    @Override
    public void dispose() {
        super.dispose();
        stopConnectionChecker();
        if (avrConnectionHandler != null) {
            avrConnectionHandler.close();
        }
    }

    /**
     * Start the status checker. Should be started only when the AVR is offline.
     */
    private synchronized void startConnectionChecker() {
        if (connectionCheckerFuture == null) {
            // Start the connection checker
            Runnable statusChecker = new Runnable() {
                @Override
                public void run() {
                    try {
                        checkStatus();
                    } catch (LinkageError e) {
                        logger.warn(
                                "Failed to check the connection for AVR @{}. If a Serial link is used to connect to the AVR, please check that the Bundle org.openhab.io.transport.serial is available. Cause: {}",
                                avrConnectionHandler.getConnectionName(), e.getMessage());
                        // Stop to check the connection with this AVR.
                        stopConnectionChecker();
                    }
                }
            };
            connectionCheckerFuture = scheduler.scheduleWithFixedDelay(statusChecker, 1, 10, TimeUnit.SECONDS);
        }
    }

    /**
     * Stop the status checker. Should be called when the AVR is online.
     */
    private synchronized void stopConnectionChecker() {
        if (connectionCheckerFuture != null) {
            if (connectionCheckerFuture != null) {
                connectionCheckerFuture.cancel(false);
                connectionCheckerFuture = null;
            }
        }
    }

    /**
     * Called when a Power ON state update is received from the AVR for the given zone.
     */
    public void onPowerOn(int zone) {
        try {
            // When the AVR is Powered ON, query the volume, the mute state and the source input of the zone
            manageVolumeLevelUpdate(avrConnectionHandler.sendVolumeQuery(zone));
            manageMuteStateUpdate(avrConnectionHandler.sendMuteQuery(zone));
            manageInputSourceChannelUpdate(avrConnectionHandler.sendSourceInputQuery(zone));
        } catch (TimeoutException e) {
            logger.error("Timeout when updating state of zone {} of AVR @{} after powerOn. Cause: {}", zone,
                    avrConnectionHandler.getConnectionName(), e.getMessage());
        }
    }

    /**
     * Called when a Power OFF state update is received from the AVR.
     */
    public void onPowerOff(int zone) {
        // When the AVR is Powered OFF, update the status of channels to Undefined
        updateState(getChannelUID(PioneerAvrBindingConstants.CHANNEL_ID_MUTE, zone), UnDefType.UNDEF);
        updateState(getChannelUID(PioneerAvrBindingConstants.CHANNEL_ID_VOLUME_DB, zone), UnDefType.UNDEF);
        updateState(getChannelUID(PioneerAvrBindingConstants.CHANNEL_ID_VOLUME_DIMMER, zone), UnDefType.UNDEF);
        updateState(getChannelUID(PioneerAvrBindingConstants.CHANNEL_ID_SET_INPUT_SOURCE, zone), UnDefType.UNDEF);
    }

    /**
     * Check the status of the AVR. Return true if the AVR is online, else return false.
     *
     * @return
     */
    private void checkStatus() {
        try {
            logger.debug("Checking status of AVR @{}", avrConnectionHandler.getConnectionName());
            // If the power query request has not been sent, the connection to the
            // AVR has failed. So update its status to OFFLINE and start the connection checker.
            AvrResponse response = avrConnectionHandler.sendPowerQuery(1);
            if (response == null) {
                updateStatus(ThingStatus.OFFLINE);
                startConnectionChecker();
            }
            // If a response is received, the AVR is ONLINE, but onConnection has been called by the connection,
            // so do nothing here.
        } catch (TimeoutException e) {
            // Timeout on the response. Since the request has been sent, the connection is open
            // => AVR is ONLINE, but onConnection has been called by the connection,
            // so do nothing here. Just log as debug
            logger.debug("Timeout during checkStatus.", e);
        }
    }

    /**
     * Update all supported zones.
     */
    protected void updateZones() {
        for (int zone = 1; zone <= modelProperties.getNbZones(); zone++) {
            // Check all supported zones
            if (isZoneSupported(zone)) {
                try {
                    managePowerStateUpdate(avrConnectionHandler.sendPowerQuery(zone));
                } catch (TimeoutException e) {
                    logger.error("Timeout when updating the zone {}. Cause: {}", zone, e.getMessage());
                }
            }
        }
    }

    /**
     * Check if the given zone is supported.
     *
     * @param zone
     * @return true if the zone is strictly positive and supported by the AVR, else false.
     */
    protected boolean isZoneSupported(int zone) {
        return zone > 0 && zone <= modelProperties.getNbZones();
    }

    /**
     * Handle an openHAB command and translate it to an AVR command.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            // The display information channel is not bound to a zone. So process it directly.
            if (channelUID.getId().contains(PioneerAvrBindingConstants.CHANNEL_ID_DISPLAY_INFORMATION)
                    && command == RefreshType.REFRESH) {
                manageDisplayedInformationUpdate(avrConnectionHandler.sendDisplayQuery());
            } else {
                // Extract the zone from the Channel UID
                int zone = getZoneFromChannelUID(channelUID.getId());

                // If the requested zone is not supported by the AVR,
                // then do not send the request.
                if (isZoneSupported(zone)) {
                    if (channelUID.getId().contains(PioneerAvrBindingConstants.CHANNEL_ID_POWER)) {
                        managePowerStateUpdate(avrConnectionHandler.sendPowerCommand(command, zone));
                    } else if (channelUID.getId().contains(PioneerAvrBindingConstants.CHANNEL_ID_VOLUME_DIMMER)
                            || channelUID.getId().contains(PioneerAvrBindingConstants.CHANNEL_ID_VOLUME_DB)) {
                        manageVolumeLevelUpdate(avrConnectionHandler.sendVolumeCommand(command, zone));
                    } else if (channelUID.getId().contains(PioneerAvrBindingConstants.CHANNEL_ID_SET_INPUT_SOURCE)) {
                        manageInputSourceChannelUpdate(avrConnectionHandler.sendInputSourceCommand(command, zone));
                    } else if (channelUID.getId().contains(PioneerAvrBindingConstants.CHANNEL_ID_MUTE)) {
                        manageMuteStateUpdate(avrConnectionHandler.sendMuteCommand(command, zone));
                    }
                } else {
                    logger.warn("Command for zone {} not send since zone {} is not supported by the AVR@{}.", zone,
                            zone, avrConnectionHandler.getConnectionName());
                }
            }
        } catch (CommandTypeNotSupportedException e) {
            logger.info("Unsupported command type {} received for channel {}.", command.toFullString(),
                    channelUID.getId());
        } catch (TimeoutException e) {
            logger.error("Timeout when processing command {} of channel {} of thing {}. Cause: {}",
                    command.toFullString(), channelUID, getThing().getUID(), e.getMessage());
        }
    }

    /**
     * Called when a status update is received from the AVR.
     *
     * The responses to explicit requests do not trigger this update.
     */
    @Override
    public void statusUpdateReceived(AvrNotificationEvent event) {
        try {
            AvrResponse notification = event.getNotification();

            switch (notification.getResponseType()) {
                case POWER_STATE:
                    managePowerStateUpdate(notification);
                    break;
                case VOLUME_LEVEL:
                    manageVolumeLevelUpdate(notification);
                    break;
                case MUTE_STATE:
                    manageMuteStateUpdate(notification);
                    break;
                case INPUT_SOURCE_CHANNEL:
                    manageInputSourceChannelUpdate(notification);
                    break;
                case DISPLAY_INFORMATION:
                    manageDisplayedInformationUpdate(notification);
                    break;
                default:
                    logger.debug("Unkown notification type from AVR @{}. Notification discarded: {}",
                            event.getNotification(), event.getConnection());
            }
        } catch (AvrConnectionException e) {
            logger.debug("Unkown notification type from AVR @{}. Notification discarded: {}", event.getNotification(),
                    event.getConnection());
        }
    }

    /**
     * Called when the AVR is disconnected
     */
    @Override
    public void onDisconnection(AvrConnection connection, Throwable cause) {
        logger.warn("The AVR @{} is disconnected. Cause: {}", connection.getConnectionName(),
                cause != null ? cause.getMessage() : "unknown");
        updateStatus(ThingStatus.OFFLINE);
        startConnectionChecker();
    }

    /**
     * Called when the AVR is connected
     */
    @Override
    public void onConnection(AvrConnection connection) {
        logger.info("The AVR @{} is connected.", connection.getConnectionName());
        updateStatus(ThingStatus.ONLINE);

        // Stop the connection checker
        stopConnectionChecker();
        // Update the zones support and status
        updateZones();

        try {
            // Update the display
            manageDisplayedInformationUpdate(avrConnectionHandler.sendDisplayQuery());
        } catch (TimeoutException e) {
            // Log as debug since the response is not guaranteed (as said in the protocol specifications)
            logger.debug(
                    "Failed to update the AVR @{} display after connection. That error can be ignored as said in the protocol specification.",
                    connection.getConnectionName());
            // Clear the value of the information channel when it cannot be updated on connection to avoid keeping the
            // previous one (which may be outdated)
            updateState(PioneerAvrBindingConstants.CHANNEL_ID_DISPLAY_INFORMATION, new StringType(""));
        }
    }

    /**
     * Notify an AVR power state update to OpenHAB
     *
     * @param response
     */
    private void managePowerStateUpdate(AvrResponse response) {
        if (response != null) {
            OnOffType state = PowerStateValues.ON_VALUE.equals(response.getParameterValue()) ? OnOffType.ON
                    : OnOffType.OFF;

            // When a Power ON state update is received, call the onPowerOn method.
            if (OnOffType.ON == state) {
                onPowerOn(response.getZone());
            } else {
                onPowerOff(response.getZone());
            }

            updateState(getChannelUID(PioneerAvrBindingConstants.CHANNEL_ID_POWER, response.getZone()), state);
        }
    }

    /**
     * Notify an AVR volume level update to OpenHAB
     *
     * @param response
     */
    private void manageVolumeLevelUpdate(AvrResponse response) {
        if (response != null && response.getResponseType() != ResponseType.NONE) {
            updateState(getChannelUID(PioneerAvrBindingConstants.CHANNEL_ID_VOLUME_DB, response.getZone()),
                    new DecimalType(volumeConverter.convertFromIpControlVolumeToDb(response.getParameterValue(),
                            response.getZone())));
            updateState(getChannelUID(PioneerAvrBindingConstants.CHANNEL_ID_VOLUME_DIMMER, response.getZone()),
                    new PercentType((int) volumeConverter
                            .convertFromIpControlVolumeToPercent(response.getParameterValue(), response.getZone())));
        }
    }

    /**
     * Notify an AVR mute state update to OpenHAB
     *
     * @param response
     */
    private void manageMuteStateUpdate(AvrResponse response) {
        if (response != null) {
            updateState(getChannelUID(PioneerAvrBindingConstants.CHANNEL_ID_MUTE, response.getZone()),
                    response.getParameterValue().equals(MuteStateValues.OFF_VALUE) ? OnOffType.OFF : OnOffType.ON);
        }
    }

    /**
     * Notify an AVR input source channel update to OpenHAB
     *
     * @param response
     */
    private void manageInputSourceChannelUpdate(AvrResponse response) {
        if (response != null) {
            updateState(getChannelUID(PioneerAvrBindingConstants.CHANNEL_ID_SET_INPUT_SOURCE, response.getZone()),
                    new StringType(response.getParameterValue()));
        }
    }

    /**
     * Notify an AVR displayed information update to OpenHAB
     *
     * @param response
     */
    private void manageDisplayedInformationUpdate(AvrResponse response) {
        if (response != null) {
            updateState(PioneerAvrBindingConstants.CHANNEL_ID_DISPLAY_INFORMATION, new StringType(
                    DisplayInformationConverter.convertMessageFromIpControl(response.getParameterValue())));
        }
    }

    /**
     * Build the channelUID from the channel name and the zone number.
     *
     * @param channelName
     * @param zone
     * @return
     */
    protected String getChannelUID(String channelName, int zone) {
        return String.format(PioneerAvrBindingConstants.GROUP_CHANNEL_ID_PATTERN, zone, channelName);
    }

    /**
     * Return the zone from the given channelUID.
     *
     * Return 0 if the zone cannot be extracted from the channelUID.
     *
     * @param channelUID
     * @return
     */
    protected int getZoneFromChannelUID(String channelUID) {
        int zone = 0;
        Matcher matcher = PioneerAvrBindingConstants.GROUP_CHANNEL_ID_ZONE_PATTERN.matcher(channelUID);
        if (matcher.find()) {
            zone = Integer.valueOf(matcher.group(1));
        }
        return zone;
    }

}
