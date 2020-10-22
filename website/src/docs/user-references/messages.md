---
title: Messages
---

This is the reference documentation for EPUBCheck messages.

Whenever it detects a conformance issue (or other noteworthy information) with your content, EPUBCheck reports a message. A message consists of several pieces of information:

- a **severity level** indicates if it is a fatal error, an error, a warning, an information, or a usage message. See the [severity levels](#severity-levels) section below for more details.
- a **message code** is used to uniquely identify the issue among the [reference list of possible messages](#message-codes)
- a **location** indicates in which file (possibly with a line and column number) the issue was detected
- a **text content** describes the reported issue in plain language

## Severity Levels

The messages reported by EPUBCheck use the following severity level:

Fatal Error
: An error which prevents EPUBCheck from further processing the content.
  This may be caused by a bug in EPUBCheck, or an unforeseen condition (like an I/O error).

Error
: A failure to match an EPUB conformance requirement.
  In the EPUB specifications, these requirements are identified with "MUST", "MUST NOT", or "REQUIRED" keywords.

Warning
: A deviation from an EPUB recommended practice.
  In the EPUB specifications, these recommendations are identified with "SHOULD", "SHOULD NOT", or "RECOMMENDED" keywords

Info
: A noteworthy information about your content.

Usage
: A minor information about your content.


## Message Codes

:::note
This list of message documentation is a work in progress,
as the message system is expected to be refactored soon.
:::

{# 
The following table is generated from a JSON file
You can find it src/_data/messages.json
You can check the script that compiles the list in the file src/_data/messages.js
#}
<div role="region" aria-labelledby="message-table-caption" tabindex="0">
<table border="0">
	<caption id="message-table-caption">EPUBCheck Message Codes</caption>
	<thead>
		<tr>
			<th>Code</th>
			<th>Severity</th>
			<th>Message</th>
			<!-- <th>References</th> -->
		</tr>
	</thead>
	<tbody>{% for area in messages %}{% for message in area.messages %}{% if message.severity != "suppressed" %}
<tr>
	<td>{{ message.code }}</td>
	<td><span class="severity {{ message.severity }}">{{ message.severity }}</span></td>
	<td>{{ message.message }}{% if message.explanation %}
	<details>
	<summary>Explanation</summary>
	{{ message.explanation }}
	</details>
	{% endif %}</td>
</tr>
{% endif %}{% endfor %}{% endfor %}</tbody>
</table>
</div>
<style>
[role="region"][aria-labelledby][tabindex] {
  overflow: auto;
}
[role="region"][aria-labelledby][tabindex]:focus {
  outline: .1em solid rgba(0,0,0,.1);
}
	.severity {
		display: inline-block;
		padding: .25em .4em;
		line-height: 1;
		text-align: center;
		white-space: nowrap;
		vertical-align: baseline;
		border-radius: .25rem;
    }
	.severity.warning {
		color: #856404;
		background-color: #fff3cd;
		border-color: #ffeeba;
	}
	.severity.info {
		color: #004085;
		background-color: #cce5ff;
		border-color: #b8daff;
	}
	.severity.error {
		color: #721c24;
		background-color: #f8d7da;
		border-color: #f5c6cb;
	}
	.severity.usage {
		color: #155724;
		background-color: #d4edda;
		border-color: #c3e6cb;
	}
	.severity.fatal {
		color: #1b1e21;
		background-color: #d6d8d9;
		border-color: #c6c8ca;
	}
</style>