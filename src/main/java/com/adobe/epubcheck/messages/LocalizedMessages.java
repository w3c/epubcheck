package com.adobe.epubcheck.messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.adobe.epubcheck.api.Report;

/**
 * Manages storage, caching and retrieval of default localized messages.
 */
public class LocalizedMessages
{

  private final Locale locale;
  private final ResourceBundle bundle;
  // Collection (static) will contain one LocalizedMessages instance for each
  // Locale that has been requested.
  private static final Map<String, LocalizedMessages> localizedMessages = new HashMap<String, LocalizedMessages>();
  // Messages are lazily instantiated and cached as they are requested.
  private final Map<MessageId, Message> cachedMessages = new EnumMap<MessageId, Message>(MessageId.class);
  private final Severities defaultSeverities = new DefaultSeverities();

  /**
   * Provides messages for the default locale.
   *
   * @return Localized messages.
   */
  public static LocalizedMessages getInstance()
  {
    return getInstance(null);
  }

  /**
   * Provides messages for the given locale.
   *
   * @param locale The locale. If null or unsupported, will use the default
   * locale instead.
   * @return Localized messages.
   */
  public static LocalizedMessages getInstance(Locale locale)
  {
    LocalizedMessages instance = null;

    if (locale == null)
    {
      locale = Locale.getDefault();
    }

    String localeKey = locale.getLanguage();
    if (localizedMessages.containsKey(localeKey))
    {
      instance = localizedMessages.get(localeKey);
    } 
    else
    {
      synchronized (LocalizedMessages.class)
      {
        if (instance == null)
        {
          instance = new LocalizedMessages(locale);
          localizedMessages.put(localeKey, instance);
        }
      }
    }

    return instance;
  }

  /**
   * Gets the message for the given id.
   *
   * @param id
   * @return A Message object, using the localized string if necessary.
   */
  public Message getMessage(MessageId id)
  {
    // Performance note: this method uses a lazy initialization pattern. When
    // a MessageId is first requested, we fetch the data from the ResourceBundle
    // and create a new Message object, which is then cached. On the next 
    // request, we'll use the cached version instead. 
    Message message;
    if (cachedMessages.containsKey(id))
    {
      message = cachedMessages.get(id);
    } 
    else
    {
      message = new Message(id, defaultSeverities.get(id), getMessageAsString(id), getSuggestion(id));
      cachedMessages.put(id, message);
    }

    return message;
  }

  /**
   * Typical pattern for instantiation should use the static getInstance() methods
   * to ensure that cached objects are used. If that behavior isn't desired,
   * direct instantiation is also an option using this constructor.
   * @param locale The locale used to localize the messages, or default.
   */
  public LocalizedMessages(Locale locale)
  {
    this.locale = (locale != null) ? locale : Locale.getDefault();
    bundle = ResourceBundle.getBundle(
      "com.adobe.epubcheck.messages.MessageBundle", this.locale, new LocalizedMessages.UTF8Control());
  }

  private String getStringFromBundle(String id)
  {
    String result = "";
    try
    {
      result = bundle.getString(id);
    } 
    catch (Exception ignore)
    {
      // Might not exist
    }

    return result;
  }

  private String getMessageAsString(MessageId id)
  {
    return getStringFromBundle(id.name());
  }



