<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
  <!-- 
  	A Schematron schema for NCX version 2005-1
  	
  	Adopted from Z3986 conformance validator ("ZedVal") schema
  	available in original at http://daisymfc.svn.sourceforge.net/viewvc/daisymfc/trunk/dmfc/src/org/daisy/util/xml/catalog/z39862005/ncx-2005-1.rng?view=log 
  	
  	This schema uses Schematron 1.5 for integration with Jing, and
  	to get line/column locators in reported errors, as implemented
  	by James Clark. It may be upgraded to ISO Schematron in the future.
  	
	Latest edit: mgylling 20111220
	  
  -->
  
  <sch:ns prefix="ncx" uri="http://www.daisy.org/z3986/2005/ncx/"/>
    
  <sch:pattern name="ncx_pageTargUniqValTypeComb" id="ncx_pageTargUniqValTypeComb">
      <!-- pageTarget combination of value and type attributes is unique -->
      <sch:rule context="ncx:pageList/ncx:pageTarget[@value]">
        <sch:assert test="count(//ncx:pageTarget[@value=current()/@value and @type=current()/@type])=1">
          pageTarget combination of value and type is not unique
        </sch:assert>        
      </sch:rule>
  </sch:pattern>
  
  <sch:pattern name="ncx_playOrderOrigin" id="ncx_playOrderOrigin">
      <!-- The sequence of play orders includes one with the value 1 -->
      <sch:rule context="ncx:ncx//*[@playOrder]">
        <sch:assert test="count(//*[@playOrder='1'])>0">
          the first playOrder value is not 1
        </sch:assert>
      </sch:rule>
  </sch:pattern>

  <sch:pattern name="ncx_multiNavLabel" id="ncx_multiNavLabel">
      <!-- multiple navLabels within an NCX node must have unique xml:lang values -->
      <sch:rule context="ncx:navLabel">
      <sch:assert test="count(../ncx:navLabel[@xml:lang=current()/@xml:lang])&lt;2">
          Multiple navLabels with same xml:lang attribute within an NCX node
        </sch:assert>        
      </sch:rule>
  </sch:pattern>
  
	<sch:pattern name="ncx_multiNavInfo" id="ncx_multiNavInfo">
	  <!-- multiple navInfos within an NCX node must have unique xml:lang values -->
	  <sch:rule context="ncx:navInfo">
	    <sch:assert test="count(../ncx:navInfo[@xml:lang=current()/@xml:lang])&lt;2">
	      Multiple navInfos with same xml:lang attribute within an NCX node
	    </sch:assert>        
	  </sch:rule>
	</sch:pattern>
        
    <sch:pattern name="ncx_playOrderNoGaps" id="ncx_playOrderNoGaps">
      <!-- The sequence of playOrder must have no gaps -->
      <sch:rule context="//*[number(@playOrder) &gt; 1]">
        <sch:assert test="count(//*[@playOrder][number(@playOrder) = number(current()/@playOrder)-1])&gt;0"> 
          playOrder sequence has gaps
        </sch:assert>        
      </sch:rule>
    </sch:pattern>

    <sch:pattern name="ncx_playOrderMatch2" id="ncx_playOrderMatch2">
      <!-- navPoints, navTargets, and pageTargets that have identical playOrder values must point to same target -->
      <sch:rule context="//*[@playOrder]">
        <sch:assert test="count(//*[@playOrder][number(@playOrder)=number(current()/@playOrder)]/ncx:content[@src != current()/ncx:content/@src])=0">
          identical playOrder values for navPoint/navTarget/pageTarget that do not refer to same target
        </sch:assert>        
      </sch:rule>
    </sch:pattern>

    <sch:pattern name="ncx_playOrderMatch" id="ncx_playOrderMatch">
      <!-- navPoints, navTargets, and pageTargets that point to same target must have identical playOrder values -->
      <sch:rule context="//*[@playOrder][ncx:content]">
        <sch:assert test="count(//*[@playOrder][ncx:content][ncx:content/@src=current()/ncx:content/@src][number(@playOrder)!=number(current()/@playOrder)])=0">
          different playOrder values for navPoint/navTarget/pageTarget that refer to same target
        </sch:assert>        
      </sch:rule>
    </sch:pattern>
    
    <sch:pattern name="ncx_idAttrUnique" id="ncx_idAttrUnique">      
      <sch:rule context="//*[@id]">
        <sch:assert test="count(//@id[normalize-space(.) = normalize-space(current()/@id)]) = 1"
           >The "id" attribute does not have a unique value</sch:assert> 
      </sch:rule>
  </sch:pattern>
  
</sch:schema>
