package com.adobe.epubcheck.ctc;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.epubcheck.core.Checker;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.ctc.epubpackage.EpubPackage;
import com.adobe.epubcheck.ctc.epubpackage.ManifestItem;
import com.adobe.epubcheck.ctc.xml.LangAttributeHandler;
import com.adobe.epubcheck.ctc.xml.XMLContentDocParser;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.util.SearchDictionary;
import com.adobe.epubcheck.util.SearchDictionary.DictionaryType;

/**
 *  ===  WARNING  ==========================================<br/>
 *  This class is scheduled to be refactored and integrated<br/>
 *  in another package.<br/>
 *  Please keep changes minimal (bug fixes only) until then.<br/>
 *  ========================================================<br/>
 */
public class EpubLangCheck implements Checker
{
  private final ZipFile zip;
  private final Report report;
  private final EpubPackage epack;

  public EpubLangCheck(EpubPackage epack, Report report)
  {
    this.zip = epack.getZip();
    this.report = report;
    this.epack = epack;
  }

  @Override
  public void check()
  {
    boolean result = false;
    SearchDictionary vtsd = new SearchDictionary(DictionaryType.VALID_TEXT_MEDIA_TYPES);
    for (int i = 0; i < epack.getManifest().itemsLength(); i++)
    {
      ManifestItem mi = epack.getManifest().getItem(i);
      if (vtsd.isValidMediaType(mi.getMediaType()))
      {
        XMLContentDocParser parser = new XMLContentDocParser(this.zip, report);
        LangAttributeHandler sh = new LangAttributeHandler();
        String fileToParse = epack.getManifestItemFileName(mi);

        ZipEntry entry = this.zip.getEntry(fileToParse);
        if (entry == null)
        {
          // already reported in core checkers
          // report.message(MessageId.RSC_001, EPUBLocation.create(this.epack.getFileName()), fileToParse);
          continue;
        }

        parser.parseDoc(fileToParse, sh);
        String langAttribute = sh.getLangAttr();
        String xmlLangAttribute = sh.getXmlLangAttr();
        if (langAttribute != null && xmlLangAttribute != null)
        {
          if (xmlLangAttribute.compareToIgnoreCase(langAttribute) != 0)
          {
            report.message(MessageId.HTM_017, EPUBLocation.create(fileToParse));
          }

          if (!isValidLanguageDefinition(xmlLangAttribute))
          {
            report.message(MessageId.HTM_018, EPUBLocation.create(fileToParse));
          }
          if (!isValidLanguageDefinition(langAttribute))
          {
            report.message(MessageId.HTM_019, EPUBLocation.create(fileToParse));
          }
        }
        else
        {
          if (xmlLangAttribute == null)
          {
            report.message(MessageId.HTM_020, EPUBLocation.create(fileToParse));
          }
          if (langAttribute == null)
          {
            report.message(MessageId.HTM_021, EPUBLocation.create(fileToParse));
          }
        }
      }
    }
  }

  private boolean isValidLanguageDefinition(String language)
  {
    // ignore language subclasses like en-us or fr-ca.
    int pos = language.indexOf("-");
    if (pos >= 0)
    {
      language = language.substring(0, pos);
    }

    for (String[] langValue : langValues)
    {
      if (language.compareToIgnoreCase(langValue[1]) == 0)
      {
        return true;
      }
    }
    return false;
  }

