package com.adobe.epubcheck.xml;

public enum XMLValidators
{
  CONTAINER_20_RNG("schema/20/rng/container.rng"),
  CONTAINER_30_RNC("schema/30/ocf-container-30.rnc"),
  CONTAINER_30_RENDITIONS_SCH("schema/30/multiple-renditions/container.sch"),
  DTBOOK_RNG("schema/20/rng/dtbook-2005-2.rng"),
  ENC_20_RNG("schema/20/rng/encryption.rng"),
  ENC_30_RNC("schema/30/ocf-encryption-30.rnc"),
  IDUNIQUE_20_SCH("schema/20/sch/id-unique.sch"),
  META_30_RNC("schema/30/ocf-metadata-30.rnc"),
  META_30_SCH("schema/30/ocf-metadata-30.sch"),
  META_EDUPUB_SCH("schema/30/edupub/edu-ocf-metadata.sch"),
  MO_30_RNC("schema/30/media-overlay-30.rnc"),
  MO_30_SCH("schema/30/media-overlay-30.sch"),
  NAV_30_RNC("schema/30/epub-nav-30.rnc"),
  NAV_30_SCH("schema/30/epub-nav-30.sch"),
  NCX_RNG("schema/20/rng/ncx.rng"),
  NCX_SCH("schema/20/sch/ncx.sch"),
  OPF_20_RNG("schema/20/rng/opf.rng"),
  OPF_20_SCH("schema/20/sch/opf.sch"),
  OPF_30_RNC("schema/30/package-30.rnc"),
  OPF_30_SCH("schema/30/package-30.sch"),
  OPF_30_COLLECTION_DICT_SCH("schema/30/dict/dict-collection.sch"),
  OPF_30_COLLECTION_DO_SCH("schema/30/collection-do-30.sch"),
  OPF_30_COLLECTION_IDX_SCH("schema/30/idx/idx-collection.sch"),
  OPF_30_COLLECTION_MANIFEST_SCH("schema/30/collection-manifest-30.sch"),
  OPF_30_COLLECTION_PREVIEW_SCH("schema/30/previews/preview-collection.sch"),
  OPF_DICT_SCH("schema/30/dict/dict-opf.sch"),
  OPF_EDUPUB_SCH("schema/30/edupub/edu-opf.sch"),
  OPF_PREVIEW_SCH("schema/30/previews/preview-pub-opf.sch"),
  RENDITION_MAPPING_RNC("schema/30/multiple-renditions/mapping.rnc"),
  RENDITION_MAPPING_SCH("schema/30/multiple-renditions/mapping.sch"),
  SEARCH_KEY_MAP_RNC("schema/30/dict/search-key-map.rnc"),
  SIG_20_RNG("schema/20/rng/signatures.rng"),
  SIG_30_RNC("schema/30/ocf-signatures-30.rnc"),
  SVG_20_RNG("schema/20/rng/svg11.rng"),
  SVG_30_RNC("schema/30/epub-svg-30.rnc"),
  SVG_30_SCH("schema/30/epub-svg-30.sch"),
  XHTML_20_NVDL("schema/20/rng/ops20.nvdl"),
  XHTML_20_SCH("schema/20/sch/xhtml.sch"),
  XHTML_30_SCH("schema/30/epub-xhtml-30.sch"),
  XHTML_30_RNC("schema/30/epub-xhtml-30.rnc"),
  XHTML_EDUPUB_STRUCTURE_SCH("schema/30/edupub/edu-structure.sch"),
  XHTML_EDUPUB_SEMANTICS_SCH("schema/30/edupub/edu-semantics.sch"),
  XHTML_DATANAV_SCH("schema/30/datanav/datanav-xhtml.sch"),
  XHTML_DICT_SCH("schema/30/dict/dict-xhtml.sch"),
  XHTML_IDX_SCH("schema/30/idx/idx-xhtml.sch"),
  XHTML_IDX_INDEX_SCH("schema/30/idx/idx-xhtml-index.sch");

  private final XMLValidator val;

  private XMLValidators(String schemaName)
  {
    val = new XMLValidator(schemaName);
  }

  public XMLValidator get()
  {
    return val;
  }

}
