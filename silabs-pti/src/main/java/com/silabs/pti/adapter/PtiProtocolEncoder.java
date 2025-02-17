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
package com.silabs.pti.adapter;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class PtiProtocolEncoder extends ProtocolEncoderAdapter {
  private final Charset charset;

  public PtiProtocolEncoder(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
    CharsetEncoder encoder = charset.newEncoder();
    String value = message == null ? "" : message.toString();
    IoBuffer buf = IoBuffer.allocate(value.length()).setAutoExpand(true);
    buf.putString(value, encoder);
    buf.flip();
    out.write(buf);
  }
}
