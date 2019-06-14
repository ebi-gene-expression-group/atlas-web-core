package uk.ac.ebi.atlas.species;

public enum AtlasInformationDataType {
  ENSEMBL("ensembl"),
  EG("ensembl_genomes"),
  WBPS("wormbase_parasite"),
  EFO("efo"),
  EFOURL("efoURL");

  private final String id;

  AtlasInformationDataType(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
