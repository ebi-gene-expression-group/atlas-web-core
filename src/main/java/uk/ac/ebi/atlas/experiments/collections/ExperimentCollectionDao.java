package uk.ac.ebi.atlas.experiments.collections;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExperimentCollectionDao {
    Optional<ExperimentCollection> findCollection(String id);
}
