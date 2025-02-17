/*******************************************************************************
 * # License
 * Copyright 2020 Silicon Laboratories Inc. www.silabs.com
 *******************************************************************************
 *
 * The licensor of this software is Silicon Laboratories Inc. Your use of this
 * software is governed by the terms of Silicon Labs Master Software License
 * Agreement (MSLA) available at
 * www.silabs.com/about-us/legal/master-software-license-agreement. This
 * software is distributed to you in Source Code format and is governed by the
 * sections of the MSLA applicable to Source Code.
 *
 ******************************************************************************/

package com.silabs.pti.debugchannel;

import com.silabs.pti.adapter.IConnectionListener;
import com.silabs.pti.adapter.IDebugMessageListener;
import com.silabs.pti.log.PtiLog;

/**
 * A connection listener that extract byte[] messages and creates DebugMessage
 * objects out of them, feeding them upwards to a listener.
 *
 * @author Timotej Created on Mar 27, 2018
 */
public class DebugMessageCollector implements IConnectionListener {

  private IDebugMessageListener listener = null;
  private final String originatorId;
  private int count;
  
  public DebugMessageCollector(final String originatorId) {
    this.originatorId = originatorId;
    this.count = 0;
  }

  @Override
  public void messageReceived(final byte[] message, final long pcTime) {
    DebugMessage debugMessage = DebugMessage.make(originatorId, message, pcTime);
    if (debugMessage != null && listener != null) {
      count++;
      try {
        listener.processMessage(debugMessage);
      } catch (Exception e) {
        PtiLog.warning("Connection listener error", e);
      }
    }

  }

  @Override
  public void connectionStateChanged(final boolean isConnected) {
  }

  public void setDebugMessageListener(final IDebugMessageListener l) {
    this.listener = l;
  }
  
  @Override
  public int count() {
    return count;
  }
}
