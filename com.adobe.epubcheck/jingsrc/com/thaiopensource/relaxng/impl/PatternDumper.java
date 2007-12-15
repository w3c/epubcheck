package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.Datatype;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

import com.thaiopensource.xml.util.WellKnownNamespaces;
import com.thaiopensource.xml.util.Name;

public class PatternDumper {
  private boolean startTagOpen = false;
  private final Vector tagStack = new Vector();
  private final PrintWriter writer;
  private int level = 0;
  private boolean suppressIndent = false;
  private final Vector patternList = new Vector();
  private final Hashtable patternTable = new Hashtable();

  private final PatternVisitor patternVisitor = new DumpPatternVisitor();
  private final PatternVisitor groupPatternVisitor = new GroupDumpPatternVisitor();
  private final PatternVisitor choicePatternVisitor = new ChoiceDumpPatternVisitor();
  private final PatternVisitor interleavePatternVisitor = new InterleaveDumpPatternVisitor();
  private final NameClassVisitor nameClassVisitor = new DumpNameClassVisitor();
  private final NameClassVisitor choiceNameClassVisitor = new ChoiceDumpNameClassVisitor();

  static public void dump(PrintWriter writer, Pattern p) {
    new PatternDumper(writer).dump(p);
  }

  static public void dump(OutputStream out, Pattern p) {
    new PatternDumper(new PrintWriter(out)).dump(p);
  }

  private PatternDumper(PrintWriter writer) {
    this.writer = writer;
  }

  private void dump(Pattern p) {
    write("<?xml version=\"1.0\"?>");
    startElement("grammar");
    attribute("xmlns", WellKnownNamespaces.RELAX_NG);
    startElement("start");
    p.accept(groupPatternVisitor);
    endElement();
    for (int i = 0; i < patternList.size(); i++) {
      startElement("define");
      Pattern tem = (Pattern)patternList.elementAt(i);
      attribute("name", getName(tem));
      tem.accept(groupPatternVisitor);
      endElement();
    }
    endElement();
    writer.println();
    writer.flush();
  }

  private String getName(Pattern p) {
    String name = (String)patternTable.get(p);
    if (name == null) {
      name = "p" + patternList.size();
      patternList.addElement(p);
      patternTable.put(p, name);
    }
    return name;
  }

  private void startElement(String name) {
    closeStartTag();
    indent(level);
    write('<');
    write(name);
    push(name);
    startTagOpen = true;
    level++;
  }

  private void closeStartTag() {
    if (startTagOpen) {
      startTagOpen = false;
      write('>');
    }
  }

  private void attribute(String name, String value) {
    write(' ');
    write(name);
    write('=');
    write('"');
    chars(value, true);
    write('"');
  }

  private void data(String str) {
    if (str.length() > 0) {
      closeStartTag();
      chars(str, false);
      suppressIndent = true;
    }
  }

