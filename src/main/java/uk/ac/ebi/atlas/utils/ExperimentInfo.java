package uk.ac.ebi.atlas.utils;

import com.google.common.collect.ImmutableList;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// This class is serialised to JSON in JsonExperimentsSummaryController
public class ExperimentInfo {
    private ExperimentType experimentType;
    private String experimentAccession;
    private String experimentDescription;
    private String loadDate;
    private String lastUpdate;
    private int numberOfAssays;
    private int numberOfContrasts;
    private String species;
    private String kingdom;
    private List<String> experimentalFactors = new ArrayList<>();
    private List<String> arrayDesigns = ImmutableList.of();
    private List<String> arrayDesignNames = ImmutableList.of();
    private ImmutableList<String> experimentProjects = ImmutableList.of();

    public ExperimentType getExperimentType() {
        return experimentType;
    }

    public ExperimentInfo setExperimentType(ExperimentType experimentType) {
        this.experimentType = experimentType;
        return this;
    }

    public String getExperimentAccession() {
        return experimentAccession;
    }

    public ExperimentInfo setExperimentAccession(String experimentAccession) {
        this.experimentAccession = experimentAccession;
        return this;
    }

    public String getExperimentDescription() {
        return experimentDescription;
    }

    public ExperimentInfo setExperimentDescription(String experimentDescription) {
        this.experimentDescription = experimentDescription;
        return this;
    }

    public int getNumberOfAssays() {
        return numberOfAssays;
    }

    public ExperimentInfo setNumberOfAssays(int numberOfAssays) {
        this.numberOfAssays = numberOfAssays;
        return this;
    }

    // Used in EL experiment-list-latest.jsp
    public int getNumberOfContrasts() {
        return numberOfContrasts;
    }

    public ExperimentInfo setNumberOfContrasts(int numberOfContrasts) {
        this.numberOfContrasts = numberOfContrasts;
        return this;
    }

    public String getSpecies() {
        return species;
    }

    public ExperimentInfo setSpecies(String species) {
        this.species = species;
        return this;
    }

    public String getKingdom() { return kingdom; }

    public ExperimentInfo setKingdom(String kingdom) {
        this.kingdom = kingdom;
        return this;
    }

    public List<String> getExperimentalFactors() {
        return experimentalFactors;
    }

    public ExperimentInfo setExperimentalFactors(Set<String> experimentalFactors) {
        this.experimentalFactors = new ArrayList<>(experimentalFactors);
        return this;
    }

    public List<String> getArrayDesigns() {
        return arrayDesigns;
    }

    public ExperimentInfo setArrayDesigns(List<String> arrayDesigns) {
        this.arrayDesigns = arrayDesigns;
        return this;
    }

    // Used in EL experiment-description.jsp
    public List<String> getArrayDesignNames() {
        return arrayDesignNames;
    }

    public ExperimentInfo setArrayDesignNames(List<String> arrayDesignNames) {
        this.arrayDesignNames = arrayDesignNames;
        return this;
    }

    public String getLoadDate() {
        return loadDate;
    }

    public ExperimentInfo setLoadDate(String loadDate) {
        this.loadDate = loadDate;
        return this;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public ExperimentInfo setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public ImmutableList<String> getExperimentProjects() { return this.experimentProjects; }

    public ExperimentInfo setExperimentProjects(ImmutableList<String> experimentProjects) {
        this.experimentProjects = experimentProjects;
        return this;
    }
}
