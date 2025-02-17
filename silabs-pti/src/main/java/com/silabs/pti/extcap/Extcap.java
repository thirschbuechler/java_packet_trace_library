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

package com.silabs.pti.extcap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.silabs.pti.discovery.DiscoveryKey;
import com.silabs.pti.discovery.DiscoveryUtil;

/**
 * When this jar file is used within wireshark, the extcap wireshark
 * functionality will pass 'extcap' as the first argument. If that happens then
 * we end up here.
 * 
 * @author timotej
 *
 */
public class Extcap implements IExtcapInterface {

  // Commands
  private static final String EC_INTERFACES = "--extcap-interfaces";
  private static final String EC_CONFIG = "--extcap-config";
  private static final String EC_CAPTURE = "--capture";
  private static final String EC_DLTS = "--extcap-dlts";

  // Additional args
  private static final String EC_INTERFACE = "--extcap-interface";
  private static final String EC_FIFO = "--fifo";
  private static final String EC_CAPTURE_FILTER = "--extcap-capture-filter";

  private File logFile = null;
  private PrintWriter logWriter;
  private final PrintStream extcapOut;
  private final List<String> extcapArgs = new ArrayList<String>();

  /**
   * Execute extcap function. Args will contain 'extcap' as the first argument.
   * 
   * @param args
   * @return
   */
  public static final int run(final String[] args) {
    return new Extcap(args).run();
  }

  /**
   * Entry point to create an instance.
   * 
   * @param args
   */
  private Extcap(final String[] args) {
    final String extcapLocation = System.getenv("EXTCAP_LOC");
    if (extcapLocation != null) {
      logFile = new File(extcapLocation, "silabs-pti.log");
    }
    extcapOut = System.out;
    for (int i = 0; i < args.length; i++) {
      if (i > 0) {
        extcapArgs.add(args[i]);
      }
    }
  }

  /**
   * This method logs to the log file, unrelated to wireshark, so you can debug
   * what's happening.
   * 
   * @param s
   */
  @Override
  public void log(final String s) {
    final String d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
    logWriter.println(d + ": " + s);
  }

  /**
   * This method prints output to the wireshark extcap communication protocol.
   * 
   * @param s
   */
  @Override
  public void extcapPrintln(final String s) {
    log("extcap <           " + s);
    extcapOut.println(s);
  }

  /**
   * The method returns one of the toplevel commands for the extcap CLI protocol.
   * 
   * @return command
   */
  private String extractCommandFromArgs() {
    for (final String extcapArg : extcapArgs) {
      switch (extcapArg) {
      case EC_INTERFACES:
        return EC_INTERFACES;
      case EC_DLTS:
        return EC_DLTS;
      case EC_CONFIG:
        return EC_CONFIG;
      case EC_CAPTURE:
        return EC_CAPTURE;
      }

    }
    return null;
  }

  /**
   * Given an argument, this returns the value of the argument, so essentially the
   * next argument.
   * 
   * @param arg
   * @return value of a given argument.
   */
  private String extractValueFromArg(final String arg) {
    for (int i = 0; i < extcapArgs.size(); i++) {
      if (extcapArgs.get(i).equals(arg)) {
        if (i + 1 < extcapArgs.size()) {
          return extcapArgs.get(i + 1);
        }
      }
    }
    return null;
  }

  /**
   * Main execution loop. Creates the logger and parses the command line.
   * 
   * @return return code from the program.
   */
  private int run() {
    try (PrintWriter l = (logFile != null ? new PrintWriter(new FileWriter(logFile, true), true)
        : new PrintWriter(System.out))) {
      logWriter = l;
      final StringBuilder sb = new StringBuilder("extcap > ");
      for (final String s : extcapArgs)
        sb.append(" ").append(s);
      log(sb.toString());
      final String cmd = extractCommandFromArgs();
      if (cmd != null) {
        switch (cmd) {
        case EC_INTERFACES:
          return extcapInterfaces();
        case EC_DLTS:
          return extcapDlts(extractValueFromArg(EC_INTERFACE));
        case EC_CONFIG:
          return extcapConfig(extractValueFromArg(EC_INTERFACE));
        case EC_CAPTURE:
          return extcapCapture(extractValueFromArg(EC_INTERFACE));
        }
      } else {
        return 1;
      }
    } catch (final Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * Entry point for the interfaces list command.
   * 
   * @return program return code
   */
  private int extcapInterfaces() {
    extcapPrintln("extcap {version=1.0}{help=http://silabs.com}");
    DiscoveryUtil.runDiscovery(packet -> {
      final Map<DiscoveryKey, String> data = DiscoveryUtil.parseDiscoveryMap(packet);
      String name = "Silicon Labs WSTK adapter";
      String ip = packet.getAddress().getHostAddress();
      if (data.containsKey(DiscoveryKey.ADAPTER_NETIF)) {
        ip = data.get(DiscoveryKey.ADAPTER_NETIF);
      }

      if (data.containsKey(DiscoveryKey.ADAPTER_NICKNAME)) {
        name = data.get(DiscoveryKey.ADAPTER_NICKNAME) + " (Silicon Labs WSTK adapter)";
      }

      extcapPrintln("interface {value=" + ip + "}{display=" + name + "}");
    });
    return 0;
  }

  /**
   * Entry point for the DLTs list.
   * 
   * @return program return code
   */
  private int extcapDlts(final String ifc) {
    if (ifc == null)
      return 1;
    extcapPrintln("dlt {number=147}{name=USER1}{display=WSTK Silicon Labs DLT}");
    return 0;
  }

  /**
   * Entry point for the capture initialization.
   * 
   * @return program return code
   */
  private int extcapCapture(final String ifc) {
    if (ifc == null)
      return 1;
    final String fifo = extractValueFromArg(EC_FIFO);
    final String filter = extractValueFromArg(EC_CAPTURE_FILTER);
    log("capture: from " + ifc + " into " + fifo + (filter == null ? " with no filter" : (" with filter " + filter)));
    final ExtcapCapture capture = new ExtcapCapture(ifc, fifo, filter);
    try {
      capture.capture(this);
      return 0;
    } catch (final IOException ioe) {
      log("error during capture: " + ioe.getMessage());
      return 1;
    }
  }

  /**
   * Entry point for the configuration information.
   * 
   * @return program return code
   */
  private int extcapConfig(final String ifc) {
    if (ifc == null)
      return 1;
//    extcapPrintln("arg {number=0}{call=--delay}{display=WSTK delay}{tooltip=Time delay between packages}{type=integer}{range=1,15}{required=true}");
//    extcapPrintln("arg {number=1}{call=--message}{display=WSTK Message}{tooltip=Package message content}{placeholder=Please enter a message here ...}{type=string}");
//    extcapPrintln("arg {number=2}{call=--verify}{display=WSTK Verify}{tooltip=Verify package content}{type=boolflag}");
//    extcapPrintln("arg {number=3}{call=--remote}{display=WSTK Remote Channel}{tooltip=Remote Channel Selector}{type=selector}");
//    extcapPrintln("arg {number=4}{call=--server}{display=WSTK IP address for log server}{type=string}{validation=\\\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\\\b}");
//    extcapPrintln("value {arg=3}{value=if1}{display=WSTK Remote1}{default=true}");
//    extcapPrintln("value {arg=3}{value=if2}{display=WSTK Remote2}{default=false}");
    return 0;
  }
}
