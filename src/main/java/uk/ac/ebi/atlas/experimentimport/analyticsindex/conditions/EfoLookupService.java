package uk.ac.ebi.atlas.experimentimport.analyticsindex.conditions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import io.atlassian.util.concurrent.LazyReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.arrayexpress.utils.efo.EFOLoader;
import uk.ac.ebi.arrayexpress.utils.efo.EFONode;
import uk.ac.ebi.arrayexpress.utils.efo.IEFO;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static uk.ac.ebi.atlas.utils.StringUtil.suffixAfterLastSlash;

@Component
public class EfoLookupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EfoLookupService.class);
    private static final String EFO_OWL_FILE_URL = "https://github.com/EBISPOT/efo/releases/download/v3.5.1/efo.owl";

    private final LazyReference<ImmutableMap<String, EFONode>> idToEFONode =
            new LazyReference<>() {
                @Override
                protected ImmutableMap<String, EFONode> create() {
                    LOGGER.debug("Loading {}...", EFO_OWL_FILE_URL);

                    ImmutableMap.Builder<String, EFONode> efoMapBuilder = ImmutableMap.builder();
                    try {
                        IEFO iefo = new EFOLoader().load(new URL(EFO_OWL_FILE_URL).openStream());

                        efoMapBuilder.putAll(
                                iefo.getMap().entrySet().stream()
                                        .collect(toMap(
                                                entry -> suffixAfterLastSlash(entry.getValue().getId()),
                                                Map.Entry::getValue)));

                        LOGGER.info("Successfully loaded EFO version {}", iefo.getVersionInfo());
                    } catch (IOException e) {
                        LOGGER.error("There was an error reading {}, the EFO map will be empty or incomplete", EFO_OWL_FILE_URL);
                    }

                    return efoMapBuilder.build();
                }
            };

    private Set<String> getAllParents(String id) {
        var parentIds = new HashSet<String>();

        if (idToEFONode.get().containsKey(id)) {
            Set<EFONode> parents = idToEFONode.get().get(id).getParents();
            for (EFONode parent : parents) {
                String parentId = suffixAfterLastSlash(parent.getId());
                parentIds.addAll(getAllParents(parentId));
                parentIds.add(parentId);
            }
        }

        return parentIds;
    }

    public Set<String> getAllParents(Set<String> ids) {
        return ids.stream().flatMap(id -> getAllParents(id).stream()).collect(toSet());
    }

    public Set<String> getLabels(Set<String> ids) {
        return ids.stream()
                .filter(id -> idToEFONode.get().containsKey(id))
                .map(id -> idToEFONode.get().get(id))
                .map(EFONode::getTerm)
                .collect(toSet());
    }

    public ImmutableSetMultimap<String, String> expandOntologyTerms(
            ImmutableSetMultimap<String, String> termIdsByAssayAccession) {

        var builder = ImmutableSetMultimap.<String, String>builder();

        for (String assayAccession : termIdsByAssayAccession.keys()) {
            builder.putAll(
                    assayAccession,
                    ImmutableSet.<String>builder()
                            .addAll(getAllParents(termIdsByAssayAccession.get(assayAccession)))
                            .addAll(termIdsByAssayAccession.get(assayAccession))
                            .build());
        }

        return builder.build();
    }
}
