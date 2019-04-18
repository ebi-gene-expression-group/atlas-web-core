package uk.ac.ebi.atlas.solr.cloud.search.jsonfacets;

public enum SolrFacetType {
    TERMS("terms"),
    RANGE("range"),
    QUERY("query");

    public final String name;

    SolrFacetType(String name) {
        this.name = name;
    }
}