  private void chars(String str, boolean isAttribute) {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      switch (c) {
      case '&':
	write("&amp;");
	break;
      case '<':
	write("&lt;");
	break;
      case '>':
	write("&gt;");
	break;
      case '"':
	if (isAttribute) {
	  write("&quot;");
	  break;
	}
	// fall through
      default:
	write(c);
	break;
      }
    }
  }
      
  private void endElement() {
    --level;
    if (startTagOpen) {
      startTagOpen = false;
      write("/>");
      pop();
    }
    else {
      if (!suppressIndent)
	indent(level);
      write("</");
      write(pop());
      write(">");
    }
    suppressIndent = false;
  }

  private void indent(int level) {
    writer.println();
    for (int i = 0; i < level; i++)
      write("  ");
  }

  private void write(String str) {
    writer.print(str);
  }

  private void write(char c) {
    writer.print(c);
  }

  private void push(String s) {
    tagStack.addElement(s);
  }

  private String pop() {
    String s = (String)tagStack.lastElement();
    tagStack.setSize(tagStack.size() - 1);
    return s;
  }

  class DumpPatternVisitor implements PatternVisitor {
    public void visitEmpty() {
      startElement("empty");
      endElement();
    }

    public void visitNotAllowed() {
      startElement("notAllowed");
      endElement();
    }

    public void visitError() {
      startElement("error");
      endElement();
    }

    public void visitGroup(Pattern p1, Pattern p2) {
      startElement("group");
      p1.accept(groupPatternVisitor);
      p2.accept(groupPatternVisitor);
      endElement();
    }

    public void visitInterleave(Pattern p1, Pattern p2) {
      startElement("interleave");
      p1.accept(interleavePatternVisitor);
      p2.accept(interleavePatternVisitor);
      endElement();
    }

    public void visitChoice(Pattern p1, Pattern p2) {
      startElement("choice");
      p1.accept(choicePatternVisitor);
      p2.accept(choicePatternVisitor);
      endElement();
    }

    public void visitOneOrMore(Pattern p) {
      startElement("oneOrMore");
      p.accept(groupPatternVisitor);
      endElement();
    }

    public void visitElement(NameClass nc, Pattern content) {
      startElement("element");
      nc.accept(nameClassVisitor);
      startElement("ref");
      attribute("name", getName(content));
      endElement();
      endElement();
    }

    public void visitAttribute(NameClass nc, Pattern value) {
      startElement("attribute");
      nc.accept(nameClassVisitor);
      value.accept(patternVisitor);
      endElement();
    }

    public void visitData(Datatype dt) {
      startElement("text");	// XXX
      endElement();
    }

    public void visitDataExcept(Datatype dt, Pattern except) {
      startElement("text");	// XXX
      endElement();
    }

    public void visitValue(Datatype dt, Object obj) {
      startElement("value");
      // XXX dump dt
      // XXX toString will not handle QName
      data(obj.toString());
      endElement();
    }

    public void visitText() {
      startElement("text");
      endElement();
    }

    public void visitList(Pattern p) {
      startElement("list");
      p.accept(groupPatternVisitor);
      endElement();
    }
  }

  class GroupDumpPatternVisitor extends DumpPatternVisitor {
    public void visitGroup(Pattern p1, Pattern p2) {
      p1.accept(this);
      p2.accept(this);
    }
  }

  class ChoiceDumpPatternVisitor extends DumpPatternVisitor {
    public void visitChoice(Pattern p1, Pattern p2) {
      p1.accept(this);
      p2.accept(this);
    }
  }

  class InterleaveDumpPatternVisitor extends DumpPatternVisitor {
    public void visitInterleave(Pattern p1, Pattern p2) {
      p1.accept(this);
      p2.accept(this);
    }
  }


  class DumpNameClassVisitor implements NameClassVisitor {
    public void visitChoice(NameClass nc1, NameClass nc2) {
      startElement("choice");
      nc1.accept(choiceNameClassVisitor);
      nc2.accept(choiceNameClassVisitor);
      endElement();
    }

    public void visitNsName(String ns) {
      startElement("nsName");
      attribute("ns", ns);
      endElement();
    }

    public void visitNsNameExcept(String ns, NameClass nc) {
      startElement("nsName");
      startElement("except");
      nc.accept(choiceNameClassVisitor);
      endElement();
      endElement();
    }

    public void visitAnyName() {
      startElement("anyName");
      endElement();
    }

    public void visitAnyNameExcept(NameClass nc) {
      startElement("anyName");
      startElement("except");
      nc.accept(choiceNameClassVisitor);
      endElement();
      endElement();
    }

    public void visitName(Name name) {
      startElement("name");
      attribute("ns", name.getNamespaceUri());
      data(name.getLocalName());
      endElement();
    }

    public void visitError() {
      startElement("error");
      endElement();
    }
    
    public void visitNull() {
      visitAnyName();
    }
  }

  class ChoiceDumpNameClassVisitor extends DumpNameClassVisitor {
    public void visitChoice(NameClass nc1, NameClass nc2) {
      nc1.accept(this);
      nc2.accept(this);
    }
  }
}
