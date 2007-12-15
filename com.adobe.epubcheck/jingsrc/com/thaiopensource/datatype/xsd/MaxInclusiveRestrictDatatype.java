package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class MaxInclusiveRestrictDatatype extends ValueRestrictDatatype {
  private final OrderRelation order;
  private final Object limit;

  MaxInclusiveRestrictDatatype(DatatypeBase base, Object limit) {
    super(base);
    this.order = base.getOrderRelation();
    this.limit = limit;
  }

  boolean satisfiesRestriction(Object value) {
    return order.isLessThan(value, limit) || super.sameValue(value, limit);
  }
}
