package uk.ac.ebi.atlas.experimentpage.context;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.atlassian.util.concurrent.LazyReference;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.ExpressionUnit;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;
import uk.ac.ebi.atlas.model.experiment.sdrf.FactorGroup;
import uk.ac.ebi.atlas.model.experiment.sdrf.RichFactorGroup;
import uk.ac.ebi.atlas.profiles.baseline.BaselineProfileStreamOptions;
import uk.ac.ebi.atlas.web.BaselineRequestPreferences;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class BaselineRequestContext<U extends ExpressionUnit.Absolute>
             extends RequestContext<AssayGroup, U, BaselineExperiment, BaselineRequestPreferences<U>>
             implements BaselineProfileStreamOptions<U> {

    private LazyReference<ImmutableMap<AssayGroup, String>> displayNamePerSelectedAssayGroup =
            new LazyReference<>() {
                @Override
                protected ImmutableMap<AssayGroup, String> create() {
                    return displayNamePerSelectedAssayGroup();
                }
            };

    public BaselineRequestContext(BaselineRequestPreferences<U> requestPreferences, BaselineExperiment experiment) {
        super(requestPreferences, experiment);
    }

    @Override
    public String displayNameForColumn(AssayGroup assayGroup) {
            return
                    Optional.ofNullable(displayNamePerSelectedAssayGroup.get().get(assayGroup))
                            .orElse(assayGroup.getId());
    }

    private List<String> typesWhoseValuesToDisplay() {

        List<FactorGroup> factorGroups = dataColumnsToBeReturned().map(experiment::getFactors).collect(toList());

        List<String> typesInOrderWeWant =
                Stream.concat(
                        experiment.getDisplayDefaults()
                                .getFactorTypes()
                                .stream(),
                        factorGroups.stream()
                                .flatMap(factors -> ImmutableList.copyOf(factors).stream().map(Factor::getType))
                                .sorted())
                        .map(Factor::normalize)
                        .distinct()
                        .collect(toList());

        List<String> typesWhoseValuesVaryAcrossSelectedDescriptors =
                RichFactorGroup.filterOutTypesWithCommonValues(typesInOrderWeWant, factorGroups);

        return typesWhoseValuesVaryAcrossSelectedDescriptors.isEmpty() ?
                experiment.getDisplayDefaults()
                        .getFactorTypes()
                        .asList()
                        .subList(0, Math.min(1, experiment.getDisplayDefaults().getFactorTypes().size())) :
                typesWhoseValuesVaryAcrossSelectedDescriptors;
    }

    private ImmutableMap<AssayGroup, String> displayNamePerSelectedAssayGroup() {
        ImmutableMap.Builder<AssayGroup, String> b = ImmutableMap.builder();

        dataColumnsToBeReturned().forEach(assayGroup -> {
            final FactorGroup factorGroup = experiment.getFactors(assayGroup);

            b.put(
                    assayGroup,
                    typesWhoseValuesToDisplay().stream()
                            .map(type -> factorGroup.factorOfType(Factor.normalize(type)).getValue())
                            .collect(Collectors.joining(", "))
            );
        });

        return b.build();
    }

    @Override
    public U getExpressionUnit() {
        return requestPreferences.getUnit();
    }
}
