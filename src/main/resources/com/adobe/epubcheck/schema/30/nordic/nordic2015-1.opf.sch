<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">

    <title>Nordic EPUB3 Package Document rules</title>

    <ns prefix="opf" uri="http://www.idpf.org/2007/opf"/>
    <ns prefix="dc" uri="http://purl.org/dc/elements/1.1/"/>
    <ns prefix="epub" uri="http://www.idpf.org/2007/ops"/>
    <ns prefix="nordic" uri="http://www.mtm.se/epub/"/>
    <ns prefix="xs" uri="http://www.w3.org/2001/XMLSchema"/>

    <pattern id="opf_nordic_1">
        <rule context="/*">
            <assert test="ends-with(base-uri(/*),'.opf')">[opf1] the OPF file must have the extension .opf</assert>
            <assert test="matches(base-uri(/*),'.*/package.opf')">[opf1] the filename of the OPF must be package.opf</assert>
            <assert test="matches(base-uri(/*),'EPUB/package.opf')">[opf1] the OPF must be contained in a folder named EPUB</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_2">
        <rule context="opf:package">
            <assert test="@version = '3.0'">[opf2] the version attribute must be 3.0</assert>
            <assert test="@unique-identifier = 'pub-identifier'">[opf2] on the package element; the unique-identifier-attribute must be present and equal 'pub-identifier'</assert>
            <assert test="namespace-uri-for-prefix('dc',.) = 'http://purl.org/dc/elements/1.1/'">[opf2] on the package element; the dublin core namespace (xmlns:dc="http://purl.org/dc/elements/1.1/")
                must be declared on the package element</assert>
            <assert test="@prefix = 'nordic: http://www.mtm.se/epub/'">[opf2] on the package element; the prefix attribute must declare the nordic metadata namespace (prefix="nordic:
                http://www.mtm.se/epub/")</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_3">
        <rule context="opf:metadata">
            <assert test="count(dc:identifier) = 1">[opf3a] there must be exactly one dc:identifier element</assert>
            <assert test="parent::opf:package/@unique-identifier = dc:identifier/@id">[opf3a] the id of the dc:identifier must equal the value of the package elements unique-identifier
                attribute</assert>
            <assert test="count(dc:identifier) = 1 and matches(dc:identifier/text(),'^[A-Za-z0-9].*$')">[opf3a] The identifier ("<value-of select="dc:identifier/text()"/>") must start with a upper- or
                lower-case letter (A-Z or a-z), or a digit (0-9).</assert>
            <assert test="count(dc:identifier) = 1 and matches(dc:identifier/text(),'^.*[A-Za-z0-9]$')">[opf3a] The identifier ("<value-of select="dc:identifier/text()"/>") must end with a upper- or
                lower-case letter (A-Z or a-z), or a digit (0-9).</assert>
            <assert test="count(dc:identifier) = 1 and matches(dc:identifier/text(),'^[A-Za-z0-9_-]*$')">[opf3a] The identifier ("<value-of select="dc:identifier/text()"/>") must only contain upper-
                or lower-case letters (A-Z or a-z), digits (0-9), dashes (-) and underscores (_).</assert>

            <assert test="count(dc:title[not(@refines)]) = 1">[opf3b] exactly one dc:title <value-of select="if (dc:title[@refines]) then '(without a &quot;refines&quot; attribute)' else ''"/> must be
                present in the package document.</assert>
            <assert test="string-length(normalize-space(dc:title[not(@refines)]/text()))">[opf3b] the dc:title <value-of
                    select="if (dc:title[@refines]) then '(without a &quot;refines&quot; attribute)' else ''"/> must not be empty.</assert>

            <assert test="count(dc:language[not(@refines)]) = 1">[opf3c] exactly one dc:language <value-of select="if (dc:language[@refines]) then '(without a &quot;refines&quot; attribute)' else ''"
                /> must be present in the package document.</assert>
            <assert test="count(dc:language[not(@refines)]) = 1 and matches(dc:language[not(@refines)]/text(), '^[a-z][a-z](-[A-Z][A-Z])?$')">[opf3c] the language code ("<value-of
                    select="dc:language[not(@refines)]/text()"/>") must be either a "two-letter lower case" code or a "two-letter lower case + hyphen + two-letter upper case" code.</assert>
            <!--<assert test="dc:language = ('no','nn-NO','nb-NO','sv','sv-FI','fi','da','en','de','de-CH','fr')" flag="warning">the language code should be one of: 'no' (Norwegian), 'nn-NO' (Norwegian
                Nynorsk), 'nb-NO' (Norwegian Bokmål), 'sv' (Swedish), 'sv-FI' (Swedish (Finland)), 'fi' (Finnish), 'da' (Danish), 'en' (English), 'de' (German), 'de-CH' (German (Switzerland)), 'fr'
                (French)</assert>-->

            <assert test="count(dc:date[not(@refines)]) = 1">[opf3d] exactly one dc:date <value-of select="if (dc:date[@refines]) then '(without a &quot;refines&quot; attribute)' else ''"/> must be
                present</assert>
            <assert test="count(dc:date[not(@refines)]) = 1 and matches(dc:date[not(@refines)], '\d\d\d\d-\d\d-\d\d')">[opf3d] the dc:date (<value-of select="dc:date/text()"/>) must be of the format
                YYYY-MM-DD (year-month-day)</assert>

            <assert test="count(dc:publisher[not(@refines)]) = 1">[opf3e] exactly one dc:publisher <value-of
                    select="if (dc:publisher[@refines]) then '(without a &quot;refines&quot; attribute)' else ''"/> must be present</assert>
            <assert test="count(dc:publisher[not(@refines)]) = 1 and dc:publisher[not(@refines)]/normalize-space(text())">[opf3e] the dc:publisher cannot be empty</assert>
            <!--<assert test="dc:publisher[not(@refines)] = ('TPB','MTM','SPSM','Nota','NLB','Celia','SBS')" flag="warning">the publisher should be one of:
                'TPB','MTM','SPSM','Nota','NLB','Celia','SBS'</assert>-->

            <assert test="count(opf:meta[@property='dcterms:modified' and not(@refines)]) = 1">[opf3f] exactly one last modified date <value-of
                    select="if (opf:meta[@property='dcterms:modified' and @refines]) then '(without a &quot;refines&quot; attribute)' else ''"/> must be present</assert>
            <assert
                test="count(opf:meta[@property='dcterms:modified' and not(@refines)]) = 1 and matches(opf:meta[@property='dcterms:modified' and not(@refines)]/text(), '\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\dZ')"
                >[opf3f] the last modified date (<value-of select="opf:meta[@property='dcterms:modified' and not(@refines)]/text()"/>) must use UTC time and be on the form "CCYY-MM-DDThh:mm:ssZ"
                (year-month-date "T" hour:minute:second "Z")</assert>

            <assert test="count(dc:creator[not(@refines)]) &gt;= 1">[opf3g] at least dc:creator (i.e. book author) <value-of
                    select="if (dc:creator[@refines]) then '(without a &quot;refines&quot; attribute)' else ''"/> must be present</assert>

            <!-- dc:contributor not required -->

            <assert test="count(dc:source[not(@refines)]) = 1">[opf3h] exactly one dc:source <value-of select="if (dc:source[@refines]) then '(without a &quot;refines&quot; attribute)' else ''"/> must
                be present</assert>
            <assert test="count(dc:source[not(@refines)]) = 1 and (starts-with(dc:source[not(@refines)],'urn:isbn:') or starts-with(dc:source[not(@refines)],'urn:issn:'))">[opf3h] the dc:source
                    ("<value-of select="dc:source[not(@refines)]/text()"/>") must start with 'urn:isbn:' or 'urn:issn'</assert>
            <assert test="count(dc:source[not(@refines)]) = 1 and matches(dc:source[not(@refines)],'^urn:is[bs]n:[\d-]+X?$')">[opf3h] the ISBN or ISSN in dc:source ("<value-of
                    select="dc:source[not(@refines)]/text()"/>") can only contain numbers and hyphens, in addition to the 'urn:isbn:' or 'urn:issn:' prefix. The last digit can also be a 'X' in some
                ISBNs.</assert>

            <assert test="count(opf:meta[@property='nordic:guidelines' and not(@refines)]) = 1">[opf3i] there must be exactly one meta element with the property "nordic:guidelines" <value-of
                    select="if (opf:meta[@property='nordic:guidelines' and @refines]) then '(without a &quot;refines&quot; attribute)' else ''"/></assert>
            <assert test="opf:meta[@property='nordic:guidelines' and not(@refines)] = '2015-1'">[opf3i] the value of nordic:guidelines must be '2015-1'</assert>

            <assert test="count(opf:meta[@property='nordic:supplier' and not(@refines)]) = 1">[opf3j] there must be exactly one meta element with the property "nordic:supplier" <value-of
                    select="if (opf:meta[@property='nordic:supplier' and @refines]) then '(without a &quot;refines&quot; attribute)' else ''"/></assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_4">
        <rule context="opf:meta[@property and not(@refines)]">
            <assert test="parent::*/opf:meta/@name = @property">[opf4] all EPUB3 meta elements <value-of
                    select="if (parent::*/opf:meta[@refines]) then '(without a &quot;refines&quot; attribute)' else ''"/> must have an equivalent OPF2 meta element (&lt;meta name="<value-of
                    select="@property"/>" content="<value-of select="text()"/>"/&gt;)</assert>
            <assert test="parent::*/opf:meta[@name = current()/@property]/string(@content) = string(.)">[opf4] the value of the EPUB3 meta elements must equal their equivalent OPF2 meta elements. The
                EPUB3 meta element is <value-of select="concat('&lt;',name(),' property=&quot;',@property,'&quot;&gt;',text(),'&lt;/',name(),'&gt;')"/> while the OPF2 element is <value-of
                    select="(parent::*/opf:meta[@name = current()/@property])[1]/concat('&lt;',name(),' name=&quot;',@name,'&quot; content=&quot;',@content,'&quot;/&gt;')"/></assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_5_a">
        <rule context="opf:manifest">
            <assert test="opf:item[@media-type='application/x-dtbncx+xml']">[opf5a] a NCX must be present in the manifest (media-type="application/x-dtbncx+xml")</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_5_b">
        <rule context="opf:item[@media-type='application/x-dtbncx+xml']">
            <assert test="@href = 'nav.ncx'">[opf5b] the NCX must be located in the same directory as the package document, and must be named "nav.ncx" (not "<value-of select="@href"/>")</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_6">
        <rule context="opf:spine">
            <assert test="@toc">[opf6] the toc attribute must be present</assert>
            <assert test="/opf:package/opf:manifest/opf:item/@id = @toc">[opf6] the toc attribute must refer to an item in the manifest</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_7">
        <rule context="opf:item[@media-type='application/xhtml+xml' and tokenize(@properties,'\s+')='nav']">
            <assert test="@href = 'nav.xhtml'">[opf7] the Navigation Document must be located in the same directory as the package document, and must be named 'nav.xhtml' (not "<value-of
                    select="@href"/>")</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_8">
        <rule context="opf:item[starts-with(@media-type,'image/')]">
            <assert test="matches(@href,'^images/[^/]+$')">[opf8] all images must be stored in the "images" directory (which is a subdirectory relative to the package document). The image file
                    "<value-of select="replace(@href,'.*/','')"/>" is located in "<value-of select="replace(@href,'[^/]+$','')"/>".</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_9">
        <rule context="opf:item[@media-type='application/xhtml+xml' and not(tokenize(@properties,'\s+')='nav')]">
            <report test="contains(@href,'/')">[opf9] all content files must be located in the same directory as the package document. The content file file "<value-of select="replace(@href,'.*/','')"
                />" is located in "<value-of select="replace(@href,'[^/]+$','')"/>".</report>
        </rule>
    </pattern>

    <pattern id="opf_nordic_10">
        <rule context="opf:itemref[../../opf:manifest/opf:item[@media-type='application/xhtml+xml' and ends-with(@href,'-cover.xhtml')]/@id = @idref]">
            <assert test="@linear = 'no'">[opf10] Cover must be marked as secondary in the spine (i.e. set linear="no" on the itemref with idref="<value-of select="@idref"/>", which refers to the
                cover)</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_11">
        <rule context="opf:itemref[../../opf:manifest/opf:item[@media-type='application/xhtml+xml' and ends-with(@href,'-rearnotes.xhtml')]/@id = @idref]">
            <assert test="@linear = 'no'">[opf11] Rearnotes must be marked as secondary in the spine (i.e. set linear="no" on the itemref with idref="<value-of select="@idref"/>, which refers to the
                rearnote)</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_12_a">
        <rule context="opf:item[@media-type='application/xhtml+xml' and not(@href='nav.xhtml' or tokenize(@properties,'\s+')='nav')]">
            <assert test="matches(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+(-\d+)?\.xhtml$')">[opf12a] The content document "<value-of select="@href"/>" has a bad filename. Content documents must match the
                "[dc:identifier]-[position in spine]-[epub:type].xhtml" file naming convention. Example: "DTB123-01-cover.xhtml". The identifier are allowed to contain the upper- and lower-case
                characters A-Z and a-z as well as digits (0-9), dashes (-) and underscores (_). The position is a positive whole number consisting of the digits 0-9. The epub:type must be all
                lower-case characters (a-z) and can contain a dash (-). An optional positive whole number (digits 0-9) can be added after the epub:type to be able to easily tell different files with
                the same epub:type apart. For instance: "DTB123-13-chapter-7.xhtml".</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_12_b">
        <rule context="opf:item[@media-type='application/xhtml+xml' and not(@href='nav.xhtml' or tokenize(@properties,'\s+')='nav') and matches(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+(-\d+)?\.xhtml$')]">
            <let name="identifier" value="replace(@href,'^([A-Za-z0-9_-]+)-\d+-[a-z-]+(-\d+)?\.xhtml$','$1')"/>
            <let name="position" value="replace(@href,'^[A-Za-z0-9_-]+-(\d+)-[a-z-]+(-\d+)?\.xhtml$','$1')"/>
            <let name="type" value="replace(@href,'^[A-Za-z0-9_-]+-\d+-([a-z-]+)(-\d+)?\.xhtml$','$1')"/>
            <let name="number" value="if (matches(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+-\d+\.xhtml$')) then replace(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+-(\d+)\.xhtml$','$1') else ''"/>
            <let name="vocab-default"
                value="('cover','frontmatter','bodymatter','backmatter','volume','part','chapter','subchapter','division','abstract','foreword','preface','prologue','introduction','preamble','conclusion','epilogue','afterword','epigraph','toc','toc-brief','landmarks','loa','loi','lot','lov','appendix','colophon','credits','keywords','index','index-headnotes','index-legend','index-group','index-entry-list','index-entry','index-term','index-editor-note','index-locator','index-locator-list','index-locator-range','index-xref-preferred','index-xref-related','index-term-category','index-term-categories','glossary','glossterm','glossdef','bibliography','biblioentry','titlepage','halftitlepage','copyright-page','seriespage ','acknowledgments','imprint','imprimatur','contributors','other-credits','errata','dedication','revision-history','case-study','help','marginalia','notice','pullquote','sidebar','warning','halftitle','fulltitle','covertitle','title','subtitle','label','ordinal','bridgehead','learning-objective','learning-objectives','learning-outcome','learning-outcomes','learning-resource','learning-resources','learning-standard','learning-standards','answer','answers','assessment','assessments','feedback','fill-in-the-blank-problem','general-problem','qna','match-problem','multiple-choice-problem','practice','practices','question','true-false-problem','panel','panel-group','balloon','text-area','sound-area','annotation','note','footnote','rearnote','footnotes','rearnotes','annoref','biblioref','glossref','noteref','referrer','credit','keyword','topic-sentence','concluding-sentence','pagebreak','page-list','table','table-row','table-cell','list','list-item','figure')"/>
            <let name="vocab-z3998"
                value="('fiction','non-fiction','article','essay','textbook','catalogue','frontmatter','bodymatter','backmatter','volume','part','chapter','subchapter','division','section','subsection','foreword','preface','prologue','introduction','preamble','conclusion','epilogue','afterword','toc','appendix','glossary','bibliography','discography','filmography','index','colophon','title','halftitle','fulltitle','subtitle','covertitle','published-works','title-page','halftitle-page','acknowledgments','imprint','imprimatur','loi','lot','publisher-address','publisher-address','editorial-note','grant-acknowledgment','contributors','other-credits','biographical-note','translator-note','errata','promotional-copy','dedication','pgroup','example','epigraph','annotation','introductory-note','commentary','clarification','correction','alteration','presentation','production','attribution','author','editor','general-editor','commentator','translator','republisher','structure','geographic','postal','email','ftp','http','ip','aside','sidebar','practice','notice','warning','marginalia','help','drama','scene','stage-direction','dramatis-personae','persona','actor','role-description','speech','diary','diary-entry','figure','plate','gallery','letter','sender','recipient','salutation','valediction','postscript','email-message','to','from','cc','bcc','subject','collection','orderedlist','unorderedlist','abbreviations','timeline','note','footnotes','footnote','rearnote','rearnotes','verse','poem','song','hymn','lyrics','text','phrase','keyword','sentence','topic-sentence','concluding-sentence','t-form','v-form','acronym','initialism','truncation','cardinal','ordinal','ratio','percentage','phone','isbn','currency','postal-code','result','fraction','mixed','decimal','roman','weight','measure','coordinate','range','result','place','nationality','organization','taxonomy','product','event','award','personal-name','given-name','surname','family-name','name-title','signature','word','compound','homograph','portmanteau','root','stem','prefix','suffix','morpheme','phoneme','grapheme','illustration','photograph','decorative','publisher-logo','frontispiece','reference','resolving-reference','noteref','annoref','citation','nonresolving-citation','continuation','continuation-of','pagebreak','page-header','page-footer','recto','verso','image-placeholder','primary','secondary','tertiary')"/>

            <assert test="$identifier = ../../opf:metadata/dc:identifier/text()">[opf12b_identifier] The "identifier" part of the filename ("<value-of select="$identifier"/>") must be the same as
                declared in metadata, i.e.: "<value-of select="../../opf:metadata/dc:identifier/text()"/>".</assert>

            <assert test="$type = ($vocab-default, $vocab-z3998)">[opf12b_type] "<value-of select="$type"/>" is not a valid type. <value-of
                    select="if (count(($vocab-default,$vocab-z3998)[starts-with(.,substring($type,1,3))])) then concat('Did you mean &quot;',(($vocab-default,$vocab-z3998)[starts-with(.,substring($type,1,3))])[1],'&quot;?') else ''"
                /> The filename of content documents must end with a epub:type defined in either the EPUB3 Structural Semantics Vocabulary (http://www.idpf.org/epub/vocab/structure/#) or the
                Z39.98-2012 Structural Semantics Vocabulary (http://www.daisy.org/z3998/2012/vocab/structure/).</assert>

            <assert
                test="not(count(../opf:item[@media-type='application/xhtml+xml' and not(@href='nav.xhtml' or tokenize(@properties,'\s+')='nav') and not(matches(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+(-\d+)?\.xhtml$'))])) and string-length($position) = string-length(../opf:item[@media-type='application/xhtml+xml' and not(@href='nav.xhtml' or tokenize(@properties,'\s+')='nav') and matches(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+(-\d+)?\.xhtml$')][1]/replace(@href,'^[A-Za-z0-9_-]+-(\d+)-[a-z-]+(-\d+)?.xhtml$','$1'))"
                >[opf12b_position] The numbering of the content documents must all have the equal number of digits.</assert>

            <report
                test="not(count(../opf:item[@media-type='application/xhtml+xml' and not(@href='nav.xhtml' or tokenize(@properties,'\s+')='nav') and not(matches(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+(-\d+)?\.xhtml$'))])) and number($position) = ( (../opf:item except .)[@media-type='application/xhtml+xml' and not(@href='nav.xhtml' or tokenize(@properties,'\s+')='nav') and matches(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+(-\d+)?\.xhtml$')]/number(replace(@href,'^[A-Za-z0-9_-]+-(\d+)-[a-z-]+(-\d+)?.xhtml$','$1')) )"
                >[opf12b_position] The numbering of the content documents must be unique for each content document. <value-of select="$position"/> is also used by another content document in the
                OPF.</report>

            <assert
                test="not(count(../opf:item[@media-type='application/xhtml+xml' and not(@href='nav.xhtml' or tokenize(@properties,'\s+')='nav') and not(matches(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+(-\d+)?\.xhtml$'))])) and number($position)-1 = ( 0 , (../opf:item except .)[@media-type='application/xhtml+xml' and not(@href='nav.xhtml' or tokenize(@properties,'\s+')='nav') and matches(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+(-\d+)?\.xhtml$')]/number(replace(@href,'^[A-Za-z0-9_-]+-(\d+)-[a-z-]+(-\d+)?.xhtml$','$1')) )"
                >[opf12b_position] The numbering of the content documents must start at 1 and increase with 1 for each item.</assert>

            <assert
                test="not(count(../opf:item[@media-type='application/xhtml+xml' and not(@href='nav.xhtml' or tokenize(@properties,'\s+')='nav') and not(matches(@href,'^[A-Za-z0-9_-]+-\d+-[a-z-]+(-\d+)?\.xhtml$'))])) and ../../opf:spine/opf:itemref[xs:integer(number($position))]/@idref = @id"
                >[opf12b_position] The <value-of select="xs:integer(number($position))"/><value-of
                    select="if (ends-with($position,'1') and not(number($position)=11)) then 'st' else if (ends-with($position,'2') and not(number($position)=12)) then 'nd' else if (ends-with($position,'3') and not(number($position)=13)) then 'rd' else 'th'"
                /> itemref (&lt;iremref id="<value-of select="../../opf:spine/opf:itemref[xs:integer(number($position))]/@id"/>" href="..."&gt;) should refer to &lt;item href="<value-of select="@href"
                />"&gt;.</assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_13">
        <rule context="opf:item[@media-type='application/xhtml+xml' and @href='nav.xhtml']">
            <assert test="tokenize(@properties,'\s+')='nav'">[opf13] the Navigation Document must be identified with the attribute properties="nav" in the OPF manifest. It currently <value-of
                    select="if (not(@properties)) then 'does not have a &quot;properties&quot; attribute' else concat('has the properties: ',string-join(tokenize(@properties,'\s+'),', '),', but not &quot;nav&quot;')"
                /></assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_14">
        <rule context="opf:itemref">
            <let name="itemref" value="."/>
            <report test="count(//opf:item[@id=$itemref/@idref and (tokenize(@properties,'\s+')='nav' or @href='nav.xhtml')])">[opf14] the Navigation Document must not be present in the OPF spine
                (itemref with idref="<value-of select="@idref"/>").</report>
        </rule>
    </pattern>

    <pattern id="opf_nordic_15_a">
        <rule context="opf:item[substring-after(@href,'/') = 'cover.jpg']">
            <assert test="tokenize(@properties,'\s+') = 'cover-image'">[opf15a] The cover image must have a properties attribute containing the value 'cover-image': <value-of
                    select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/></assert>
        </rule>
    </pattern>

    <pattern id="opf_nordic_15_b">
        <rule context="opf:item[tokenize(@properties,'\s+') = 'cover-image']">
            <assert test="substring-after(@href,'/') = 'cover.jpg'">[opf15b] The image with property value 'cover-image' must have the filename 'cover.jpg': <value-of
                    select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/></assert>
        </rule>
    </pattern>

</schema>
