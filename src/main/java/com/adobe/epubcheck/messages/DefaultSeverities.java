package com.adobe.epubcheck.messages;

import java.util.EnumMap;
import java.util.Map;

/**
 * A container for handling the default mapping of message id to severity.
 */
class DefaultSeverities implements Severities
{

  private static final Map<MessageId, Severity> severities = new EnumMap<MessageId, Severity>(MessageId.class);

  public DefaultSeverities()
  {
    initialize();
  }

  @Override
  public Severity get(MessageId id)
  {
    Severity severity = severities.get(id);
    if (severity == null)
    {
      //Indicates a programmer error
      throw new IllegalArgumentException("Severity " + id.name() + " is invalid.");
    }
    return severity;
  }

  private void initialize()
  {
    if (severities.isEmpty() == false)
    {
      return;
    }
    // Info
    severities.put(MessageId.INF_001, Severity.INFO);

    // Accessibility
    severities.put(MessageId.ACC_001, Severity.USAGE);
    severities.put(MessageId.ACC_002, Severity.USAGE);
    severities.put(MessageId.ACC_003, Severity.SUPPRESSED);
    severities.put(MessageId.ACC_004, Severity.SUPPRESSED);
    severities.put(MessageId.ACC_005, Severity.SUPPRESSED);
    severities.put(MessageId.ACC_006, Severity.SUPPRESSED);
    severities.put(MessageId.ACC_007, Severity.SUPPRESSED);
    severities.put(MessageId.ACC_008, Severity.USAGE);
    severities.put(MessageId.ACC_009, Severity.USAGE);
    severities.put(MessageId.ACC_010, Severity.SUPPRESSED);
    severities.put(MessageId.ACC_011, Severity.WARNING);
    severities.put(MessageId.ACC_012, Severity.SUPPRESSED);
    severities.put(MessageId.ACC_013, Severity.USAGE);
    severities.put(MessageId.ACC_014, Severity.USAGE);
    severities.put(MessageId.ACC_015, Severity.USAGE);
    severities.put(MessageId.ACC_016, Severity.USAGE);
    severities.put(MessageId.ACC_017, Severity.USAGE);

    // CHK
    severities.put(MessageId.CHK_001, Severity.ERROR);
    severities.put(MessageId.CHK_002, Severity.ERROR);
    severities.put(MessageId.CHK_003, Severity.ERROR);
    severities.put(MessageId.CHK_004, Severity.ERROR);
    severities.put(MessageId.CHK_005, Severity.ERROR);
    severities.put(MessageId.CHK_006, Severity.ERROR);
    severities.put(MessageId.CHK_007, Severity.ERROR);
    severities.put(MessageId.CHK_008, Severity.ERROR);

    // CSS
    severities.put(MessageId.CSS_001, Severity.ERROR);
    severities.put(MessageId.CSS_002, Severity.ERROR);
    severities.put(MessageId.CSS_003, Severity.ERROR);
    severities.put(MessageId.CSS_004, Severity.ERROR);
    severities.put(MessageId.CSS_005, Severity.ERROR);
    severities.put(MessageId.CSS_006, Severity.USAGE);
    severities.put(MessageId.CSS_007, Severity.INFO);
    severities.put(MessageId.CSS_008, Severity.ERROR);
    severities.put(MessageId.CSS_009, Severity.USAGE);
    severities.put(MessageId.CSS_010, Severity.ERROR);
    severities.put(MessageId.CSS_011, Severity.SUPPRESSED);
    severities.put(MessageId.CSS_012, Severity.USAGE);
    severities.put(MessageId.CSS_013, Severity.USAGE);
    severities.put(MessageId.CSS_015, Severity.ERROR);
    severities.put(MessageId.CSS_016, Severity.SUPPRESSED);
    severities.put(MessageId.CSS_017, Severity.USAGE);
    severities.put(MessageId.CSS_019, Severity.WARNING);
    severities.put(MessageId.CSS_020, Severity.ERROR);
    severities.put(MessageId.CSS_021, Severity.USAGE);
    severities.put(MessageId.CSS_022, Severity.USAGE);
    severities.put(MessageId.CSS_023, Severity.USAGE);
    severities.put(MessageId.CSS_024, Severity.USAGE);
    severities.put(MessageId.CSS_025, Severity.USAGE);
    severities.put(MessageId.CSS_028, Severity.USAGE);

    // HTML
    severities.put(MessageId.HTM_001, Severity.ERROR);
    severities.put(MessageId.HTM_002, Severity.WARNING);
    severities.put(MessageId.HTM_003, Severity.ERROR);
    severities.put(MessageId.HTM_004, Severity.ERROR);
    severities.put(MessageId.HTM_005, Severity.USAGE);
    severities.put(MessageId.HTM_006, Severity.USAGE);
    severities.put(MessageId.HTM_007, Severity.WARNING);
    severities.put(MessageId.HTM_008, Severity.ERROR);
    severities.put(MessageId.HTM_009, Severity.ERROR);
    severities.put(MessageId.HTM_010, Severity.USAGE);
    severities.put(MessageId.HTM_011, Severity.ERROR);
    severities.put(MessageId.HTM_012, Severity.USAGE);
    severities.put(MessageId.HTM_013, Severity.USAGE);
    severities.put(MessageId.HTM_014, Severity.WARNING);
    severities.put(MessageId.HTM_014a, Severity.WARNING);
    severities.put(MessageId.HTM_015, Severity.SUPPRESSED);
    severities.put(MessageId.HTM_016, Severity.SUPPRESSED);
    severities.put(MessageId.HTM_017, Severity.ERROR);
    severities.put(MessageId.HTM_018, Severity.USAGE);
    severities.put(MessageId.HTM_019, Severity.USAGE);
    severities.put(MessageId.HTM_020, Severity.USAGE);
    severities.put(MessageId.HTM_021, Severity.USAGE);
    severities.put(MessageId.HTM_022, Severity.USAGE);
    severities.put(MessageId.HTM_023, Severity.SUPPRESSED);
    severities.put(MessageId.HTM_024, Severity.SUPPRESSED);
    severities.put(MessageId.HTM_025, Severity.WARNING);
    severities.put(MessageId.HTM_027, Severity.USAGE);
    severities.put(MessageId.HTM_028, Severity.USAGE);
    severities.put(MessageId.HTM_029, Severity.USAGE);
    severities.put(MessageId.HTM_033, Severity.USAGE);
    severities.put(MessageId.HTM_036, Severity.SUPPRESSED);
    severities.put(MessageId.HTM_038, Severity.SUPPRESSED);
    severities.put(MessageId.HTM_044, Severity.USAGE);
    severities.put(MessageId.HTM_045, Severity.USAGE);
    severities.put(MessageId.HTM_046, Severity.ERROR);
    severities.put(MessageId.HTM_047, Severity.ERROR);
    severities.put(MessageId.HTM_048, Severity.ERROR);
    severities.put(MessageId.HTM_049, Severity.ERROR);
    severities.put(MessageId.HTM_050, Severity.USAGE);
    severities.put(MessageId.HTM_051, Severity.WARNING);
    severities.put(MessageId.HTM_052, Severity.ERROR);
    severities.put(MessageId.HTM_053, Severity.INFO);

    // Media
    severities.put(MessageId.MED_001, Severity.ERROR);
    severities.put(MessageId.MED_002, Severity.ERROR);
    severities.put(MessageId.MED_003, Severity.ERROR);
    severities.put(MessageId.MED_004, Severity.ERROR);
    severities.put(MessageId.MED_005, Severity.ERROR);
    severities.put(MessageId.MED_006, Severity.USAGE);
    severities.put(MessageId.MED_007, Severity.ERROR);
    severities.put(MessageId.MED_016, Severity.ERROR);

    // NAV
    severities.put(MessageId.NAV_001, Severity.ERROR);
    severities.put(MessageId.NAV_002, Severity.USAGE);
    severities.put(MessageId.NAV_003, Severity.ERROR);
    severities.put(MessageId.NAV_004, Severity.USAGE);
    severities.put(MessageId.NAV_005, Severity.USAGE);
    severities.put(MessageId.NAV_006, Severity.USAGE);
    severities.put(MessageId.NAV_007, Severity.USAGE);
    severities.put(MessageId.NAV_008, Severity.USAGE);
    severities.put(MessageId.NAV_009, Severity.ERROR);
    severities.put(MessageId.NAV_010, Severity.ERROR);
    severities.put(MessageId.NAV_011, Severity.WARNING);

    // NCX
    severities.put(MessageId.NCX_001, Severity.ERROR);
    severities.put(MessageId.NCX_002, Severity.ERROR);
    severities.put(MessageId.NCX_003, Severity.USAGE);
    severities.put(MessageId.NCX_004, Severity.USAGE);
    severities.put(MessageId.NCX_005, Severity.USAGE);
    severities.put(MessageId.NCX_006, Severity.USAGE);

    // OPF
    severities.put(MessageId.OPF_001, Severity.ERROR);
    severities.put(MessageId.OPF_002, Severity.FATAL);
    severities.put(MessageId.OPF_003, Severity.WARNING);
    severities.put(MessageId.OPF_004, Severity.WARNING);
    severities.put(MessageId.OPF_004a, Severity.ERROR);
    severities.put(MessageId.OPF_004b, Severity.ERROR);
    severities.put(MessageId.OPF_004c, Severity.ERROR);
    severities.put(MessageId.OPF_004d, Severity.ERROR);
    severities.put(MessageId.OPF_004e, Severity.WARNING);
    severities.put(MessageId.OPF_004f, Severity.WARNING);
    severities.put(MessageId.OPF_005, Severity.ERROR);
    severities.put(MessageId.OPF_006, Severity.ERROR);
    severities.put(MessageId.OPF_007, Severity.WARNING);
    severities.put(MessageId.OPF_007a, Severity.ERROR);
    severities.put(MessageId.OPF_007b, Severity.WARNING);
    severities.put(MessageId.OPF_008, Severity.ERROR);
    severities.put(MessageId.OPF_009, Severity.ERROR);
    severities.put(MessageId.OPF_010, Severity.ERROR);
    severities.put(MessageId.OPF_011, Severity.ERROR);
    severities.put(MessageId.OPF_012, Severity.ERROR);
    severities.put(MessageId.OPF_013, Severity.ERROR);
    severities.put(MessageId.OPF_014, Severity.ERROR);
    severities.put(MessageId.OPF_015, Severity.ERROR);
    severities.put(MessageId.OPF_016, Severity.ERROR);
    severities.put(MessageId.OPF_017, Severity.ERROR);
    severities.put(MessageId.OPF_018, Severity.WARNING);
    severities.put(MessageId.OPF_018b, Severity.USAGE);
    severities.put(MessageId.OPF_019, Severity.FATAL);
    severities.put(MessageId.OPF_020, Severity.SUPPRESSED);
    severities.put(MessageId.OPF_021, Severity.WARNING);
    severities.put(MessageId.OPF_025, Severity.ERROR);
    severities.put(MessageId.OPF_026, Severity.ERROR);
    severities.put(MessageId.OPF_027, Severity.ERROR);
    severities.put(MessageId.OPF_028, Severity.ERROR);
    severities.put(MessageId.OPF_029, Severity.ERROR);
    severities.put(MessageId.OPF_030, Severity.ERROR);
    severities.put(MessageId.OPF_031, Severity.ERROR);
    severities.put(MessageId.OPF_032, Severity.ERROR);
    severities.put(MessageId.OPF_033, Severity.ERROR);
    severities.put(MessageId.OPF_034, Severity.ERROR);
    severities.put(MessageId.OPF_035, Severity.WARNING);
    severities.put(MessageId.OPF_036, Severity.USAGE);
    severities.put(MessageId.OPF_037, Severity.WARNING);
    severities.put(MessageId.OPF_038, Severity.WARNING);
    severities.put(MessageId.OPF_039, Severity.WARNING);
    severities.put(MessageId.OPF_040, Severity.ERROR);
    severities.put(MessageId.OPF_041, Severity.ERROR);
    severities.put(MessageId.OPF_042, Severity.ERROR);
    severities.put(MessageId.OPF_043, Severity.ERROR);
    severities.put(MessageId.OPF_044, Severity.ERROR);
    severities.put(MessageId.OPF_045, Severity.ERROR);
    severities.put(MessageId.OPF_046, Severity.ERROR);
    severities.put(MessageId.OPF_047, Severity.USAGE);
    severities.put(MessageId.OPF_048, Severity.ERROR);
    severities.put(MessageId.OPF_049, Severity.ERROR);
    severities.put(MessageId.OPF_050, Severity.ERROR);
    severities.put(MessageId.OPF_051, Severity.SUPPRESSED);
    severities.put(MessageId.OPF_052, Severity.ERROR);
    severities.put(MessageId.OPF_053, Severity.WARNING);
    severities.put(MessageId.OPF_054, Severity.ERROR);
    severities.put(MessageId.OPF_055, Severity.WARNING);
    severities.put(MessageId.OPF_056, Severity.USAGE);
    severities.put(MessageId.OPF_057, Severity.SUPPRESSED);
    severities.put(MessageId.OPF_058, Severity.USAGE);
    severities.put(MessageId.OPF_059, Severity.USAGE);
    severities.put(MessageId.OPF_060, Severity.ERROR);
    severities.put(MessageId.OPF_061, Severity.WARNING);
    severities.put(MessageId.OPF_062, Severity.USAGE);
    severities.put(MessageId.OPF_063, Severity.WARNING);
    severities.put(MessageId.OPF_064, Severity.INFO);
    severities.put(MessageId.OPF_065, Severity.ERROR);
    severities.put(MessageId.OPF_066, Severity.ERROR);
    severities.put(MessageId.OPF_067, Severity.ERROR);
    severities.put(MessageId.OPF_068, Severity.ERROR);
    severities.put(MessageId.OPF_069, Severity.ERROR);
    severities.put(MessageId.OPF_070, Severity.WARNING);
    severities.put(MessageId.OPF_071, Severity.ERROR);
    severities.put(MessageId.OPF_072, Severity.USAGE);
    severities.put(MessageId.OPF_073, Severity.ERROR);
    severities.put(MessageId.OPF_074, Severity.ERROR);
    severities.put(MessageId.OPF_075, Severity.ERROR);
    severities.put(MessageId.OPF_076, Severity.ERROR);
    severities.put(MessageId.OPF_077, Severity.WARNING);
    severities.put(MessageId.OPF_078, Severity.ERROR);
    severities.put(MessageId.OPF_079, Severity.WARNING);
    severities.put(MessageId.OPF_080, Severity.WARNING);
    severities.put(MessageId.OPF_081, Severity.ERROR);
    severities.put(MessageId.OPF_082, Severity.ERROR);
    severities.put(MessageId.OPF_083, Severity.ERROR);
    severities.put(MessageId.OPF_084, Severity.ERROR);
    severities.put(MessageId.OPF_085, Severity.WARNING);
    severities.put(MessageId.OPF_086, Severity.WARNING);
    severities.put(MessageId.OPF_086b, Severity.USAGE);
    severities.put(MessageId.OPF_087, Severity.USAGE);
    severities.put(MessageId.OPF_088, Severity.USAGE);
    severities.put(MessageId.OPF_089, Severity.ERROR);
    severities.put(MessageId.OPF_090, Severity.USAGE);

    // PKG
    severities.put(MessageId.PKG_001, Severity.WARNING);
    severities.put(MessageId.PKG_003, Severity.ERROR);
    severities.put(MessageId.PKG_004, Severity.FATAL);
    severities.put(MessageId.PKG_005, Severity.ERROR);
    severities.put(MessageId.PKG_006, Severity.ERROR);
    severities.put(MessageId.PKG_007, Severity.ERROR);
    severities.put(MessageId.PKG_008, Severity.FATAL);
    severities.put(MessageId.PKG_009, Severity.ERROR);
    severities.put(MessageId.PKG_010, Severity.WARNING);
    severities.put(MessageId.PKG_011, Severity.ERROR);
    severities.put(MessageId.PKG_012, Severity.USAGE);
    severities.put(MessageId.PKG_013, Severity.ERROR);
    severities.put(MessageId.PKG_014, Severity.WARNING);
    severities.put(MessageId.PKG_015, Severity.FATAL);
    severities.put(MessageId.PKG_016, Severity.WARNING);
    severities.put(MessageId.PKG_017, Severity.WARNING);
    severities.put(MessageId.PKG_018, Severity.FATAL);
    severities.put(MessageId.PKG_020, Severity.ERROR);
    severities.put(MessageId.PKG_021, Severity.ERROR);
    severities.put(MessageId.PKG_022, Severity.WARNING);
    severities.put(MessageId.PKG_023, Severity.USAGE);
    severities.put(MessageId.PKG_024, Severity.INFO);

    // Resources
    severities.put(MessageId.RSC_001, Severity.ERROR);
    severities.put(MessageId.RSC_002, Severity.FATAL);
    severities.put(MessageId.RSC_003, Severity.ERROR);
    severities.put(MessageId.RSC_004, Severity.INFO);
    severities.put(MessageId.RSC_005, Severity.ERROR);
    severities.put(MessageId.RSC_006, Severity.ERROR);
    severities.put(MessageId.RSC_006b, Severity.USAGE);
    severities.put(MessageId.RSC_007, Severity.ERROR);
    severities.put(MessageId.RSC_007w, Severity.WARNING);
    severities.put(MessageId.RSC_008, Severity.ERROR);
    severities.put(MessageId.RSC_009, Severity.WARNING);
    severities.put(MessageId.RSC_010, Severity.ERROR);
    severities.put(MessageId.RSC_011, Severity.ERROR);
    severities.put(MessageId.RSC_012, Severity.ERROR);
    severities.put(MessageId.RSC_013, Severity.ERROR);
    severities.put(MessageId.RSC_014, Severity.ERROR);
    severities.put(MessageId.RSC_015, Severity.ERROR);
    severities.put(MessageId.RSC_016, Severity.FATAL);
    severities.put(MessageId.RSC_017, Severity.WARNING);
    severities.put(MessageId.RSC_018, Severity.WARNING);
    severities.put(MessageId.RSC_019, Severity.WARNING);
    severities.put(MessageId.RSC_020, Severity.ERROR);
    severities.put(MessageId.RSC_021, Severity.ERROR);
    severities.put(MessageId.RSC_022, Severity.INFO);
    severities.put(MessageId.RSC_023, Severity.WARNING);

    // Scripting
    severities.put(MessageId.SCP_001, Severity.USAGE);
    severities.put(MessageId.SCP_002, Severity.USAGE);
    severities.put(MessageId.SCP_003, Severity.USAGE);
    severities.put(MessageId.SCP_004, Severity.ERROR);
    severities.put(MessageId.SCP_005, Severity.SUPPRESSED);
    severities.put(MessageId.SCP_006, Severity.USAGE);
    severities.put(MessageId.SCP_007, Severity.USAGE);
    severities.put(MessageId.SCP_008, Severity.USAGE);
    severities.put(MessageId.SCP_009, Severity.USAGE);
    severities.put(MessageId.SCP_010, Severity.USAGE);
  }

}