  private final String[][] langValues = new String[][]
      {
          {"Abkhazian", "ab"},
          {"Afar", "aa"},
          {"Afrikaans", "af"},
          {"Albanian", "sq"},
          {"Amharic", "am"},
          {"Arabic", "ar"},
          {"Aragonese", "an"},
          {"Armenian", "hy"},
          {"Assamese", "as"},
          {"Aymara", "ay"},
          {"Azerbaijani", "az"},
          {"Bashkir", "ba"},
          {"Basque", "eu"},
          {"Bengali (Bangla)", "bn"},
          {"Bhutani", "dz"},
          {"Bihari", "bh"},
          {"Bislama", "bi"},
          {"Breton", "br"},
          {"Bulgarian", "bg"},
          {"Burmese", "my"},
          {"Byelorussian (Belarusian)", "be"},
          {"Cambodian", "km"},
          {"Catalan", "ca"},
          {"Cherokee", " "},
          {"Chewa", " "},
          {"Chinese (Simplified)", "zh"},
          {"Chinese (Traditional)", "zh"},
          {"Corsican", "co"},
          {"Croatian", "hr"},
          {"Czech", "cs"},
          {"Danish", "da"},
          {"Divehi", " "},
          {"Dutch", "nl"},
          {"Edo", " "},
          {"English", "en"},
          {"Esperanto", "eo"},
          {"Estonian", "et"},
          {"Faeroese", "fo"},
          {"Farsi", "fa"},
          {"Fiji", "fj"},
          {"Finnish", "fi"},
          {"Flemish", " "},
          {"French", "fr"},
          {"Frisian", "fy"},
          {"Fulfulde", " "},
          {"Galician", "gl"},
          {"Gaelic (Scottish)", "gd"},
          {"Gaelic (Manx)", "gv"},
          {"Georgian", "ka"},
          {"German", "de"},
          {"Greek", "el"},
          {"Greenlandic", "kl"},
          {"Guarani", "gn"},
          {"Gujarati", "gu"},
          {"Haitian Creole", "ht"},
          {"Hausa", "ha"},
          {"Hawaiian", "haw"},
          {"Hebrew", "he"},
          {"Hindi", "hi"},
          {"Hungarian", "hu"},
          {"Ibibio", " "},
          {"Icelandic", "is"},
          {"Ido", "io"},
          {"Igbo", " "},
          {"Indonesian", "id, in"},
          {"Interlingua", "ia"},
          {"Interlingue", "ie"},
          {"Inuktitut", "iu"},
          {"Inupiak", "ik"},
          {"Irish", "ga"},
          {"Italian", "it"},
          {"Japanese", "ja"},
          {"Javanese", "jv"},
          {"Kannada", "kn"},
          {"Kanuri", " "},
          {"Kashmiri", "ks"},
          {"Kazakh", "kk"},
          {"Kinyarwanda (Ruanda)", "rw"},
          {"Kirghiz", "ky"},
          {"Kirundi (Rundi)", "rn"},
          {"Konkani", " "},
          {"Korean", "ko"},
          {"Kurdish", "ku"},
          {"Laothian", "lo"},
          {"Latin", "la"},
          {"Latvian (Lettish)", "lv"},
          {"Limburgish ( Limburger)", "li"},
          {"Lingala", "ln"},
          {"Lithuanian", "lt"},
          {"Macedonian", "mk"},
          {"Malagasy", "mg"},
          {"Malay", "ms"},
          {"Malayalam", "ml"},
          {" ", " "},
          {"Maltese", "mt"},
          {"Maori", "mi"},
          {"Marathi", "mr"},
          {"Moldavian", "mo"},
          {"Mongolian", "mn"},
          {"Nauru", "na"},
          {"Nepali", "ne"},
          {"Norwegian", "no"},
          {"Occitan", "oc"},
          {"Oriya", "or"},
          {"Oromo (Afaan Oromo)", "om"},
          {"Papiamentu", " "},
          {"Pashto (Pushto)", "ps"},
          {"Polish", "pl"},
          {"Portuguese", "pt"},
          {"Punjabi", "pa"},
          {"Quechua", "qu"},
          {"Rhaeto-Romance", "rm"},
          {"Romanian", "ro"},
          {"Russian", "ru"},
          {"Sami (Lappish)", " "},
          {"Samoan", "sm"},
          {"Sangro", "sg"},
          {"Sanskrit", "sa"},
          {"Serbian", "sr"},
          {"Serbo-Croatian", "sh"},
          {"Sesotho", "st"},
          {"Setswana", "tn"},
          {"Shona", "sn"},
          {"Sichuan Yi", "ii"},
          {"Sindhi", "sd"},
          {"Sinhalese", "si"},
          {"Siswati", "ss"},
          {"Slovak", "sk"},
          {"Slovenian", "sl"},
          {"Somali", "so"},
          {"Spanish", "es"},
          {"Sundanese", "su"},
          {"Swahili (Kiswahili)", "sw"},
          {"Swedish", "sv"},
          {"Syriac", " "},
          {"Tagalog", "tl"},
          {"Tajik", "tg"},
          {"Tamazight", " "},
          {"Tamil", "ta"},
          {"Tatar", "tt"},
          {"Telugu", "te"},
          {"Thai", "th"},
          {"Tibetan", "bo"},
          {"Tigrinya", "ti"},
          {"Tonga", "to"},
          {"Tsonga", "ts"},
          {"Turkish", "tr"},
          {"Turkmen", "tk"},
          {"Twi", "tw"},
          {"Uighur", "ug"},
          {"Ukrainian", "uk"},
          {"Urdu", "ur"},
          {"Uzbek", "uz"},
          {"Venda", " "},
          {"Vietnamese", "vi"},
          {"Volapük", "vo"},
          {"Wallon", "wa"},
          {"Welsh", "cy"},
          {"Wolof", "wo"},
          {"Xhosa", "xh"},
          {"Yi", " "},
          {"Yiddish", "yi, ji"},
          {"Yoruba", "yo"},
          {"Zulu", "zu"},
      };
}
