<?xml version="1.0"?>
<!-- * ********************************************************************* -->
<!-- * WARNING: This file is no longer maintained and hasn't been updated in -->
<!-- * years. There are numerous requirements in the HTML spec which should -->
<!-- * rightly be captured by assertions in this schema but that aren't. So -->
<!-- * don't consider the set of assertions here to be thorough or complete. -->
<!-- * ********************************************************************* -->

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- Schematron assertions for HTML5                                         -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

Copyright (c) 2005-2007 Elika J. Etemad (fantasai) and Henri Sivonen (hsivonen)
Copyright (c) 2007-2012 Mozilla Foundation

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

<schema xmlns='http://www.ascc.net/xml/schematron'>
	<ns prefix='h' uri='http://www.w3.org/1999/xhtml'/>

	<pattern name="required attributes">
		<rule context='h:bdo'>
			<assert test='@dir'>
				A &#x201C;bdo&#x201D; element must have a
				&#x201C;dir&#x201D; attribute.
			</assert>
		</rule>
		<rule context='h:img[not(@alt)]'>
			<assert test='(@title and not(@title = ""))
			or //h:meta[(translate(@name,
				"GENERATOR", "generator") = "generator")]
			or (ancestor::h:figure
				and (ancestor::h:figure[1]/h:figcaption
					and not(ancestor::h:figure[1]/h:figcaption = ""))
				and normalize-space(ancestor::h:figure[1])
					= normalize-space(ancestor::h:figure[1]/h:figcaption)
				and not(ancestor::h:figure[1]//*[
					local-name() = "audio"
					or local-name() = "canvas"
					or local-name() = "embed"
					or local-name() = "iframe"
					or local-name() = "math"
					or local-name() = "object"
					or local-name() = "svg"
					or local-name() = "video"])
				and not(count(ancestor::h:figure[1]//h:img) > 1))'>
				An &#x201C;img&#x201D; element must have an
				&#x201C;alt&#x201D; attribute, except under certain
				conditions. For details, consult guidance on
				providing text alternatives for images.
				http://www.w3.org/wiki/HTML/Usage/TextAlternatives
			</assert>
		</rule>
	</pattern>

	<pattern name='Triggered on mutually exclusive elements and prohibited-descendant cases'>

	<!-- Exclusions and prohibited-descendant contraints  - - - - - - - - - - - -->

	<rule context='h:form|h:dfn|h:noscript|h:address'>
			<report test='ancestor::*[name() = name(current())]'>
				The &#x201C;<name/>&#x201D; element must not contain any nested 
				&#x201C;<name/>&#x201D; elements.
			</report>
		</rule>

	<rule context='h:label'>
			<report test='ancestor::*[name() = name(current())]'>
				The &#x201C;<name/>&#x201D; element must not contain any nested 
				&#x201C;<name/>&#x201D; elements.
			</report>
			<report test='count(descendant::h:input
			                   | descendant::h:button
			                   | descendant::h:select
			                   | descendant::h:keygen
			                   | descendant::h:textarea) > 1'>
				The &#x201C;label&#x201D; element may contain at most one descendant
				&#x201C;input&#x201D; element,
				&#x201C;button&#x201D; element,
				&#x201C;select&#x201D; element,
				or &#x201C;textarea&#x201D; element.
			</report>
			<report test='@for and 
			              not(//h:input[not(translate(@type, "HIDEN", "hiden")="hidden")][@id = current()/@for] or 
			              //h:textarea[@id = current()/@for] or 
			              //h:select[@id = current()/@for] or 
			              //h:button[@id = current()/@for] or 
			              //h:keygen[@id = current()/@for] or 
			              //h:output[@id = current()/@for])'>
				The &#x201C;for&#x201D; attribute of the &#x201C;label&#x201D; 
				element must refer to a form control.
			</report>
		</rule>

		<rule context='h:section|h:nav|h:article|h:aside'>
			<report test='ancestor::h:address'>
				The sectioning element &#x201C;<name/>&#x201D; must not
				appear as a descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:footer'>
			<report test='ancestor::h:header'>
				The element &#x201C;footer&#x201D; must not
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The element &#x201C;footer&#x201D; must not
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The element &#x201C;footer&#x201D; must not
				appear as a descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:h1|h:h2|h:h3|h:h4|h:h5|h:h6'>
			<report test='ancestor::h:address'>
				The &#x201C;<name/>&#x201D; element must not appear as a 
				descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:header'>
			<report test='ancestor::h:footer'>
				The &#x201C;<name/>&#x201D; element must not appear as a 
				descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The &#x201C;<name/>&#x201D; element must not appear as a 
				descendant of the &#x201C;address&#x201D; element.
			</report>
			<report test='ancestor::h:header'>
				The &#x201C;header&#x201D; element must not appear as a 
				descendant of the &#x201C;header&#x201D; element.
			</report>
		</rule>

		<rule context='h:table'>
			<report test='ancestor::h:caption'>
				The element &#x201C;table&#x201D; must not appear as a
				descendant of the &#x201C;caption&#x201D; element.
			</report>
			<report test='@summary' role='warning'>
				The &#x201C;summary&#x201D; attribute on the
				&#x201C;table&#x201D; element is obsolete.
				Consider describing the structure of the table
				in a &#x201C;caption&#x201D; element or
				in a &#x201C;figure&#x201D; element containing
				the &#x201C;table&#x201D; element; or, simplify
				the structue of the table so that no
				description is needed.
			</report>
		</rule>
	</pattern>

	<!-- Interactive element exclusions -->
	<pattern name='interactive element exclusions'>
	
		<!-- 
		   - Interactive descendants:
		   - a
		   - video[controls]
		   - audio[controls]
		   - details
		   - menu[type=toolbar]
		   - button
		   - input[type!=hidden]
		   - textarea
		   - select
		   - img[usemap]
		   - embed
		   - iframe
		   - keygen
		   - label
		   - object[usemap]
		
		   - Interactive ancestors
		   - a
		   - button
		  -->

		<rule context='h:a|h:details|h:embed|h:iframe|h:label'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
		</rule>

		<rule context='h:button|h:textarea|h:select|h:keygen|h:input[not(translate(@type, "HIDEN", "hiden")="hidden")]'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:label[@for] and not(ancestor::h:label[@for = current()/@id])'>
				Any &#x201C;<name/>&#x201D; element descendant of a &#x201C;label&#x201D; element
				with a &#x201C;for&#x201D; attribute must have an
				ID value that matches that &#x201C;for&#x201D; attribute.
			</report>
		</rule>

		<rule context='h:video[@controls]|h:audio[@controls]'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;<name/>&#x201D;
				with the attribute &#x201C;controls&#x201D; must not
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;<name/>&#x201D;
				with the attribute &#x201C;controls&#x201D; must not
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
		</rule>

		<rule context='h:menu[translate(@type, "TOLBAR", "tolbar")="toolbar"]'>
			<report test='ancestor::h:a'>
				The element &#x201C;menu&#x201D;
				with the attribute &#x201C;type&#x201D; whose value is &#x201C;toolbar&#x201D; must not
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The element &#x201C;menu&#x201D;
				with the attribute &#x201C;type&#x201D; whose value is &#x201C;toolbar&#x201D; must not
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
		</rule>

		<rule context='h:img[@usemap]'>
			<report test='ancestor::h:a'>
				The element &#x201C;img&#x201D;
				with the attribute &#x201C;usemap&#x201D; must not
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The element &#x201C;img&#x201D;
				with the attribute &#x201C;usemap&#x201D; must not
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
		</rule>

		<rule context='h:object[@usemap]'>
			<report test='ancestor::h:a'>
				The element &#x201C;object&#x201D;
				with the attribute &#x201C;usemap&#x201D; must not
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The element &#x201C;object&#x201D;
				with the attribute &#x201C;usemap&#x201D; must not
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
		</rule>
	</pattern>

	<!-- REVISIT fieldset http://lists.whatwg.org/pipermail/whatwg-whatwg.org/2006-April/006181.html -->

	<!-- Misc requirements -->
		
	<pattern name="miscellaneous requirements">
		<rule context='h:area'>
			<assert test='ancestor::h:map'>
				The &#x201C;area&#x201D; element must have an ancestor
				&#x201C;map&#x201D; element.
			</assert>
		</rule>

		<rule context='h:img[@ismap]'>
			<assert test='ancestor::h:a[@href]'>
				The &#x201C;img&#x201D; element with the 
				&#x201C;ismap&#x201D; attribute set must have an ancestor 
				&#x201C;a&#x201D; element with the &#x201C;href&#x201D; attribute.
			</assert>
		</rule>

		<rule context='h:input'>
			<report test='@list and not(//h:datalist[@id = current()/@list])'>
				The &#x201C;list&#x201D; attribute of the &#x201C;input&#x201D; 
				element must refer to a &#x201C;datalist&#x201D; element.
			</report>
			<report test='@type = "button" and (not(@value) or @value = "")'>
				Element &#x201C;input&#x201D; with attribute &#x201C;type&#x201D;
				whose value is &#x201C;button&#x201D; must have non-empty attribute
				&#x201C;value&#x201D;.
			</report>
		</rule>

		<rule context='h:track'>
			<report test='@label = ""'>
				Attribute &#x201C;label&#x201D; for element &#x201C;track&#x201D;
				must have non-empty value.
			</report>
			<report test='@default and preceding-sibling::h:track[@default]'>
				The &#x201C;default&#x201D; attribute must not occur on more than one
				&#x201C;track&#x201D; element within the same &#x201C;audio&#x201D;
				element or &#x201C;video&#x201D; element.
			</report>
		</rule>

		<rule context='h:map[@id and @name]'>
			<assert test='@id = @name'>
				The &#x201C;id&#x201D; attribute on a &#x201C;map&#x201D; element must have
				the same value as the &#x201C;name&#x201D; attribute.
			</assert>
		</rule>

		<rule context='h:select'>
			<report test='not(@multiple) and count(descendant::h:option[@selected]) > 1'>
				The &#x201C;select&#x201D; element cannot have more than one 
				selected &#x201C;option&#x201D; element descendant unless the 
				&#x201C;multiple&#x201D; attribute is specified.
			</report>
			<report test='@required and not(@multiple)
				and (not(@size)
					or (starts-with(normalize-space(@size), "+")
					and substring-after(@size,"+") = 1)
					or @size = 1)
				and not(h:option)'>
				A &#x201C;select&#x201D; element with
				a &#x201C;required&#x201D; attribute and without
				a &#x201C;multiple&#x201D; attribute, and whose
				size is &#x201C;1&#x201D;, must have a child
				&#x201C;option&#x201D; element.
			</report>
		</rule>

		<rule context='h:select[@required and not(@multiple)
			and (not(@size)
				or (starts-with(normalize-space(@size), "+")
				and substring-after(@size,"+") = 1)
				or @size = 1)]/h:option[1]'>
			<assert test='(@value and @value = "")
				or ((not(@value) or @value = "") and . = "")'>
				The first child &#x201C;option&#x201D; element
				of a &#x201C;select&#x201D; element with
				a &#x201C;required&#x201D; attribute and without
				a &#x201C;multiple&#x201D; attribute, and whose
				size is &#x201C;1&#x201D;, must have either an
				empty &#x201C;value&#x201D; attribute, or must
				have no text content.
			</assert>
		</rule>

		<rule context='h:script'>
			<report test='@language and translate(@language, "JAVSCRIPT", "javscript")="javascript"
				and @type and not(translate(@type, "EXJAVSCRIPT", "exjavscript")="text/javascript")'>
				Element &#x201C;script&#x201D; with attribute
				&#x201C;language&#x201D; whose value is &#x201C;JavaScript&#x201D;
				must not have attribute &#x201C;type&#x201D; whose value is not
				&#x201C;text/javascript&#x201D;.
			</report>
			<report test='not(@src) and @charset'>
				Element &#x201C;script&#x201D; must not have attribute
				&#x201C;charset&#x201D; unless attribute &#x201C;src&#x201D; is
				also specified.
			</report>
			<report test='not(@src) and @defer'>
				Element &#x201C;script&#x201D; must not have attribute
				&#x201C;defer&#x201D; unless attribute &#x201C;src&#x201D; is
				also specified.
			</report>
			<report test='not(@src) and @async'>
				Element &#x201C;script&#x201D; must not have attribute
				&#x201C;async&#x201D; unless attribute &#x201C;src&#x201D; is
				also specified.
			</report>
		</rule>

		<rule context='h:time'>
			<report test='ancestor::h:time'>
				The element &#x201C;time&#x201D; must not
				appear as a descendant of the &#x201C;time&#x201D; element.
			</report>
		</rule>

		<rule context='h:progress'>
			<report test='ancestor::h:progress'>
				The element &#x201C;progress&#x201D; must not
				appear as a descendant of the &#x201C;progress&#x201D; element.
			</report>
			<assert test='@max and @value and number(@value) &lt;= number(@max)'>
				The value of the &#x201C;value&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</assert>
			<assert test='not(@max) and @value and number(@value) &lt;= 1'>
				The value of the  &#x201C;value&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</assert>
		</rule>

		<!-- 
			min <= value <= max
    		min <= low <= high <= max
			min <= optimum <= max 
		-->

		<rule context='h:meter'>
			<report test='ancestor::h:meter'>
				The element &#x201C;meter&#x201D; must not
				appear as a descendant of the &#x201C;meter&#x201D; element.
			</report>
			<report test='@min and @value and not(number(@min) &lt;= number(@value))'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				the value of the &#x201C;value&#x201D; attribute.
			</report>
			<report test='not(@min) and @value and not(0 &lt;= number(@value))'>
				The value of the &#x201C;value&#x201D; attribute must be greater than or equal to
				zero when the &#x201C;min&#x201D; attribute is absent.
			</report>
			<report test='@value and @max and not(number(@value) &lt;= number(@max))'>
				The value of the  &#x201C;value&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</report>
			<report test='@value and not(@max) and not(number(@value) &lt;= 1)'>
				The value of the  &#x201C;value&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</report>
			<report test='@min and @max and not(number(@min) &lt;= number(@max))'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</report>
			<report test='not(@min) and @max and not(0 &lt;= number(@max))'>
				The value of the &#x201C;max&#x201D; attribute must be greater than or equal to
				zero when the &#x201C;min&#x201D; attribute is absent.
			</report>
			<report test='@min and not(@max) and not(number(@min) &lt;= 1)'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</report>
			<report test='@min and @low and not(number(@min) &lt;= number(@low))'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				the value of the &#x201C;low&#x201D; attribute.
			</report>
			<report test='not(@min) and @low and not(0 &lt;= number(@low))'>
				The value of the &#x201C;low&#x201D; attribute must be greater than or equal to
				zero when the &#x201C;min&#x201D; attribute is absent.
			</report>
			<report test='@min and @high and not(number(@min) &lt;= number(@high))'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				the value of the &#x201C;high&#x201D; attribute.
			</report>
			<report test='not(@min) and @high and not(0 &lt;= number(@high))'>
				The value of the &#x201C;high&#x201D; attribute must be greater than or equal to
				zero when the &#x201C;min&#x201D; attribute is absent.
			</report>
			<report test='@low and @high and not(number(@low) &lt;= number(@high))'>
				The value of the  &#x201C;low&#x201D; attribute must be less than or equal to
				the value of the &#x201C;high&#x201D; attribute.
			</report>
			<report test='@high and @max and not(number(@high) &lt;= number(@max))'>
				The value of the  &#x201C;high&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</report>
			<report test='@high and not(@max) and not(number(@high) &lt;= 1)'>
				The value of the  &#x201C;high&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</report>
			<report test='@low and @max and not(number(@low) &lt;= number(@max))'>
				The value of the  &#x201C;low&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</report>
			<report test='@low and not(@max) and not(number(@low) &lt;= 1)'>
				The value of the  &#x201C;low&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</report>
			<report test='@min and @optimum and not(number(@min) &lt;= number(@optimum))'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				the value of the &#x201C;optimum&#x201D; attribute.
			</report>
			<report test='not(@min) and @optimum and not(0 &lt;= number(@optimum))'>
				The value of the &#x201C;optimum&#x201D; attribute must be greater than or equal to
				zero when the &#x201C;min&#x201D; attribute is absent.
			</report>
			<report test='@optimum and @max and not(number(@optimum) &lt;= number(@max))'>
				The value of the  &#x201C;optimum&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</report>
			<report test='@optimum and not(@max) and not(number(@optimum) &lt;= 1)'>
				The value of the  &#x201C;optimum&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</report>
		</rule>
	</pattern>

	<!-- Obsolete Elements - - - - - - - - - - - - - - - - - - - - - - -->
	<pattern name="obsolete elements">
		<rule context='h:acronym'>
			<report test='true()'>
				The &#x201C;acronym&#x201D; element is obsolete. Use the &#x201C;abbr&#x201D; element instead.
			</report>
		</rule>

		<rule context='h:applet'>
			<report test='true()'>
				The &#x201C;applet&#x201D; element is obsolete. Use the &#x201C;object&#x201D; element instead.
			</report>
		</rule>

		<rule context='h:center|h:font|h:big|h:strike|h:tt|h:basefont'>
			<report test='true()'>
				The &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:dir'>
			<report test='true()'>
				The &#x201C;dir&#x201D; element is obsolete. Use the &#x201C;ul&#x201D; element instead.
			</report>
		</rule>

		<rule context='h:frameset|h:noframes'>
			<report test='true()'>
				The &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;iframe&#x201D; element and CSS instead, or use server-side includes.
			</report>
		</rule>
	</pattern>

	<!-- Obsolete Attributes- - - - - - - - - - - - - - - - - - - - - - -->

	<pattern name="obsolete attributes">
		<rule context='h:a'>
			<report test='@coords'>
				The &#x201C;coords&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				For image maps, use the &#x201C;area&#x201D; element instead of the &#x201C;a&#x201D; element.
			</report>
			<report test='@shape'>
				The &#x201C;shape&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				For image maps, use the &#x201C;area&#x201D; element instead of the &#x201C;a&#x201D; element.
			</report>
			<report test='@urn'>
				The &#x201C;urn&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Specify the preferred persistent identifier using the &#x201C;href&#x201D; attribute instead.
			</report>
			<report test='@charset'>
				The &#x201C;charset&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use an HTTP Content-Type header on the linked resource instead.
			</report>
			<report test='@methods'>
				The &#x201C;methods&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the HTTP OPTIONS feature instead.
			</report>
			<report test='@rev'>
				The &#x201C;rev&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;rel&#x201D; attribute instead, with a term having the opposite meaning.
			</report>
		</rule>

		<rule context='h:link'>
			<report test='@target'>
				The &#x201C;target&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				You can safely omit it.
			</report>
			<report test='@urn'>
				The &#x201C;urn&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Specify the preferred persistent identifier using the &#x201C;href&#x201D; attribute instead.
			</report>
			<report test='@charset'>
				The &#x201C;charset&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use an HTTP Content-Type header on the linked resource instead.
			</report>
			<report test='@methods'>
				The &#x201C;methods&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the HTTP OPTIONS feature instead.
			</report>
			<report test='@rev'>
				The &#x201C;rev&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;rel&#x201D; attribute instead, with a term having the opposite meaning.
			</report>
		</rule>

		<rule context="h:area">
			<report test='@nohref'>
				The &#x201C;nohref&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Omitting the &#x201C;href&#x201D; attribute is sufficient.
			</report>
		</rule>

		<rule context='h:embed'>
			<report test='@name'>
				The &#x201C;name&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;id&#x201D; attribute instead.
			</report>
		</rule>

		<rule context='h:head'>
			<report test='@profile'>
				The &#x201C;profile&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				To declare which &#x201C;meta&#x201D; element terms are used in the document, instead register the
				names as meta extensions. &lt;http://wiki.whatwg.org/wiki/MetaExtensions>
				To trigger specific UA behaviors, use a &#x201C;link&#x201D; element instead.
			</report>
		</rule>

		<rule context='h:html'>
			<report test='@version'>
				The &#x201C;version&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				You can safely omit it.
			</report>
		</rule>

		<rule context='h:iframe'>
			<report test='@longdesc'>
				The &#x201C;longdesc&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use a regular &#x201C;a&#x201D; element to link to the description.
			</report>
		</rule>

		<rule context='h:img'>
			<report test='@longdesc'>
				The &#x201C;longdesc&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use a regular &#x201C;a&#x201D; element to link to the description.
			</report>
			<report test='@name'>
				The &#x201C;name&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;id&#x201D; attribute instead.
			</report>
		</rule>

		<rule context='h:input'>
			<report test='@usemap'>
				The &#x201C;usemap&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;img&#x201D; element instead of the &#x201C;input&#x201D; element for image maps.
			</report>
		</rule>

		<rule context='h:li|h:ul'>
			<report test='@type'>
				The &#x201C;type&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:meta'>
			<report test='@scheme'>
				The &#x201C;scheme&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use only one scheme per field, or make the scheme declaration part of the value.
			</report>
			<report test='translate(@http-equiv,"CONTELAGUA", "contelagua")="content-language"' role='warning'>
				Using the &#x201C;meta&#x201D; element to specify the document-wide default language is obsolete.
				Consider specifying the language on the root element instead.
			</report>
		</rule>

		<rule context='h:object'>
			<report test='@archive'>
				The &#x201C;archive&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;data&#x201D; attribute and &#x201C;type&#x201D; attribute to invoke plugins.
				To set a parameter with the name &#x201C;archive&#x201D;, use the &#x201C;param&#x201D; element.
			</report>
			<report test='@classid'>
				The &#x201C;classid&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;data&#x201D; attribute and &#x201C;type&#x201D; attribute to invoke plugins.
				To set a parameter with the name &#x201C;classid&#x201D;, use the &#x201C;param&#x201D; element.
			</report>
			<report test='@code'>
				The &#x201C;code&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;data&#x201D; attribute and &#x201C;type&#x201D; attribute to invoke plugins.
				To set a parameter with the name &#x201C;code&#x201D;, use the &#x201C;param&#x201D; element.
			</report>
			<report test='@codebase'>
				The &#x201C;codebase&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;data&#x201D; attribute and &#x201C;type&#x201D; attribute to invoke plugins.
				To set a parameter with the name &#x201C;codebase&#x201D;, use the &#x201C;param&#x201D; element.
			</report>
			<report test='@codetype'>
				The &#x201C;codetype&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;data&#x201D; attribute and &#x201C;type&#x201D; attribute to invoke plugins.
				To set a parameter with the name &#x201C;codetype&#x201D;, use the &#x201C;param&#x201D; element.
			</report>
			<report test='@declare'>
				The &#x201C;declare&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Repeat the &#x201C;object&#x201D; element completely each time the resource is to be reused.
			</report>
			<report test='@standby'>
				The &#x201C;standby&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Optimize the linked resource so that it loads quickly or, at least, incrementally.
			</report>
		</rule>

		<rule context='h:option'>
			<report test='@name'>
				The &#x201C;name&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;id&#x201D; attribute instead.
			</report>
		</rule>

		<rule context='h:param'>
			<report test='@type'>
				The &#x201C;type&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;name&#x201D; attribute and &#x201C;value&#x201D; attribute without declaring value types.
			</report>
			<report test='@valuetype'>
				The &#x201C;valuetype&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;name&#x201D; attribute and &#x201C;value&#x201D; attribute without declaring value types.
			</report>
		</rule>

		<rule context='h:script[@language and not(translate(@language, "JAVSCRIPT", "javscript")="javascript")]'>
			<report test='true()'>
				The &#x201C;language&#x201D; attribute on the &#x201C;script&#x201D; element is obsolete.
				Use the &#x201C;type&#x201D; attribute instead.
			</report>
		</rule>

		<rule context='h:td|h:th'>
			<report test='@scope and self::h:td'>
				The &#x201C;scope&#x201D; attribute on the &#x201C;td&#x201D; element is obsolete.
				Use the &#x201C;scope&#x201D; attribute on a &#x201C;th&#x201D; element instead.
			</report>
			<report test='@abbr'>
				The &#x201C;abbr&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Consider instead beginning the cell contents with concise text, followed by further elaboration if needed.
			</report>
			<report test='@axis'>
				The &#x201C;axis&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use the &#x201C;scope&#x201D; attribute instead.
			</report>
		</rule>
	</pattern>

	<pattern name="obsolete presentational align attribute">
		<rule context='h:caption|h:iframe|h:img|h:input|h:object|h:embed|h:legend
			|h:table|h:hr|h:div|h:h1|h:h2|h:h3|h:h4|h:h5|h:h6|h:p|h:col|h:colgroup
			|h:tbody|h:td|h:tfoot|h:th|h:thead|h:tr'>
			<report test='@align'>
				The &#x201C;align&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>
	</pattern>

	<pattern name="obsolete presentational width attribute">
		<rule context='h:col|h:colgroup|h:hr|h:pre|h:table|h:td|h:th'>
			<report test='@width'>
				The &#x201C;width&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>
	</pattern>

	<pattern name="obsolete presentational table attributes">
		<rule context='h:col|h:colgroup|h:tbody|h:td|h:tfoot|h:th|h:thead|h:tr'>
			<report test='@char'>
				The &#x201C;char&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@charoff'>
				The &#x201C;charoff&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@valign'>
				The &#x201C;valign&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>
	</pattern>

	<pattern name="obsolete presentational attributes">
		<rule context='h:body'>
			<report test='@alink'>
				The &#x201C;alink&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@background'>
				The &#x201C;background&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@bgcolor'>
				The &#x201C;bgcolor&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@link'>
				The &#x201C;link&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@marginbottom'>
				The &#x201C;marginbottom&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@marginheight'>
				The &#x201C;marginheight&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@marginleft'>
				The &#x201C;marginleft&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@marginright'>
				The &#x201C;marginright&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@margintop'>
				The &#x201C;margintop&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@marginwidth'>
				The &#x201C;marginwidth&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@text'>
				The &#x201C;text&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@vlink'>
				The &#x201C;vlink&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:br'>
			<report test='@clear'>
				The &#x201C;clear&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:embed'>
			<report test='@hspace'>
				The &#x201C;hspace&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@vspace'>
				The &#x201C;vspace&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:hr'>
			<report test='@noshade'>
				The &#x201C;noshade&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@size'>
				The &#x201C;size&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@color'>
				The &#x201C;color&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:dl|h:menu|h:ol|h:ul'>
			<report test='@compact'>
				The &#x201C;compact&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:iframe'>
			<report test='@allowtransparency'>
				The &#x201C;allowtransparency&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@frameborder'>
				The &#x201C;frameborder&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@marginheight'>
				The &#x201C;marginheight&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@marginwidth'>
				The &#x201C;marginwidth&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@scrolling'>
				The &#x201C;scrolling&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:img|h:object'>
			<report test='@hspace'>
				The &#x201C;hspace&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@vspace'>
				The &#x201C;vspace&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@border and self::h:object'>
				The &#x201C;border&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:table'>
			<report test='@bgcolor'>
				The &#x201C;bgcolor&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@border and not(@border = "" or @border = "1")'>
				The value of the &#x201C;border&#x201D; attribute on the &#x201C;<name/>&#x201D; element
				must be either &#x201C;1&#x201D; or the empty string. To regulate the thickness of table borders, 
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@cellpadding'>
				The &#x201C;cellpadding&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@cellspacing'>
				The &#x201C;cellspacing&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@frame'>
				The &#x201C;frame&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@rules'>
				The &#x201C;rules&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:td|h:th'>
			<report test='@bgcolor'>
				The &#x201C;bgcolor&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@height'>
				The &#x201C;height&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
			<report test='@nowrap'>
				The &#x201C;nowrap&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>

		<rule context='h:tr'>
			<report test='@bgcolor'>
				The &#x201C;bgcolor&#x201D; attribute on the &#x201C;<name/>&#x201D; element is obsolete.
				Use CSS instead. http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes
			</report>
		</rule>
	</pattern>

<!-- lang and xml:lang in XHTML  - - - - - - - - - - - - - - - - - -->

	<pattern name='lang and xml:lang in XHTML'>
		<rule context='h:*[@lang and @xml:lang]'>
			<assert test='translate(@lang, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz") = translate(@xml:lang, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz")'>
				When the attribute &#x201C;lang&#x201D; in no namespace and the attribute &#x201C;lang&#x201D;
				in the XML namespace are both present, they must have the same value.
			</assert>
		</rule>
	</pattern>

<!-- IDREFs  - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

	<!-- Assuming that ID uniqueness is already enforced. -->

	<pattern name='contextmenu must refer to a menu'>
		<rule context='h:*[@contextmenu]'>
		  <assert test='//h:menu[@id = current()/@contextmenu]'>
				The &#x201C;contextmenu&#x201D; attribute must refer to a 
				&#x201C;menu&#x201D; element.
			</assert>
		</rule>
	</pattern>

	<!-- FIXME form attribute -->
	
	<!-- FIXME output for -->
	
<!-- Unique Definitions  - - - - - - - - - - - - - - - - - - - - - -->
	
	<!-- Only one definition per term per document' -->

<!-- ARIA containment    - - - - - - - - - - - - - - - - - - - - - -->

	<pattern name='Mutually Exclusive Role triggers'>

    <!-- XXX columnheader and rowheader require row parent -->

		<rule context='*[@role="option"]'>
			<assert test='../@role="listbox"'>
				An element with &#x201C;role=option&#x201D; requires 
				&#x201C;role=listbox&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="menuitem"]'>
			<assert test='../@role="menu"'>
				An element with &#x201C;role=menuitem&#x201D; requires 
				&#x201C;role=menu&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="menuitemcheckbox"]'>
			<assert test='../@role="menu"'>
				An element with &#x201C;role=menuitemcheckbox&#x201D; requires 
				&#x201C;role=menu&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="menuitemradio"]'>
			<assert test='../@role="menu"'>
				An element with &#x201C;role=menuitemradio&#x201D; requires 
				&#x201C;role=menu&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="tab"]'>
			<assert test='../@role="tablist"'>
				An element with &#x201C;role=tab&#x201D; requires 
				&#x201C;role=tablist&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="treeitem"]'>
			<assert test='../@role="tree"'>
				An element with &#x201C;role=treeitem&#x201D; requires 
				&#x201C;role=tree&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="listitem"]'>
			<assert test='../@role="list"'>
				An element with &#x201C;role=listitem&#x201D; requires 
				&#x201C;role=list&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="row"]'>
			<assert test='../@role="grid" or 
			              ../../@role="grid" or
						  ../@role="treegrid" or 
			              ../../@role="treegrid"'>
				An element with &#x201C;role=row&#x201D; requires 
				&#x201C;role=treegrid&#x201D; or &#x201C;role=grid&#x201D; on the parent or grandparent.
			</assert>
		</rule> 
		<!-- XXX hoping for a spec change so not bothering with the reciprocal case -->

		<rule context='*[@role="gridcell"]'>
			<assert test='../@role="row"'>
				An element with &#x201C;role=gridcell&#x201D; requires 
				&#x201C;role=row&#x201D; on the parent.
			</assert>
		</rule>
		<!-- XXX hoping for a spec change so not bothering with the reciprocal case -->

	</pattern>
	
	<pattern name='Not Option'>
		<rule context='*[not(@role="option")]'>
			<report test='../@role="listbox"'>
				An element must not be a child of
				&#x201C;role=listbox&#x201D; unless it has &#x201C;role=option&#x201D;.
			</report>
		</rule>
	</pattern>
	
	<pattern name='Not menuitem*'>
		<rule context='*[not(@role="menuitem" or 
		                     @role="menuitemcheckbox" or 
		                     @role="menuitemradio")]'>
			<report test='../@role="menu"'>
				An element must not be a child of
				&#x201C;role=menu&#x201D; unless it has 
				&#x201C;role=menuitem&#x201D;, 
				&#x201C;role=menuitemcheckbox&#x201D; or 
				&#x201C;role=menuitemradio&#x201D;.
			</report>
		</rule>
	</pattern>
	
	<pattern name='Not treeitem'>
		<rule context='*[not(@role="treeitem")]'>
			<report test='../@role="tree"'>
				An element must not be a child of
				&#x201C;role=tree&#x201D; unless it has 
				&#x201C;role=treeitem&#x201D;.
			</report>
		</rule>
	</pattern>
	
	<pattern name='Not listitem'>
		<rule context='*[not(@role="listitem")]'>
			<report test='../@role="list"'>
				An element must not be a child of
				&#x201C;role=list&#x201D; unless it has 
				&#x201C;role=listitem&#x201D;.
			</report>
		</rule>
		<!-- XXX role=group omitted due to lack of detail in spec -->
	</pattern>
	
	<pattern name='Not radio'>
		<rule context='*[not(@role="radio")]'>
			<report test='../@role="radiogroup"'>
				An element must not be a child of
				&#x201C;role=radiogroup&#x201D; unless it has 
				&#x201C;role=radio&#x201D;.
			</report>
		</rule>
	</pattern>
	
	<pattern name='Not gridcell'>
		<rule context='*[not(@role="gridcell")]'>
			<report test='../@role="row"'>
				An element must not be a child of
				&#x201C;role=row&#x201D; unless it has 
				&#x201C;role=gridcell&#x201D;.
			</report>
		</rule>
	</pattern>
	
	<pattern name='Not tab'>
		<rule context='*[not(@role="tab")]'>
			<report test='../@role="tablist"'>
				An element must not be a child of
				&#x201C;role=tablist&#x201D; unless it has 
				&#x201C;role=role&#x201D;.
			</report>
		</rule>
	</pattern>

  <!-- XXX combobox requires a listbox child -->
	
	<pattern name='aria-activedescendant must refer to a descendant'>
		<rule context='*[@aria-activedescendant]'>
			<assert test='descendant::*[@id = current()/@aria-activedescendant]'>
				The &#x201C;aria-activedescendant&#x201D; attribute must refer to a 
				descendant element.
			</assert>
		</rule>
	</pattern>

	<pattern name='controls must not dangle'>
		<rule context='*[@aria-controls]'>
		  <assert test='//*[@id = current()/@aria-controls]'>
				The &#x201C;aria-controls&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='describedby must not dangle'>
		<rule context='*[@aria-describedby]'>
		  <assert test='//*[@id = current()/@aria-describedby]'>
				The &#x201C;aria-describedby&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='flowto must not dangle'>
		<rule context='*[@aria-flowto]'>
		  <assert test='//*[@id = current()/@aria-flowto]'>
				The &#x201C;aria-flowto&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='labelledby must not dangle'>
		<rule context='*[@aria-labelledby]'>
		  <assert test='//*[@id = current()/@aria-labelledby]'>
				The &#x201C;aria-labelledby&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='owns must not dangle'>
		<rule context='*[@aria-owns]'>
		  <assert test='//*[@id = current()/@aria-owns]'>
				The &#x201C;aria-owns&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!--                               Warnings                                  -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<pattern name="Warnings for HTML5 attributes that are obsolete but conforming">
		<rule context='h:img'>
			<report test='@border' role='warning'>
				The &#x201C;border&#x201D; attribute on the &#x201C;img&#x201D; element is obsolete.
				Consider specifying &#x201C;img { border: 0; }&#x201C; in CSS instead.
			</report>
		</rule>
		<rule context='h:script[translate(@language, "JAVSCRIPT", "javscript")="javascript"]'>
			<report test='not(@type) or translate(@type, "EXJAVSCRIPT", "exjavscript")="text/javascript"' role='warning'>
				The &#x201C;language&#x201D; attribute on the &#x201C;script&#x201D; element is obsolete. You can safely omit it.
			</report>
		</rule>
		<rule context='h:a'>
			<report test='@name' role='warning'>
				The &#x201C;name&#x201D; attribute on the &#x201C;a&#x201D; element is obsolete. Consider putting an
				&#x201C;id&#x201D; attribute on the nearest container instead.
			</report>
		</rule>
	</pattern>

	<pattern name="Other warnings">
		<rule context='h:video|h:audio'>
			<report test='count(h:track[@default]) > 1' role='warning'>
				&#x201C;<name/>&#x201D; element has more than one &#x201C;track&#x201D;
				child element with a &#x201C;default&#x201D; attribute.
			</report>
		</rule>
	</pattern>

</schema>
