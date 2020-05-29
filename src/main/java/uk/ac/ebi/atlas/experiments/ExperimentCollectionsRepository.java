package uk.ac.ebi.atlas.experiments;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperimentCollectionsRepository {
    List<String> getExperimentCollections(String experimentAccession);
}