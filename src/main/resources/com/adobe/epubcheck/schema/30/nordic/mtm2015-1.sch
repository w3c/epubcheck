<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">

    <sch:title>DTBook 2005 Schematron tests for TPB 2010-1 rules</sch:title>

    <sch:ns prefix="dtbk" uri="http://www.daisy.org/z3986/2005/dtbook/"/>

    <!-- Rule 7: No <list> or <dl> inside <p> -->
    <sch:pattern id="dtbook_TPB_7">
        <!--
	p should only allow inline elements. The lists and definition lists inside p
	will be converted to span elements in Daisy 2.02
	-->
        <sch:rule context="dtbk:p">
            <sch:report test="dtbk:list">[tpb07] Lists are not allowed inside paragraphs.</sch:report>
            <sch:report test="dtbk:dl">[tpb07] Definition lists are not allowed inside paragraphs.</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 8: Only allow pagenum[@front] in frontmatter -->
    <sch:pattern id="dtbook_TPB_8">
        <sch:rule context="dtbk:pagenum[@page='front']">
            <sch:assert test="ancestor::dtbk:frontmatter">[tpb08]&lt;pagenum page="front"/&gt; may only occur in &lt;frontmatter/&gt;</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 9: Disallow empty elements (with a few exceptions) -->
    <sch:pattern id="dtbook_TPB_9">
        <sch:rule context="dtbk:*">
            <sch:report
                test="normalize-space(.)='' and not(*) and not(self::dtbk:img or self::dtbk:br or self::dtbk:meta or self::dtbk:link or self::dtbk:col or self::dtbk:th or self::dtbk:td or self::dtbk:dd or self::dtbk:pagenum[@page='special'])"
                >[tpb09] Element may not be empty</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 10: Metadata for dc:Language, dc:Date and dc:Publisher must exist -->
    <sch:pattern id="dtbook_TPB_10">
        <sch:rule context="dtbk:head">
            <!-- dc:Language -->
            <sch:assert test="count(dtbk:meta[@name='dc:Language'])>=1">[tpb10] Meta dc:Language must occur at least once</sch:assert>
            <!-- dc:Date -->
            <sch:assert test="count(dtbk:meta[@name='dc:Date'])=1">[tpb10] Meta dc:Date=YYYY-MM-DD must occur once</sch:assert>
            <sch:report test="dtbk:meta[@name='dc:Date' and translate(@content, '0123456789', '0000000000')!='0000-00-00']">[tpb10] Meta dc:Date must have format YYYY-MM-DD</sch:report>
            <!-- dc:Publisher -->
            <sch:assert test="count(dtbk:meta[@name='dc:Publisher'])=1">[tpb10] Meta dc:Publisher must occur once</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 11: Root element must have @xml:lang -->
    <sch:pattern id="dtbook_TPB_11">
        <sch:rule context="dtbk:dtbook">
            <sch:assert test="@xml:lang">[tpb11] Root element must have an xml:lang attribute</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 12: Frontmatter starts with doctitle and docauthor -->
    <sch:pattern id="dtbook_TPB_12_a">
        <sch:rule context="dtbk:frontmatter">
            <sch:assert test="dtbk:*[1][self::dtbk:doctitle]">[tpb12a] Frontmatter must begin with a doctitle element</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="dtbook_TPB_12_b">
        <sch:rule context="dtbk:frontmatter/dtbk:docauthor">
            <sch:assert test="preceding-sibling::*[self::dtbk:doctitle or self::dtbk:docauthor]">[tpb12b] Docauthor may only be preceded by doctitle</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 13: All documents must have frontmatter and bodymatter -->
    <sch:pattern id="dtbook_TPB_13">
        <sch:rule context="dtbk:book">
            <sch:assert test="dtbk:frontmatter">[tpb13] A document must have frontmatter</sch:assert>
            <sch:assert test="dtbk:bodymatter">[tpb13] A document must have bodymatter</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 14:  Don't allow <h x+1> in <level x+1> unless <h x> in <level x> is present -->
    <sch:pattern id="dtbook_TPB_14_a">
        <sch:rule context="dtbk:level1[dtbk:level2/dtbk:h2]">
            <sch:assert test="dtbk:h1">[tpb14a] level1 with no h1 when level2 is present</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="dtbook_TPB_14_b">
        <sch:rule context="dtbk:level2[dtbk:level3/dtbk:h3]">
            <sch:assert test="dtbk:h2">[tpb14b] level2 with no h2 when level3 is present</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="dtbook_TPB_14_c">
        <sch:rule context="dtbk:level3[dtbk:level4/dtbk:h4]">
            <sch:assert test="dtbk:h3">[tpb14c] level3 with no h3 when level4 is present</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="dtbook_TPB_14_d">
        <sch:rule context="dtbk:level4[dtbk:level5/dtbk:h5]">
            <sch:assert test="dtbk:h4">[tpb14d] level4 with no h4 when level5 is present</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="dtbook_TPB_14_e">
        <sch:rule context="dtbk:level5[dtbk:level6/dtbk:h6]">
            <sch:assert test="dtbk:h5">[tpb14e] level5 with no h5 when level6 is present</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="dtbook_TPB_14_f">
        <sch:rule context="dtbk:level[dtbk:level/dtbk:hd]">
            <sch:assert test="dtbk:hd">[tpb14f] level with no hd when level is present</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 18: Disallow level -->
    <sch:pattern id="dtbook_TPB_18">
        <sch:rule context="dtbk:level">
            <sch:report test="true()">[tpb18] Element level is not allowed</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 20: No imggroup in inline context -->
    <sch:pattern id="dtbook_TPB_20">
        <sch:rule context="dtbk:imggroup">
            <sch:report
                test="ancestor::dtbk:a        or ancestor::dtbk:abbr       or ancestor::dtbk:acronym    or ancestor::dtbk:annoref   or
                          ancestor::dtbk:bdo      or ancestor::dtbk:code       or ancestor::dtbk:dfn        or ancestor::dtbk:em        or
                          ancestor::dtbk:kbd      or ancestor::dtbk:linenum    or ancestor::dtbk:noteref    or ancestor::dtbk:lic       or
                          ancestor::dtbk:q        or ancestor::dtbk:samp       or ancestor::dtbk:sent       or ancestor::dtbk:span      or
                          ancestor::dtbk:strong   or ancestor::dtbk:sub        or ancestor::dtbk:sup        or ancestor::dtbk:w         or
                          ancestor::dtbk:address  or ancestor::dtbk:author     or ancestor::dtbk:bridgehead or ancestor::dtbk:byline    or
                          ancestor::dtbk:cite     or ancestor::dtbk:covertitle or ancestor::dtbk:dateline   or ancestor::dtbk:docauthor or
                          ancestor::dtbk:doctitle or ancestor::dtbk:dt         or ancestor::dtbk:h1         or ancestor::dtbk:h2        or
                          ancestor::dtbk:h3       or ancestor::dtbk:h4         or ancestor::dtbk:h5         or ancestor::dtbk:h6        or
                          ancestor::dtbk:hd       or ancestor::dtbk:line       or ancestor::dtbk:p"
                >[tpb20] Image groups are not allowed in inline context</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 21: No nested tables -->
    <sch:pattern id="dtbook_TPB_21">
        <sch:rule context="dtbk:table">
            <sch:report test="ancestor::dtbk:table">[tpb21] Nested tables are not allowed</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 23: Increasing pagenum[@page='normal'] values -->
    <sch:pattern id="dtbook_TPB_23">
        <sch:rule context="dtbk:pagenum[@page='normal' and preceding::dtbk:pagenum[@page='normal']]">
            <sch:assert test="number(current()) > number(preceding::dtbk:pagenum[@page='normal'][1])">[tpb23] pagenum[@page='normal'] values must increase</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 24: Values of pagenum[@page='front'] must be unique -->
    <sch:pattern id="dtbook_TPB_24">
        <sch:rule context="dtbk:pagenum[@page='front']">
            <sch:assert test="count(//dtbk:pagenum[@page='front' and string(.)=string(current())])=1">[tpb24] pagenum[@page='front'] values must be unique</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 26: Each note must have a noteref -->
    <sch:pattern id="dtbook_TPB_26">
        <sch:rule context="dtbk:note">
            <sch:assert test="count(//dtbk:noteref[translate(@idref, '#', '')=current()/@id])>=1">[tpb26] Each note must have at least one noteref</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 27: Each annotation must have an annoref -->
    <sch:pattern id="dtbook_TPB_27">
        <sch:rule context="dtbk:annotation">
            <sch:assert test="count(//dtbk:annoref[translate(@idref, '#', '')=current()/@id])>=1">[tpb27] Each annotation must have at least one annoref</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 29: No block elements in inline context -->
    <sch:pattern id="dtbook_TPB_29a">
        <sch:rule
            context="dtbk:*[self::dtbk:address    or self::dtbk:annotation or self::dtbk:author   or
  	                          self::dtbk:blockquote or self::dtbk:bridgehead or self::dtbk:caption  or
  	                          self::dtbk:dateline   or self::dtbk:div        or self::dtbk:dl       or
  	                          self::dtbk:docauthor  or self::dtbk:doctitle   or
  	                          self::dtbk:epigraph   or self::dtbk:hd         or self::dtbk:line     or
  	                          self::dtbk:linegroup  or
  	                          self::dtbk:list       or self::dtbk:note       or self::dtbk:p        or
  	                          self::dtbk:poem       or self::dtbk:sidebar    or self::dtbk:table    or
  	                          self::dtbk:title]">
            <sch:report
                test="ancestor::dtbk:a      or ancestor::dtbk:abbr or ancestor::dtbk:acronym or ancestor::dtbk:annoref or
  	                    ancestor::dtbk:bdo    or ancestor::dtbk:code or ancestor::dtbk:dfn     or ancestor::dtbk:em      or
  	                    ancestor::dtbk:kbd or ancestor::dtbk:linenum or ancestor::dtbk:noteref or
  	                    ancestor::dtbk:q      or ancestor::dtbk:samp or ancestor::dtbk:sent    or ancestor::dtbk:span    or
  	                    ancestor::dtbk:strong or ancestor::dtbk:sub  or ancestor::dtbk:sup     or ancestor::dtbk:w"
                >[tpb29] Block element <sch:name/> used in inline context</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 29: No block elements in inline context - continued -->
    <sch:pattern id="dtbook_TPB_29b">
        <sch:rule
            context="dtbk:*[self::dtbk:address    or self::dtbk:annotation or self::dtbk:author   or
  	                          self::dtbk:blockquote or self::dtbk:bridgehead or self::dtbk:caption  or
  	                          self::dtbk:dateline   or self::dtbk:div        or self::dtbk:dl       or
  	                          self::dtbk:epigraph   or self::dtbk:hd         or self::dtbk:linegoup or
  	                          self::dtbk:list       or self::dtbk:note       or self::dtbk:p        or
  	                          self::dtbk:poem       or self::dtbk:sidebar    or self::dtbk:table    or
  	                          self::dtbk:title      or self::dtbk:level      or self::dtbk:level1   or
  	                          self::dtbk:level2     or self::dtbk:level3     or self::dtbk:level4    or
  	                          self::dtbk:level5     or self::dtbk:level6]">
            <sch:report
                test="following-sibling::dtbk:a      or following-sibling::dtbk:abbr or following-sibling::dtbk:acronym or following-sibling::dtbk:annoref or
  	                    following-sibling::dtbk:bdo    or following-sibling::dtbk:code or following-sibling::dtbk:dfn     or following-sibling::dtbk:em      or
  	                    following-sibling::dtbk:kbd or following-sibling::dtbk:linenum or following-sibling::dtbk:noteref or
  	                    following-sibling::dtbk:q      or following-sibling::dtbk:samp or following-sibling::dtbk:sent    or following-sibling::dtbk:span    or
  	                    following-sibling::dtbk:strong or following-sibling::dtbk:sub  or following-sibling::dtbk:sup     or following-sibling::dtbk:w       or
  	                    normalize-space(string-join(following-sibling::text(),''))!=''"
                >[tpb29] Block element as sibling to inline element</sch:report>
            <sch:report
                test="preceding-sibling::dtbk:a      or preceding-sibling::dtbk:abbr or preceding-sibling::dtbk:acronym or preceding-sibling::dtbk:annoref or
  	                    preceding-sibling::dtbk:bdo    or preceding-sibling::dtbk:code or preceding-sibling::dtbk:dfn     or preceding-sibling::dtbk:em      or
  	                    preceding-sibling::dtbk:kbd or preceding-sibling::dtbk:linenum or preceding-sibling::dtbk:noteref or
  	                    preceding-sibling::dtbk:q      or preceding-sibling::dtbk:samp or preceding-sibling::dtbk:sent    or preceding-sibling::dtbk:span    or
  	                    preceding-sibling::dtbk:strong or preceding-sibling::dtbk:sub  or preceding-sibling::dtbk:sup     or preceding-sibling::dtbk:w       or
  	                    normalize-space(string-join(preceding-sibling::text(),''))!=''"
                >[tpb29] Block element <sch:name/> as sibling to inline element</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 29: No block elements in inline context - continued -->
    <sch:pattern id="dtbook_TPB_29c">
        <sch:rule
            context="dtbk:prodnote[ancestor::dtbk:a        or ancestor::dtbk:abbr       or ancestor::dtbk:acronym    or ancestor::dtbk:annoref   or
                                     ancestor::dtbk:bdo      or ancestor::dtbk:code       or ancestor::dtbk:dfn        or ancestor::dtbk:em        or
                                     ancestor::dtbk:kbd      or ancestor::dtbk:linenum    or ancestor::dtbk:noteref    or
                                     ancestor::dtbk:q        or ancestor::dtbk:samp       or ancestor::dtbk:sent       or ancestor::dtbk:span      or
                                     ancestor::dtbk:strong   or ancestor::dtbk:sub        or ancestor::dtbk:sup        or ancestor::dtbk:w         or
                                     ancestor::dtbk:address  or ancestor::dtbk:author     or ancestor::dtbk:bridgehead or ancestor::dtbk:byline    or
                                     ancestor::dtbk:cite     or ancestor::dtbk:covertitle or ancestor::dtbk:dateline   or ancestor::dtbk:docauthor or
                                     ancestor::dtbk:doctitle or ancestor::dtbk:dt         or ancestor::dtbk:h1         or ancestor::dtbk:h2        or
                                     ancestor::dtbk:h3       or ancestor::dtbk:h4         or ancestor::dtbk:h5         or ancestor::dtbk:h6        or
                                     ancestor::dtbk:hd       or ancestor::dtbk:line       or ancestor::dtbk:p]">
            <sch:report
                test="descendant::dtbk:*[self::dtbk:address    or self::dtbk:annotation or self::dtbk:author   or
  	                                       self::dtbk:blockquote or self::dtbk:bridgehead or self::dtbk:caption  or
                                           self::dtbk:dateline   or self::dtbk:div        or self::dtbk:dl       or
                                           self::dtbk:docauthor  or self::dtbk:doctitle   or
                                           self::dtbk:epigraph   or self::dtbk:hd         or self::dtbk:line     or
  	                                       self::dtbk:linegroup  or
                                           self::dtbk:list       or self::dtbk:note       or self::dtbk:p        or
                                           self::dtbk:poem       or self::dtbk:sidebar    or self::dtbk:table    or
                                           self::dtbk:title]"
                >[tpb29] Prodnote in inline context used as block element</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 40: No page numbering gaps for pagenum[@page='normal'] -->
    <sch:pattern id="dtbook_TPB_40">
        <sch:rule context="dtbk:pagenum[@page='normal']">
            <sch:report test="preceding::dtbk:pagenum[@page='normal'] and number(preceding::dtbk:pagenum[@page='normal'][1]) != number(.)-1">[tpb40] No gaps may occur in page numbering</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 43: dc:Publisher must be 'TPB', 'MTM', 'SPSM', 'Nota', 'NLB', 'Celia', 'SBS' or 'Dedicon' -->
    <sch:pattern id="dtbook_TPB_43">
        <sch:rule context="dtbk:head">
            <!-- dc:Publisher -->
            <sch:assert test="dtbk:meta[@name='dc:Publisher' and (@content='TPB' or @content='MTM' or @content='SPSM' or @content='Nota' or @content='NLB' or @content='Celia' or @content='SBS' or @content='Dedicon')]"
                >[tpb43] Meta dc:Publisher must exist and have value 'TPB', 'MTM', 'SPSM', 'Nota', 'NLB', 'Celia', 'SBS' or 'Dedicon'.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 51 & 52: -->
    <sch:pattern id="dtbook_TPB_5152">
        <sch:rule context="dtbk:img">
            <sch:assert test="contains(@src,'.jpg') and substring-after(@src,'.jpg')=''">[tpb52] Images must have the .jpg file extension.</sch:assert>
            <sch:report test="contains(@src,'.jpg') and string-length(@src)=4">[tpb52] Images must have a base name, not just an extension.</sch:report>
            <sch:report test="contains(@src,'/')">[tpb51] Images must be in the same folder as the DTBook file.</sch:report>
            <sch:assert test="string-length(translate(substring(@src,1,string-length(@src)-4),'-_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789',''))=0">[tpb52] Image file name
                contains an illegal character (must be -_a-zA-Z0-9).</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 59: No pagegnum between a term and a definition in definition lists -->
    <sch:pattern id="dtbook_TPB_59">
        <sch:rule context="dtbk:dl/dtbk:pagenum">
            <sch:assert test="preceding-sibling::*[1][self::dtbk:dd] and following-sibling::*[1][self::dtbk:dt]">[tpb59] pagenum in definition list must occur between dd and dt</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 63: Only note references within the same document -->
    <sch:pattern id="dtbook_TPB_63">
        <sch:rule context="dtbk:noteref">
            <sch:assert test="not(contains(@idref, '#')) or starts-with(@idref,'#')">[tpb63] Only note references within the same document are allowed</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 64: Only annotation references within the same document -->
    <sch:pattern id="dtbook_TPB_64">
        <sch:rule context="dtbk:annoref">
            <sch:assert test="not(contains(@idref, '#')) or starts-with(@idref,'#')">[tpb64] Only annotation references within the same document are allowed</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 67: doctitle and docauthor only allowed in frontmatter -->
    <sch:pattern id="dtbook_TPB_67_a">
        <sch:rule context="dtbk:doctitle">
            <sch:assert test="parent::dtbk:frontmatter">[tpb67a] doctitle is only allowed in frontmatter</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="dtbook_TPB_67_b">
        <sch:rule context="dtbk:docauthor">
            <sch:assert test="parent::dtbk:frontmatter">[tpb67b] docauthor is only allowed in frontmatter</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 68: No smilref attributes -->
    <sch:pattern id="dtbook_TPB_68">
        <sch:rule context="dtbk:*">
            <sch:report test="@smilref">[tpb68] smilref attributes in a plain DTBook file is not allowed</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 72: Only allow DTBook 2005-3 -->
    <sch:pattern id="dtbook_TPB_72">
        <sch:rule context="dtbk:dtbook">
            <sch:assert test="@version='2005-3'">[tpb72] DTBook version must be 2005-3.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 93: Some elements may not start of end with whitespace -->
    <sch:pattern id="dtbook_TPB_93">
        <sch:rule context="dtbk:*[self::dtbk:h1 or self::dtbk:h2 or self::dtbk:h3 or self::dtbk:h4 or self::dtbk:h5 or self::dtbk:h6 or self::dtbk:hd]">
            <sch:report test="normalize-space(substring(.,1,1))=''">[tpb93] element <sch:name/> may not have leading whitespace</sch:report>
            <sch:report test="normalize-space(substring(.,string-length(.),1))='' and not(dtbk:* and normalize-space(dtbk:*[last()]/following-sibling::text())='')">[tpb93] element <sch:name/> may not
                have trailing whitespace</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 96: no nested prodnotes or image groups -->
    <sch:pattern id="dtbook_TPB_96_a">
        <sch:rule context="dtbk:prodnote">
            <sch:report test="ancestor::dtbk:prodnote">[tpb96a] nested production notes are not allowed</sch:report>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="dtbook_TPB_96_b">
        <sch:rule context="dtbk:imggroup">
            <sch:report test="ancestor::dtbk:imggroup">[tpb96b] nested image groups are not allowed</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 101: All imggroup elements must have a img element -->
    <sch:pattern id="dtbook_TPB_101">
        <sch:rule context="dtbk:imggroup">
            <sch:assert test="dtbk:img">[tpb101] There must be an img element in every imggroup</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 104: Headings may not be empty elements -->
    <sch:pattern id="dtbook_TPB_104">
        <sch:rule context="dtbk:*[self::dtbk:h1 or self::dtbk:h2 or self::dtbk:h3 or self::dtbk:h4 or self::dtbk:h5 or self::dtbk:h6 or self::dtbk:hd[parent::dtbk:level]]">
            <sch:report test="normalize-space(.)=''">[tpb104] Heading <sch:name/> may not be empty</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 105: Page attribute must appear on all pagenum elements -->
    <sch:pattern id="dtbook_TPB_105">
        <sch:rule context="dtbk:pagenum">
            <sch:assert test="@page">[tpb105] Page attribute must appear on pagenum elements</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 110: pagenum in headings -->
    <sch:pattern id="dtbook_TPB_110">
        <sch:rule context="dtbk:pagenum">
            <sch:report test="ancestor::*[self::dtbk:h1 or self::dtbk:h2 or self::dtbk:h3 or self::dtbk:h4 or self::dtbk:h5 or self::dtbk:h6 or self::dtbk:hd]">[tpb110] pagenum elements are not
                allowed in headings</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 116: Don't allow arabic numbers in pagenum/@page="front" -->
    <sch:pattern id="dtbook_TPB_116">
        <sch:rule context="dtbk:pagenum">
            <sch:report test="@page='front' and translate(.,'0123456789','xxxxxxxxxx')!=.">[tpb116] Arabic numbers in page="front" are not allowed</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 120:  Allow only pagenum before hx in levelx -->
    <sch:pattern id="dtbook_TPB_120">
        <sch:rule context="dtbk:*[self::dtbk:h1 or self::dtbk:h2 or self::dtbk:h3 or self::dtbk:h4 or self::dtbk:h5 or self::dtbk:h6]">
            <sch:assert test="not(preceding-sibling::dtbk:*) or preceding-sibling::dtbk:pagenum">[tpb120] Only pagenum elements are allowed before the heading <sch:name/>.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 121:  pagenum in tables must occur between table rows -->
    <sch:pattern id="dtbook_TPB_121">
        <sch:rule context="dtbk:pagenum[ancestor::dtbk:table]">
            <sch:assert test="preceding-sibling::dtbk:tr or following-sibling::dtbk:tr">[tpb121] Page numbers in tables must be placed between table rows.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 123 (39): No class attributes on level[2-6]. level1 allows 'part', 'nonstandardpagination', 'colophon' (if located in frontmatter) and 'jacketcopy' (if located in frontmatter and immediately after docauthor or doctitle) -->
    <sch:pattern id="dtbook_TPB_123">
        <sch:rule context="dtbk:level1">
            <sch:report test="tokenize(@class,'\s+')='jacketcopy' and (not(parent::dtbk:frontmatter))">[tpb123] Jacket copy must be in frontmatter</sch:report>
            <sch:report test="tokenize(@class,'\s+')='jacketcopy' and (not(preceding-sibling::*[1][self::dtbk:docauthor or self::dtbk:doctitle]))">[tpb123] Jacket copy must follow immediately after docauthor or
                doctitle</sch:report>

            <sch:report test="tokenize(@class,'\s+')='colophon' and parent::dtbk:bodymatter">[tpb123] Colophon is not allowed in bodymatter</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 124 (106): All documents must have at least one pagenum -->
    <sch:pattern id="dtbook_TPB_124">
        <sch:rule context="dtbk:book">
            <sch:assert test="count(//dtbk:pagenum)>=1">[tpb124] All documents must contain page numbers</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 125 (109): Only allow images in JPG format -->
    <sch:pattern id="dtbook_TPB_125">
        <sch:rule context="dtbk:img">
            <sch:assert test="string-length(@src)>=5">[tpb125] Invalid image filename.</sch:assert>
            <sch:assert test="substring(@src,string-length(@src) - 3, 4)='.jpg'">[tpb125] Images must be in JPG (*.jpg) format.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 126: pagenum must not occur directly after hx unless the hx is preceded by a pagenum -->
    <sch:pattern id="dtbook_TPB_126">
        <sch:rule context="dtbk:pagenum">
            <sch:report
                test="preceding-sibling::*[1][self::dtbk:h1 or self::dtbk:h2 or self::dtbk:h3 or self::dtbk:h4 or self::dtbk:h5 or self::dtbk:h6] and
  		                  not(preceding-sibling::*[2][self::dtbk:pagenum])"
                >[tpb126] pagenum must not occur directly after hx unless the hx is preceded by a pagenum</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 127: Table of contents must be inside a level1 -->
    <sch:pattern id="dtbook_TPB_127">
        <sch:rule context="dtbk:list[tokenize(@class,'\s+')='toc']">
            <sch:assert test="parent::dtbk:level1">[tpb127] Table of contents (&lt;list class="toc"&gt;)must be inside a level1</sch:assert>
            <sch:report test="ancestor::dtbk:list[tokenize(@class,'\s+')='toc']">[tpb127] Nested lists in table of contents must not have a 'toc' class</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 128: tracking metadata must exist (track:Guidelines) -->
    <sch:pattern id="dtbook_TPB_128_a">
        <sch:rule context="dtbk:head">
            <sch:assert test="count(dtbk:meta[@name='track:Guidelines'])=1">[tpb128] track:Guidelines metadata must occur once.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="dtbook_TPB_128_b">
        <sch:rule context="dtbk:meta[@name='track:Guidelines']">
            <sch:assert test="@content=('2011-1','2011-2','2015-1')">[tpb128] track:Guidelines metadata value must be 2011-1, 2011-2 or 2015-1.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 130 (44): dc:Language must equal root element xml:lang -->
    <sch:pattern id="dtbook_TPB_130">
        <sch:rule context="dtbk:meta[@name='dc:Language']">
            <sch:assert test="@content=/dtbk:dtbook/@xml:lang">[tpb130] dc:Language metadata must equal the root element xml:lang</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 131 (35): Allowed values in xml:lang -->
    <sch:pattern id="dtbook_TPB_131">
        <sch:rule context="*[@xml:lang]">
            <sch:assert test="matches(@xml:lang,'^[a-z][a-z](-[A-Z][A-Z]+)?$')">[tpb131] xml:lang must match '^[a-z][a-z](-[A-Z][A-Z]+)?$'</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 133: Disallowed elements -->
    <sch:pattern id="dtbook_TPB_133">
        <sch:rule
            context="dtbk:*[self::dtbk:link or self::dtbk:level or self::dtbk:epigraph or self::dtbk:byline or
  	                          self::dtbk:dateline or self::dtbk:cite or self::dtbk:sent or self::dtbk:w or
  	                          self::dtbk:covertitle or self::dtbk:bridgehead or self::dtbk:thead or
  	                          self::dtbk:tfoot or self::dtbk:tbody or self::dtbk:colgroup or self::dtbk:col or
  	                          self::dtbk:address or self::dtbk:annotation or self::dtbk:a or self::dtbk:dfn or
  	                          self::dtbk:kbd or self::dtbk:samp or self::dtbk:abbr or self::dtbk:acronym or
  	                          self::dtbk:q or self::dtbk:bdo or self::dtbk:bdo or self::dtbk:annoref]">
            <sch:assert test="false()">[tpb133] Element <sch:name/> is disallowed</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 134: Disallowed attributes -->
    <sch:pattern id="dtbook_TPB_134_c">
        <sch:rule context="dtbk:meta">
            <sch:report test="@scheme">[tpb134c] Attribute 'scheme' is not allowed on the <sch:name/> element</sch:report>
            <sch:report test="@http-equiv">[tpb134c] Attribute 'http-equiv' is not allowed on the <sch:name/> element</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 135: Poem contents -->
    <sch:pattern id="dtbook_TPB_135_a">
        <sch:rule context="dtbk:*[self::dtbk:title or self::dtbk:author]">
            <sch:assert test="parent::dtbk:poem">[tpb135a] Element <sch:name/> is only allowed in poem context</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="dtbook_TPB_135_b">
        <sch:rule context="dtbk:poem">
            <sch:assert test="dtbk:linegroup">[tpb135b] Every poem must contain a linegroup</sch:assert>
            <sch:report test="dtbk:line">[tpb135b] Poem lines must be wrapped in a linegroup</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 136: List types -->
    <sch:pattern id="dtbook_TPB_136">
        <sch:rule context="dtbk:list">
            <sch:assert test="@type='pl'">[tpb136] Lists must be of type 'pl' (with any bullets or numbers in the text node)</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 137: Language used in unnumbered pages -->
    <sch:pattern id="dtbook_TPB_137">
        <sch:rule context="dtbk:pagenum">
            <sch:report test="@page='special' and .='Onumrerad sida' and lang('en')">[tpb137] Swedish description of unnumbered page used in english context</sch:report>
            <sch:report test="@page='special' and .='Unnumbered page' and lang('sv')">[tpb137] English description of unnumbered page used in swedish context</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 140: Jacket copy must contain at least one prodnote, at most one of each @class value and no other elements -->
    <sch:pattern id="dtbook_TPB_140">
        <sch:rule context="dtbk:level1[tokenize(@class,'\s+')='jacketcopy']">
            <sch:assert test="count(*)=count(dtbk:prodnote)">[tpb140] Only prodnote allowed in jacket copy</sch:assert>
            <sch:assert test="count(dtbk:prodnote)>=1">[tpb140] There must be at least one prodnote in jacket copy</sch:assert>
            <sch:report test="count(dtbk:prodnote[tokenize(@class,'\s+')='frontcover'])>1">[tpb140] Too many prodnotes with @class='frontcover' in jacket copy</sch:report>
            <sch:report test="count(dtbk:prodnote[tokenize(@class,'\s+')='rearcover'])>1">[tpb140] Too many prodnotes with @class='rearcover' in jacket copy</sch:report>
            <sch:report test="count(dtbk:prodnote[tokenize(@class,'\s+')='leftflap'])>1">[tpb140] Too many prodnotes with @class='leftflap' in jacket copy</sch:report>
            <sch:report test="count(dtbk:prodnote[tokenize(@class,'\s+')='rightflap'])>1">[tpb140] Too many prodnotes with @class='rightflap' in jacket copy</sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 141: Prodnotes in jacket copy must contain text and have a @class=['frontcover', 'rearcover', 'leftflap' or 'rightflap'] -->
    <sch:pattern id="dtbook_TPB_141">
        <sch:rule context="dtbk:prodnote[parent::dtbk:level1[tokenize(@class,'\s+')='jacketcopy']]">
            <sch:assert test="count(tokenize(@class,'\s+')[.=('frontcover','rearcover','leftflap','rightflap')]) = 1">[tpb141] prodnote in jacket copy must have a class attribute with one of
                'frontcover', 'rearcover', 'leftflap' or 'rightflap'</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 142: Only @page='special' in level1/@class='nonstandardpagination' -->
    <sch:pattern id="dtbook_TPB_142">
        <sch:rule context="dtbk:pagenum[ancestor::dtbk:level1[tokenize(@class,'\s+')='nonstandardpagination']]">
            <sch:assert test="@page='special'">[tpb142] Only @page='special' is allowed in level1/@class='nonstandardpagination'</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- Rule 143: Don't allow pagenum last in a list -->
    <sch:pattern id="dtbook_TPB_143">
        <sch:rule context="dtbk:pagenum[parent::dtbk:list]">
            <sch:report test="not(following-sibling::*)">[tpb143] pagenum is not allowed last in a list</sch:report>
        </sch:rule>
    </sch:pattern>

</sch:schema>