  /**
   * Returns the localized string for the message, formatted with the given
   * arguments.
   *
   * If the message ID is declared as having a localized argument (see
   * {@link MessageId#hasLocalizedArgument()}). Such a localized argument has
   * its own {@link ResourceBundle} key, of the form `MSG_ID.keyword`, and can
   * have its own formatting arguments.
   *
   * This enables using a single message ID for a set of checks of the same
   * nature, with a shared wording pattern but with a localized changing part.
   *
   * When reporting a message with a localized argument (typically with
   * {@link Report#message(MessageId, com.adobe.epubcheck.api.EPUBLocation, Object...)}),
   * the first argument matching a keyword associated with the message ID in the
   * resource bundle is replaced with its string value from the resource bundle.
   *
   * Any remaining arguments will be used to format the localized argument
   * itself. As a consequence, a localized argument must always be the last
   * parameter in the primary format string.
   *
   * For example, all the obsolete features can be reported with the message
   * "Usage of %1$s is obsolete", where `%1$s` is replaced by the localized name
   * of the obsolete feature.
   *
   * This method is used internally by {@link Message#getMessage(Object...)},
   * when formatting the localized message.
   *
   * Example:
   *
   * Given the message code `XXX_001` with the following strings in the resource
   * bundle:
   *
   * ```
   * XXX_001=this is a message with %1$s and %2$s
   * XXX_001.key_a=variation A
   * XXX_001.key_b=variation B (%1$s)
   * ```
   * A call to:
   *
   * - `report.message(XXX_001, location, "an argument", "another argument")`
   * returns "this is a message with an argument and another argument"
   * (localized arguments are not used)
   * - `report.message(XXX_001, location, "an argument", "key_a")`
   * returns "this is a message with an argument and variation A"
   * - `report.message(XXX_001, location, "an argument", "key_b", "nice!")`
   * returns "this is a message with an argument and variation B (nice!)"
   * - `report.message(XXX_001, location, "key_a", "another argument")`
   * returns "this is a message with %1$s and %2$s !!! Format specifier '%2$s'"
   * as "another argument" is considered an argument for "key_a" and not used
   * to format the main XXX_001 message.
   *
   * @param message
   *        the message holding the string to format
   * @param args
   *        arguments referenced by the format specifiers, or a keyword used
   *        to lookup a localized argument for the message ID
   * @return the formatted string for the given message
   */
  public String formatMessage(Message message, Object... args)
  {
    try
    {
      // if the message has a localized argument, pre-process the arguments
      // by replacing it by its bundle value
      if (message.getID().hasLocalizedArgument())
      {
        for (int i = 0; i < args.length; i++)
        {
          Object arg = args[i];
          String key = (arg == null) ? null : message.getID().name() + "." + arg;
          if (arg != null && bundle.containsKey(key))
          {
            // We found the localized argument, get it from the bundle
            String localizedArg = bundle.getString(key);
            // If there are remaining args, use them to format
            // the localized argument
            if (i < args.length - 1)
            {
              localizedArg = String.format(localizedArg,
                  Arrays.copyOfRange(args, i + 1, args.length));
            }
            // create the new argument array
            args[i] = localizedArg;
            args = Arrays.copyOf(args, i + 1);
            break;
          }
        }
      }
      // Finally, format the message
      return String.format(message.getMessage(), args);
    } catch (IllegalFormatException e)
    {
      return message.getMessage() + " !!! " + e.getMessage();
    }
  }

  /**
   * Returns the suggestion message for the given message ID.
   * In other words, for a message ID of `XXX_NNN`,
   * returns the bundle message named `XXX_NNN_SUG`.
   * 
   * @param id a message ID
   * @return the associated suggestion, or the empty string if there's none.
   */
  public String getSuggestion(MessageId id)
  {
    return getStringFromBundle(id.name() + "_SUG");
  }


  /**
   * Returns the suggestion message for the given message ID and key.
   * In other words, for a message ID of `XXX_NNN`, and a key `key`,
   * returns the bundle message named `XXX_NNN_SUG.key`.
   * If the suggestion key is not found, returns the bundle message 
   * named `XXX_NNN_SUG.default`.
   * If this latter is not found, returns the bundle message nameed
   * `XXX_NNN_SUG`.
   * 
   * @param id a message ID
   * @param key the key of a specific suggestion string
   * @return the associated suggestion string 
   */
  public String getSuggestion(MessageId id, String key)
  {
    String messageKey = id.name() + "_SUG." + key;
    String messageDefaultKey = id.name() + "_SUG.default";
    return bundle.containsKey(messageKey) ? getStringFromBundle(messageKey)
        : (bundle.containsKey(messageDefaultKey) ? getStringFromBundle(messageDefaultKey)
            : getSuggestion(id));
  }

  public static class UTF8Control extends ResourceBundle.Control
  {

    @Override
    public ResourceBundle newBundle(
            String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            boolean reload) throws IllegalAccessException,
            InstantiationException,
            IOException
    {
      // The below is a copy of the default implementation.
      String bundleName = toBundleName(baseName, locale);
      String resourceName = toResourceName(bundleName, "properties"); //$NON-NLS-1$
      ResourceBundle bundle = null;
      InputStream stream = null;
      if (reload)
      {
        URL url = loader.getResource(resourceName);
        if (url != null)
        {
          URLConnection connection = url.openConnection();
          if (connection != null)
          {
            connection.setUseCaches(false);
            stream = connection.getInputStream();
          }
        }
      } else
      {
        stream = loader.getResourceAsStream(resourceName);
      }
      if (stream != null)
      {
        try
        {
          // Only this line is changed to make it to read properties files as
          // UTF-8.
          bundle = new PropertyResourceBundle(
                  new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
        } finally
        {
          stream.close();
        }
      }
      return bundle;
    }
  }
}
