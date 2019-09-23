package uk.ac.ebi.atlas.utils;

import com.google.common.collect.ImmutableList;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// This class is serialised to JSON in JsonExperimentsSummaryController
public class ExperimentInfo {
    private List<String> technologyType;
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

    public ExperimentType getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(ExperimentType experimentType) {
        this.experimentType = experimentType;
    }

    public String getExperimentAccession() {
        return experimentAccession;
    }

    public void setExperimentAccession(String experimentAccession) {
        this.experimentAccession = experimentAccession;
    }

    public void setTechnologyType(List<String> technologyType) {
        this.technologyType = technologyType;
    }
    public String getExperimentDescription() {
        return experimentDescription;
    }

    public void setExperimentDescription(String experimentDescription) {
        this.experimentDescription = experimentDescription;
    }

    public int getNumberOfAssays() {
        return numberOfAssays;
    }

    public void setNumberOfAssays(int numberOfAssays) {
        this.numberOfAssays = numberOfAssays;
    }

    // Used in EL experiment-list-latest.jsp
    public int getNumberOfContrasts() {
        return numberOfContrasts;
    }

    public void setNumberOfContrasts(int numberOfContrasts) {
        this.numberOfContrasts = numberOfContrasts;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getKingdom() {
        return kingdom;
    }

    public void setKingdom(String kingdom) {
        this.kingdom = kingdom;
    }

    public List<String> getExperimentalFactors() {
        return experimentalFactors;
    }

    public void setExperimentalFactors(Set<String> experimentalFactors) {
        this.experimentalFactors = new ArrayList<>(experimentalFactors);
    }

    public List<String> getArrayDesigns() {
        return arrayDesigns;
    }

    public void setArrayDesigns(List<String> arrayDesigns) {
        this.arrayDesigns = arrayDesigns;
    }

    // Used in EL experiment-description.jsp
    public List<String> getArrayDesignNames() {
        return arrayDesignNames;
    }

    public void setArrayDesignNames(List<String> arrayDesignNames) {
        this.arrayDesignNames = arrayDesignNames;
    }

    public String getLoadDate() {
        return loadDate;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLoadDate(String loadDate) {
        this.loadDate = loadDate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
