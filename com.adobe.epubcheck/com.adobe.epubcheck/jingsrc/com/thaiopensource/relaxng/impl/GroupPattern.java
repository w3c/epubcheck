package com.thaiopensource.relaxng.impl;

class GroupPattern extends BinaryPattern {
  GroupPattern(Pattern p1, Pattern p2) {
    super(p1.isNullable() && p2.isNullable(),
	  combineHashCode(GROUP_HASH_CODE, p1.hashCode(), p2.hashCode()),
	  p1,
	  p2);
  }

  Pattern expand(SchemaPatternBuilder b) {
    Pattern ep1 = p1.expand(b);
    Pattern ep2 = p2.expand(b);
    if (ep1 != p1 || ep2 != p2)
      return b.makeGroup(ep1, ep2);
    else
      return this;
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha) throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_group");
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_group");
    }
    super.checkRestrictions(context == ELEMENT_REPEAT_CONTEXT
			    ? ELEMENT_REPEAT_GROUP_CONTEXT
			    : context,
			    dad,
			    alpha);
    if (context != LIST_CONTEXT
	&& !contentTypeGroupable(p1.getContentType(), p2.getContentType()))
      throw new RestrictionViolationException("group_string");
  }

  void accept(PatternVisitor visitor) {
    visitor.visitGroup(p1, p2);
  }
  Object apply(PatternFunction f) {
    return f.caseGroup(this);
  }
}
