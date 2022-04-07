package com.adobe.epubcheck.util;

import java.util.Locale;

import com.google.common.base.Enums;
import com.google.common.base.Strings;

//FIXME 2022 move to URLUtils
public enum URISchemes
{

  AAA,
  AAAS,
  ACAP,
  CAP,
  CID,
  CRID,
  DATA,
  DAV,
  DICT,
  DNS,
  FAX,
  FILE,
  FTP,
  GO,
  GOPHER,
  H323,
  HTTP,
  HTTPS,
  ICAP,
  IM,
  IMAP,
  INFO,
  IPP,
  IRC,
  IRIS,
  IRIS_BEEP,
  IRIS_XPC,
  IRIS_XPCS,
  IRIS_LWZ,
  JAVASCRIPT,
  LDAP,
  MAILTO,
  MID,
  MODEM,
  MSRP,
  MSRPS,
  MTQP,
  MUPDATE,
  NEWS,
  NFS,
  NNTP,
  OPAQUELOCKTOKEN,
  POP,
  PRES,
  RTSP,
  SERVICE,
  SHTTP,
  SIP,
  SIPS,
  SNMP,
  SOAP_BEEP,
  SOAP_BEEPS,
  TAG,
  TEL,
  TELNET,
  TFTP,
  THISMESSAGE,
  TIP,
  TV,
  URN,
  VEMMI,
  XMLRPC_BEEP,
  XMLRPC_BEEPS,
  XMPP,
  Z39_50R,
  Z39_50S,
  AFS,
  DTN,
  IAX,
  MAILSERVER,
  PACK,
  TN3270,
  PROSPERO,
  SNEWS,
  VIDEOTEX,
  WAIS;

  public static boolean contains(String scheme)
  {
    return Enums.getIfPresent(URISchemes.class,
        Strings.nullToEmpty(scheme).toUpperCase(Locale.ROOT).replace('.', '_')).isPresent();
  }
}
