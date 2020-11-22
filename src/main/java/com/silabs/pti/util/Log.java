//Copyright 2005 Ember Corporation. All rights reserved.


package com.silabs.pti.util;

/**
 * This class provides logging through eclipse loggers. All workbench
 * code should use it to log stuff.
 *
 * Created on Jul 26, 2005
 * @author Timotej (timotej@ember.com)
 * @since 4.6
 */

public class Log {

  private static ILogger logger = new DefaultLogger();

  // Don't instantiate
  private Log() {
  }

  public static void setLogger(final ILogger loggerInstance) {
    logger = loggerInstance;
  }
  /** Log simple informational message. */
  public static void info(final String message) {
    info(message, null);
  }

  /** Log simple informational message with exception */
  public static void info(final String message, final Throwable throwable) {
    logger.log(Severity.INFO, message, throwable);
  }

  /** Log error */
  public static void error(final String message) {
    error(message, null);
  }
  
  /** Log error 
   * @since 5.0 */
  public static void error(final Throwable throwable) {
    error(null, throwable);
  }

  /** Log error with exception */
  public static void error(final String message, final Throwable throwable) {
    logger.log(Severity.ERROR, message, throwable);
  }

  /** Log warning */
  public static void warning(final String message) {
    warning(message, null);
  }

  /** Log warning with exception */
  public static void warning(final String message, final Throwable throwable) {
    logger.log(Severity.WARNING, message, throwable);
  }

  /**
   * Log message with full information.
   * Calling this is identical to calling separate info(), error()
   * and warning() methods.
   */
  public static void message(final Severity severity, final String message, final Throwable t) {
    switch(severity) {
    case ERROR:
    case INFO:
    case WARNING:
      logger.log(severity, message, t);
      break;
    case NONE:
      break;
    }
  }

}
