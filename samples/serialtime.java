/**
 * Sample program that reads tags for a fixed period of time (500ms)
 * and prints the tags found, while logging the serial message with timestamps.
 */

// Import the API
package samples;
import com.thingmagic.*;
import java.util.Date;

public class serialtime
{
  static SerialPrinter serialPrinter;
  static StringPrinter stringPrinter;
  static TransportListener currentListener;

  static void usage()
  {
    System.out.printf("Usage: demo reader-uri <command> [args]\n" +
                      "  (URI: 'tmr:///COM1' or 'tmr://astra-2100d3/' " +
                      "or 'tmr:///dev/ttyS0')\n\n" +
                      "Available commands:\n");
    System.exit(1);
  }

   public static void setTrace(Reader r, String args[])
  {
    if (args[0].toLowerCase().equals("on"))
    {
        r.addTransportListener(Reader.simpleTransportListener);
        currentListener = Reader.simpleTransportListener;
    }
    else if (currentListener != null)
    {
        r.removeTransportListener(Reader.simpleTransportListener);
    }
  }

   static class SerialPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.print(tx ? "Sending: " : "Received:");
      for (int i = 0; i < data.length; i++)
      {
        if (i > 0 && (i & 15) == 0)
          System.out.printf("\n         ");
        System.out.printf(" %02x", data[i]);
      }
      System.out.printf("\n");
    }
  }

  static class StringPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.println((tx ? "Sending:\n" : "Receiving:\n") +
                         new String(data));
    }
  }
  public static void main(String argv[])
  {
    // Program setup
    TagFilter target;

    Reader r;
    int nextarg;
    boolean trace;

    r = null;
    target = null;
    trace = false;

    nextarg = 0;

    if (argv.length < 1)
      usage();

    if (argv[nextarg].equals("-v"))
    {
      trace = true;
      nextarg++;
    }

    // Create Reader object, connecting to physical device
    try
    {

      TagReadData[] tagReads;

      r = Reader.create(argv[nextarg]);
      if (trace)
      {
        setTrace(r, new String[] {"on"});
      }
      r.connect();
      if (Reader.Region.UNSPEC == (Reader.Region)r.paramGet("/reader/region/id"))
      {
          r.paramSet("/reader/region/id", Reader.Region.NA);
      }

      // Add the serial-reader specific message logger
      r.addTransportListener(new TimestampListener());

      // Read tags
      tagReads = r.read(500);
      // Print tag reads
      for (TagReadData tr : tagReads)
        System.out.println(tr.toString());
      // Shut down reader
      r.destroy();
    } 
    catch (ReaderException re)
    {
      System.out.println("ReaderException: " + re.getMessage());
    }
    catch (Exception re)
    {
        System.out.println("Exception: " + re.getMessage());
    }
  }

  static class TimestampListener implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      
      System.out.printf("%d %s\n  ",
                        new Date().getTime(),
                        tx ? "Sending" : "Received");
      for (int i = 0; i < data.length; i++)
      {
        if (i > 0 && (i & 15) == 0)
          System.out.printf("\n  ");
        System.out.printf(" %02x", data[i]);
      }
      System.out.printf("\n");
    }
      
  }

}

