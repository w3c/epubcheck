package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class MinExclusiveRestrictDatatype extends ValueRestrictDatatype {
  private final OrderRelation order;
  private final Object limit;

  MinExclusiveRestrictDatatype(DatatypeBase base, Object limit) {
    super(base);
    this.order = base.getOrderRelation();
    this.limit = limit;
  }

  boolean satisfiesRestriction(Object value) {
    return order.isLessThan(limit, value);
  }
}
