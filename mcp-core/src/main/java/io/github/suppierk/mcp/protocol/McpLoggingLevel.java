package io.github.suppierk.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The severity of a log message. These map to syslog message severities, as specified in RFC-5424:
 * https://datatracker.ietf.org/doc/html/rfc5424#section-6.2.1.
 *
 * @see <a href="https://modelcontextprotocol.io/specification/2026-07-28/schema">MCP schema</a>
 */
public enum McpLoggingLevel {
  /** An alert condition. */
  @JsonProperty("alert")
  ALERT,

  /** A critical condition. */
  @JsonProperty("critical")
  CRITICAL,

  /** Debug information. */
  @JsonProperty("debug")
  DEBUG,

  /** An emergency condition. */
  @JsonProperty("emergency")
  EMERGENCY,

  /** An error condition. */
  @JsonProperty("error")
  ERROR,

  /** Informational output. */
  @JsonProperty("info")
  INFO,

  /** A normal but significant condition. */
  @JsonProperty("notice")
  NOTICE,

  /** A warning condition. */
  @JsonProperty("warning")
  WARNING;
}
