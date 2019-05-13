package uk.ac.ebi.atlas.experimentimport.analyticsindex.conditions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import io.atlassian.util.concurrent.LazyReference;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.arrayexpress.utils.efo.EFOLoader;
import uk.ac.ebi.arrayexpress.utils.efo.EFONode;
import uk.ac.ebi.arrayexpress.utils.efo.IEFO;
import uk.ac.ebi.atlas.utils.StringUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ebi.atlas.utils.StringUtil.suffixAfterLastSlash;

@Component
@NonNullByDefault
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

    private ImmutableSet<String> getAllParents(String id) {
        if (idToEFONode.get().containsKey(id)) {
            return idToEFONode.get().get(id).getParents().stream()
                    .map(EFONode::getId)
                    .map(StringUtil::suffixAfterLastSlash)
                    .flatMap(foo -> Stream.concat(ImmutableSet.of(foo).stream(), getAllParents(foo).stream()))
                    .collect(toImmutableSet());
        }

        return ImmutableSet.of();
    }

    public ImmutableSet<String> getAllParents(Collection<String> ids) {
        return ids.stream().flatMap(id -> getAllParents(id).stream()).collect(toImmutableSet());
    }

    public ImmutableSet<String> getLabels(Collection<String> ids) {
        return ids.stream()
                .filter(id -> idToEFONode.get().containsKey(id))
                .map(id -> idToEFONode.get().get(id))
                .map(EFONode::getTerm)
                .collect(toImmutableSet());
    }

    public ImmutableSetMultimap<String, String> expandOntologyTerms(Multimap<String, String> termIdsByAssayAccession) {
        return termIdsByAssayAccession.keys().stream()
                .collect(flatteningToImmutableSetMultimap(
                        assayAccession -> assayAccession,
                        assayAccession -> Stream.concat(
                                getAllParents(termIdsByAssayAccession.get(assayAccession)).stream(),
                                termIdsByAssayAccession.get(assayAccession).stream())));
    }
}
