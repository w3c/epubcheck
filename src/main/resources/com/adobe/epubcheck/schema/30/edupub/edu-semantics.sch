<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>
	<ns uri="http://www.w3.org/1999/xhtml" prefix="html"/>

	<pattern id="edupub.structure.answers">
		<rule context="*[@epub:type='answers']">
			<assert test="descendant::*[@epub:type='answer']">An element that carries the
				answers term requires at least one descendant element that carries the 
				answer term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.assessments">
		<rule context="*[@epub:type='assessments']">
			<assert test="descendant::*[@epub:type='assessment']">An element that carries the
				assessments term requires at least one descendant element that carries the 
				assessment term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.biblio">
		<rule context="*[@epub:type='bibliography']">
			<assert test="descendant::*[@epub:type='biblioentry']">An element that carries the
				bibliography term requires at least one descendant element that carries the
				biblioentry term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.credits">
		<rule context="*[@epub:type='credits']">
			<assert test="descendant::*[@epub:type='credit']">An element that carries the
				the credits term requires at least one descendant element that carries the 
				credit term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.fill-in-the-blank-problem">
		<rule context="*[@epub:type='fill-in-the-blank-problem']">
			<assert test="descendant::*[@epub:type='question']">An element that carries the
				fill-in-the-blank-problem term requires at least one descendant element that carries
				the question term.</assert>
			<assert test="count(descendant::*[@epub:type='answer']) &lt; 2">An element that carries the
				fill-in-the-blank-problem term must not contain more than one descendant element
				that carries the answer term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.footnotes">
		<rule context="*[@epub:type='footnotes']">
			<assert test="descendant::*[@epub:type='footnote']">An element that carries the
				footnotes term requires at least one descendant element that carries the 
				footnote term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.general-problem">
		<rule context="*[@epub:type='general-problem']">
			<assert test="descendant::*[@epub:type='question']">An element that carries the
				general-problem term requires at least one descendant element that carries the
				question term.</assert>
			<assert test="count(descendant::*[@epub:type='answer']) &lt; 2">An element that carries the
				general-problem term must not contain more than one descendant element that carries
				the answer term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.keywords">
		<rule context="*[@epub:type='keywords']">
			<assert test="descendant::*[@epub:type='keyword']">An element that carries the
				keywords term requires at least one descendant element that carries the 
				keyword term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.learning-objectives">
		<rule context="*[@epub:type='learning-objectives']">
			<assert test="descendant::*[@epub:type='learning-objective']">An element that carries the
				learning-objectives term requires at least one descendant element that carries the 
				learning-objective term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.learning-outcomes">
		<rule context="*[@epub:type='learning-outcomes']">
			<assert test="descendant::*[@epub:type='learning-outcome']">An element that carries the
				learning-outcomes term requires at least one descendant element that carries the 
				learning-outcome term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.learning-resources">
		<rule context="*[@epub:type='learning-resources']">
			<assert test="descendant::*[@epub:type='learning-resource']">An element that carries the
				learning-resources term requires at least one descendant element that carries the 
				learning-resource term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.learning-standards">
		<rule context="*[@epub:type='learning-standards']">
			<assert test="descendant::*[@epub:type='learning-standard']">An element that carries the
				learning-standards term requires at least one descendant element that carries the 
				learning-standard term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.match-problem">
		<rule context="*[@epub:type='match-problem']">
			<assert test="descendant::*[@epub:type='question']">An element that carries the
				match-problem term requires at least one descendant element that carries the
				question term.</assert>
			<assert test="count(descendant::*[@epub:type='answer']) &lt; 2">An element that carries the
				match-problem term must not contain more than one descendant element that carries
				the answer term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.multiple-choice-problem">
		<rule context="*[@epub:type='multiple-choice']">
			<assert test="descendant::*[@epub:type='question']">An element that carries the
				multiple-choice-problem term requires at least one descendant element that carries
				the question term.</assert>
			<assert test="count(descendant::*[@epub:type='answer']) &lt; 2">An element that carries the
				multiple-choice-problem term must not contain more than one descendant element that
				carries the answer term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.ordinal">
		<rule context="*[@epub:type='ordinal']">
			<report test="descendant::*[@epub:type='ordinal']">An element that carries the ordinal
				term must not have descendants that also carry the ordinal term.</report>
		</rule>
	</pattern>

	<pattern id="edupub.structure.practices">
		<rule context="*[@epub:type='practices']">
			<assert test="descendant::*[@epub:type='practice']">An element that carries the
				practies term requires at least one descendant element that carries
				the practice term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.rearnotes">
		<rule context="*[@epub:type='rearnotes']">
			<assert test="descendant::*[@epub:type='rearnote']">An element that carries the
				rearnotes term requires at least one descendant element that carries 
				the rearnote term.</assert>
		</rule>
	</pattern>

	<pattern id="edupub.structure.true-false-problem">
		<rule context="*[@epub:type='true-false-problem']">
			<assert test="descendant::*[@epub:type='question']">An element that carries the
				true-false-problem term requires at least one descendant element that carries the
				question term.</assert>
			<assert test="count(descendant::*[@epub:type='answer']) &lt; 2">An element that carries the
				true-false-problem term must not contain more than one descendant element that
				carries the answer term.</assert>
		</rule>
	</pattern>

</schema>
