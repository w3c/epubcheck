package com.thaiopensource.relaxng.impl;

import org.relaxng.datatype.ValidationContext;

import java.util.Hashtable;

import com.thaiopensource.xml.util.Name;

final class PatternMemo {
  private final Pattern pattern;
  private final ValidatorPatternBuilder builder;
  private final boolean notAllowed;
  private PatternMemo memoEndAttributes;
  private PatternMemo memoTextOnly;
  private PatternMemo memoEndTagDeriv;
  private PatternMemo memoMixedTextDeriv;
  private PatternMemo memoIgnoreMissingAttributes;
  private Hashtable startTagOpenDerivMap;
  private Hashtable startTagOpenRecoverDerivMap;
  private Hashtable startAttributeDerivMap;
  private DataDerivType memoDataDerivType;

  PatternMemo(Pattern pattern, ValidatorPatternBuilder builder) {
    this.pattern = pattern;
    this.builder = builder;
    this.notAllowed = pattern.isNotAllowed();
  }

  Pattern getPattern() {
    return pattern;
  }

  ValidatorPatternBuilder getPatternBuilder() {
    return builder;
  }

  boolean isNotAllowed() {
    return notAllowed;
  }

  PatternMemo endAttributes() {
    if (memoEndAttributes == null)
      memoEndAttributes = applyForPatternMemo(builder.getEndAttributesFunction());
    return memoEndAttributes;
  }

  PatternMemo endAttributes(PatternFunction f) {
    if (memoEndAttributes == null)
      memoEndAttributes = applyForPatternMemo(f);
    return memoEndAttributes;
  }

  PatternMemo ignoreMissingAttributes() {
    if (memoIgnoreMissingAttributes == null)
      memoIgnoreMissingAttributes
	= applyForPatternMemo(builder.getIgnoreMissingAttributesFunction());
    return memoIgnoreMissingAttributes;
  }

  PatternMemo ignoreMissingAttributes(PatternFunction f) {
    if (memoIgnoreMissingAttributes == null)
      memoIgnoreMissingAttributes = applyForPatternMemo(f);
    return memoIgnoreMissingAttributes;
  }

  PatternMemo textOnly() {
    if (memoTextOnly == null)
      memoTextOnly = applyForPatternMemo(builder.getTextOnlyFunction());
    return memoTextOnly;
  }

  PatternMemo textOnly(PatternFunction f) {
    if (memoTextOnly == null)
      memoTextOnly = applyForPatternMemo(f);
    return memoTextOnly;
  }

  PatternMemo endTagDeriv() {
    if (memoEndTagDeriv == null)
      memoEndTagDeriv = applyForPatternMemo(builder.getEndTagDerivFunction());
    return memoEndTagDeriv;
  }

  PatternMemo endTagDeriv(PatternFunction f) {
    if (memoEndTagDeriv == null)
      memoEndTagDeriv = applyForPatternMemo(f);
    return memoEndTagDeriv;
  }


  PatternMemo mixedTextDeriv() {
    if (memoMixedTextDeriv == null)
      memoMixedTextDeriv = applyForPatternMemo(builder.getMixedTextDerivFunction());
    return memoMixedTextDeriv;
  }

  PatternMemo mixedTextDeriv(PatternFunction f) {
    if (memoMixedTextDeriv == null)
      memoMixedTextDeriv = applyForPatternMemo(f);
    return memoMixedTextDeriv;
  }

  PatternMemo startTagOpenDeriv(Name name) {
    return startTagOpenDeriv(name, null);
  }

  PatternMemo startTagOpenDeriv(StartTagOpenDerivFunction f) {
    return startTagOpenDeriv(f.getName(), f);
  }

  private PatternMemo startTagOpenDeriv(Name name, StartTagOpenDerivFunction f) {
    PatternMemo tem;
    if (startTagOpenDerivMap == null)
      startTagOpenDerivMap = new Hashtable();
    else {
      tem = (PatternMemo)startTagOpenDerivMap.get(name);
      if (tem != null)
	return tem;
    }
    if (f == null)
      f = new StartTagOpenDerivFunction(name, builder);
    tem = applyForPatternMemo(f);
    startTagOpenDerivMap.put(name, tem);
    return tem;
  }

  PatternMemo startTagOpenRecoverDeriv(Name name) {
    return startTagOpenRecoverDeriv(name, null);
  }

  PatternMemo startTagOpenRecoverDeriv(StartTagOpenRecoverDerivFunction f) {
    return startTagOpenRecoverDeriv(f.getName(), f);
  }

  private PatternMemo startTagOpenRecoverDeriv(Name name, StartTagOpenRecoverDerivFunction f) {
    PatternMemo tem;
    if (startTagOpenRecoverDerivMap == null)
      startTagOpenRecoverDerivMap = new Hashtable();
    else {
      tem = (PatternMemo)startTagOpenRecoverDerivMap.get(name);
      if (tem != null)
	return tem;
    }
    if (f == null)
      f = new StartTagOpenRecoverDerivFunction(name, builder);
    tem = applyForPatternMemo(f);
    startTagOpenRecoverDerivMap.put(name, tem);
    return tem;
  }

  PatternMemo startAttributeDeriv(Name name) {
    return startAttributeDeriv(name, null);
  }

  PatternMemo startAttributeDeriv(StartAttributeDerivFunction f) {
    return startAttributeDeriv(f.getName(), f);
  }

  private PatternMemo startAttributeDeriv(Name name, StartAttributeDerivFunction f) {
    PatternMemo tem;
    if (startAttributeDerivMap == null)
      startAttributeDerivMap = new Hashtable();
    else {
      tem = (PatternMemo)startAttributeDerivMap.get(name);
      if (tem != null)
	return tem;
    }
    if (f == null)
      f = new StartAttributeDerivFunction(name, builder);
    tem = applyForPatternMemo(f);
    startAttributeDerivMap.put(name, tem);
    return tem;
  }

  DataDerivType dataDerivType() {
    if (memoDataDerivType == null)
      memoDataDerivType = DataDerivTypeFunction.dataDerivType(builder, pattern).copy();
    return memoDataDerivType;
  }

  PatternMemo dataDeriv(String str, ValidationContext vc) {
    return dataDerivType().dataDeriv(builder, pattern, str, vc);
  }

  PatternMemo recoverAfter() {
    // XXX memoize
    return applyForPatternMemo(builder.getRecoverAfterFunction());
  }

  private PatternMemo applyForPatternMemo(PatternFunction f) {
    return builder.getPatternMemo(pattern.applyForPattern(f));
  }
}
