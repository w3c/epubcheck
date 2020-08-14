/*
 * Copyright (c) 2011 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public enum MessageId implements Comparable<MessageId>
{
  // General info messages
  INF_001("INF-001"),
  
  // Messages relating to accessibility
  ACC_001("ACC-001"),
  ACC_002("ACC-002"),
  ACC_003("ACC-003"),
  ACC_004("ACC-004"),
  ACC_005("ACC-005"),
  ACC_006("ACC-006"),
  ACC_007("ACC-007"),
  ACC_008("ACC-008"),
  ACC_009("ACC-009"),
  ACC_010("ACC-010"),
  ACC_011("ACC-011"),
  ACC_012("ACC-012"),
  ACC_013("ACC-013"),
  ACC_014("ACC-014"),
  ACC_015("ACC-015"),
  ACC_016("ACC-016"),
  ACC_017("ACC-017"),

  // Messages relating to the checker configuration
  CHK_001("CHK-001"),
  CHK_002("CHK-002"),
  CHK_003("CHK-003"),
  CHK_004("CHK-004"),
  CHK_005("CHK-005"),
  CHK_006("CHK-006"),
  CHK_007("CHK-007"),
  CHK_008("CHK-008"),

  // Messages associated with styles
  CSS_001("CSS-001"),
  CSS_002("CSS-002"),
  CSS_003("CSS-003"),
  CSS_004("CSS-004"),
  CSS_005("CSS-005"),
  CSS_006("CSS-006"),
  CSS_007("CSS-007"),
  CSS_008("CSS-008"),
  CSS_009("CSS-009"),
  CSS_010("CSS-010"),
  CSS_011("CSS-011"),
  CSS_012("CSS-012"),
  CSS_013("CSS-013"),
  CSS_015("CSS-015"),
  CSS_016("CSS-016"),
  CSS_017("CSS-017"),
  CSS_019("CSS-019"),
  CSS_020("CSS-020"),
  CSS_021("CSS-021"),
  CSS_022("CSS-022"),
  CSS_023("CSS-023"),
  CSS_024("CSS-024"),
  CSS_025("CSS-025"),
  CSS_028("CSS-028"),

  // Messages relating to xhtml markup
  HTM_001("HTM-001"),
  HTM_002("HTM-002"),
  HTM_003("HTM-003"),
  HTM_004("HTM-004"),
  HTM_005("HTM-005"),
  HTM_006("HTM-006"),
  HTM_007("HTM-007"),
  HTM_008("HTM-008"),
  HTM_009("HTM-009"),
  HTM_010("HTM-010"),
  HTM_011("HTM-011"),
  HTM_012("HTM-012"),
  HTM_013("HTM-013"),
  HTM_014("HTM-014"),
  HTM_014a("HTM-014a"),
  HTM_015("HTM-015"),
  HTM_016("HTM-016"),
  HTM_017("HTM-017"),
  HTM_018("HTM-018"),
  HTM_019("HTM-019"),
  HTM_020("HTM-020"),
  HTM_021("HTM-021"),
  HTM_022("HTM-022"),
  HTM_023("HTM-023"),
  HTM_024("HTM-024"),
  HTM_025("HTM-025"),
  HTM_027("HTM-027"),
  HTM_028("HTM-028"),
  HTM_029("HTM-029"),
  HTM_033("HTM-033"),
  HTM_036("HTM-036"),
  HTM_038("HTM-038"),
  HTM_044("HTM-044"),
  HTM_045("HTM-045"),
  HTM_046("HTM-046"),
  HTM_047("HTM-047"),
  HTM_048("HTM-048"),
  HTM_049("HTM-049"),
  HTM_050("HTM-050"),
  HTM_051("HTM-051"),
  HTM_052("HTM-052"),
  HTM_053("HTM_053"),

  // Messages associated with media (images, audio and video)
  MED_001("MED-001"),
  MED_002("MED-002"),
  MED_003("MED-003"),
  MED_004("MED-004"),
  MED_005("MED-005"),
  MED_006("MED_006"),
  MED_007("MED_007"),
  MED_016("MED_016"),

  // Epub3 based table of content errors
  NAV_001("NAV-001"),
  NAV_002("NAV-002"),
  NAV_003("NAV-003"),
  NAV_004("NAV-004"),
  NAV_005("NAV-005"),
  NAV_006("NAV-006"),
  NAV_007("NAV-007"),
  NAV_008("NAV-008"),
  NAV_009("NAV-009"),
  NAV_010("NAV-010"),
  NAV_011("NAV-011"),

  // Epub2 based table of content messages
  NCX_001("NCX-001"),
  NCX_002("NCX-002"),
  NCX_003("NCX-003"),
  NCX_004("NCX-004"),
  NCX_005("NCX-005"),
  NCX_006("NCX-006"),

  // Messages related to the markup in the OPF file
  OPF_001("OPF-001"),
  OPF_002("OPF-002"),
  OPF_003("OPF-003"),
  OPF_004("OPF-004"),
  OPF_004a("OPF-004a"),
  OPF_004b("OPF-004b"),
  OPF_004c("OPF-004c"),
  OPF_004d("OPF-004d"),
  OPF_004e("OPF-004e"),
  OPF_004f("OPF-004f"),
  OPF_005("OPF-005"),
  OPF_006("OPF-006"),
  OPF_007("OPF-007"),
  OPF_007a("OPF-007a"),
  OPF_007b("OPF-007b"),
  OPF_008("OPF-008"),
  OPF_009("OPF-009"),
  OPF_010("OPF-010"),
  OPF_011("OPF-011"),
  OPF_012("OPF-012"),
  OPF_013("OPF-013"),
  OPF_014("OPF-014"),
  OPF_015("OPF-015"),
  OPF_016("OPF-016"),
  OPF_017("OPF-017"),
  OPF_018("OPF-018"),
  OPF_018b("OPF-018b"),
  OPF_019("OPF-019"),
  OPF_020("OPF-020"),
  OPF_021("OPF-021"),
  OPF_025("OPF-025"),
  OPF_026("OPF-026"),
  OPF_027("OPF-027"),
  OPF_028("OPF-028"),
  OPF_029("OPF-029"),
  OPF_030("OPF-030"),
  OPF_031("OPF-031"),
  OPF_032("OPF-032"),
  OPF_033("OPF-033"),
  OPF_034("OPF-034"),
  OPF_035("OPF-035"),
  OPF_036("OPF-036"),
  OPF_037("OPF-037"),
  OPF_038("OPF-038"),
  OPF_039("OPF-039"),
  OPF_040("OPF-040"),
  OPF_041("OPF-041"),
  OPF_042("OPF-042"),
  OPF_043("OPF-043"),
  OPF_044("OPF-044"),
  OPF_045("OPF-045"),
  OPF_046("OPF-046"),
  OPF_047("OPF-047"),
  OPF_048("OPF-048"),
  OPF_049("OPF-049"),
  OPF_050("OPF-050"),
  OPF_051("OPF-051"),
  OPF_052("OPF-052"),
  OPF_053("OPF-053"),
  OPF_054("OPF-054"),
  OPF_055("OPF-055"),
  OPF_056("OPF-056"),
  OPF_057("OPF-057"),
  OPF_058("OPF-058"),
  OPF_059("OPF-059"),
  OPF_060("OPF-060"),
  OPF_061("OPF-061"),
  OPF_062("OPF-062"),
  OPF_063("OPF-063"),
  OPF_064("OPF-064"),
  OPF_065("OPF-065"),
  OPF_066("OPF-066"),
  OPF_067("OPF-067"),
  OPF_068("OPF-068"),
  OPF_069("OPF-069"),
  OPF_070("OPF-070"),
  OPF_071("OPF-071"),
  OPF_072("OPF-072"),
  OPF_073("OPF-073"),
  OPF_074("OPF-074"),
  OPF_075("OPF-075"),
  OPF_076("OPF-076"),
  OPF_077("OPF-077"),
  OPF_078("OPF-078"),
  OPF_079("OPF-079"),
  OPF_080("OPF-080"),
  OPF_081("OPF-081"),
  OPF_082("OPF-082"),
  OPF_083("OPF-083"),
  OPF_084("OPF-084"),
  OPF_085("OPF-085"),
  OPF_086("OPF-086"),
  OPF_086b("OPF-086b"),
  OPF_087("OPF-087"),
  OPF_088("OPF-088"),
  OPF_089("OPF-089"),
  OPF_090("OPF-090"),

  // Messages relating to the entire package
  PKG_001("PKG-001"),
  PKG_003("PKG-003"),
  PKG_004("PKG-004"),
  PKG_005("PKG-005"),
  PKG_006("PKG-006"),
  PKG_007("PKG-007"),
  PKG_008("PKG-008"),
  PKG_009("PKG-009"),
  PKG_010("PKG-010"),
  PKG_011("PKG-011"),
  PKG_012("PKG-012"),
  PKG_013("PKG-013"),
  PKG_014("PKG-014"),
  PKG_015("PKG-015"),
  PKG_016("PKG-016"),
  PKG_017("PKG-017"),
  PKG_018("PKG-018"),
  PKG_020("PKG-020"),
  PKG_021("PKG-021"),
  PKG_022("PKG-022"),
  PKG_023("PKG-023"),
  PKG_024("PKG-024"),

  // Messages relating to resources
  RSC_001("RSC-001"),
  RSC_002("RSC-002"),
  RSC_003("RSC-003"),
  RSC_004("RSC-004"),
  RSC_005("RSC-005"),
  RSC_006("RSC-006"),
  RSC_006b("RSC-006b"),
  RSC_007("RSC-007"),
  RSC_007w("RSC-007w"),
  RSC_008("RSC-008"),
  RSC_009("RSC-009"),
  RSC_010("RSC-010"),
  RSC_011("RSC-011"),
  RSC_012("RSC-012"),
  RSC_013("RSC-013"),
  RSC_014("RSC-014"),
  RSC_015("RSC-015"),
  RSC_016("RSC-016"),
  RSC_017("RSC-017"),
  RSC_018("RSC-018"),
  RSC_019("RSC-019"),
  RSC_020("RSC-020"),
  RSC_021("RSC-021"),
  RSC_022("RSC-022"),
  RSC_023("RSC-023"),

  // Messages relating to scripting
  SCP_001("SCP-001"),
  SCP_002("SCP-002"),
  SCP_003("SCP-003"),
  SCP_004("SCP-004"),
  SCP_005("SCP-005"),
  SCP_006("SCP-006"),
  SCP_007("SCP-007"),
  SCP_008("SCP-008"),
  SCP_009("SCP-009"),
  SCP_010("SCP-010");

  private final String messageId;

  MessageId(String feature)
  {
    this.messageId = feature;
  }

  public String toString()
  {
    return messageId;
  }

  private static final Map<String, MessageId> map = new HashMap<String, MessageId>();

  static
  {
    for (MessageId type : MessageId.values())
    {
      map.put(type.messageId, type);
    }
  }

  public static MessageId fromString(String messageId)
  {
    if (map.containsKey(messageId))
    {
      return map.get(messageId);
    }
    throw new NoSuchElementException("MessageId." + messageId + " not found");
  }

}
