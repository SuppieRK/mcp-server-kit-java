package io.github.suppierk.mcp.protocol;

/**
 * The severity of a log message. These map to syslog message severities, as specified in RFC-5424:
 * https://datatracker.ietf.org/doc/html/rfc5424#section-6.2.1.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public enum McpLoggingLevel {
  /** An alert condition. */
  ALERT,

  /** A critical condition. */
  CRITICAL,

  /** Debug information. */
  DEBUG,

  /** An emergency condition. */
  EMERGENCY,

  /** An error condition. */
  ERROR,

  /** Informational output. */
  INFO,

  /** A normal but significant condition. */
  NOTICE,

  /** A warning condition. */
  WARNING;
}
